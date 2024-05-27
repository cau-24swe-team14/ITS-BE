package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.Issue;
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

  @Query("SELECT i FROM Issue i WHERE i.id.projectId = :projectId AND (i.reporter.id = :accountId OR i.manager.id = :accountId OR i.assignee.id = :accountId OR i.fixer.id = :accountId)")
  List<Issue> findByProjectIdAndAccountId(Long projectId, Long accountId);
}
