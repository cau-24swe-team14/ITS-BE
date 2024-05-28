package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.dto.AddCommentRequest;
import com.example.issuetrackingsystem.dto.AddCommentResponse;
import com.example.issuetrackingsystem.dto.AddIssueRequest;
import com.example.issuetrackingsystem.dto.DetailsIssueResponse;
import com.example.issuetrackingsystem.dto.ModifyIssueRequest;
import com.example.issuetrackingsystem.dto.SearchIssueResponse;
import com.example.issuetrackingsystem.dto.SuggestIssueAssigneeResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public interface IssueService {
  String addIssue(Long accountId, Long projectId, AddIssueRequest addIssueRequest);
  void modifyIssue(Long accountId, Long projectId, Long issueId, ModifyIssueRequest modifyIssueRequest);
  DetailsIssueResponse findIssue(Long accountId, Long projectId, Long issueId);
  List<AddCommentResponse> addComment(Long accountId, Long projectId, Long issueId, AddCommentRequest addCommentRequest);
  SuggestIssueAssigneeResponse suggestAssignee(Long accountId, Long projectId, Long issueId);
  List<SearchIssueResponse> searchIssue(Long accountId, Long projectId, Map<String, String> searchKey);
}
