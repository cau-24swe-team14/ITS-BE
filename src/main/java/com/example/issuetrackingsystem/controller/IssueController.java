package com.example.issuetrackingsystem.controller;

import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.service.IssueService;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}")
public class IssueController {

  private final IssueService issueService;

  public IssueController(IssueService issueService) {
    this.issueService = issueService;
  }

  @PostMapping("/issues")
  public ResponseEntity issueAdd(HttpSession session, @PathVariable("projectId") Long projectId,
      @RequestBody AddIssueRequest addIssueRequest) {
    Long accountId = (Long) session.getAttribute("id");

//    if (accountId == null) {
//      return ResponseEntity
//          .status(HttpStatus.UNAUTHORIZED)
//          .body("로그인 정보가 없습니다.");
//    }
    accountId = 3L;

    String location;

    try {
      location = issueService.addIssue(accountId, projectId, addIssueRequest);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .location(URI.create(location)).build();
  }

  @PatchMapping("/issues/{issueId}")
  public ResponseEntity issueModify(HttpSession session, @PathVariable("projectId") Long projectId,
      @PathVariable("issueId") Long issueId, @RequestBody ModifyIssueRequest modifyIssueRequest) {
    Long accountId = (Long) session.getAttribute("id");

//    if (accountId == null) {
//      return ResponseEntity
//          .status(HttpStatus.UNAUTHORIZED)
//          .body("로그인 정보가 없습니다.");
//    }
    accountId = 1L;

    try {
      issueService.modifyIssue(accountId, projectId, issueId, modifyIssueRequest);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
