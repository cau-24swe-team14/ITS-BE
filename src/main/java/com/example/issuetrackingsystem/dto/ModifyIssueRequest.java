package com.example.issuetrackingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModifyIssueRequest {
  private String title;
  private String description;
  private Integer keyword;
  private String assignee;
  private Integer priority;
  private Integer status;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private String dueDate;
}