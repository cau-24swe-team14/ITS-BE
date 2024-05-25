package com.example.issuetrackingsystem.controller;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.ProjectAccountRole;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.repository.AccountRepository;
import com.example.issuetrackingsystem.repository.IssueRepository;
import com.example.issuetrackingsystem.repository.ProjectAccountRepository;
import com.example.issuetrackingsystem.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
      @Autowired IssueRepository issueRepository) {
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
    LocalDateTime dueDate = LocalDateTime.parse(
        LocalDateTime.now().plusDays(3).format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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
}
