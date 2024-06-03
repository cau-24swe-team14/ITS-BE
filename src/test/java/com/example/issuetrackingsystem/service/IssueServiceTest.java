package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.*;
import com.example.issuetrackingsystem.domain.enums.*;
import com.example.issuetrackingsystem.domain.key.*;
import com.example.issuetrackingsystem.dto.*;
import com.example.issuetrackingsystem.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

  @Mock
  private IssueRepository issueRepository;
  @Mock
  private ProjectAccountRepository projectAccountRepository;
  @Mock
  private AccountRepository accountRepository;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private CommentRepository commentRepository;

  @InjectMocks
  private IssueServiceImpl issueService;

  private Long accountId;
  private Long projectId;
  private Long issueId;

  private ProjectAccount.ProjectAccountBuilder projectAccountBuilder;
  private ProjectAccountPK projectAccountPK;
  private Account account;
  private Project project;
  private Issue issue;
  private AddIssueRequest addIssueRequest;
  private ModifyIssueRequest modifyIssueRequest;

  @BeforeEach
  void setUp() {
    accountId = 2L;
    projectId = 1L;
    issueId = 1L;

    account = Account.builder()
        .accountId(accountId)
        .username("user")
        .build();

    project = Project.builder()
        .projectId(projectId)
        .title("Test Project")
        .description("Test Project Description")
        .status(ProjectStatus.IN_PROGRESS)
        .date(LocalDateTime.now())
        .build();

    projectAccountPK = ProjectAccountPK.builder()
        .accountId(accountId)
        .projectId(projectId)
        .build();

    projectAccountBuilder = ProjectAccount.builder()
        .id(projectAccountPK)
        .project(project)
        .account(account)
        .date(LocalDateTime.now())
        .role(ProjectAccountRole.tester);

    issue = Issue.builder()
        .id(IssuePK.builder()
            .issueId(issueId)
            .projectId(projectId)
            .build())
        .title("Test Issue")
        .description("Test Issue Description")
        .reporter(account)
        .reportedDate(LocalDateTime.now())
        .dueDate(LocalDate.now())
        .priority(IssuePriority.CRITICAL)
        .status(IssueStatus.NEW)
        .build();

    addIssueRequest = AddIssueRequest.builder()
        .title("Test Issue")
        .description("Test Issue Description")
        .dueDate(LocalDate.now().toString())
        .priority(0)
        .keyword(0)
        .build();

    modifyIssueRequest = ModifyIssueRequest.builder()
        .title("Updated Issue")
        .description("Updated Description")
        .priority(1)
        .build();
  }

  @Test
  void testAddIssue() {
    when(projectAccountRepository.findById(any(ProjectAccountPK.class))).thenReturn(
        Optional.of(projectAccountBuilder.build()));
    when(issueRepository.findMaxIdByProjectId(projectId)).thenReturn(issueId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    String issueUrl = issueService.addIssue(accountId, projectId, addIssueRequest);

    assertEquals("/projects/" + projectId + "/issues/" + (issueId + 1), issueUrl);
    verify(issueRepository, times(1)).save(any(Issue.class));
    verify(projectRepository, times(1)).save(any(Project.class));
  }

  @Test
  void testModifyIssue() {
    // ProjectAccount가 존재하는지 확인
    when(projectAccountRepository.findById(any(ProjectAccountPK.class))).thenReturn(
        Optional.of(projectAccountBuilder.build()));

    // AccountRepository에서 계정을 가져오는지 확인
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    // IssueRepository에서 이슈를 가져오는지 확인
    when(issueRepository.findById(any(IssuePK.class))).thenReturn(Optional.of(issue));

    // ProjectRepository에서 프로젝트를 가져오는지 확인
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    // modifyIssue 호출
    assertDoesNotThrow(
        () -> issueService.modifyIssue(accountId, projectId, issueId, modifyIssueRequest));

    // IssueRepository의 save 메서드가 한 번 호출되는지 확인
    verify(issueRepository, times(1)).save(any(Issue.class));
  }

  @Test
  void testFindIssue() {
    projectAccountBuilder.role(ProjectAccountRole.PL);
    when(projectAccountRepository.findById(any(ProjectAccountPK.class))).thenReturn(Optional.of(projectAccountBuilder.build()));
    when(issueRepository.findById(any(IssuePK.class))).thenReturn(Optional.of(issue));
    when(commentRepository.findByIssue(any(Issue.class))).thenReturn(Optional.of(Collections.emptyList()));

    DetailsIssueResponse issueResponse = issueService.findIssue(accountId, projectId, issueId);

    assertNotNull(issueResponse);
    assertEquals(issueId, issueResponse.getId());
  }

  @Test
  void testAddComment() {
    when(projectAccountRepository.findById(any(ProjectAccountPK.class))).thenReturn(Optional.of(projectAccountBuilder.build()));
    when(issueRepository.findById(any(IssuePK.class))).thenReturn(Optional.of(issue));
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(commentRepository.findMaxIdByIssueId(issueId)).thenReturn(1L);

    AddCommentRequest addCommentRequest = AddCommentRequest.builder().content("Test Comment").build();
    List<AddCommentResponse> comments = issueService.addComment(accountId, projectId, issueId, addCommentRequest);

    assertNotNull(comments);
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  void testSuggestAssignee() {
    projectAccountBuilder.role(ProjectAccountRole.PL);
    when(projectAccountRepository.findById(any(ProjectAccountPK.class))).thenReturn(Optional.of(projectAccountBuilder.build()));
    when(issueRepository.findById(any(IssuePK.class))).thenReturn(Optional.of(issue));

    Object[] assignee = new Object[]{new Object[]{"assignedDev"}};
    when(issueRepository.findAssigneeSuggestion(projectId)).thenReturn(assignee);
    Mockito.lenient().when(issueRepository.findAssigneeSuggestionByKeyword(eq(projectId), any(IssueKeyword.class))).thenReturn(assignee);

    SuggestIssueAssigneeResponse response = issueService.suggestAssignee(accountId, projectId, issueId);

    assertNotNull(response);
    assertEquals("assignedDev", response.getUsername());
  }

  @Test
  void testSearchIssue() {
    when(projectAccountRepository.findById(any(ProjectAccountPK.class))).thenReturn(Optional.of(projectAccountBuilder.build()));
    when(issueRepository.findById_ProjectIdAndTitle(anyLong(), anyString())).thenReturn(Collections.singletonList(issue));

    Map<String, String> searchKey = new HashMap<>();
    searchKey.put("title", "Test Issue");

    List<SearchIssueResponse> searchResults = issueService.searchIssue(accountId, projectId, searchKey);

    assertNotNull(searchResults);
    assertFalse(searchResults.isEmpty());
  }
}
