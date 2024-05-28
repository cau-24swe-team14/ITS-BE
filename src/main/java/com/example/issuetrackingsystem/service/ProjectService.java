package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.dto.ProjectResponse;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse;
import java.util.List;

public interface ProjectService {
  ProjectResponse getProjectList(Long accountId);
  ProjectTrendResponse findProjectTrend(Long accountId, Long projectId, String category);
}
