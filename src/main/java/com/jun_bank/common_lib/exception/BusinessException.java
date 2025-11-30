package com.jun_bank.common_lib.exception;

import lombok.Getter;

/**
 * 비즈니스 예외 기본 클래스
 * 모든 비즈니스 로직 예외의 부모 클래스
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * // 기본 에러 메시지 사용
 * throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
 *
 * // 커스텀 메시지 사용
 * throw new BusinessException(UserErrorCode.USER_NOT_FOUND, "ID가 " + id + "인 사용자를 찾을 수 없습니다.");
 *
 * // 원인 예외 포함
 * throw new BusinessException(GlobalErrorCode.DATABASE_ERROR, e);
 * }</pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * ErrorCode만으로 예외 생성
     * 기본 에러 메시지 사용
     *
     * @param errorCode 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 커스텀 메시지로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param message   커스텀 에러 메시지
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 원인 예외로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param cause     원인 예외
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param message   커스텀 에러 메시지
     * @param cause     원인 예외
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * HTTP 상태 코드 반환
     */
    public int getStatus() {
        return errorCode.getStatus();
    }

    /**
     * 에러 코드 문자열 반환
     */
    public String getCode() {
        return errorCode.getCode();
    }
}