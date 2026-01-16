package com.killseat.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {

    //회원,인증 관련 (M)
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "M001", "아이디 혹은 비밀번호를 확인해주세요"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "회원 정보를 찾을 수 없습니다."),
    TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "M003", "로그인이 인증되지 않았습니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M004", "이미 사용 중인 이메일입니다."),

    //댓글 관련 (C)
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "댓글을 찾을 수 없습니다."),
    ACCESS_DENIED_COMMENT(HttpStatus.FORBIDDEN, "C002", "댓글에 대한 권한이 없습니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "부모 댓글을 찾을 수 없습니다."),
    PARENT_COMMENT_NOT_MATCH(HttpStatus.BAD_REQUEST, "C004", "부모 댓글이 해당 게시글에 속하지 않습니다."),

    //게시글 관련 (POST)
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST001", "게시글을 찾을 수 없습니다."),

    //결제 관련 (PAY)
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY001", "결제 정보를 찾을 수 없습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "PAY002", "결제 가능한 상태가 아닙니다."),
    PAYMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "PAY003", "결제 금액 검증에 실패했습니다."),
    SEAT_OCCUPANCY_EXPIRED(HttpStatus.GONE, "PAY004", "좌석 선점 시간이 만료되었습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY005", "예약 정보를 찾을 수 없습니다."),
    ALREADY_PROCESSED_PAYMENT(HttpStatus.CONFLICT, "PAY006", "이미 처리된 결제 건입니다."),

    //공통 에러 (G)
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "G001", "필수 요청 파라미터가 누락되었습니다."),
    INVALID_INPUT_FORMAT(HttpStatus.BAD_REQUEST, "G002", "입력 형식이 올바르지 않습니다."),
    REJECTED_PERMISSION(HttpStatus.BAD_REQUEST, "G003", "허용되지 않은 요청입니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
