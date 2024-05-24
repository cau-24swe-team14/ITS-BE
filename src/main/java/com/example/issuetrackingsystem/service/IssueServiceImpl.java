package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.ProjectUserRole;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.repository.AccountRepository;
import com.example.issuetrackingsystem.repository.IssueRepository;
import com.example.issuetrackingsystem.repository.ProjectAccountRepository;
import com.example.issuetrackingsystem.repository.ProjectRepository;
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
        .projectId(projectId).build()).orElse(null);

    if (projectAccount == null) {
      throw new ITSException(ErrorCode.ISSUE_CREATION_FORBIDDEN);
    } else if (projectAccount.getRole() != ProjectUserRole.tester) {
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
    String priority = addIssueRequest.getPriority();
    Issue issue;

    if (priority == null) {
      issue = Issue.builder()
          .id(issuePK)
          .project(project)
          .title(addIssueRequest.getTitle())
          .description(addIssueRequest.getDescription())
          .reporter(reporter)
          .dueDate(addIssueRequest.getDueDate())
          .build();
    } else {
      issue = Issue.builder()
          .id(issuePK)
          .project(project)
          .title(addIssueRequest.getTitle())
          .description(addIssueRequest.getDescription())
          .reporter(reporter)
          .priority(IssuePriority.valueOf(priority))
          .dueDate(addIssueRequest.getDueDate())
          .build();
    }

    issueRepository.save(issue);

    return "/projects/" + projectId + "/issues/" + newIssueId;
  }
}
