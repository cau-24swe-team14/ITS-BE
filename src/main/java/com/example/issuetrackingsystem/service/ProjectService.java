package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.dto.ProjectTrendResponse;

public interface ProjectService {
  ProjectTrendResponse findProjectTrend(Long accountId, Long projectId, String category);
}
