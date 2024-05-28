package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Comment;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.IssueKeyword;
import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.IssueStatus;
import com.example.issuetrackingsystem.domain.enums.ProjectAccountRole;
import com.example.issuetrackingsystem.domain.key.CommentPK;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.dto.AddCommentRequest;
import com.example.issuetrackingsystem.dto.AddCommentResponse;
import com.example.issuetrackingsystem.dto.AddCommentResponse;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.dto.DetailsIssueResponse;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.dto.SearchIssueResponse;
import com.example.issuetrackingsystem.dto.SuggestIssueAssigneeResponse;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.repository.AccountRepository;
import com.example.issuetrackingsystem.repository.CommentRepository;
import com.example.issuetrackingsystem.repository.IssueRepository;
import com.example.issuetrackingsystem.repository.ProjectAccountRepository;
import com.example.issuetrackingsystem.repository.ProjectRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;
  private final ProjectAccountRepository projectAccountRepository;
  private final AccountRepository accountRepository;
  private final ProjectRepository projectRepository;
  private final CommentRepository commentRepository;

  public IssueServiceImpl(IssueRepository issueRepository, ProjectAccountRepository projectAccountRepository,
      AccountRepository accountRepository, ProjectRepository projectRepository, CommentRepository commentRepository) {
    this.issueRepository = issueRepository;
    this.projectAccountRepository = projectAccountRepository;
    this.accountRepository = accountRepository;
    this.projectRepository = projectRepository;
    this.commentRepository = commentRepository;
  }

  @Override
  @Transactional
  public String addIssue(Long accountId, Long projectId, AddIssueRequest addIssueRequest) {
    // 사용자가 해당 프로젝트의 tester인지 검증
    ProjectAccount projectAccount = projectAccountRepository.findById(ProjectAccountPK.builder()
        .accountId(accountId)
        .projectId(projectId).build()).orElseThrow(() -> new ITSException(ErrorCode.ISSUE_CREATION_FORBIDDEN));

    if (projectAccount.getRole() != ProjectAccountRole.tester) {
      throw new ITSException(ErrorCode.ISSUE_CREATION_FORBIDDEN);
    }

    // project별 issue_id auto increment
    Long maxIssueId = issueRepository.findMaxIdByProjectId(projectId);
    Long newIssueId = (maxIssueId != null ? maxIssueId : 0) + 1;

    // 이슈 생성
    IssuePK issuePK = IssuePK.builder()
        .issueId(newIssueId)
        .projectId(projectId)
        .build();
    Project project = projectRepository.findById(projectId).orElse(null);
    Account reporter = accountRepository.findById(accountId).orElse(null);
    Integer priority = addIssueRequest.getPriority();
    Integer keyword = addIssueRequest.getKeyword();

    Issue.IssueBuilder newIssue = Issue.builder()
        .id(issuePK)
        .project(project)
        .title(addIssueRequest.getTitle())
        .description(addIssueRequest.getDescription())
        .reporter(reporter)
        .dueDate(LocalDate.parse(addIssueRequest.getDueDate(), DateTimeFormatter.ISO_DATE));

    if (priority != null) {
      newIssue.priority(IssuePriority.values()[priority]);
    }
    if (keyword != null) {
      newIssue.keyword(IssueKeyword.values()[keyword]);
    }

    issueRepository.save(newIssue.build());

    return "/projects/" + projectId + "/issues/" + newIssueId;
  }

  @Override
  @Transactional
  public void modifyIssue(Long accountId, Long projectId, Long issueId, ModifyIssueRequest modifyIssueRequest) {
    // 사용자가 해당 프로젝트에 속해 있는지 검증
    ProjectAccount projectAccount = projectAccountRepository.findById(ProjectAccountPK.builder()
        .accountId(accountId)
        .projectId(projectId).build())
        .orElseThrow(() -> new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN));

    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new ITSException(ErrorCode.ISSUE_UPDATE_BAD_REQUEST));

    // 이슈 조회
    IssuePK issuePK = IssuePK.builder()
        .issueId(issueId)
        .projectId(projectId)
        .build();

    Issue issue = issueRepository.findById(issuePK)
        .orElseThrow(() -> new ITSException(ErrorCode.ISSUE_NOT_FOUND));

    Issue.IssueBuilder modifiedIssue = Issue.builder()
        .id(issue.getId())
        .project(issue.getProject())
        .title(issue.getTitle())
        .description(issue.getDescription())
        .keyword(issue.getKeyword())
        .reporter(issue.getReporter())
        .reportedDate(issue.getReportedDate())
        .dueDate(issue.getDueDate())
        .manager(issue.getManager())
        .assignee(issue.getAssignee())
        .fixer(issue.getFixer())
        .priority(issue.getPriority())
        .status(issue.getStatus())
        .closedDate(issue.getClosedDate());

    List<String> commentContentList = new ArrayList<>();

    if (modifyIssueRequest.getAssignee() != null) { // assignee 변경

      // 사용자가 해당 프로젝트의 PL인지, 이슈가 NEW 상태인지 검증
      if (projectAccount.getRole() != ProjectAccountRole.PL) {
        throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
      }
      if (issue.getStatus() != IssueStatus.NEW) {
        throw new ITSException(ErrorCode.ISSUE_UPDATE_BAD_REQUEST);
      }
      modifiedIssue.assignee(accountRepository.findByUsername(modifyIssueRequest.getAssignee())
              .orElseThrow(() -> new ITSException(ErrorCode.ISSUE_UPDATE_BAD_REQUEST)))
          .status(IssueStatus.ASSIGNED)
          .manager(account);
      commentContentList.add(account.getUsername()
          + " assigned this to "
          + modifyIssueRequest.getAssignee()
          +".");

    } else if (modifyIssueRequest.getStatus() != null) { // status 변경

      IssueStatus newStatus = IssueStatus.values()[modifyIssueRequest.getStatus()];

      switch (newStatus) {
        case FIXED:
          // 사용자가 해당 이슈의 assignee인지, 이슈가 ASSIGNED, REOPENED 상태인지 검증
          if (!Objects.equals(issue.getAssignee().getAccountId(), accountId)) {
            throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
          }
          if (!Arrays.asList(new IssueStatus[]{IssueStatus.ASSIGNED, IssueStatus.REOPENED}).contains(issue.getStatus())) {
            throw new ITSException(ErrorCode.ISSUE_UPDATE_BAD_REQUEST);
          }
          modifiedIssue.fixer(accountRepository.findById(accountId)
                  .orElseThrow(() -> new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN)))
              .status(newStatus);
          break;

        case RESOLVED:
          // 사용자가 해당 이슈의 reporter인지, 이슈가 FIXED 상태인지 검증
          if (!Objects.equals(issue.getReporter().getAccountId(), accountId)) {
            throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
          }
          if (issue.getStatus() != IssueStatus.FIXED) {
            throw new ITSException(ErrorCode.ISSUE_UPDATE_BAD_REQUEST);
          }
          modifiedIssue.status(newStatus);
          break;

        case CLOSED:
          // 사용자가 해당 이슈의 manager인지, 이슈가 RESOLVED 상태인지 검증
          if (!Objects.equals(issue.getManager().getAccountId(), accountId)) {
            throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
          }
          if (issue.getStatus() != IssueStatus.RESOLVED) {
            throw new ITSException(ErrorCode.ISSUE_UPDATE_BAD_REQUEST);
          }
          modifiedIssue.status(newStatus)
              .closedDate(LocalDateTime.now());
          break;

        case REOPENED:
          // 사용자가 해당 이슈의 reporter인지, 이슈가 CLOSED 상태인지 검증
          if (!Objects.equals(issue.getReporter().getAccountId(), accountId)) {
            throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
          }
          if (issue.getStatus() != IssueStatus.CLOSED) {
            throw new ITSException(ErrorCode.ISSUE_UPDATE_BAD_REQUEST);
          }
          modifiedIssue.status(newStatus)
              .closedDate(null);
          break;

        default:
          throw new ITSException(ErrorCode.ISSUE_UPDATE_BAD_REQUEST);
      }
      commentContentList.add(account.getUsername()
          + " "
          + newStatus.name().toLowerCase()
          + " this.");
    } else {

      if (modifyIssueRequest.getTitle() != null) { // title 변경

        // 사용자가 해당 이슈의 reporter인지 검증
        if (!Objects.equals(issue.getReporter().getAccountId(), accountId)) {
          throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
        }
        modifiedIssue.title(modifyIssueRequest.getTitle());
        commentContentList.add(account.getUsername()
            + " changed the title to "
            + modifyIssueRequest.getTitle()
            + ".");

      }

      if (modifyIssueRequest.getDescription() != null) { // description 변경

        // 사용자가 해당 이슈의 reporter인지 검증
        if (!Objects.equals(issue.getReporter().getAccountId(), accountId)) {
          throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
        }
        modifiedIssue.description(modifyIssueRequest.getDescription());
        commentContentList.add(account.getUsername()
            + " changed the description to "
            + modifyIssueRequest.getDescription()
            + ".");

      }

      if (modifyIssueRequest.getKeyword() != null) { // keyword 변경

        // 사용자가 해당 이슈의 reporter인지 검증
        if (!Objects.equals(issue.getReporter().getAccountId(), accountId)) {
          throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
        }
        modifiedIssue.keyword(IssueKeyword.values()[modifyIssueRequest.getKeyword()]);
        commentContentList.add(account.getUsername()
            + " changed the keyword to "
            + IssueKeyword.values()[modifyIssueRequest.getKeyword()].name()
            + ".");

      }

      if (modifyIssueRequest.getPriority() != null) { // priority 변경

        // 사용자가 해당 이슈의 reporter인지 검증
        if (!Objects.equals(issue.getReporter().getAccountId(), accountId)) {
          throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
        }
        modifiedIssue.priority(IssuePriority.values()[modifyIssueRequest.getPriority()]);
        commentContentList.add(account.getUsername()
            + " changed the priority to "
            + IssuePriority.values()[modifyIssueRequest.getPriority()].name()
            + ".");

      }

      if (modifyIssueRequest.getDueDate() != null) { // due date 변경

        // 사용자가 해당 이슈의 reporter인지 검증
        if (!Objects.equals(issue.getReporter().getAccountId(), accountId)) {
          throw new ITSException(ErrorCode.ISSUE_UPDATE_FORBIDDEN);
        }
        modifiedIssue.dueDate(LocalDate.parse(modifyIssueRequest.getDueDate(), DateTimeFormatter.ISO_DATE));
        commentContentList.add(account.getUsername()
            + " changed the due date to "
            + modifyIssueRequest.getDueDate()
            + ".");

      }
    }

    issueRepository.save(modifiedIssue.build());

    for (String content : commentContentList) {
      addComment(accountId, projectId, issueId, new AddCommentRequest(content));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public DetailsIssueResponse findIssue(Long accountId, Long projectId, Long issueId) {
    ProjectAccount projectAccount = null;
    // 사용자가 Admin이거나 해당 프로젝트에 속해 있는지 검증
    if (accountId != 1L) {
       projectAccount = projectAccountRepository.findById(ProjectAccountPK.builder()
          .accountId(accountId)
          .projectId(projectId).build()).orElseThrow(() -> new ITSException(ErrorCode.ISSUE_DETAILS_FORBIDDEN));
    }

    Issue issue = issueRepository.findById(IssuePK.builder()
            .projectId(projectId)
            .issueId(issueId)
        .build())
        .orElseThrow(() -> new ITSException(ErrorCode.ISSUE_NOT_FOUND));

    List<Comment> commentList = commentRepository.findByIssue(issue).orElse(Collections.emptyList());
    List<AddCommentResponse> addCommentResponseList = new ArrayList<>();

    for (Comment comment : commentList) {
      addCommentResponseList.add(AddCommentResponse.builder()
              .id(comment.getId().getCommentId())
              .username(comment.getAccount().getUsername())
              .content(comment.getContent())
              .date(comment.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
          .build());
    }

    DetailsIssueResponse detailsIssueResponse = DetailsIssueResponse.builder()
        .id(issueId)
        .projectId(projectId)
        .title(issue.getTitle())
        .description(issue.getDescription())
        .keyword(issue.getKeyword() != null ? issue.getKeyword().ordinal() : null)
        .reporter(issue.getReporter() != null ? issue.getReporter().getUsername() : null)
        .reportedDate(issue.getReportedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        .manager(issue.getManager() != null ? issue.getManager().getUsername() : null)
        .assignee(issue.getAssignee() != null ? issue.getAssignee().getUsername() : null)
        .fixer(issue.getFixer() != null ? issue.getFixer().getUsername() : null)
        .priority(issue.getPriority() != null ? issue.getPriority().ordinal() : null)
        .status(issue.getStatus() != null ? issue.getStatus().ordinal() : null)
        .dueDate(issue.getDueDate().format(DateTimeFormatter.ISO_DATE))
        .closedDate(issue.getClosedDate() != null ? issue.getClosedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
        .comment(addCommentResponseList)
        .accountRole(projectAccount.getRole().ordinal())
        .build();

    return detailsIssueResponse;
  }

  @Override
  @Transactional
  public List<AddCommentResponse> addComment(Long accountId, Long projectId, Long issueId, AddCommentRequest addCommentRequest) {
    projectAccountRepository.findById(ProjectAccountPK.builder()
        .accountId(accountId)
        .projectId(projectId).build()).orElseThrow(() -> new ITSException(ErrorCode.COMMENT_CREATION_FORBIDDEN));

    // issue별 comment_id auto increment
    Long maxCommentId = commentRepository.findMaxIdByIssueId(issueId);
    Long newCommentId = (maxCommentId != null ? maxCommentId : 0) + 1;

    // 코멘트 생성
    IssuePK issuePK = IssuePK.builder()
        .projectId(projectId)
        .issueId(issueId)
        .build();

    Issue issue = issueRepository.findById(issuePK)
        .orElseThrow(() -> new ITSException(ErrorCode.COMMENT_CREATION_BAD_REQUEST));

    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new ITSException(ErrorCode.COMMENT_CREATION_FORBIDDEN));

    Comment comment = Comment.builder()
        .id(CommentPK.builder()
            .issueId(issuePK)
            .commentId(newCommentId)
            .build())
        .issue(issue)
        .account(account)
        .content(addCommentRequest.getContent())
        .date(LocalDateTime.now())
        .build();

    commentRepository.save(comment);

    List<Comment> commentList = commentRepository.findAll();
    List<AddCommentResponse> addCommentResponseList = new ArrayList<>();

    for (Comment c : commentList) {
      addCommentResponseList.add(AddCommentResponse.builder()
              .id(c.getId().getCommentId())
              .username(c.getAccount().getUsername())
              .content(c.getContent())
              .date(c.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
          .build());
    }

    return addCommentResponseList;
  }

  @Override
  public SuggestIssueAssigneeResponse suggestAssignee(Long accountId, Long projectId,
      Long issueId) {
    // 사용자가 해당 프로젝트의 PL인지 검증
    ProjectAccount projectAccount = projectAccountRepository.findById(ProjectAccountPK.builder()
            .accountId(accountId)
            .projectId(projectId).build())
        .orElseThrow(() -> new ITSException(ErrorCode.ISSUE_ASSIGNEE_SUGGESTION_FORBIDDEN));

    if (projectAccount.getRole() != ProjectAccountRole.PL) {
      throw new ITSException(ErrorCode.ISSUE_ASSIGNEE_SUGGESTION_FORBIDDEN);
    }

    Issue issue = issueRepository.findById(IssuePK.builder()
            .projectId(projectId)
            .issueId(issueId)
            .build())
        .orElseThrow(() -> new ITSException(ErrorCode.ISSUE_ASSIGNEE_SUGGESTION_BAD_REQUEST));

    IssueKeyword issueKeyword = issue.getKeyword();

    SuggestIssueAssigneeResponse suggestIssueAssigneeResponse;

    if (issueKeyword == null) {
      Object[] assignee = issueRepository.findAssigneeSuggestion();
      if (assignee.length == 0) {
        List<ProjectAccount> devs = projectAccountRepository.findByProjectProjectIdAndRole(projectId, ProjectAccountRole.dev);
        if (devs.isEmpty()) {
          suggestIssueAssigneeResponse = SuggestIssueAssigneeResponse.builder()
              .username("There are no suitable dev.")
              .build();
        } else {
          ProjectAccount randomDev = devs.get(new Random().nextInt(devs.size()));
          suggestIssueAssigneeResponse = SuggestIssueAssigneeResponse.builder()
              .username(randomDev.getAccount().getUsername())
              .build();
        }
      } else {
        suggestIssueAssigneeResponse = SuggestIssueAssigneeResponse.builder()
            .username((String) ((Object[]) assignee[0])[0])
            .build();
      }
    } else {
      Object[] assignee = issueRepository.findAssigneeSuggestionByKeyword(issueKeyword);
      if (assignee.length == 0) {
        List<ProjectAccount> devs = projectAccountRepository.findByProjectProjectIdAndRole(projectId, ProjectAccountRole.dev);
        if (devs.isEmpty()) {
          suggestIssueAssigneeResponse = SuggestIssueAssigneeResponse.builder()
              .username("There are no suitable dev.")
              .build();
        } else {
          ProjectAccount randomDev = devs.get(new Random().nextInt(devs.size()));
          suggestIssueAssigneeResponse = SuggestIssueAssigneeResponse.builder()
              .username(randomDev.getAccount().getUsername())
              .build();
        }
      } else {
        suggestIssueAssigneeResponse = SuggestIssueAssigneeResponse.builder()
            .username((String) ((Object[]) assignee[0])[0])
            .build();
      }
    }

    return suggestIssueAssigneeResponse;
  }

  @Override
  public List<SearchIssueResponse> searchIssue(Long accountId, Long projectId, Map<String, String> searchKey) {
    if (accountId != 1L) {
      projectAccountRepository.findById(ProjectAccountPK.builder()
          .accountId(accountId)
          .projectId(projectId).build()).orElseThrow(() -> new ITSException(ErrorCode.ISSUE_DETAILS_FORBIDDEN));
    }
    String key = null, value = null;
    for (String k: searchKey.keySet()) {
      key = k;
      value = searchKey.get(k);
    }

    List<Issue> issueList = new ArrayList<>();

    switch (key) {
      case "title":
        issueList = issueRepository.findById_ProjectIdAndTitle(projectId, value);
        break;
      case "description":
        issueList = issueRepository.findById_ProjectIdAndDescription(projectId, value);
        break;
      case "keyword":
        issueList = issueRepository.findById_ProjectIdAndKeyword(projectId, IssueKeyword.valueOf(value));
        break;
      case "reporter":
        issueList = issueRepository.findById_ProjectIdAndReporter_Username(projectId, value);
        break;
      case "manager":
        issueList = issueRepository.findById_ProjectIdAndManager_Username(projectId, value);
        break;
      case "assignee":
        issueList = issueRepository.findById_ProjectIdAndAssignee_Username(projectId, value);
        break;
      case "fixer":
        issueList = issueRepository.findById_ProjectIdAndFixer_Username(projectId, value);
        break;
      case "priority":
        issueList = issueRepository.findById_ProjectIdAndPriority(projectId, IssuePriority.valueOf(value));
        break;
      case "status":
        issueList = issueRepository.findById_ProjectIdAndStatus(projectId, IssueStatus.valueOf(value));
        break;
      default:
        throw new ITSException(ErrorCode.ISSUE_SEARCH_BAD_REQUEST);
    }

    return issueList.stream()
        .map(issue -> SearchIssueResponse.builder()
            .id(issue.getId().getIssueId())
            .title(issue.getTitle())
            .status(issue.getStatus().ordinal())
            .reportedDate(issue.getReportedDate().format(DateTimeFormatter.ISO_DATE))
            .dueDate(issue.getDueDate().format(DateTimeFormatter.ISO_DATE))
            .build())
        .collect(Collectors.toList());
  }
}
