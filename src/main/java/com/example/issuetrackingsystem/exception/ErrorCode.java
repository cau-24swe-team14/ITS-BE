package com.example.issuetrackingsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 이슈를 찾을 수 없습니다."),
  ISSUE_CREATION_FORBIDDEN(HttpStatus.FORBIDDEN, "이슈를 생성할 권한이 없습니다."),
  ISSUE_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "이슈를 수정할 권한이 없습니다."),
  ISSUE_UPDATE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "이슈 수정 요청이 올바르지 않습니다."),
  ISSUE_DETAILS_FORBIDDEN(HttpStatus.FORBIDDEN, "이슈를 조회할 권한이 없습니다."),
  COMMENT_CREATION_FORBIDDEN(HttpStatus.FORBIDDEN, "이슈에 코멘트를 등록할 권한이 없습니다."),
  COMMENT_CREATION_BAD_REQUEST(HttpStatus.BAD_REQUEST, "이슈 코멘트 등록 요청이 올바르지 않습니다."),
  ;

  private final HttpStatus httpStatus;
  private final String message;
}
