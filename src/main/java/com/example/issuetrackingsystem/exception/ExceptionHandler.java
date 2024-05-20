package com.example.issuetrackingsystem.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {
  @org.springframework.web.bind.annotation.ExceptionHandler(ITSException.class)
  protected ResponseEntity<ErrorResponse> handleException(ITSException e) {
    if (e.getErrorCode().getHttpStatus().is5xxServerError()) {
      log.error("[ERROR]  MESSAGE : {}", e.getErrorCode().getMessage());
    } else if (e.getErrorCode().getHttpStatus().is4xxClientError()) {
      log.warn("[WARNING] MESSAGE : {}", e.getErrorCode().getMessage());
      log.debug("STACKTRACE : ", e);
    }

    return ErrorResponse.toResponseEntity(e.getErrorCode());
  }
}
