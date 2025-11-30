package com.jun_bank.common_lib.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 전역 공통 에러 코드
 * 모든 서비스에서 공통으로 사용하는 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    // ========================================
    // 서버 에러 (500번대)
    // ========================================
    INTERNAL_SERVER_ERROR("GLOBAL_001", "서버 내부 오류가 발생했습니다.", 500),
    SERVICE_UNAVAILABLE("GLOBAL_002", "서비스를 일시적으로 사용할 수 없습니다.", 503),
    DATABASE_ERROR("GLOBAL_003", "데이터베이스 오류가 발생했습니다.", 500),
    EXTERNAL_API_ERROR("GLOBAL_004", "외부 서비스 연동 중 오류가 발생했습니다.", 502),
    EXTERNAL_API_TIMEOUT("GLOBAL_005", "외부 서비스 응답 시간이 초과되었습니다.", 504),

    // ========================================
    // 클라이언트 에러 - 요청 (400번대)
    // ========================================
    BAD_REQUEST("GLOBAL_100", "잘못된 요청입니다.", 400),
    INVALID_INPUT_VALUE("GLOBAL_101", "유효하지 않은 입력값입니다.", 400),
    INVALID_TYPE_VALUE("GLOBAL_102", "잘못된 데이터 타입입니다.", 400),
    MISSING_PARAMETER("GLOBAL_103", "필수 파라미터가 누락되었습니다.", 400),
    INVALID_JSON_FORMAT("GLOBAL_104", "JSON 형식이 올바르지 않습니다.", 400),
    METHOD_NOT_ALLOWED("GLOBAL_105", "지원하지 않는 HTTP 메소드입니다.", 405),
    UNSUPPORTED_MEDIA_TYPE("GLOBAL_106", "지원하지 않는 미디어 타입입니다.", 415),

    // ========================================
    // 인증/인가 에러 (401, 403)
    // ========================================
    UNAUTHORIZED("GLOBAL_200", "인증이 필요합니다.", 401),
    INVALID_TOKEN("GLOBAL_201", "유효하지 않은 토큰입니다.", 401),
    EXPIRED_TOKEN("GLOBAL_202", "만료된 토큰입니다.", 401),
    TOKEN_MISSING("GLOBAL_203", "토큰이 누락되었습니다.", 401),
    INVALID_CREDENTIALS("GLOBAL_204", "인증 정보가 올바르지 않습니다.", 401),
    FORBIDDEN("GLOBAL_210", "접근 권한이 없습니다.", 403),
    ACCESS_DENIED("GLOBAL_211", "해당 리소스에 대한 권한이 없습니다.", 403),

    // ========================================
    // 리소스 에러 (404, 409)
    // ========================================
    RESOURCE_NOT_FOUND("GLOBAL_300", "요청한 리소스를 찾을 수 없습니다.", 404),
    ENDPOINT_NOT_FOUND("GLOBAL_301", "요청한 API 엔드포인트를 찾을 수 없습니다.", 404),
    CONFLICT("GLOBAL_310", "리소스 충돌이 발생했습니다.", 409),
    DUPLICATE_RESOURCE("GLOBAL_311", "이미 존재하는 리소스입니다.", 409),
    OPTIMISTIC_LOCK_FAILURE("GLOBAL_312", "동시 수정 충돌이 발생했습니다. 다시 시도해주세요.", 409),

    // ========================================
    // 비즈니스 로직 에러
    // ========================================
    BUSINESS_EXCEPTION("GLOBAL_400", "비즈니스 로직 오류가 발생했습니다.", 422),
    INVALID_STATE("GLOBAL_401", "현재 상태에서는 해당 작업을 수행할 수 없습니다.", 422),
    OPERATION_NOT_PERMITTED("GLOBAL_402", "허용되지 않은 작업입니다.", 422),

    // ========================================
    // 서비스 간 통신 에러
    // ========================================
    SERVICE_COMMUNICATION_ERROR("GLOBAL_500", "서비스 간 통신 오류가 발생했습니다.", 503),
    CIRCUIT_BREAKER_OPEN("GLOBAL_501", "서비스가 일시적으로 차단되었습니다. 잠시 후 다시 시도해주세요.", 503),
    RETRY_EXHAUSTED("GLOBAL_502", "재시도 횟수를 초과했습니다.", 503);

    private final String code;
    private final String message;
    private final int status;
}