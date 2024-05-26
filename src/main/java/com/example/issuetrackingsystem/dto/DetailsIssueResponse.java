package com.example.issuetrackingsystem.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailsIssueResponse {
  private Long id;
  private Long projectId;
  private String title;
  private String description;
  private Integer keyword;
  private String reporter;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private String reportedDate;
  private String manager;
  private String assignee;
  private String fixer;
  private Integer priority;
  private Integer status;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private String dueDate;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private String closedDate;
  private List<CommentResponse> comment;
}
