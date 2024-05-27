package com.example.issuetrackingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectTrendResponse {
  private ProjectTrendData daily;
  private ProjectTrendData weekly;
  private ProjectTrendData monthly;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProjectTrendData<T> {
    private T data;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IssueCount {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String date;
    private Integer count;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BestIssue {
    private Long issueId;
    private String title;
    private Integer count;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BestMemberData {
    private BestMember PL;
    private BestMember dev;
    private BestMember tester;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BestMember {
    private String username;
    private Integer count;
  }
}