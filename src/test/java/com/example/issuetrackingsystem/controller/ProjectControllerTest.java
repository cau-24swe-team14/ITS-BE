package com.example.issuetrackingsystem.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.ProjectAccountRole;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.repository.AccountRepository;
import com.example.issuetrackingsystem.repository.CommentRepository;
import com.example.issuetrackingsystem.repository.IssueRepository;
import com.example.issuetrackingsystem.repository.ProjectAccountRepository;
import com.example.issuetrackingsystem.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
public class ProjectControllerTest {
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

  @Test
  @DisplayName("projectTrends: 프로젝트 트렌드 조회에 성공한다.")
//  @Order(5)
  @Disabled
  void testProjectTrendEndpoint() throws Exception {
    Long projectId = 1L;

    mockMvc.perform(get("/projects/{projectId}/trend", projectId)
            .param("category", "best-member")
            .session(sessions[0])
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
