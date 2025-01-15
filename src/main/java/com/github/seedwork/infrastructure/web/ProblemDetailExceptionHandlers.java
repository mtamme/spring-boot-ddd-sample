package com.github.seedwork.infrastructure.web;

import com.github.seedwork.core.problem.NotFoundException;
import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProblemDetailExceptionHandlers {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProblemDetailExceptionHandlers.class);

  private static final Problem INVALID_PROBLEM = new Problem("invalid");
  private static final Problem MALFORMED_PROBLEM = new Problem("malformed");
  private static final Problem METHOD_NOT_ALLOWED_PROBLEM = new Problem("method-not-allowed");
  private static final Problem NOT_ACCEPTABLE_PROBLEM = new Problem("not-acceptable");
  private static final Problem CONFLICT_PROBLEM = new Problem("conflict");
  private static final Problem INTERNAL_PROBLEM = new Problem("internal");

  private static ResponseEntity<ProblemDetail> toResponseEntity(final HttpStatus status, final Problem problem) {
    final var body = ProblemDetail.forStatusAndDetail(status, problem.message());

    body.setType(problem.type());

    return ResponseEntity.of(body)
      .build();
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final ProblemException exception) {
    return toResponseEntity(HttpStatus.CONFLICT, exception.getProblem());
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final NotFoundException exception) {
    return toResponseEntity(HttpStatus.NOT_FOUND, exception.getProblem());
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final ConstraintViolationException exception) {
    return toResponseEntity(HttpStatus.BAD_REQUEST, INVALID_PROBLEM.withMessage(exception.getMessage()));
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final MissingRequestValueException exception) {
    return toResponseEntity(HttpStatus.BAD_REQUEST, INVALID_PROBLEM.withMessage(exception.getMessage()));
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final HttpMessageNotReadableException exception) {
    return toResponseEntity(HttpStatus.BAD_REQUEST, MALFORMED_PROBLEM.withMessage(exception.getMessage()));
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final HttpRequestMethodNotSupportedException exception) {
    return toResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, METHOD_NOT_ALLOWED_PROBLEM.withMessage(exception.getMessage()));
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final HttpMediaTypeNotAcceptableException exception) {
    return toResponseEntity(HttpStatus.NOT_ACCEPTABLE, NOT_ACCEPTABLE_PROBLEM.withMessage(exception.getMessage()));
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final DataIntegrityViolationException exception) {
    return toResponseEntity(HttpStatus.CONFLICT, CONFLICT_PROBLEM.withMessage(exception.getMessage()));
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final Throwable throwable) {
    LOGGER.error("An unexpected error occurred", throwable);

    return toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_PROBLEM.withMessage(throwable.getMessage()));
  }
}
