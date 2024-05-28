package com.example.issuetrackingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIssueResponse {
    private Long id;
    private String title;
    private Integer status;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String reportedDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String dueDate;
}
