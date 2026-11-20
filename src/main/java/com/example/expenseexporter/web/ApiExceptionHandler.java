package com.example.expenseexporter.web;

import com.example.expenseexporter.formula.FormulaSyntaxException;
import com.example.expenseexporter.validation.InvalidRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.core.JacksonException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(FormulaSyntaxException.class)
  ProblemDetail invalidFormula(FormulaSyntaxException e) {
    return problem(HttpStatus.BAD_REQUEST, "Invalid formula", e.getMessage());
  }

  @ExceptionHandler(InvalidRuleException.class)
  ProblemDetail invalidRule(InvalidRuleException e) {
    return problem(HttpStatus.BAD_REQUEST, "Invalid rule", e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail invalidRequest(IllegalArgumentException e) {
    return problem(HttpStatus.BAD_REQUEST, "Invalid request", e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail unexpected(Exception e) {
    log.error("Unexpected error", e);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "An unexpected error occurred");
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    ProblemDetail body = problem(status, "Invalid request body", readableMessage(ex));
    return handleExceptionInternal(ex, body, headers, status, request);
  }

  private static String readableMessage(HttpMessageNotReadableException ex) {
    String constructorMessage = null;
    String jacksonMessage = null;
    for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
      if (cause instanceof JacksonException jackson && jacksonMessage == null) {
        jacksonMessage = jackson.getOriginalMessage();
      }
      if ((cause instanceof IllegalArgumentException || cause instanceof NullPointerException)
          && cause.getMessage() != null) {
        constructorMessage = cause.getMessage();
      }
    }
    if (constructorMessage != null) {
      return constructorMessage;
    }
    return jacksonMessage != null ? jacksonMessage : "Request body is missing or is not valid JSON";
  }

  private static ProblemDetail problem(HttpStatusCode status, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }
}
