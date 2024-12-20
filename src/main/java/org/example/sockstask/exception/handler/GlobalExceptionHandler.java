package org.example.sockstask.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.sockstask.exception.InvalidCsvFileException;
import org.example.sockstask.exception.InvalidParametersException;
import org.example.sockstask.exception.NotEnoughSocksException;
import org.example.sockstask.exception.SockNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body("Invalid " + ex.getName() + " parameter");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleNotValidException(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("Invalid request parameter");
    }

    @ExceptionHandler({
            NotEnoughSocksException.class,
            InvalidCsvFileException.class,
            InvalidParametersException.class
    })
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(SockNotFoundException.class)
    public ResponseEntity<String> handleSockNotFound(SockNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong...");
    }

}
