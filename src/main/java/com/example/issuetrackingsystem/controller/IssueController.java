package com.example.issuetrackingsystem.controller;

import com.example.issuetrackingsystem.dto.AddCommentRequest;
import com.example.issuetrackingsystem.dto.AddCommentResponse;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.dto.DetailsIssueResponse;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.dto.SearchIssueResponse;
import com.example.issuetrackingsystem.dto.SuggestIssueAssigneeResponse;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.service.IssueService;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.util.List;
import java.util.Map;
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

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

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

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

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

  @GetMapping("/issues/{issueId}")
  public ResponseEntity issueDetails(HttpSession session, @PathVariable("projectId") Long projectId, @PathVariable("issueId") Long issueId) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    DetailsIssueResponse detailsIssueResponse;

    try {
      detailsIssueResponse = issueService.findIssue(accountId, projectId, issueId);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(detailsIssueResponse);
  }

  @PostMapping("/issues/{issueId}/comments")
  public ResponseEntity commentAdd(HttpSession session, @PathVariable("projectId") Long projectId,
      @PathVariable("issueId") Long issueId, @RequestBody AddCommentRequest addCommentRequest) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    List<AddCommentResponse> addCommentResponseList;

    try {
      addCommentResponseList = issueService.addComment(accountId, projectId, issueId, addCommentRequest);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(addCommentResponseList);
  }

  @GetMapping("/issues/{issueId}/assignee-suggestions")
  public ResponseEntity assigneeSuggest(HttpSession session, @PathVariable("projectId") Long projectId,
      @PathVariable("issueId") Long issueId) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    SuggestIssueAssigneeResponse suggestIssueAssigneeResponse;

    try {
      suggestIssueAssigneeResponse = issueService.suggestAssignee(accountId, projectId, issueId);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(suggestIssueAssigneeResponse);
  }

  @GetMapping("/issues")
  public ResponseEntity searchIssues(HttpSession session, @PathVariable("projectId") Long projectId, @RequestParam Map<String, String> searchKey) {
    Long accountId = (Long) session.getAttribute("id");

    if (accountId == null) {
      throw new ITSException(ErrorCode.UNAUTHORIZED);
    }

    List<SearchIssueResponse> searchIssueResponseList;

    try {
      searchIssueResponseList = issueService.searchIssue(accountId, projectId, searchKey);
    } catch (ITSException e) {
      return ResponseEntity
          .status(e.getErrorCode().getHttpStatus())
          .body(e.getErrorCode().getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(searchIssueResponseList);
  }
}
