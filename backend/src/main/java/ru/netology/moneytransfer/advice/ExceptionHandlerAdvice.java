package ru.netology.moneytransfer.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.moneytransfer.model.response.OperationFailure;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<OperationFailure> constraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(" "));

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new OperationFailure(getId(), message));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<OperationFailure> validationException(ValidationException ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new OperationFailure(getId(), ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<OperationFailure> httpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(new OperationFailure(getId(), "Невозможно прочитать входящее сообщение."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<OperationFailure> runtimeException(RuntimeException ex) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(new OperationFailure(getId(), ex.getMessage()));
    }

    private int getId() {
        return (int) System.currentTimeMillis();
    }

}
