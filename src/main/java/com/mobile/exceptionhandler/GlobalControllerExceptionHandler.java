package com.mobile.exceptionhandler;

import com.mobile.exception.PhoneNotAvailableException;
import com.mobile.exception.PhoneNotCheckedOutException;
import com.mobile.exception.PhoneNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler {
  private static final String EXCEPTTION = "exception={}";

  @ExceptionHandler({
    PhoneNotAvailableException.class,
    PhoneNotCheckedOutException.class,
    PhoneNotFoundException.class
  })
  public final ResponseEntity<ExceptionResponse> handleBusinessException(
      Exception e, final WebRequest request) {
    log.error(EXCEPTTION, e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ExceptionResponse.builder()
                .message(e.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build());
  }

  @ExceptionHandler(HttpClientErrorException.class)
  public final ResponseEntity<ExceptionResponse> handleClientErrorException(
      HttpClientErrorException e, final WebRequest request) {
    log.error(EXCEPTTION, e.getMessage(), e);
    return ResponseEntity.status(e.getStatusCode())
        .body(
            ExceptionResponse.builder()
                .message(e.getResponseBodyAsString())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build());
  }

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<ExceptionResponse> handleGlobalException(
      Exception e, final WebRequest request) {
    log.error(EXCEPTTION, e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ExceptionResponse.builder()
                .message(e.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build());
  }
}
