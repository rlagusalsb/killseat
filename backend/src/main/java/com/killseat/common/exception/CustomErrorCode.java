package com.killseat.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {

    //회원,인증 관련 에러 (M)
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "M001", "아이디 혹은 비밀번호를 확인해주세요"),
    TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "M003", "로그인이 인증되지 않았습니다");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
