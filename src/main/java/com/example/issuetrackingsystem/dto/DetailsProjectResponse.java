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
public class DetailsProjectResponse {
    private Integer accountRole;
    private Long id;
    private String title;
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String date;
    private Integer status;
    private List<MemberData> member;
    private List<IssueData> issue;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberData {
        private Long id;
        private String username;
        private Integer role;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueData {
        private Long id;
        private String title;
        private Integer status;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private String reportedDate;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private String dueDate;
    }
}
