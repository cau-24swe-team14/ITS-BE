package com.example.issuetrackingsystem.dto;

import com.example.issuetrackingsystem.dto.DetailsProjectResponse.MemberData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProjectRequest {
    private String title;
    private String description;
    private List<ProjectMemberData> member;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectMemberData {
        private String username;
        private Integer role;
    }
}
