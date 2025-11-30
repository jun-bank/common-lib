package com.jun_bank.common_lib.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JsonUtils 테스트")
class JsonUtilsTest {

    // 테스트용 DTO
    record TestDto(Long id, String name, Instant createdAt) {}

    record NestedDto(String title, List<TestDto> items) {}

    @Nested
    @DisplayName("Object → JSON 변환")
    class ObjectToJson {

        @Test
        @DisplayName("객체를 JSON 문자열로 변환")
        void toJson() {
            // given
            TestDto dto = new TestDto(1L, "테스트", Instant.parse("2025-01-15T10:30:00Z"));

            // when
            String json = JsonUtils.toJson(dto);

            // then
            assertThat(json).contains("\"id\":1");
            assertThat(json).contains("\"name\":\"테스트\"");
            assertThat(json).contains("\"createdAt\":\"2025-01-15T10:30:00Z\"");
        }

        @Test
        @DisplayName("null 필드는 JSON에서 제외")
        void nullFieldsExcluded() {
            // given
            TestDto dto = new TestDto(1L, null, null);

            // when
            String json = JsonUtils.toJson(dto);

            // then
            assertThat(json).contains("\"id\":1");
            assertThat(json).doesNotContain("name");
            assertThat(json).doesNotContain("createdAt");
        }

        @Test
        @DisplayName("toJsonSafe - 성공 시 Optional 반환")
        void toJsonSafeSuccess() {
            // given
            TestDto dto = new TestDto(1L, "테스트", null);

            // when
            Optional<String> result = JsonUtils.toJsonSafe(dto);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).contains("\"id\":1");
        }

        @Test
        @DisplayName("toPrettyJson - 포맷팅된 JSON 반환")
        void toPrettyJson() {
            // given
            TestDto dto = new TestDto(1L, "테스트", null);

            // when
            String json = JsonUtils.toPrettyJson(dto);

            // then
            assertThat(json).contains("\n");  // 줄바꿈 포함
            assertThat(json).contains("  ");  // 들여쓰기 포함
        }

        @Test
        @DisplayName("toBytes - byte 배열로 변환")
        void toBytes() {
            // given
            TestDto dto = new TestDto(1L, "테스트", null);

            // when
            byte[] bytes = JsonUtils.toBytes(dto);

            // then
            assertThat(bytes).isNotEmpty();
            assertThat(new String(bytes)).contains("\"id\":1");
        }
    }

    @Nested
    @DisplayName("JSON → Object 변환")
    class JsonToObject {

        @Test
        @DisplayName("JSON 문자열을 객체로 변환")
        void fromJson() {
            // given
            String json = """
                    {"id":1,"name":"테스트","createdAt":"2025-01-15T10:30:00Z"}
                    """;

            // when
            TestDto dto = JsonUtils.fromJson(json, TestDto.class);

            // then
            assertThat(dto.id()).isEqualTo(1L);
            assertThat(dto.name()).isEqualTo("테스트");
            assertThat(dto.createdAt()).isEqualTo(Instant.parse("2025-01-15T10:30:00Z"));
        }

        @Test
        @DisplayName("알 수 없는 필드 무시")
        void ignoreUnknownFields() {
            // given
            String json = """
                    {"id":1,"name":"테스트","unknownField":"ignored"}
                    """;

            // when
            TestDto dto = JsonUtils.fromJson(json, TestDto.class);

            // then
            assertThat(dto.id()).isEqualTo(1L);
            assertThat(dto.name()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("fromJsonSafe - 성공 시 Optional 반환")
        void fromJsonSafeSuccess() {
            // given
            String json = """
                    {"id":1,"name":"테스트"}
                    """;

            // when
            Optional<TestDto> result = JsonUtils.fromJsonSafe(json, TestDto.class);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("fromJsonSafe - 실패 시 빈 Optional 반환")
        void fromJsonSafeFail() {
            // given
            String invalidJson = "invalid json";

            // when
            Optional<TestDto> result = JsonUtils.fromJsonSafe(invalidJson, TestDto.class);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("잘못된 JSON 변환 시 예외")
        void invalidJsonThrowsException() {
            // given
            String invalidJson = "invalid json";

            // when & then
            assertThatThrownBy(() -> JsonUtils.fromJson(invalidJson, TestDto.class))
                    .isInstanceOf(JsonUtils.JsonConversionException.class)
                    .hasMessageContaining("JSON 역직렬화에 실패");
        }

        @Test
        @DisplayName("TypeReference로 제네릭 타입 변환")
        void fromJsonWithTypeReference() {
            // given
            String json = """
                    [{"id":1,"name":"첫번째"},{"id":2,"name":"두번째"}]
                    """;

            // when
            List<TestDto> list = JsonUtils.fromJson(json, new TypeReference<>() {});

            // then
            assertThat(list).hasSize(2);
            assertThat(list.get(0).id()).isEqualTo(1L);
            assertThat(list.get(1).id()).isEqualTo(2L);
        }

        @Test
        @DisplayName("fromBytes - byte 배열에서 변환")
        void fromBytes() {
            // given
            String json = """
                    {"id":1,"name":"테스트"}
                    """;
            byte[] bytes = json.getBytes();

            // when
            TestDto dto = JsonUtils.fromBytes(bytes, TestDto.class);

            // then
            assertThat(dto.id()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("편의 메서드")
    class ConvenienceMethods {

        @Test
        @DisplayName("fromJsonToList - List로 변환")
        void fromJsonToList() {
            // given
            String json = """
                    [{"id":1,"name":"첫번째"},{"id":2,"name":"두번째"}]
                    """;

            // when
            List<TestDto> list = JsonUtils.fromJsonToList(json, TestDto.class);

            // then
            assertThat(list).hasSize(2);
        }

        @Test
        @DisplayName("fromJsonToMap - Map으로 변환")
        void fromJsonToMap() {
            // given
            String json = """
                    {"key1":"value1","key2":123,"key3":true}
                    """;

            // when
            Map<String, Object> map = JsonUtils.fromJsonToMap(json);

            // then
            assertThat(map).containsEntry("key1", "value1");
            assertThat(map).containsEntry("key2", 123);
            assertThat(map).containsEntry("key3", true);
        }

        @Test
        @DisplayName("parseJson - JsonNode로 파싱")
        void parseJson() {
            // given
            String json = """
                    {"id":1,"nested":{"value":"test"}}
                    """;

            // when
            JsonNode node = JsonUtils.parseJson(json);

            // then
            assertThat(node.get("id").asLong()).isEqualTo(1L);
            assertThat(node.get("nested").get("value").asText()).isEqualTo("test");
        }

        @Test
        @DisplayName("convert - 객체 타입 변환")
        void convert() {
            // given
            Map<String, Object> source = Map.of(
                    "id", 1,
                    "name", "테스트"
            );

            // when
            TestDto dto = JsonUtils.convert(source, TestDto.class);

            // then
            assertThat(dto.id()).isEqualTo(1L);
            assertThat(dto.name()).isEqualTo("테스트");
        }
    }

    @Nested
    @DisplayName("JSON 유효성 검사")
    class JsonValidation {

        @Test
        @DisplayName("유효한 JSON")
        void validJson() {
            assertThat(JsonUtils.isValidJson("{\"key\":\"value\"}")).isTrue();
            assertThat(JsonUtils.isValidJson("[1,2,3]")).isTrue();
            assertThat(JsonUtils.isValidJson("\"string\"")).isTrue();
            assertThat(JsonUtils.isValidJson("123")).isTrue();
            assertThat(JsonUtils.isValidJson("true")).isTrue();
            assertThat(JsonUtils.isValidJson("null")).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 JSON")
        void invalidJson() {
            assertThat(JsonUtils.isValidJson("invalid")).isFalse();
            assertThat(JsonUtils.isValidJson("{key:value}")).isFalse();  // 따옴표 없음
            assertThat(JsonUtils.isValidJson("{\"key\":}")).isFalse();  // 값 없음
        }

        @Test
        @DisplayName("null 또는 빈 문자열")
        void nullOrEmpty() {
            assertThat(JsonUtils.isValidJson(null)).isFalse();
            assertThat(JsonUtils.isValidJson("")).isFalse();
            assertThat(JsonUtils.isValidJson("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("Java 8 날짜/시간 처리")
    class DateTimeHandling {

        record DateTimeDto(Instant instant, LocalDateTime localDateTime) {}

        @Test
        @DisplayName("Instant ISO 8601 형식으로 직렬화")
        void instantSerialization() {
            // given
            Instant instant = Instant.parse("2025-01-15T10:30:00Z");
            DateTimeDto dto = new DateTimeDto(instant, null);

            // when
            String json = JsonUtils.toJson(dto);

            // then
            assertThat(json).contains("\"instant\":\"2025-01-15T10:30:00Z\"");
            assertThat(json).doesNotContain("1736937000");  // timestamp 형식 아님
        }

        @Test
        @DisplayName("Instant ISO 8601 형식에서 역직렬화")
        void instantDeserialization() {
            // given
            String json = """
                    {"instant":"2025-01-15T10:30:00Z"}
                    """;

            // when
            DateTimeDto dto = JsonUtils.fromJson(json, DateTimeDto.class);

            // then
            assertThat(dto.instant()).isEqualTo(Instant.parse("2025-01-15T10:30:00Z"));
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripConsistency {

        @Test
        @DisplayName("Object → JSON → Object 일관성")
        void roundTrip() {
            // given
            TestDto original = new TestDto(1L, "테스트", Instant.parse("2025-01-15T10:30:00Z"));

            // when
            String json = JsonUtils.toJson(original);
            TestDto restored = JsonUtils.fromJson(json, TestDto.class);

            // then
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("중첩 객체 양방향 변환")
        void nestedRoundTrip() {
            // given
            NestedDto original = new NestedDto("제목", List.of(
                    new TestDto(1L, "첫번째", null),
                    new TestDto(2L, "두번째", null)
            ));

            // when
            String json = JsonUtils.toJson(original);
            NestedDto restored = JsonUtils.fromJson(json, NestedDto.class);

            // then
            assertThat(restored.title()).isEqualTo(original.title());
            assertThat(restored.items()).hasSize(2);
            assertThat(restored.items().get(0).name()).isEqualTo("첫번째");
        }
    }
}