package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.dto.AddIssueRequest;

public interface IssueService {
  String addIssue(Long accountId, Long projectId, AddIssueRequest addIssueRequest);
}
