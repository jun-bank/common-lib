package com.jun_bank.common_lib.exception;

/**
 * 에러 코드 인터페이스
 * 모든 도메인별 에러 코드 Enum이 구현해야 하는 인터페이스
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Getter
 * @RequiredArgsConstructor
 * public enum UserErrorCode implements ErrorCode {
 *     USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", 404),
 *     DUPLICATE_EMAIL("USER_002", "이미 존재하는 이메일입니다.", 409);
 *
 *     private final String code;
 *     private final String message;
 *     private final int status;
 * }
 * }</pre>
 */
public interface ErrorCode {

    /**
     * 에러 코드 반환
     * 형식: {도메인}_{번호} (예: USER_001, ACCOUNT_002)
     *
     * @return 에러 코드 문자열
     */
    String getCode();

    /**
     * 에러 메시지 반환
     *
     * @return 사용자에게 보여줄 에러 메시지
     */
    String getMessage();

    /**
     * HTTP 상태 코드 반환
     *
     * @return HTTP 상태 코드 (예: 400, 404, 500)
     */
    int getStatus();
}