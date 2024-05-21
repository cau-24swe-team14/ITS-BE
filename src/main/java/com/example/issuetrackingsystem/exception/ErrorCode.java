package com.example.issuetrackingsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 이슈를 찾을 수 없습니다."),
  ;

  private final HttpStatus httpStatus;
  private final String message;
}