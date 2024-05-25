package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.IssueKeyword;
import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.IssueStatus;
import com.example.issuetrackingsystem.domain.enums.ProjectAccountRole;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.dto.AddCommentRequest;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.repository.AccountRepository;
import com.example.issuetrackingsystem.repository.IssueRepository;
import com.example.issuetrackingsystem.repository.ProjectAccountRepository;
import com.example.issuetrackingsystem.repository.ProjectRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;
  private final ProjectAccountRepository projectAccountRepository;
  private final AccountRepository accountRepository;
  private final ProjectRepository projectRepository;

  public IssueServiceImpl(IssueRepository issueRepository, ProjectAccountRepository projectAccountRepository,
      AccountRepository accountRepository, ProjectRepository projectRepository) {
    this.issueRepository = issueRepository;
    this.projectAccountRepository = projectAccountRepository;
    this.accountRepository = accountRepository;
    this.projectRepository = projectRepository;
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
  @Transactional
  public List<AddCommentRequest> addComment(Long accountId, Long projectId, Long issueId, AddCommentRequest addCommentRequest) {
    return new ArrayList<>();
  }
}
