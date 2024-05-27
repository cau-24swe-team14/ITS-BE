package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p JOIN ProjectAccount pa ON p.projectId = pa.project.projectId WHERE pa.account.accountId = :accountId")
    List<Project> findByAccountId(Long accountId);
}
