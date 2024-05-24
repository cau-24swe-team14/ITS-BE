package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.ProjectAccount;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAccountRepository extends JpaRepository<ProjectAccount, ProjectAccountPK> {

}
