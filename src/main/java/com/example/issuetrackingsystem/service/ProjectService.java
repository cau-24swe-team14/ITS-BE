package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.dto.IssueResponse;
import com.example.issuetrackingsystem.dto.ProjectResponse;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse;
import java.util.List;

public interface ProjectService {
  List<ProjectResponse> getProjectList(Long accountId);
  List<IssueResponse> getIssueList(Long projectId, Long accountId);
  ProjectTrendResponse findProjectTrend(Long accountId, Long projectId, String category);
}
