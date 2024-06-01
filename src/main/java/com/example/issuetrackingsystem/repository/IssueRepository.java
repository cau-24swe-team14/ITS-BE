package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.enums.IssueKeyword;
import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.IssueStatus;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueRepository extends JpaRepository<Issue, IssuePK> {
  @Query("SELECT COALESCE(MAX(i.id.issueId), 0) "
      + "FROM Issue i "
      + "WHERE i.id.projectId = :projectId")
  Long findMaxIdByProjectId(@Param("projectId") Long projectId);

  @Query("SELECT YEAR(i.reportedDate), MONTH(i.reportedDate), DAY(i.reportedDate), COUNT(*) "
      + "FROM Issue i "
      + "WHERE i.id.projectId = :projectId "
      + "AND i.reportedDate >= :startDate "
      + "GROUP BY YEAR(i.reportedDate), MONTH(i.reportedDate), DAY(i.reportedDate)")
  List<Object[]> countByReportedDate_Day(@Param("projectId") Long projectId, @Param("startDate") LocalDateTime startDate);

  @Query("SELECT YEAR(i.reportedDate), MONTH(i.reportedDate), COUNT(*) "
      + "FROM Issue i "
      + "WHERE i.id.projectId = :projectId "
      + "AND i.reportedDate >= :startDate "
      + "GROUP BY YEAR(i.reportedDate), MONTH(i.reportedDate)")
  List<Object[]> countByReportedDate_Month(@Param("projectId") Long projectId, @Param("startDate") LocalDateTime startDate);

  @Query("SELECT YEAR(i.closedDate), MONTH(i.closedDate), DAY(i.closedDate), COUNT(*) "
      + "FROM Issue i "
      + "WHERE i.id.projectId = :projectId "
      + "AND i.closedDate >= :startDate "
      + "GROUP BY YEAR(i.closedDate), MONTH(i.closedDate), DAY(i.closedDate)")
  List<Object[]> countByClosedDate_Day(@Param("projectId") Long projectId, @Param("startDate") LocalDateTime startDate);

  @Query("SELECT YEAR(i.closedDate), MONTH(i.closedDate), COUNT(*) "
      + "FROM Issue i "
      + "WHERE i.id.projectId = :projectId "
      + "AND i.closedDate >= :startDate "
      + "GROUP BY YEAR(i.closedDate), MONTH(i.closedDate)")
  List<Object[]> countByClosedDate_Month(@Param("projectId") Long projectId, @Param("startDate") LocalDateTime startDate);

  @Query("SELECT i.manager.username, COUNT(*) AS count " +
      "FROM Issue i " +
      "WHERE i.reportedDate >= :startDate " +
      "GROUP BY i.manager.username " +
      "ORDER BY count DESC " +
      "LIMIT 1")
  Object[] findBestManagerDuringLastWeek(@Param("startDate") LocalDateTime startDate);

  @Query("SELECT i.assignee.username, COUNT(*) AS count " +
      "FROM Issue i " +
      "WHERE i.reportedDate >= :startDate " +
      "GROUP BY i.assignee.username " +
      "ORDER BY count DESC " +
      "LIMIT 1")
  Object[] findBestAssigneeDuringLastWeek(@Param("startDate") LocalDateTime startDate);

  @Query("SELECT i.reporter.username, COUNT(*) AS count " +
      "FROM Issue i " +
      "WHERE i.reportedDate >= :startDate " +
      "GROUP BY i.reporter.username " +
      "ORDER BY count DESC " +
      "LIMIT 1")
  Object[] findBestReporterDuringLastWeek(@Param("startDate") LocalDateTime startDate);

  @Query("SELECT i.assignee.username, COUNT(*) AS assigneeCount, SUM(CASE WHEN i.status <> 4 THEN 1 ELSE 0 END) AS openIssueCount " +
      "FROM Issue i " +
      "WHERE i.keyword = :keyword " +
      "GROUP BY i.assignee.username " +
      "ORDER BY assigneeCount DESC, openIssueCount ASC "
      + "LIMIT 1")
  Object[] findAssigneeSuggestionByKeyword(@Param("keyword") IssueKeyword issueKeyword);

  @Query("SELECT i.assignee.username, COUNT(*) AS assigneeCount, SUM(CASE WHEN i.status <> 4 THEN 1 ELSE 0 END) AS openIssueCount " +
      "FROM Issue i " +
      "GROUP BY i.assignee.username " +
      "ORDER BY assigneeCount DESC, openIssueCount ASC "
      + "LIMIT 1")
  Object[] findAssigneeSuggestion();

  @Query("SELECT i FROM Issue i WHERE i.id.projectId = :projectId")
  List<Issue> findByProjectId(@Param("projectId") Long projectId);

  List<Issue> findById_ProjectIdAndTitle(Long projectId, String title);

  List<Issue> findById_ProjectIdAndDescription(Long projectId, String description);

  List<Issue> findById_ProjectIdAndKeyword(Long projectId, IssueKeyword keyword);

  List<Issue> findById_ProjectIdAndReporter_Username(Long projectId, String reporter);

  List<Issue> findById_ProjectIdAndManager_Username(Long projectId, String manager);

  List<Issue> findById_ProjectIdAndAssignee_Username(Long projectId, String assignee);

  List<Issue> findById_ProjectIdAndFixer_Username(Long projectId, String fixer);

  List<Issue> findById_ProjectIdAndPriority(Long projectId, IssuePriority priority);

  List<Issue> findById_ProjectIdAndStatus(Long projectId, IssueStatus status);
}
