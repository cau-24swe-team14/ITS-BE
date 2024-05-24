package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
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
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IssueServiceTest {

  @Mock
  private ProjectAccountRepository projectAccountRepository;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private IssueRepository issueRepository;

  @InjectMocks
  private IssueServiceImpl issueService;

  @DisplayName("addIssue: 사용자가 테스터일 때 이슈 추가에 성공한다")
  @Test
  public void addIssue_WhenUserIsTester_Success() {
    // parameter Mock 데이터 설정
    Long accountId = 1L;
    Long projectId = 1L;
    AddIssueRequest addIssueRequest = AddIssueRequest.builder()
        .title("Test Issue")
        .description("Description")
        .dueDate(LocalDateTime.now().plusDays(3))
        .priority("MAJOR")
        .build();

    // domain Mock 데이터 설정
    Project project = Project.builder()
        .projectId(projectId)
        .build();
    Account account = Account.builder()
        .accountId(accountId)
        .build();

    Issue issue = Issue.builder()
        .id(IssuePK.builder()
            .projectId(projectId)
            .issueId(2L)
            .build())
        .build();

    // 사용자가 tester 역할인 경우의 Mock 데이터 설정
    ProjectAccount projectAccount = ProjectAccount.builder()
        .id(ProjectAccountPK.builder()
            .accountId(accountId)
            .projectId(projectId)
            .build())
        .account(account)
        .project(project)
        .role(ProjectUserRole.tester)
        .build();

    // Mock 객체의 동작 설정
    given(projectAccountRepository.findById(any(ProjectAccountPK.class))).willReturn(Optional.of(projectAccount));
    given(issueRepository.findMaxIdByProjectId(projectId)).willReturn(1L);
    given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
    given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
    given(issueRepository.save(any(Issue.class))).willReturn(issue);

    // 테스트
    String result = issueService.addIssue(accountId, projectId, addIssueRequest);

    // 결과 확인
    assertThat(result).isEqualTo("/projects/1/issues/2");
  }

  @DisplayName("addIssue: 사용자가 테스터가 아닐 때 이슈 추가에 실패한다")
  @Test
  public void addIssue_WhenUserIsTester_Fail() {
    // parameter Mock 데이터 설정
    Long accountId = 1L;
    Long projectId = 1L;
    AddIssueRequest addIssueRequest = AddIssueRequest.builder()
        .title("Test Issue")
        .description("Description")
        .dueDate(LocalDateTime.now().plusDays(3))
        .priority("MAJOR")
        .build();

    // domain Mock 데이터 설정
    Project project = Project.builder()
        .projectId(projectId)
        .build();
    Account account = Account.builder()
        .accountId(accountId)
        .build();

    // 사용자가 tester 역할이 아닌 경우의 Mock 데이터 설정
    ProjectAccount projectAccount = ProjectAccount.builder()
        .id(ProjectAccountPK.builder()
            .accountId(accountId)
            .projectId(projectId)
            .build())
        .account(account)
        .project(project)
        .role(ProjectUserRole.dev) // Assuming the user is a developer
        .build();

    // Mock 객체의 동작 설정
    given(projectAccountRepository.findById(any(ProjectAccountPK.class))).willReturn(Optional.of(projectAccount));

    // 테스트
    assertThrows(ITSException.class, () -> issueService.addIssue(accountId, projectId, addIssueRequest));
  }
}