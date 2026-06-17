package io.github.dbwhd5566.trafficqueuelab.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_001", "요청 값이 올바르지 않습니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_400_002", "요청 본문을 읽을 수 없습니다."),
    MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_400_003", "필수 요청 파라미터가 누락되었습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON_400_004", "요청 파라미터 타입이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_001", "요청한 리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401_001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403_001", "권한이 없습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_404_001", "계정을 찾을 수 없습니다."),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_404_001", "쿠폰을 찾을 수 없습니다."),
    COUPON_SOLD_OUT(HttpStatus.CONFLICT, "COUPON_409_001", "쿠폰이 모두 소진되었습니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "COUPON_409_002", "이미 발급받은 쿠폰입니다."),
    DB_CONNECTION_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "DB_503_001", "DB 커넥션을 확보하지 못했습니다. 잠시 후 다시 시도해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
