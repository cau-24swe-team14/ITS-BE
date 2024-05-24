package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}
