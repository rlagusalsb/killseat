package com.killseat.common.exception;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //SSE 연결 중단 및 비동기 요청 타임아웃 처리
    //반환 타입을 void로 설정하여 추가적인 응답(JSON) 생성을 방지
    @ExceptionHandler({IOException.class, AsyncRequestTimeoutException.class})
    public void handleAsyncException(Exception ex) {
        log.warn("SSE연결 타임아웃: {}", ex.getMessage());
    }

    //비즈니스 로직 상 발생하는 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.warn("CustomException 발생: [{}] {}",
                ex.getErrorCode().getErrorCode(), ex.getErrorCode().getMessage());

        CustomErrorCode errorCode = ex.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode.getErrorCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(errorResponse);
    }

    @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        ErrorResponse errorResponse = new ErrorResponse("VALID_ERROR", errorMessage);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    //위에서 정의되지 않은 모든 예외를 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        ErrorResponse errorResponse = new ErrorResponse("SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

        return ResponseEntity.internalServerError().body(errorResponse);
    }
}