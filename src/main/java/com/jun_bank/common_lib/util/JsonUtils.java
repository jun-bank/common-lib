package com.jun_bank.common_lib.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JSON 유틸리티
 * Jackson ObjectMapper를 사용한 JSON 직렬화/역직렬화
 *
 * <p>ObjectMapper 설정:</p>
 * <ul>
 *   <li>Java 8 날짜/시간 지원 (JavaTimeModule)</li>
 *   <li>날짜를 ISO 8601 형식으로 출력</li>
 *   <li>null 값 제외</li>
 *   <li>알 수 없는 속성 무시</li>
 * </ul>
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();

        // Java 8 날짜/시간 지원
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        // 날짜를 timestamp가 아닌 ISO 8601 문자열로 출력
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null 값 제외
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 알 수 없는 속성 무시
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 빈 객체 직렬화 허용
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private JsonUtils() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /**
     * 설정된 ObjectMapper 반환
     *
     * @return ObjectMapper 인스턴스
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    // ========================================
    // Object → JSON 변환
    // ========================================

    /**
     * 객체를 JSON 문자열로 변환
     *
     * @param object 변환할 객체
     * @return JSON 문자열
     * @throws JsonConversionException 변환 실패 시
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("객체를 JSON으로 변환 실패: {}", object, e);
            throw new JsonConversionException("JSON 직렬화에 실패했습니다.", e);
        }
    }

    /**
     * 객체를 JSON 문자열로 변환 (실패 시 빈 Optional 반환)
     *
     * @param object 변환할 객체
     * @return Optional<JSON 문자열>
     */
    public static Optional<String> toJsonSafe(Object object) {
        try {
            return Optional.of(OBJECT_MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.warn("객체를 JSON으로 변환 실패: {}", object, e);
            return Optional.empty();
        }
    }

    /**
     * 객체를 Pretty JSON 문자열로 변환 (디버깅용)
     *
     * @param object 변환할 객체
     * @return Pretty JSON 문자열
     */
    public static String toPrettyJson(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("객체를 Pretty JSON으로 변환 실패: {}", object, e);
            throw new JsonConversionException("JSON 직렬화에 실패했습니다.", e);
        }
    }

    /**
     * 객체를 byte 배열로 변환
     *
     * @param object 변환할 객체
     * @return byte 배열
     */
    public static byte[] toBytes(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("객체를 bytes로 변환 실패: {}", object, e);
            throw new JsonConversionException("JSON 직렬화에 실패했습니다.", e);
        }
    }

    // ========================================
    // JSON → Object 변환
    // ========================================

    /**
     * JSON 문자열을 객체로 변환
     *
     * @param json  JSON 문자열
     * @param clazz 대상 클래스
     * @param <T>   대상 타입
     * @return 변환된 객체
     * @throws JsonConversionException 변환 실패 시
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON을 객체로 변환 실패: {}", json, e);
            throw new JsonConversionException("JSON 역직렬화에 실패했습니다.", e);
        }
    }

    /**
     * JSON 문자열을 객체로 변환 (실패 시 빈 Optional 반환)
     *
     * @param json  JSON 문자열
     * @param clazz 대상 클래스
     * @param <T>   대상 타입
     * @return Optional<변환된 객체>
     */
    public static <T> Optional<T> fromJsonSafe(String json, Class<T> clazz) {
        try {
            return Optional.of(OBJECT_MAPPER.readValue(json, clazz));
        } catch (JsonProcessingException e) {
            log.warn("JSON을 객체로 변환 실패: {}", json, e);
            return Optional.empty();
        }
    }

    /**
     * JSON 문자열을 제네릭 타입으로 변환
     * List, Map 등 제네릭 타입 변환 시 사용
     *
     * @param json          JSON 문자열
     * @param typeReference 타입 참조
     * @param <T>           대상 타입
     * @return 변환된 객체
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON을 객체로 변환 실패: {}", json, e);
            throw new JsonConversionException("JSON 역직렬화에 실패했습니다.", e);
        }
    }

    /**
     * byte 배열을 객체로 변환
     *
     * @param bytes byte 배열
     * @param clazz 대상 클래스
     * @param <T>   대상 타입
     * @return 변환된 객체
     */
    public static <T> T fromBytes(byte[] bytes, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (Exception e) {
            log.error("bytes를 객체로 변환 실패", e);
            throw new JsonConversionException("JSON 역직렬화에 실패했습니다.", e);
        }
    }

    // ========================================
    // 편의 메서드
    // ========================================

    /**
     * JSON 문자열을 List로 변환
     *
     * @param json  JSON 문자열
     * @param clazz 요소 클래스
     * @param <T>   요소 타입
     * @return List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.error("JSON을 List로 변환 실패: {}", json, e);
            throw new JsonConversionException("JSON 역직렬화에 실패했습니다.", e);
        }
    }

    /**
     * JSON 문자열을 Map으로 변환
     *
     * @param json JSON 문자열
     * @return Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON을 Map으로 변환 실패: {}", json, e);
            throw new JsonConversionException("JSON 역직렬화에 실패했습니다.", e);
        }
    }

    /**
     * JSON 문자열을 JsonNode로 파싱
     *
     * @param json JSON 문자열
     * @return JsonNode
     */
    public static JsonNode parseJson(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패: {}", json, e);
            throw new JsonConversionException("JSON 파싱에 실패했습니다.", e);
        }
    }

    /**
     * 객체를 다른 타입으로 변환
     * DTO 변환 등에 사용
     *
     * @param source      원본 객체
     * @param targetClass 대상 클래스
     * @param <T>         대상 타입
     * @return 변환된 객체
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        return OBJECT_MAPPER.convertValue(source, targetClass);
    }

    /**
     * JSON 유효성 검사
     *
     * @param json JSON 문자열
     * @return 유효한 JSON이면 true
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    // ========================================
    // 예외 클래스
    // ========================================

    /**
     * JSON 변환 실패 예외
     */
    public static class JsonConversionException extends RuntimeException {

        public JsonConversionException(String message) {
            super(message);
        }

        public JsonConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}