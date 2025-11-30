package com.jun_bank.common_lib.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse 테스트")
class ApiResponseTest {

    @Nested
    @DisplayName("성공 응답 생성")
    class SuccessResponse {

        @Test
        @DisplayName("데이터와 함께 성공 응답 생성")
        void successWithData() {
            // given
            String data = "테스트 데이터";

            // when
            ApiResponse<String> response = ApiResponse.success(data);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(data);
            assertThat(response.getMessage()).isNull();
            assertThat(response.getError()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("데이터와 메시지를 포함한 성공 응답 생성")
        void successWithDataAndMessage() {
            // given
            String data = "테스트 데이터";
            String message = "성공 메시지";

            // when
            ApiResponse<String> response = ApiResponse.success(data, message);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(data);
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getError()).isNull();
        }

        @Test
        @DisplayName("데이터 없이 성공 응답 생성")
        void successWithoutData() {
            // when
            ApiResponse<Void> response = ApiResponse.success();

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isNull();
            assertThat(response.getError()).isNull();
        }

        @Test
        @DisplayName("메시지만 포함한 성공 응답 생성")
        void successWithMessageOnly() {
            // given
            String message = "작업이 완료되었습니다.";

            // when
            ApiResponse<Void> response = ApiResponse.successWithMessage(message);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getData()).isNull();
        }

        @Test
        @DisplayName("traceId 포함 성공 응답 생성")
        void successWithTrace() {
            // given
            String data = "테스트 데이터";
            String traceId = "abc123def456";

            // when
            ApiResponse<String> response = ApiResponse.successWithTrace(data, traceId);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(data);
            assertThat(response.getTraceId()).isEqualTo(traceId);
        }
    }

    @Nested
    @DisplayName("실패 응답 생성")
    class ErrorResponse {

        @Test
        @DisplayName("에러 코드와 메시지로 실패 응답 생성")
        void errorWithCodeAndMessage() {
            // given
            String code = "USER_001";
            String message = "사용자를 찾을 수 없습니다.";
            int status = 404;

            // when
            ApiResponse<Void> response = ApiResponse.error(code, message, status);

            // then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getData()).isNull();
            assertThat(response.getError()).isNotNull();
            assertThat(response.getError().getCode()).isEqualTo(code);
            assertThat(response.getError().getMessage()).isEqualTo(message);
            assertThat(response.getError().getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("경로 포함 실패 응답 생성")
        void errorWithPath() {
            // given
            String code = "USER_001";
            String message = "사용자를 찾을 수 없습니다.";
            int status = 404;
            String path = "/api/v1/users/999";

            // when
            ApiResponse<Void> response = ApiResponse.error(code, message, status, path);

            // then
            assertThat(response.getError().getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("ErrorDetail 객체로 실패 응답 생성")
        void errorWithErrorDetail() {
            // given
            ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.of(
                    "GLOBAL_001", "서버 내부 오류", 500, "/api/test"
            );

            // when
            ApiResponse<Void> response = ApiResponse.error(errorDetail);

            // then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getError()).isEqualTo(errorDetail);
        }

        @Test
        @DisplayName("traceId 포함 실패 응답 생성")
        void errorWithTrace() {
            // given
            ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.of(
                    "USER_001", "사용자를 찾을 수 없습니다.", 404
            );
            String traceId = "abc123def456";

            // when
            ApiResponse<Void> response = ApiResponse.errorWithTrace(errorDetail, traceId);

            // then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getTraceId()).isEqualTo(traceId);
        }
    }

    @Nested
    @DisplayName("ErrorDetail 테스트")
    class ErrorDetailTest {

        @Test
        @DisplayName("기본 ErrorDetail 생성")
        void createErrorDetail() {
            // when
            ApiResponse.ErrorDetail detail = ApiResponse.ErrorDetail.of(
                    "TEST_001", "테스트 에러", 400
            );

            // then
            assertThat(detail.getCode()).isEqualTo("TEST_001");
            assertThat(detail.getMessage()).isEqualTo("테스트 에러");
            assertThat(detail.getStatus()).isEqualTo(400);
            assertThat(detail.getPath()).isNull();
        }

        @Test
        @DisplayName("경로 포함 ErrorDetail 생성")
        void createErrorDetailWithPath() {
            // when
            ApiResponse.ErrorDetail detail = ApiResponse.ErrorDetail.of(
                    "TEST_001", "테스트 에러", 400, "/api/test"
            );

            // then
            assertThat(detail.getPath()).isEqualTo("/api/test");
        }
    }
}