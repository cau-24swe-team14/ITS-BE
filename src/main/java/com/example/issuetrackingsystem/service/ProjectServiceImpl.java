package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.Project;
import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import com.example.issuetrackingsystem.dto.IssueResponse;
import com.example.issuetrackingsystem.dto.ProjectResponse;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
  private final ProjectRepository projectRepository;
  private final IssueRepository issueRepository;
  private final ProjectAccountRepository projectAccountRepository;
  private final CommentRepository commentRepository;

  public ProjectServiceImpl(ProjectRepository projectRepository, IssueRepository issueRepository,
      ProjectAccountRepository projectAccountRepository, CommentRepository commentRepository) {
    this.projectRepository = projectRepository;
    this.issueRepository = issueRepository;
    this.projectAccountRepository = projectAccountRepository;
    this.commentRepository = commentRepository;
  }

  @Override
  public List<ProjectResponse> getProjectList(Long accountId) {
    List<Project> projects = projectRepository.findByAccountId(accountId);

    if (projects == null) {
      projects = new ArrayList<>();
    }

    return projects.stream()
        .map(project -> new ProjectResponse(project.getProjectId(), project.getTitle(), project.getStatus()))
        .collect(Collectors.toList());
  }

  @Override
  public List<IssueResponse> getIssueList(Long projectId, Long accountId) {
    List<Issue> issues = issueRepository.findByProjectIdAndAccountId(projectId, accountId);

    if (issues == null) {
      issues = new ArrayList<>();
    }

    return issues.stream()
        .map(issue -> new IssueResponse();
        .collect(Collectors.toList());
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