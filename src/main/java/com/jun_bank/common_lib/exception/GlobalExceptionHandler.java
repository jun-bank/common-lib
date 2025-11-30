package com.jun_bank.common_lib.exception;

import com.jun_bank.common_lib.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 전역 예외 핸들러
 * 모든 예외를 일관된 형식으로 처리
 *
 * <p>각 서비스에서 이 핸들러를 상속하여 도메인별 예외 처리 추가 가능:</p>
 * <pre>{@code
 * @RestControllerAdvice
 * public class UserExceptionHandler extends GlobalExceptionHandler {
 *
 *     @ExceptionHandler(UserNotFoundException.class)
 *     public ResponseEntity<ApiResponse<Void>> handleUserNotFound(
 *             HttpServletRequest request, UserNotFoundException e) {
 *         // 커스텀 처리
 *     }
 * }
 * }</pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========================================
    // 비즈니스 예외 처리
    // ========================================

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            HttpServletRequest request,
            BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();
        log.warn("비즈니스 예외 발생: {} - {} (path: {})",
                errorCode.getCode(), e.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getCode(),
                e.getMessage(),
                errorCode.getStatus(),
                request.getRequestURI()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // ========================================
    // Validation 예외 처리
    // ========================================

    /**
     * @Valid 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            HttpServletRequest request,
            MethodArgumentNotValidException e) {

        String message = e.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

        log.warn("입력값 검증 실패: {} (path: {})", message, request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.INVALID_INPUT_VALUE.getCode(),
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 바인딩 예외 처리 (@ModelAttribute 등)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(
            HttpServletRequest request,
            BindException e) {

        String message = e.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

        log.warn("바인딩 실패: {} (path: {})", message, request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.INVALID_INPUT_VALUE.getCode(),
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // ========================================
    // 요청 관련 예외 처리
    // ========================================

    /**
     * JSON 파싱 실패 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpServletRequest request,
            HttpMessageNotReadableException e) {

        log.warn("JSON 파싱 실패: {} (path: {})", e.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.INVALID_JSON_FORMAT.getCode(),
                GlobalErrorCode.INVALID_JSON_FORMAT.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 필수 파라미터 누락 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            HttpServletRequest request,
            MissingServletRequestParameterException e) {

        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        log.warn("필수 파라미터 누락: {} (path: {})", e.getParameterName(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.MISSING_PARAMETER.getCode(),
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 파라미터 타입 불일치 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            HttpServletRequest request,
            MethodArgumentTypeMismatchException e) {

        String message = String.format("파라미터 '%s'의 타입이 올바르지 않습니다.", e.getName());
        log.warn("파라미터 타입 불일치: {} (path: {})", e.getName(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.INVALID_TYPE_VALUE.getCode(),
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 지원하지 않는 HTTP 메소드 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
            HttpServletRequest request,
            HttpRequestMethodNotSupportedException e) {

        log.warn("지원하지 않는 HTTP 메소드: {} {} (path: {})",
                e.getMethod(), request.getRequestURI(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.METHOD_NOT_ALLOWED.getCode(),
                GlobalErrorCode.METHOD_NOT_ALLOWED.getMessage(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 지원하지 않는 미디어 타입 처리
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(
            HttpServletRequest request,
            HttpMediaTypeNotSupportedException e) {

        log.warn("지원하지 않는 미디어 타입: {} (path: {})",
                e.getContentType(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
                GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    /**
     * 엔드포인트 없음 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
            HttpServletRequest request,
            NoHandlerFoundException e) {

        log.warn("엔드포인트 없음: {} {} (path: {})",
                e.getHttpMethod(), e.getRequestURL(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.ENDPOINT_NOT_FOUND.getCode(),
                GlobalErrorCode.ENDPOINT_NOT_FOUND.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ========================================
    // 기타 예외 처리
    // ========================================

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            HttpServletRequest request,
            IllegalArgumentException e) {

        log.warn("잘못된 인자: {} (path: {})", e.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.BAD_REQUEST.getCode(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * IllegalStateException 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(
            HttpServletRequest request,
            IllegalStateException e) {

        log.warn("잘못된 상태: {} (path: {})", e.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.INVALID_STATE.getCode(),
                e.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * 모든 미처리 예외 처리 (최후의 방어선)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(
            HttpServletRequest request,
            Exception e) {

        log.error("처리되지 않은 예외 발생 (path: {}): ", request.getRequestURI(), e);

        ApiResponse<Void> response = ApiResponse.error(
                GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}