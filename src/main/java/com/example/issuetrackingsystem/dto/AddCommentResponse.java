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
public class AddCommentResponse {
  private Long id;
  private String username;
  private String content;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private String date;
}