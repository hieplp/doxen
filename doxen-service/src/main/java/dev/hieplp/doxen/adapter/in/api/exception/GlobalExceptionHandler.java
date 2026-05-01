package dev.hieplp.doxen.adapter.in.api.exception;

import dev.hieplp.doxen.adapter.in.api.dto.response.base.ApiResponse;
import dev.hieplp.doxen.adapter.in.api.statuscode.ErrorCode;
import dev.hieplp.doxen.adapter.in.api.statuscode.StatusCode;
import dev.hieplp.doxen.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException exception) {
        log.warn("Resource not found. message={}", exception.getMessage());
        return error(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MissingRequestHeaderException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        log.warn("Bad request. message={}", exception.getMessage());
        return error(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("Unexpected error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus httpStatus, StatusCode statusCode) {
        return ResponseEntity.status(httpStatus).body(new ApiResponse<>(statusCode));
    }
}
