package com.jun_bank.common_lib.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessException 테스트")
class BusinessExceptionTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("ErrorCode만으로 예외 생성")
        void withErrorCodeOnly() {
            // when
            BusinessException exception = new BusinessException(GlobalErrorCode.RESOURCE_NOT_FOUND);

            // then
            assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.RESOURCE_NOT_FOUND);
            assertThat(exception.getMessage()).isEqualTo("요청한 리소스를 찾을 수 없습니다.");
            assertThat(exception.getCode()).isEqualTo("GLOBAL_300");
            assertThat(exception.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("ErrorCode와 커스텀 메시지로 예외 생성")
        void withCustomMessage() {
            // given
            String customMessage = "ID가 999인 사용자를 찾을 수 없습니다.";

            // when
            BusinessException exception = new BusinessException(
                    GlobalErrorCode.RESOURCE_NOT_FOUND, customMessage);

            // then
            assertThat(exception.getMessage()).isEqualTo(customMessage);
            assertThat(exception.getCode()).isEqualTo("GLOBAL_300");
        }

        @Test
        @DisplayName("ErrorCode와 원인 예외로 예외 생성")
        void withCause() {
            // given
            RuntimeException cause = new RuntimeException("DB 연결 실패");

            // when
            BusinessException exception = new BusinessException(
                    GlobalErrorCode.DATABASE_ERROR, cause);

            // then
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).isEqualTo("데이터베이스 오류가 발생했습니다.");
        }

        @Test
        @DisplayName("ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성")
        void withMessageAndCause() {
            // given
            String customMessage = "users 테이블 조회 실패";
            RuntimeException cause = new RuntimeException("Connection timeout");

            // when
            BusinessException exception = new BusinessException(
                    GlobalErrorCode.DATABASE_ERROR, customMessage, cause);

            // then
            assertThat(exception.getMessage()).isEqualTo(customMessage);
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getStatus()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("커스텀 ErrorCode 테스트")
    class CustomErrorCodeTest {

        enum TestErrorCode implements ErrorCode {
            TEST_ERROR("TEST_001", "테스트 에러입니다.", 400),
            ANOTHER_ERROR("TEST_002", "다른 에러입니다.", 422);

            private final String code;
            private final String message;
            private final int status;

            TestErrorCode(String code, String message, int status) {
                this.code = code;
                this.message = message;
                this.status = status;
            }

            @Override
            public String getCode() {
                return code;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public int getStatus() {
                return status;
            }
        }

        @Test
        @DisplayName("커스텀 ErrorCode로 예외 생성")
        void withCustomErrorCode() {
            // when
            BusinessException exception = new BusinessException(TestErrorCode.TEST_ERROR);

            // then
            assertThat(exception.getCode()).isEqualTo("TEST_001");
            assertThat(exception.getMessage()).isEqualTo("테스트 에러입니다.");
            assertThat(exception.getStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("다양한 상태 코드 처리")
        void variousStatusCodes() {
            // when
            BusinessException exception = new BusinessException(TestErrorCode.ANOTHER_ERROR);

            // then
            assertThat(exception.getStatus()).isEqualTo(422);
        }
    }

    @Nested
    @DisplayName("GlobalErrorCode 테스트")
    class GlobalErrorCodeTest {

        @Test
        @DisplayName("서버 에러 코드")
        void serverErrorCodes() {
            assertThat(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus()).isEqualTo(500);
            assertThat(GlobalErrorCode.SERVICE_UNAVAILABLE.getStatus()).isEqualTo(503);
            assertThat(GlobalErrorCode.DATABASE_ERROR.getStatus()).isEqualTo(500);
        }

        @Test
        @DisplayName("클라이언트 에러 코드")
        void clientErrorCodes() {
            assertThat(GlobalErrorCode.BAD_REQUEST.getStatus()).isEqualTo(400);
            assertThat(GlobalErrorCode.INVALID_INPUT_VALUE.getStatus()).isEqualTo(400);
            assertThat(GlobalErrorCode.METHOD_NOT_ALLOWED.getStatus()).isEqualTo(405);
        }

        @Test
        @DisplayName("인증/인가 에러 코드")
        void authErrorCodes() {
            assertThat(GlobalErrorCode.UNAUTHORIZED.getStatus()).isEqualTo(401);
            assertThat(GlobalErrorCode.FORBIDDEN.getStatus()).isEqualTo(403);
            assertThat(GlobalErrorCode.INVALID_TOKEN.getStatus()).isEqualTo(401);
        }

        @Test
        @DisplayName("리소스 에러 코드")
        void resourceErrorCodes() {
            assertThat(GlobalErrorCode.RESOURCE_NOT_FOUND.getStatus()).isEqualTo(404);
            assertThat(GlobalErrorCode.CONFLICT.getStatus()).isEqualTo(409);
            assertThat(GlobalErrorCode.DUPLICATE_RESOURCE.getStatus()).isEqualTo(409);
        }

        @Test
        @DisplayName("서비스 간 통신 에러 코드")
        void serviceErrorCodes() {
            assertThat(GlobalErrorCode.SERVICE_COMMUNICATION_ERROR.getStatus()).isEqualTo(503);
            assertThat(GlobalErrorCode.CIRCUIT_BREAKER_OPEN.getStatus()).isEqualTo(503);
            assertThat(GlobalErrorCode.RETRY_EXHAUSTED.getStatus()).isEqualTo(503);
        }
    }
}