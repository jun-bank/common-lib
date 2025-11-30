package com.jun_bank.common_lib.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

/**
 * 통합 API 응답 포맷
 * 모든 REST API 응답에서 사용하는 표준 응답 구조
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 요청 성공 여부
     */
    private final boolean success;

    /**
     * 응답 메시지 (선택적)
     */
    private final String message;

    /**
     * 응답 데이터
     */
    private final T data;

    /**
     * 에러 정보 (실패 시)
     */
    private final ErrorDetail error;

    /**
     * 응답 생성 시간 (ISO 8601)
     */
    private final Instant timestamp;

    /**
     * 분산 추적 ID
     */
    private final String traceId;

    // ========================================
    // 성공 응답 팩토리 메서드
    // ========================================

    /**
     * 데이터와 함께 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 데이터와 메시지를 포함한 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 데이터 없이 성공 응답 생성
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 메시지만 포함한 성공 응답 생성
     */
    public static <T> ApiResponse<T> successWithMessage(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    // ========================================
    // 실패 응답 팩토리 메서드
    // ========================================

    /**
     * 에러 코드와 메시지로 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String code, String message, int status) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.of(code, message, status))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 에러 코드, 메시지, 경로로 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String code, String message, int status, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.of(code, message, status, path))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * ErrorDetail로 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorDetail errorDetail) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(errorDetail)
                .timestamp(Instant.now())
                .build();
    }

    // ========================================
    // traceId 포함 응답 (분산 추적용)
    // ========================================

    /**
     * traceId를 포함한 성공 응답 생성
     */
    public static <T> ApiResponse<T> successWithTrace(T data, String traceId) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .traceId(traceId)
                .build();
    }

    /**
     * traceId를 포함한 실패 응답 생성
     */
    public static <T> ApiResponse<T> errorWithTrace(ErrorDetail errorDetail, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(errorDetail)
                .timestamp(Instant.now())
                .traceId(traceId)
                .build();
    }

    // ========================================
    // 내부 클래스: 에러 상세 정보
    // ========================================

    /**
     * 에러 상세 정보
     */
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 에러 코드 (예: USER_001, GLOBAL_100)
         */
        private final String code;

        /**
         * 에러 메시지
         */
        private final String message;

        /**
         * HTTP 상태 코드
         */
        private final int status;

        /**
         * 요청 경로
         */
        private final String path;

        public static ErrorDetail of(String code, String message, int status) {
            return ErrorDetail.builder()
                    .code(code)
                    .message(message)
                    .status(status)
                    .build();
        }

        public static ErrorDetail of(String code, String message, int status, String path) {
            return ErrorDetail.builder()
                    .code(code)
                    .message(message)
                    .status(status)
                    .path(path)
                    .build();
        }
    }
}