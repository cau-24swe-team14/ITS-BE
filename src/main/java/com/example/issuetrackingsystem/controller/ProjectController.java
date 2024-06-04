package com.example.issuetrackingsystem.controller;

import com.example.issuetrackingsystem.dto.AddProjectRequest;
import com.example.issuetrackingsystem.dto.DetailsProjectResponse;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.dto.ModifyProjectRequest;
import com.example.issuetrackingsystem.dto.ProjectResponse;
import com.example.issuetrackingsystem.dto.ProjectTrendResponse;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public ResponseEntity projectList(HttpSession session) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    ProjectResponse projects;

    try {
      projects = projectService.projectList(accountId);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity.
        status(HttpStatus.OK)
        .body(projects);
  }

  @GetMapping("/{projectId}")
  public ResponseEntity ProjectDetails(HttpSession session, @PathVariable("projectId") Long projectId) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    DetailsProjectResponse detailsProjectResponse;

    try {
      detailsProjectResponse = projectService.findProject(projectId, accountId);

    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity.
        status(HttpStatus.OK)
        .body(detailsProjectResponse);
  }

  @PostMapping
  public ResponseEntity addProject(HttpSession session, @RequestBody AddProjectRequest addProjectRequest) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    String Location;

    try {
      Location = projectService.addProject(accountId, addProjectRequest);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity.
        status(HttpStatus.CREATED)
        .location(URI.create(Location))
        .build();
  }

  @PatchMapping("/{projectId}")
  public ResponseEntity projectModify(HttpSession session, @PathVariable("projectId") Long projectId,
      @RequestBody ModifyProjectRequest modifyProjectRequest) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    try {
      projectService.modifyProject(accountId, projectId, modifyProjectRequest);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @GetMapping("/{projectId}/trend")
  public ResponseEntity projectTrends(HttpSession session, @PathVariable("projectId") Long projectId, @RequestParam("category") String category) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    ProjectTrendResponse projectTrendResponse;

    try {
      projectTrendResponse = projectService.trendProject(accountId, projectId, category);
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
