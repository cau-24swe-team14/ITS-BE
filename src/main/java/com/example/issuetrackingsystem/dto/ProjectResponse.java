package com.example.issuetrackingsystem.dto;

import com.example.issuetrackingsystem.domain.enums.ProjectStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Integer isAdmin;
    private List<ProjectData> project;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectData {
        private Long id;
        private String title;
        private Integer status;
    }
}
