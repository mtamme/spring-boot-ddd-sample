package com.github.seedwork.infrastructure.web;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;
import com.github.seedwork.infrastructure.persistence.SqlClass;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ProblemDetailExceptionHandlers {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProblemDetailExceptionHandlers.class);

  private static final Problem INVALID_PROBLEM = Problem.of("invalid");
  private static final Problem MALFORMED_PROBLEM = Problem.of("malformed");
  private static final Problem NOT_FOUND_PROBLEM = Problem.of("not-found");
  private static final Problem METHOD_NOT_ALLOWED_PROBLEM = Problem.of("method-not-allowed");
  private static final Problem NOT_ACCEPTABLE_PROBLEM = Problem.of("not-acceptable");
  private static final Problem LOCKING_PROBLEM = Problem.of("locking", "Locking conflict");
  private static final Problem DUPLICATE_PROBLEM = Problem.of("duplicate", "Duplicate conflict");
  private static final Problem INTERNAL_PROBLEM = Problem.of("internal", "Unexpected error");

  private static ResponseEntity<ProblemDetail> toResponseEntity(final HttpStatus status, final Problem problem) {
    final var body = ProblemDetail.forStatusAndDetail(status, problem.message());

    body.setType(problem.type());

    return ResponseEntity.of(body)
      .build();
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final ProblemException exception) {
    final var problem = exception.getProblem();

    return switch (problem.kind()) {
      case CONFLICT -> toResponseEntity(HttpStatus.CONFLICT, problem);
      case NOT_FOUND -> toResponseEntity(HttpStatus.NOT_FOUND, problem);
      default -> toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    };
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
  public ResponseEntity<ProblemDetail> handleException(final NoResourceFoundException exception) {
    return toResponseEntity(HttpStatus.NOT_FOUND, NOT_FOUND_PROBLEM.withMessage(exception.getMessage()));
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
  public ResponseEntity<ProblemDetail> handleException(final Throwable throwable) {
    if (throwable instanceof OptimisticLockingFailureException) {
      return toResponseEntity(HttpStatus.CONFLICT, LOCKING_PROBLEM);
    }
    if ((throwable instanceof DataIntegrityViolationException exception) &&
      SqlClass.INTEGRITY_CONSTRAINT_VIOLATION.isSubclass(exception.getRootCause())) {
      return toResponseEntity(HttpStatus.CONFLICT, DUPLICATE_PROBLEM);
    }
    LOGGER.error("An unexpected error occurred", throwable);

    return toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_PROBLEM);
  }
}
