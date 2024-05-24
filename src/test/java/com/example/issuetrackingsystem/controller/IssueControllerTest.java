package com.example.issuetrackingsystem.controller;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.ProjectUserRole;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
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
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
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

  private static MockHttpSession session;

  @BeforeAll
  public static void repositorySetUp(@Autowired AccountRepository accountRepository,
      @Autowired ProjectRepository projectRepository,
      @Autowired ProjectAccountRepository projectAccountRepository) {

    Account account = accountRepository.save(Account.builder()
        .username("username")
        .password("password")
        .build());

    Project project = projectRepository.save(Project.builder()
        .title("title")
        .description("description")
        .build());

    projectAccountRepository.save(ProjectAccount.builder()
        .id(ProjectAccountPK.builder()
            .accountId(account.getAccountId())
            .projectId(project.getProjectId())
            .build())
        .account(account)
        .project(project)
        .role(ProjectUserRole.tester)
        .build());

    Long accountId = account.getAccountId();
    session = new MockHttpSession();
    session.setAttribute("id", accountId);
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
      @Autowired IssueRepository issueRepository) throws Exception {
    issueRepository.deleteAll();
    projectAccountRepository.deleteAll();
    projectRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @DisplayName("issueAdd: 이슈 추가에 성공한다.")
  @Test
  public void issueAdd_Success() throws Exception {
    // given
    String title = "title";
    String description = "description";
    String priority = "MAJOR";
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

    String url = "/projects/" + projectId + "/issues";

    // when
    mockMvc.perform(post(url)
        .contentType(MediaType.APPLICATION_JSON)
        .session(session)
        .content(objectMapper.writeValueAsString(addIssueRequest)))
        .andExpect(status().isCreated());

    // then
    List<Issue> all = issueRepository.findAll();

    assertThat(all.size()).isEqualTo(1);
    assertThat(all.get(0).getTitle()).isEqualTo(title);
    assertThat(all.get(0).getDescription()).isEqualTo(description);
    assertThat(all.get(0).getPriority()).isEqualTo(IssuePriority.valueOf(priority));
    assertThat(all.get(0).getDueDate()).isEqualTo(dueDate);
  }
}
