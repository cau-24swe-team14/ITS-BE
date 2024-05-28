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
  PROJECT_TREND_FORBIDDEN(HttpStatus.FORBIDDEN, "프로젝트 트렌드를 조회할 권한이 없습니다."),
  PROJECT_TREND_BAD_REQUEST(HttpStatus.BAD_REQUEST, "프로젝트 트렌트 요청이 올바르지 않습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인 정보가 없습니다."),
  ISSUE_ASSIGNEE_SUGGESTION_FORBIDDEN(HttpStatus.FORBIDDEN, "이슈 assignee를 추천받을 권한이 없습니다."),
  ISSUE_ASSIGNEE_SUGGESTION_BAD_REQUEST(HttpStatus.BAD_REQUEST, "이슈 assignee 추천 요청이 올바르지 않습니다."),
  LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),
  USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
  PASSWORDS_DO_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
  PROJECT_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 프로젝트를 찾을 수 없습니다."),
  PROJECT_DETAIL_FORBIDDEN(HttpStatus.FORBIDDEN, "프로젝트 상세 조회 권한이 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
