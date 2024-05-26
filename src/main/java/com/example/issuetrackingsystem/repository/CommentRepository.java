package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.Comment;
import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.key.CommentPK;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, CommentPK> {
  Optional<List<Comment>> findByIssue(Issue issue);
}
