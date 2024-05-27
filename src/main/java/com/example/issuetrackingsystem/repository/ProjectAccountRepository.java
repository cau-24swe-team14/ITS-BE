package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.enums.ProjectAccountRole;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAccountRepository extends JpaRepository<ProjectAccount, ProjectAccountPK> {
  List<ProjectAccount> findByProjectProjectIdAndRole(Long projectId, ProjectAccountRole role);
}
