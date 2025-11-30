package com.jun_bank.common_lib.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 서비스 간 통합 이벤트
 * Kafka를 통해 서비스 간 전달되는 이벤트 래퍼
 *
 * <p>DomainEvent를 감싸서 추가 메타데이터(트레이싱, 소스 서비스 등)를 포함</p>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * // 이벤트 생성
 * UserCreatedEvent domainEvent = new UserCreatedEvent(userId, email, name);
 * IntegrationEvent integrationEvent = IntegrationEvent.from(domainEvent, "user-service", traceId);
 *
 * // Kafka로 발행
 * kafkaTemplate.send("user-events", integrationEvent);
 * }</pre>
 *
 * <p>Kafka 메시지 형식:</p>
 * <pre>{@code
 * {
 *   "eventId": "550e8400-e29b-41d4-a716-446655440000",
 *   "eventType": "UserCreatedEvent",
 *   "occurredAt": "2025-01-15T10:30:00Z",
 *   "sourceService": "user-service",
 *   "traceId": "abc123def456",
 *   "spanId": "span789",
 *   "payload": {
 *     "userId": 1,
 *     "email": "user@example.com",
 *     "name": "홍길동"
 *   }
 * }
 * }</pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 통합 이벤트 고유 ID
     */
    private final String eventId;

    /**
     * 이벤트 타입 (예: UserCreatedEvent)
     */
    private final String eventType;

    /**
     * 이벤트 발생 시간
     */
    private final Instant occurredAt;

    /**
     * 이벤트 발행 서비스
     */
    private final String sourceService;

    /**
     * 분산 추적 ID
     */
    private final String traceId;

    /**
     * Span ID (선택적)
     */
    private final String spanId;

    /**
     * 이벤트 버전
     */
    private final int version;

    /**
     * 이벤트 페이로드 (실제 이벤트 데이터)
     */
    private final Object payload;

    /**
     * 추가 메타데이터 (선택적)
     */
    private final Map<String, String> metadata;

    /**
     * Aggregate ID (선택적)
     */
    private final String aggregateId;

    /**
     * Aggregate 타입 (선택적)
     */
    private final String aggregateType;

    // ========================================
    // 멱등성 / 순서 보장 / 재시도 관련
    // ========================================

    /**
     * 파티션 키 (Kafka 순서 보장용)
     * 동일 키를 가진 이벤트는 동일 파티션으로 전송되어 순서 보장
     */
    private final String partitionKey;

    /**
     * 시퀀스 번호 (동일 Aggregate 내 이벤트 순서)
     */
    private final Long sequenceNumber;

    /**
     * 재시도 횟수
     */
    @Builder.Default
    private final int retryCount = 0;

    /**
     * 이벤트 만료 시간 (TTL)
     * null이면 만료 없음
     */
    private final Instant expiresAt;

    /**
     * 이벤트 만료 여부 확인
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * 재시도 이벤트 생성 (retryCount 증가)
     */
    public IntegrationEvent retry() {
        return IntegrationEvent.builder()
                .eventId(this.eventId)
                .eventType(this.eventType)
                .occurredAt(this.occurredAt)
                .sourceService(this.sourceService)
                .traceId(this.traceId)
                .spanId(this.spanId)
                .version(this.version)
                .payload(this.payload)
                .metadata(this.metadata)
                .aggregateId(this.aggregateId)
                .aggregateType(this.aggregateType)
                .partitionKey(this.partitionKey)
                .sequenceNumber(this.sequenceNumber)
                .retryCount(this.retryCount + 1)
                .expiresAt(this.expiresAt)
                .build();
    }

    /**
     * DomainEvent에서 IntegrationEvent 생성 (기본)
     *
     * @param domainEvent   도메인 이벤트
     * @param sourceService 발행 서비스명
     * @return IntegrationEvent
     */
    public static IntegrationEvent from(DomainEvent domainEvent, String sourceService) {
        return IntegrationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(domainEvent.getEventType())
                .occurredAt(domainEvent.getOccurredAt())
                .sourceService(sourceService)
                .version(domainEvent.getVersion())
                .payload(domainEvent)
                .aggregateId(domainEvent.getAggregateId())
                .aggregateType(domainEvent.getAggregateType())
                .partitionKey(domainEvent.getAggregateId())  // 기본: aggregateId로 순서 보장
                .build();
    }

    /**
     * DomainEvent에서 IntegrationEvent 생성 (트레이싱 정보 포함)
     *
     * @param domainEvent   도메인 이벤트
     * @param sourceService 발행 서비스명
     * @param traceId       분산 추적 ID
     * @return IntegrationEvent
     */
    public static IntegrationEvent from(DomainEvent domainEvent, String sourceService, String traceId) {
        return IntegrationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(domainEvent.getEventType())
                .occurredAt(domainEvent.getOccurredAt())
                .sourceService(sourceService)
                .traceId(traceId)
                .version(domainEvent.getVersion())
                .payload(domainEvent)
                .aggregateId(domainEvent.getAggregateId())
                .aggregateType(domainEvent.getAggregateType())
                .partitionKey(domainEvent.getAggregateId())
                .build();
    }

    /**
     * DomainEvent에서 IntegrationEvent 생성 (전체 옵션)
     *
     * @param domainEvent    도메인 이벤트
     * @param sourceService  발행 서비스명
     * @param traceId        분산 추적 ID
     * @param spanId         Span ID
     * @param metadata       추가 메타데이터
     * @param sequenceNumber 시퀀스 번호
     * @param expiresAt      만료 시간
     * @return IntegrationEvent
     */
    public static IntegrationEvent from(
            DomainEvent domainEvent,
            String sourceService,
            String traceId,
            String spanId,
            Map<String, String> metadata,
            Long sequenceNumber,
            Instant expiresAt) {

        return IntegrationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(domainEvent.getEventType())
                .occurredAt(domainEvent.getOccurredAt())
                .sourceService(sourceService)
                .traceId(traceId)
                .spanId(spanId)
                .version(domainEvent.getVersion())
                .payload(domainEvent)
                .metadata(metadata)
                .aggregateId(domainEvent.getAggregateId())
                .aggregateType(domainEvent.getAggregateType())
                .partitionKey(domainEvent.getAggregateId())
                .sequenceNumber(sequenceNumber)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * DomainEvent에서 IntegrationEvent 생성 (커스텀 파티션 키)
     *
     * @param domainEvent   도메인 이벤트
     * @param sourceService 발행 서비스명
     * @param partitionKey  파티션 키 (순서 보장 기준)
     * @return IntegrationEvent
     */
    public static IntegrationEvent from(DomainEvent domainEvent, String sourceService, String traceId, String partitionKey) {
        return IntegrationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(domainEvent.getEventType())
                .occurredAt(domainEvent.getOccurredAt())
                .sourceService(sourceService)
                .traceId(traceId)
                .version(domainEvent.getVersion())
                .payload(domainEvent)
                .aggregateId(domainEvent.getAggregateId())
                .aggregateType(domainEvent.getAggregateType())
                .partitionKey(partitionKey)
                .build();
    }

    /**
     * 직접 생성 (DomainEvent 없이)
     *
     * @param eventType     이벤트 타입
     * @param sourceService 발행 서비스명
     * @param payload       페이로드
     * @return IntegrationEvent
     */
    public static IntegrationEvent create(String eventType, String sourceService, Object payload) {
        return IntegrationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .sourceService(sourceService)
                .version(1)
                .payload(payload)
                .build();
    }

    /**
     * 직접 생성 (만료 시간 포함)
     *
     * @param eventType     이벤트 타입
     * @param sourceService 발행 서비스명
     * @param payload       페이로드
     * @param ttlSeconds    TTL (초)
     * @return IntegrationEvent
     */
    public static IntegrationEvent createWithTtl(String eventType, String sourceService, Object payload, long ttlSeconds) {
        return IntegrationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .sourceService(sourceService)
                .version(1)
                .payload(payload)
                .expiresAt(Instant.now().plusSeconds(ttlSeconds))
                .build();
    }
}