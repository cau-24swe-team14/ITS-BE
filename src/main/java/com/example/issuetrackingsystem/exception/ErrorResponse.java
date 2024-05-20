package com.example.issuetrackingsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
  private int status;
  private String code;
  private String message;

  public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode e) {
    return ResponseEntity
        .status(e.getHttpStatus())
        .body(ErrorResponse.builder()
            .status(e.getHttpStatus().value())
            .code(e.name())
            .message(e.getMessage())
            .build()
        );
  }
}
