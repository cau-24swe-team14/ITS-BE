package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.ProjectAccountRole;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.dto.AddProjectRequest;
import com.example.issuetrackingsystem.dto.AddProjectRequest.ProjectMemberData;
import com.example.issuetrackingsystem.dto.DetailsProjectResponse;
import com.example.issuetrackingsystem.dto.DetailsProjectResponse.IssueData;
import com.example.issuetrackingsystem.dto.DetailsProjectResponse.MemberData;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.dto.ModifyProjectRequest;
import com.example.issuetrackingsystem.dto.ProjectResponse;
import com.example.issuetrackingsystem.dto.ProjectResponse.ProjectData;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse.BestIssue;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse.BestMember;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse.BestMemberData;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse.ProjectTrendData;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.repository.AccountRepository;
import com.example.issuetrackingsystem.repository.CommentRepository;
import com.example.issuetrackingsystem.repository.IssueRepository;
import com.example.issuetrackingsystem.repository.ProjectAccountRepository;
import com.example.issuetrackingsystem.repository.ProjectRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
  private final AccountRepository accountRepository;
  private final ProjectRepository projectRepository;
  private final IssueRepository issueRepository;
  private final ProjectAccountRepository projectAccountRepository;
  private final CommentRepository commentRepository;

  public ProjectServiceImpl(AccountRepository accountRepository, ProjectRepository projectRepository, IssueRepository issueRepository,
      ProjectAccountRepository projectAccountRepository, CommentRepository commentRepository) {
    this.accountRepository = accountRepository;
    this.projectRepository = projectRepository;
    this.issueRepository = issueRepository;
    this.projectAccountRepository = projectAccountRepository;
    this.commentRepository = commentRepository;
  }

  @Override
  public ProjectResponse getProjectList(Long accountId) {
    List<Project> projects;
    if (accountId == 1) {
      projects = projectRepository.findAll();
    } else {
      projects = projectRepository.findByAccountId(accountId);
    }

    if (projects == null) {
      projects = new ArrayList<>();
    }

    return ProjectResponse.builder()
        .isAdmin(accountId == 1L ? 1 : 0)
        .project(projects.stream()
            .map(project -> ProjectData.builder()
                .id(project.getProjectId())
                .title(project.getTitle())
                .status(project.getStatus().ordinal())
                .build())
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public DetailsProjectResponse findProject(Long projectId, Long accountId) {
    ProjectAccount projectAccount = null;
    if (accountId != 1L) {
      projectAccount = projectAccountRepository.findById(ProjectAccountPK.builder()
          .accountId(accountId)
          .projectId(projectId)
          .build()).orElseThrow(() -> new ITSException(ErrorCode.PROJECT_DETAIL_FORBIDDEN));
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ITSException(ErrorCode.PROJECT_DETAIL_NOT_FOUND));

    List<Issue> issues = issueRepository.findByProjectId(projectId);

    if (issues == null) {
      issues = new ArrayList<>();
    }

    List<ProjectAccount> projectAccountList = projectAccountRepository.findById_ProjectId(projectId);

    DetailsProjectResponse.DetailsProjectResponseBuilder detailsProjectResponseBuilder = DetailsProjectResponse.builder()
        .accountRole(accountId == 1 ? -1 : projectAccount.getRole().ordinal())
        .id(projectId)
        .title(project.getTitle())
        .description(project.getDescription())
        .date(project.getDate().format(DateTimeFormatter.ISO_DATE))
        .status(project.getStatus().ordinal())
        .member(projectAccountList.stream()
            .map(member -> MemberData.builder()
                .id(member.getAccount().getAccountId())
                .username(member.getAccount().getUsername())
                .role(member.getRole().ordinal())
                .build())
            .collect(Collectors.toList()));

    if (issues.isEmpty()) {
      return detailsProjectResponseBuilder.build();
    } else {
      return detailsProjectResponseBuilder.issue(issues.stream()
              .map(issue -> IssueData.builder()
                  .id(issue.getId().getIssueId())
                  .title(issue.getTitle())
                  .status(issue.getStatus().ordinal())
                  .reportedDate(issue.getReportedDate().format(DateTimeFormatter.ISO_DATE))
                  .dueDate(issue.getDueDate().format(DateTimeFormatter.ISO_DATE))
                  .build())
              .collect(Collectors.toList()))
          .build();
    }
  }

  @Override
  public String addProject(Long accountId, AddProjectRequest addProjectRequest) {
    if (accountId != 1L) {
      throw new ITSException(ErrorCode.PROJECT_CREATION_FORBIDDEN);
    }

    Project project = projectRepository.save(Project.builder()
        .title(addProjectRequest.getTitle())
        .description(addProjectRequest.getDescription())
        .build());

    for(ProjectMemberData member: addProjectRequest.getMember()) {
      Account account = accountRepository.findByUsername(member.getUsername())
          .orElseThrow(() -> new ITSException(ErrorCode.PROJECT_CREATION_BAD_REQUEST));
      projectAccountRepository.save(ProjectAccount.builder()
          .id(ProjectAccountPK.builder()
              .projectId(project.getProjectId())
              .accountId(account.getAccountId())
              .build())
          .project(project)
          .account(account)
          .role(ProjectAccountRole.values()[member.getRole()])
          .build());
    }
    return "/projects/" + project.getProjectId();
  }

  @Override
  public void modifyProject(Long accountId, Long projectId,
      ModifyProjectRequest modifyProjectRequest) {
    if (accountId != 1L) {
      throw new ITSException(ErrorCode.PROJECT_UPDATE_FORBIDDEN);
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ITSException(ErrorCode.PROJECT_UPDATE_NOT_FOUND));

    List<ModifyProjectRequest.ProjectMemberData> projectMemberDataList = modifyProjectRequest.getMember();

    if (projectMemberDataList != null) {
      for (ModifyProjectRequest.ProjectMemberData projectMember : projectMemberDataList) {
        Account account = accountRepository.findByUsername(projectMember.getUsername())
            .orElseThrow(() -> new ITSException(ErrorCode.USERNAME_NOT_FOUND));
        projectAccountRepository.save(ProjectAccount.builder()
            .id(ProjectAccountPK.builder()
                .projectId(projectId)
                .accountId(account.getAccountId())
                .build())
            .project(project)
            .account(account)
            .role(ProjectAccountRole.values()[projectMember.getRole()])
            .build());
      }
    }

    Project.ProjectBuilder projectBuilder = Project.builder()
        .projectId(projectId)
        .date(project.getDate())
        .status(project.getStatus());

    if (modifyProjectRequest.getTitle() == null) {
      projectBuilder.title(project.getTitle());
    } else {
      projectBuilder.title(modifyProjectRequest.getTitle());
    }

    if (modifyProjectRequest.getDescription() == null) {
      projectBuilder.description(project.getDescription());
    } else {
      projectBuilder.description(modifyProjectRequest.getDescription());
    }

    projectRepository.save(projectBuilder.build());
  }

  @Override
  public ProjectTrendResponse findProjectTrend(Long accountId, Long projectId, String category) {
    // 사용자가 해당 프로젝트에 속해 있는지 검증
    ProjectAccount projectAccount = projectAccountRepository.findById(ProjectAccountPK.builder()
            .accountId(accountId)
            .projectId(projectId).build())
        .orElseThrow(() -> new ITSException(ErrorCode.PROJECT_TREND_FORBIDDEN));

    ProjectTrendResponse projectTrendResponse = null;

    LocalDate currentDate = LocalDate.now();

    switch (category) {
      case "new-issue":
        List<Object[]> dailyNewIssueCountObjectList = issueRepository.countByReportedDate_Day(projectId,
            LocalDateTime.of(LocalDate.from(LocalDateTime.now().minusDays(6)), LocalTime.of(0, 0)));

        Map<String, Integer> dailyNewDataMap = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
          LocalDate date = currentDate.minusDays(i);
          String key = String.format("%04d-%02d-%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
          dailyNewDataMap.put(key, 0);
        }

        for (Object[] object : dailyNewIssueCountObjectList) {
          String key = String.format("%04d-%02d-%01d", Integer.parseInt(String.valueOf(object[0])), Integer.parseInt(String.valueOf(object[1])), Integer.parseInt(String.valueOf(object[2])));
          if (dailyNewDataMap.containsKey(key)) {
            dailyNewDataMap.put(key, Integer.parseInt(String.valueOf(object[3])));
          }
        }

        List<ProjectTrendResponse.IssueCount> dailyNewIssueCountList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dailyNewDataMap.entrySet()) {
          dailyNewIssueCountList.add(ProjectTrendResponse.IssueCount.builder()
              .date(entry.getKey())
              .count(entry.getValue())
              .build());
        }

        List<Object[]> monthlyNewIssueCountObjectList = issueRepository.countByReportedDate_Month(projectId,
            LocalDateTime.of(LocalDate.from(LocalDateTime.now().minusMonths(5).withDayOfMonth(1)), LocalTime.of(0, 0)));

        Map<String, Integer> monthlyNewDataMap = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
          LocalDate date = currentDate.minusMonths(i);
          String key = String.format("%04d-%02d", date.getYear(), date.getMonthValue());
          monthlyNewDataMap.put(key, 0);
        }

        for (Object[] object : monthlyNewIssueCountObjectList) {
          String key = String.format("%04d-%02d", Integer.parseInt(String.valueOf(object[0])), Integer.parseInt(String.valueOf(object[1])));
          if (monthlyNewDataMap.containsKey(key)) {
            monthlyNewDataMap.put(key, Integer.parseInt(String.valueOf(object[2])));
          }
        }

        List<ProjectTrendResponse.IssueCount> monthlyNewIssueCountList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : monthlyNewDataMap.entrySet()) {
          monthlyNewIssueCountList.add(ProjectTrendResponse.IssueCount.builder()
              .date(entry.getKey() + "-01")
              .count(entry.getValue())
              .build());
        }

        projectTrendResponse = ProjectTrendResponse.builder()
            .daily(ProjectTrendData.builder()
                .data(dailyNewIssueCountList)
                .build())
            .monthly(ProjectTrendData.builder()
                .data(monthlyNewIssueCountList)
                .build())
            .build();
        break;

      case "closed-issue":
        List<Object[]> dailyClosedIssueCountObjectList = issueRepository.countByClosedDate_Day(projectId,
            LocalDateTime.of(LocalDate.from(LocalDateTime.now().minusDays(6)), LocalTime.of(0, 0)));

        Map<String, Integer> dailyClosedDataMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
          LocalDate date = currentDate.minusDays(i);
          String key = String.format("%04d-%02d-%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
          dailyClosedDataMap.put(key, 0);
        }

        for (Object[] object : dailyClosedIssueCountObjectList) {
          String key = String.format("%04d-%02d-%01d", Integer.parseInt(String.valueOf(object[0])), Integer.parseInt(String.valueOf(object[1])), Integer.parseInt(String.valueOf(object[2])));
          if (dailyClosedDataMap.containsKey(key)) {
            dailyClosedDataMap.put(key, Integer.parseInt(String.valueOf(object[3])));
          }
        }

        List<ProjectTrendResponse.IssueCount> dailyClosedIssueCountList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dailyClosedDataMap.entrySet()) {
          dailyClosedIssueCountList.add(ProjectTrendResponse.IssueCount.builder()
              .date(entry.getKey())
              .count(entry.getValue())
              .build());
        }

        List<Object[]> monthlyClosedIssueCountObjectList = issueRepository.countByClosedDate_Month(projectId,
            LocalDateTime.of(LocalDate.from(LocalDateTime.now().minusMonths(5).withDayOfMonth(1)), LocalTime.of(0, 0)));

        Map<String, Integer> monthlyClosedDataMap = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
          LocalDate date = currentDate.minusMonths(i);
          String key = String.format("%04d-%02d", date.getYear(), date.getMonthValue());
          monthlyClosedDataMap.put(key, 0);
        }

        for (Object[] object : monthlyClosedIssueCountObjectList) {
          String key = String.format("%04d-%02d", Integer.parseInt(String.valueOf(object[0])), Integer.parseInt(String.valueOf(object[1])));
          if (monthlyClosedDataMap.containsKey(key)) {
            monthlyClosedDataMap.put(key, Integer.parseInt(String.valueOf(object[2])));
          }
        }

        List<ProjectTrendResponse.IssueCount> monthlyClosedIssueCountList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : monthlyClosedDataMap.entrySet()) {
          monthlyClosedIssueCountList.add(ProjectTrendResponse.IssueCount.builder()
              .date(entry.getKey() + "-01")
              .count(entry.getValue())
              .build());
        }

        projectTrendResponse = ProjectTrendResponse.builder()
            .daily(ProjectTrendData.builder()
                .data(dailyClosedIssueCountList)
                .build())
            .monthly(ProjectTrendData.builder()
                .data(monthlyClosedIssueCountList)
                .build())
            .build();
        break;

      case "best-issue":
        List<Object[]> top3IssueObjectDaily = commentRepository.findTop3IssuesWithMostComments(currentDate.atStartOfDay());
        List<BestIssue> top3IssueDaily = new ArrayList<>();

        for (Object[] object : top3IssueObjectDaily) {
          top3IssueDaily.add(BestIssue.builder()
              .issueId(Long.parseLong(String.valueOf(object[0])))
              .title(String.valueOf(object[1]))
              .count(Integer.parseInt(String.valueOf(object[2])))
              .build());
        }

        List<Object[]> top3IssueObjectMonthly = commentRepository.findTop3IssuesWithMostComments(currentDate.withDayOfMonth(1).atStartOfDay());
        List<BestIssue> top3IssueMonthly = new ArrayList<>();

        for (Object[] object : top3IssueObjectMonthly) {
          top3IssueMonthly.add(BestIssue.builder()
              .issueId(Long.parseLong(String.valueOf(object[0])))
              .title(String.valueOf(object[1]))
              .count(Integer.parseInt(String.valueOf(object[2])))
              .build());
        }

        projectTrendResponse = ProjectTrendResponse.builder()
            .daily(ProjectTrendData.builder()
                .data(top3IssueDaily)
                .build())
            .monthly(ProjectTrendData.builder()
                .data(top3IssueMonthly)
                .build())
            .build();
        break;

      case "best-member":
        Object[] bestPLObject = issueRepository.findBestManagerDuringLastWeek(currentDate.minusDays(6).atStartOfDay());
        BestMember bestPL = BestMember.builder()
            .username(String.valueOf(bestPLObject[0]))
            .count(Integer.parseInt(String.valueOf(bestPLObject[1])))
            .build();

        Object[] bestDevObject = issueRepository.findBestAssigneeDuringLastWeek(currentDate.minusDays(6).atStartOfDay());
        BestMember bestDev = BestMember.builder()
            .username(String.valueOf(bestDevObject[0]))
            .count(Integer.parseInt(String.valueOf(bestDevObject[1])))
            .build();

        Object[] bestTesterObject = issueRepository.findBestReporterDuringLastWeek(currentDate.minusDays(6).atStartOfDay());
        BestMember bestTester = BestMember.builder()
            .username(String.valueOf(bestTesterObject[0]))
            .count(Integer.parseInt(String.valueOf(bestTesterObject[1])))
            .build();

        projectTrendResponse = ProjectTrendResponse.builder()
            .weekly(ProjectTrendData.builder()
                .data(BestMemberData.builder()
                    .PL(bestPL)
                    .dev(bestDev)
                    .tester(bestTester)
                    .build())
                .build())
            .build();
        break;

      default:
        throw new ITSException(ErrorCode.PROJECT_TREND_BAD_REQUEST);
    }

    return projectTrendResponse;
  }
}