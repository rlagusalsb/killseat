package com.killseat.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
