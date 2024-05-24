package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.Issue;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueRepository extends JpaRepository<Issue, IssuePK> {
  @Query("SELECT COALESCE(MAX(i.id.issueId), 0) FROM Issue i WHERE i.id.projectId = :projectId")
  Long findMaxIdByProjectId(@Param("projectId") Long projectId);

}
