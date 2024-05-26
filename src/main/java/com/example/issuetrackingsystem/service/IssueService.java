package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.dto.AddCommentRequest;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.dto.DetailsIssueResponse;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import java.util.List;

public interface IssueService {
  String addIssue(Long accountId, Long projectId, AddIssueRequest addIssueRequest);
  void modifyIssue(Long accountId, Long projectId, Long issueId, ModifyIssueRequest modifyIssueRequest);
  DetailsIssueResponse findIssue(Long accountId, Long projectId, Long issueId);
  List<AddCommentRequest> addComment(Long accountId, Long projectId, Long issueId, AddCommentRequest addCommentRequest);
}
