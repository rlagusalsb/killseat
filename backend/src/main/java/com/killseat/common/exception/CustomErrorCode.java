package com.killseat.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {

    //회원, 인증 관련 (M)
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
    ALREADY_PROCESSED_PAYMENT(HttpStatus.CONFLICT, "PAY005", "이미 처리된 결제 건입니다."),

    //공연 관련 (PER)
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PER001", "해당 공연 정보를 찾을 수 없습니다."),
    PERFORMANCE_NOT_OPEN(HttpStatus.BAD_REQUEST, "PER002", "현재 예매 가능한 상태의 공연이 아닙니다."),
    INVALID_PERFORMANCE_STATUS(HttpStatus.BAD_REQUEST, "PER003", "변경할 수 없는 공연 상태입니다."),

    //공연 회차 관련 (SCH)
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCH001", "해당 공연 회차 정보를 찾을 수 없습니다."),
    MISSING_SCHEDULE(HttpStatus.BAD_REQUEST, "SCH002", "공연 등록 시 최소 하나 이상의 회차가 필요합니다."),
    SCHEDULE_ALREADY_PASSED(HttpStatus.BAD_REQUEST, "SCH003", "이미 지난 날짜의 회차는 등록할 수 없습니다."),

    //좌석 관련 (S)
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "좌석 정보를 찾을 수 없습니다."),
    SEAT_ALREADY_OCCUPIED(HttpStatus.CONFLICT, "S002", "이미 선택되었거나 사용 중인 좌석입니다."),
    SEAT_ALREADY_EXISTS(HttpStatus.CONFLICT, "S003", "해당 회차에 이미 생성된 좌석 데이터가 존재합니다."),

    //대기열 관련 (Q)
    ALREADY_ACTIVATED_USER(HttpStatus.BAD_REQUEST, "Q001", "이미 입장 허가된 사용자입니다."),
    ALREADY_IN_WAITING_QUEUE(HttpStatus.BAD_REQUEST, "Q002", "이미 대기열에 등록되어 있습니다."),
    QUEUE_NOT_FOUND(HttpStatus.NOT_FOUND, "Q003", "대기열 정보를 찾을 수 없습니다."),

    //예약 및 예매 관련 (R)
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "예약 정보를 찾을 수 없습니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "R002", "현재 상태에서는 예약 처리가 불가능합니다."),
    PERFORMANCE_RESERVATION_NOT_OPEN(HttpStatus.BAD_REQUEST, "R003", "공연 예매 기간이 아닙니다."), // PER002와 용도가 겹칠 수 있으나 유지
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "R004", "이미 다른 사용자가 선점 중인 좌석입니다."),
    CANNOT_CANCEL_AFTER_CLOSE(HttpStatus.BAD_REQUEST, "R005", "예매 종료 후에는 취소를 할 수 없습니다."),

    //공통 에러 (G)
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "G001", "필수 요청 파라미터가 누락되었습니다."),
    INVALID_INPUT_FORMAT(HttpStatus.BAD_REQUEST, "G002", "입력 형식이 올바르지 않습니다."),
    REJECTED_PERMISSION(HttpStatus.BAD_REQUEST, "G003", "허용되지 않은 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}