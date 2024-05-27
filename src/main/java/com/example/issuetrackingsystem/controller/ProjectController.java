package com.example.issuetrackingsystem.controller;

import com.example.issuetrackingsystem.dto.ProjectResponse;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectController {
  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @GetMapping
  public ResponseEntity getProjectList(HttpSession session) {
    Long accountId = (Long) session.getAttribute("id");

    try {
      if (accountId == null) {
        throw new ITSException(ErrorCode.UNAUTHORIZED);
      }
      List<ProjectResponse> projects = projectService.getProjectList(accountId);
      return ResponseEntity.ok(projects);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    } //catch (Exception e) {
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
//    }
  }

  @GetMapping("/{projectId}/trend")
  public ResponseEntity projectTrends(HttpSession session, @PathVariable("projectId") Long projectId, @RequestParam("category") String category) {
    Long accountId = (Long) session.getAttribute("id");

//    if (accountId == null) {
//      return ResponseEntity
//          .status(HttpStatus.UNAUTHORIZED)
//          .body("로그인 정보가 없습니다.");
//    }
    accountId = 1L;

    ProjectTrendResponse projectTrendResponse;

    try {
      projectTrendResponse = projectService.findProjectTrend(accountId, projectId, category);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(projectTrendResponse);
  }
}
