package dev.study.orderplatform.web.error;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.study.orderplatform.domain.exception.CustomersNotFoundException;
import dev.study.orderplatform.domain.exception.OrderNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(OrderNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Order not found", exception.getMessage());
    }

    @ExceptionHandler(CustomersNotFoundException.class)
    ResponseEntity<ProblemDetail> handleCustomersNotFound(CustomersNotFoundException exception) {
        var status = HttpStatus.UNPROCESSABLE_CONTENT;
        var detail = ProblemDetail.forStatusAndDetail(
                status, "One or more order items reference customers that do not exist");
        detail.setTitle("Customers not found");
        detail.setProperty("missingCustomerIds", exception.missingCustomerIds());
        return ResponseEntity.status(status).body(detail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        detail.setTitle("Invalid request");
        List<Violation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .toList();
        detail.setProperty("violations", violations);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ProblemDetail> handleBadRequest(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detailMessage) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, detailMessage);
        detail.setTitle(title);
        return ResponseEntity.status(status).body(detail);
    }

    private record Violation(String field, String message) {
    }
}
