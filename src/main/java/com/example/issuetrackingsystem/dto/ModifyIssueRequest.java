package com.example.issuetrackingsystem.dto;

import com.example.issuetrackingsystem.domain.Issue;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public class ModifyIssueRequest {

    private String title;
    private String description;
    private Integer priority;
    private Integer keyword;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String dueDate;
    private String assignee;
    private Integer status;
  }