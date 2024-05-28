package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.dto.AddProjectRequest;
import com.example.issuetrackingsystem.dto.DetailsProjectResponse;
import com.example.issuetrackingsystem.dto.ModifyProjectRequest;
import com.example.issuetrackingsystem.dto.ProjectResponse;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse;
import java.util.List;

public interface ProjectService {
  ProjectResponse getProjectList(Long accountId);
  DetailsProjectResponse findProject(Long projectId, Long accountId);
  ProjectTrendResponse findProjectTrend(Long accountId, Long projectId, String category);

  String addProject(Long accountId, AddProjectRequest addProjectRequest);

  void modifyProject(Long accountId, Long projectId, ModifyProjectRequest modifyProjectRequest);
}
