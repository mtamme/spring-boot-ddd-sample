package com.github.seedwork.infrastructure.web;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;
import com.github.seedwork.core.problem.ProblemType;
import com.github.seedwork.infrastructure.persistence.SqlState;
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
public class ProblemDetailExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProblemDetailExceptionHandler.class);

  private static ResponseEntity<ProblemDetail> toResponseEntity(final HttpStatus status, final Problem problem) {
    return toResponseEntity(status, problem.type(), problem.message());
  }

  private static ResponseEntity<ProblemDetail> toResponseEntity(final HttpStatus status,
                                                                final ProblemType type,
                                                                final String message) {
    final var body = ProblemDetail.forStatusAndDetail(status, message);

    body.setType(type.uri());

    return ResponseEntity.of(body)
      .build();
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final ProblemException exception) {
    final var problem = exception.getProblem();

    return switch (problem.kind()) {
      case INVARIANT -> toResponseEntity(HttpStatus.CONFLICT, problem);
      case PRECONDITION -> toResponseEntity(HttpStatus.UNPROCESSABLE_CONTENT, problem);
      case NOT_FOUND -> toResponseEntity(HttpStatus.NOT_FOUND, problem);
    };
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final ConstraintViolationException exception) {
    return toResponseEntity(HttpStatus.BAD_REQUEST, ProblemTypes.INVALID, exception.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final MissingRequestValueException exception) {
    return toResponseEntity(HttpStatus.BAD_REQUEST, ProblemTypes.INVALID, exception.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final HttpMessageNotReadableException exception) {
    return toResponseEntity(HttpStatus.BAD_REQUEST, ProblemTypes.MALFORMED, exception.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final NoResourceFoundException exception) {
    return toResponseEntity(HttpStatus.NOT_FOUND, ProblemTypes.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final HttpRequestMethodNotSupportedException exception) {
    return toResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, ProblemTypes.METHOD_NOT_ALLOWED, exception.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final HttpMediaTypeNotAcceptableException exception) {
    return toResponseEntity(HttpStatus.NOT_ACCEPTABLE, ProblemTypes.NOT_ACCEPTABLE, exception.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handleException(final Throwable throwable) {
    if (throwable instanceof OptimisticLockingFailureException) {
      return toResponseEntity(HttpStatus.CONFLICT, ProblemTypes.LOCKING, "Locking conflict");
    }
    if ((throwable instanceof DataIntegrityViolationException exception) &&
      SqlState.INTEGRITY_CONSTRAINT_VIOLATION.isSubclass(exception.getRootCause())) {
      return toResponseEntity(HttpStatus.CONFLICT, ProblemTypes.DUPLICATE, "Duplicate conflict");
    }
    LOGGER.error("An unexpected error occurred", throwable);

    return toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ProblemTypes.INTERNAL, "Unexpected error");
  }
}
