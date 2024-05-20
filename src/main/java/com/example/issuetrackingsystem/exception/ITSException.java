package com.example.issuetrackingsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ITSException extends RuntimeException {
  private ErrorCode errorCode;
}
