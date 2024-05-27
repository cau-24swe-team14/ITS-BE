package com.example.issuetrackingsystem.dto;

import com.example.issuetrackingsystem.domain.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long accountId;
    private String title;
    private ProjectStatus status;
}
