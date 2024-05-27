package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.Comment;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.key.CommentPK;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, CommentPK> {
  Optional<List<Comment>> findByIssue(Issue issue);

  @Query("SELECT COALESCE(MAX(c.id.commentId), 0) "
      + "FROM Comment c "
      + "WHERE c.id.issueId.issueId = :issueId")
  Long findMaxIdByIssueId(@Param("issueId") Long issueId);

  @Query("SELECT c.id.issueId.issueId, c.issue.title, COUNT(*) AS count "
      + "FROM Comment c "
      + "WHERE c.date >= :startDate "
      + "GROUP BY c.id.issueId.issueId, c.id.issueId.projectId "
      + "ORDER BY count DESC "
      + "LIMIT 3")
  List<Object[]> findTop3IssuesWithMostComments(@Param("startDate") LocalDateTime startDate);
}
