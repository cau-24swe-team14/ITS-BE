package com.example.issuetrackingsystem.controller;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.IssueStatus;
import com.example.issuetrackingsystem.domain.enums.ProjectAccountRole;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.dto.AddCommentRequest;
import com.example.issuetrackingsystem.dto.AddCommentResponse;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.dto.DetailsIssueResponse;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.repository.AccountRepository;
import com.example.issuetrackingsystem.repository.CommentRepository;
import com.example.issuetrackingsystem.repository.IssueRepository;
import com.example.issuetrackingsystem.repository.ProjectAccountRepository;
import com.example.issuetrackingsystem.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IssueControllerTest {
  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  IssueRepository issueRepository;

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  ProjectRepository projectRepository;

  @Autowired
  ProjectAccountRepository projectAccountRepository;

  @Autowired
  CommentRepository commentRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private static MockHttpSession[] sessions;

  @BeforeAll
  public static void repositorySetUp(@Autowired AccountRepository accountRepository,
      @Autowired ProjectRepository projectRepository,
      @Autowired ProjectAccountRepository projectAccountRepository) {

    Project project = projectRepository.save(Project.builder()
        .title("title")
        .description("description")
        .build());

    Account[] accounts = new Account[3];
    sessions = new MockHttpSession[3];

    for (int i = 0; i < 3; i++) {
      accounts[i] = accountRepository.save(Account.builder()
          .username("username"+i)
          .password("password")
          .build());

      projectAccountRepository.save(ProjectAccount.builder()
          .id(ProjectAccountPK.builder()
              .accountId(accounts[i].getAccountId())
              .projectId(project.getProjectId())
              .build())
          .account(accounts[i])
          .project(project)
          .role(ProjectAccountRole.values()[i])
          .build());

      Long accountId = accounts[i].getAccountId();
      sessions[i] = new MockHttpSession();
      sessions[i].setAttribute("id", accountId);
    }
  }

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .build();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @AfterAll
  public static void tearDown(@Autowired AccountRepository accountRepository,
      @Autowired ProjectRepository projectRepository,
      @Autowired ProjectAccountRepository projectAccountRepository,
      @Autowired IssueRepository issueRepository,
      @Autowired CommentRepository commentRepository) {
    commentRepository.deleteAll();
    issueRepository.deleteAll();
    projectAccountRepository.deleteAll();
    projectRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @DisplayName("issueAdd: 이슈 추가에 성공한다.")
  @Test
  @Order(1)
  public void issueAdd_Success() throws Exception {
    // given
    String title = "title";
    String description = "description";
    Integer priority = 2;
    String dueDate = LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_DATE);
    AddIssueRequest addIssueRequest = AddIssueRequest.builder()
        .title(title)
        .description(description)
        .priority(priority)
        .dueDate(dueDate)
        .build();

    Long projectId = projectRepository.findAll().get(0).getProjectId();

    // when
    mockMvc.perform(post("/projects/{projectId}/issues", projectId)
            .contentType(MediaType.APPLICATION_JSON)
            .session(sessions[2])
            .content(objectMapper.writeValueAsString(addIssueRequest)))
        .andExpect(status().isCreated());

    // then
    List<Issue> all = issueRepository.findAll();

    assertThat(all.size()).isEqualTo(1);
    assertThat(all.get(0).getTitle()).isEqualTo(title);
    assertThat(all.get(0).getDescription()).isEqualTo(description);
    assertThat(all.get(0).getPriority()).isEqualTo(IssuePriority.values()[priority]);
    assertThat(all.get(0).getDueDate()).isEqualTo(dueDate);
  }

  @DisplayName("issueModify: 이슈 assignee 수정에 성공한다.")
  @Test
  @Order(2)
  public void issueModify_Success() throws Exception {
    // given
    // 수정할 이슈의 ID
    Long issueId = issueRepository.findAll().get(0).getId().getIssueId();
    String username = accountRepository.findAll().get(1).getUsername();

    // 이슈 수정 요청
    ModifyIssueRequest modifyIssueRequest = ModifyIssueRequest.builder()
        .assignee(username)
        .build();

    Long projectId = projectRepository.findAll().get(0).getProjectId();

    // when
    mockMvc.perform(patch("/projects/{projectId}/issues/{issueId}", projectId, issueId)
            .contentType(MediaType.APPLICATION_JSON)
            .session(sessions[0])
            .content(objectMapper.writeValueAsString(modifyIssueRequest)))
        .andExpect(status().isNoContent());

    // then
    // 이슈가 성공적으로 수정되었는지 확인
    Issue modifiedIssue = issueRepository.findById(IssuePK.builder()
        .issueId(issueId)
        .projectId(projectId).build()).orElseThrow();
    assertThat(modifiedIssue.getAssignee().getUsername()).isEqualTo(username);
  }

  @DisplayName("findIssue: 이슈 상세 정보를 성공적으로 가져온다.")
  @Test
  @Order(3)
  public void findIssue_Success() throws Exception {
    // given
    String title = "issue title";
    String description = "issue description";
    LocalDateTime reportedDate = LocalDateTime.now();
    String dueDate = LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_DATE);

    Project project = projectRepository.findAll().get(0);
    Long projectId = project.getProjectId();

    Issue issue = issueRepository.save(Issue.builder()
        .id(IssuePK.builder()
            .projectId(projectId)
            .issueId(1L)
            .build())
        .project(project)
        .title(title)
        .description(description)
        .reportedDate(reportedDate)
        .dueDate(LocalDate.parse(dueDate))
        .priority(IssuePriority.CRITICAL)
        .reporter(accountRepository.findAll().get(0))
        .status(IssueStatus.NEW)
        .build());

    // when
    String responseString = mockMvc.perform(get("/projects/{projectId}/issues/{issueId}", projectId, 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .session(sessions[1]))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    DetailsIssueResponse response = objectMapper.readValue(responseString, DetailsIssueResponse.class);

    // then
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getProjectId()).isEqualTo(issue.getProject().getProjectId());
    assertThat(response.getTitle()).isEqualTo(issue.getTitle());
    assertThat(response.getDescription()).isEqualTo(issue.getDescription());
    assertThat(response.getPriority()).isEqualTo(issue.getPriority().ordinal());
    assertThat(response.getReporter()).isEqualTo(issue.getReporter().getUsername());
    assertThat(response.getReportedDate()).isEqualTo(issue.getReportedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    assertThat(response.getDueDate()).isEqualTo(issue.getDueDate().toString());
  }

  @DisplayName("commentAdd: 이슈 코멘트 추가에 성공한다.")
  @Test
  @Order(4)
  public void commentAdd_Success() throws Exception {
    // Given
    Long accountId = 1L;
    Long projectId = 1L;
    Long issueId = 1L;
    AddCommentRequest addCommentRequest = AddCommentRequest.builder()
        .content("Test comment content")
        .build();

    LocalDateTime date = LocalDateTime.now();

    List<AddCommentResponse> addCommentResponseList = new ArrayList<>();
    AddCommentResponse addCommentResponse = AddCommentResponse.builder()
        .id(1L)
        .username("test_user")
        .content("Test comment content")
        .date(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        .build();
    addCommentResponseList.add(addCommentResponse);

    // when
    String responseString = mockMvc.perform(post("/projects/{projectId}/issues/{issueId}/comments", projectId, issueId)
            .contentType(MediaType.APPLICATION_JSON)
            .session(sessions[1])
            .content(objectMapper.writeValueAsString(addCommentRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then
    assertThat(addCommentResponseList).hasSize(1);
    assertThat(addCommentResponseList.get(0).getId()).isEqualTo(1L);
    assertThat(addCommentResponseList.get(0).getUsername()).isEqualTo("test_user");
    assertThat(addCommentResponseList.get(0).getContent()).isEqualTo("Test comment content");
    assertThat(addCommentResponseList.get(0).getDate()).isEqualTo(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
  }
}
