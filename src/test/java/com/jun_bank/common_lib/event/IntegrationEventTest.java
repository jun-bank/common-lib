package com.jun_bank.common_lib.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IntegrationEvent 테스트")
class IntegrationEventTest {

    // 테스트용 도메인 이벤트
    static class UserCreatedEvent extends DomainEvent {
        private final Long userId;
        private final String email;

        public UserCreatedEvent(Long userId, String email) {
            super();
            this.userId = userId;
            this.email = email;
        }

        @Override
        public String getAggregateId() {
            return userId.toString();
        }

        @Override
        public String getAggregateType() {
            return "User";
        }

        public Long getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 - from()")
    class FromMethod {

        @Test
        @DisplayName("기본 from() 메서드")
        void basicFrom() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(1L, "test@example.com");

            // when
            IntegrationEvent event = IntegrationEvent.from(domainEvent, "user-service");

            // then
            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getEventType()).isEqualTo("UserCreatedEvent");
            assertThat(event.getSourceService()).isEqualTo("user-service");
            assertThat(event.getPayload()).isEqualTo(domainEvent);
            assertThat(event.getAggregateId()).isEqualTo("1");
            assertThat(event.getAggregateType()).isEqualTo("User");
            assertThat(event.getPartitionKey()).isEqualTo("1");  // aggregateId로 자동 설정
            assertThat(event.getRetryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("traceId 포함 from() 메서드")
        void fromWithTraceId() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(1L, "test@example.com");
            String traceId = "abc123def456";

            // when
            IntegrationEvent event = IntegrationEvent.from(domainEvent, "user-service", traceId);

            // then
            assertThat(event.getTraceId()).isEqualTo(traceId);
            assertThat(event.getPartitionKey()).isEqualTo("1");
        }

        @Test
        @DisplayName("커스텀 파티션 키 from() 메서드")
        void fromWithCustomPartitionKey() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(1L, "test@example.com");
            String customPartitionKey = "region-kr";

            // when
            IntegrationEvent event = IntegrationEvent.from(
                    domainEvent, "user-service", "traceId", customPartitionKey);

            // then
            assertThat(event.getPartitionKey()).isEqualTo(customPartitionKey);
        }

        @Test
        @DisplayName("전체 옵션 from() 메서드")
        void fromWithAllOptions() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(1L, "test@example.com");
            Map<String, String> metadata = Map.of("key1", "value1");
            Instant expiresAt = Instant.now().plusSeconds(3600);

            // when
            IntegrationEvent event = IntegrationEvent.from(
                    domainEvent,
                    "user-service",
                    "traceId",
                    "spanId",
                    metadata,
                    100L,
                    expiresAt
            );

            // then
            assertThat(event.getSpanId()).isEqualTo("spanId");
            assertThat(event.getMetadata()).containsEntry("key1", "value1");
            assertThat(event.getSequenceNumber()).isEqualTo(100L);
            assertThat(event.getExpiresAt()).isEqualTo(expiresAt);
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 - create()")
    class CreateMethod {

        @Test
        @DisplayName("기본 create() 메서드")
        void basicCreate() {
            // given
            Map<String, Object> payload = Map.of("key", "value");

            // when
            IntegrationEvent event = IntegrationEvent.create(
                    "CustomEvent", "custom-service", payload);

            // then
            assertThat(event.getEventType()).isEqualTo("CustomEvent");
            assertThat(event.getSourceService()).isEqualTo("custom-service");
            assertThat(event.getPayload()).isEqualTo(payload);
            assertThat(event.getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("TTL 포함 createWithTtl() 메서드")
        void createWithTtl() {
            // given
            Map<String, Object> payload = Map.of("code", "123456");
            Instant before = Instant.now();

            // when
            IntegrationEvent event = IntegrationEvent.createWithTtl(
                    "VerificationCode", "auth-service", payload, 300);  // 5분

            // then
            assertThat(event.getExpiresAt()).isNotNull();
            assertThat(event.getExpiresAt()).isAfter(before.plusSeconds(299));
            assertThat(event.getExpiresAt()).isBefore(before.plusSeconds(301));
        }
    }

    @Nested
    @DisplayName("만료 기능")
    class ExpirationFeature {

        @Test
        @DisplayName("만료되지 않은 이벤트")
        void notExpired() {
            // given
            IntegrationEvent event = IntegrationEvent.createWithTtl(
                    "TestEvent", "test-service", "payload", 3600);

            // when & then
            assertThat(event.isExpired()).isFalse();
        }

        @Test
        @DisplayName("만료된 이벤트")
        void expired() {
            // given
            IntegrationEvent event = IntegrationEvent.builder()
                    .eventId("test-id")
                    .eventType("TestEvent")
                    .occurredAt(Instant.now())
                    .sourceService("test-service")
                    .expiresAt(Instant.now().minusSeconds(1))  // 1초 전 만료
                    .build();

            // when & then
            assertThat(event.isExpired()).isTrue();
        }

        @Test
        @DisplayName("만료 시간 없으면 만료되지 않음")
        void noExpirationTime() {
            // given
            IntegrationEvent event = IntegrationEvent.create(
                    "TestEvent", "test-service", "payload");

            // when & then
            assertThat(event.getExpiresAt()).isNull();
            assertThat(event.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("재시도 기능")
    class RetryFeature {

        @Test
        @DisplayName("retry() 메서드로 retryCount 증가")
        void retryIncrementsCount() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(1L, "test@example.com");
            IntegrationEvent original = IntegrationEvent.from(domainEvent, "user-service");

            // when
            IntegrationEvent retried = original.retry();

            // then
            assertThat(original.getRetryCount()).isEqualTo(0);
            assertThat(retried.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("retry()는 동일한 eventId 유지")
        void retryKeepsSameEventId() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(1L, "test@example.com");
            IntegrationEvent original = IntegrationEvent.from(domainEvent, "user-service");

            // when
            IntegrationEvent retried = original.retry();

            // then
            assertThat(retried.getEventId()).isEqualTo(original.getEventId());
        }

        @Test
        @DisplayName("여러 번 재시도")
        void multipleRetries() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(1L, "test@example.com");
            IntegrationEvent original = IntegrationEvent.from(domainEvent, "user-service");

            // when
            IntegrationEvent retry1 = original.retry();
            IntegrationEvent retry2 = retry1.retry();
            IntegrationEvent retry3 = retry2.retry();

            // then
            assertThat(retry1.getRetryCount()).isEqualTo(1);
            assertThat(retry2.getRetryCount()).isEqualTo(2);
            assertThat(retry3.getRetryCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("retry()는 모든 필드 유지")
        void retryKeepsAllFields() {
            // given
            IntegrationEvent original = IntegrationEvent.builder()
                    .eventId("test-id")
                    .eventType("TestEvent")
                    .occurredAt(Instant.now())
                    .sourceService("test-service")
                    .traceId("trace-123")
                    .spanId("span-456")
                    .version(2)
                    .payload("test payload")
                    .aggregateId("agg-1")
                    .aggregateType("TestAggregate")
                    .partitionKey("partition-1")
                    .sequenceNumber(10L)
                    .retryCount(0)
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            // when
            IntegrationEvent retried = original.retry();

            // then
            assertThat(retried.getEventId()).isEqualTo(original.getEventId());
            assertThat(retried.getEventType()).isEqualTo(original.getEventType());
            assertThat(retried.getOccurredAt()).isEqualTo(original.getOccurredAt());
            assertThat(retried.getSourceService()).isEqualTo(original.getSourceService());
            assertThat(retried.getTraceId()).isEqualTo(original.getTraceId());
            assertThat(retried.getSpanId()).isEqualTo(original.getSpanId());
            assertThat(retried.getVersion()).isEqualTo(original.getVersion());
            assertThat(retried.getPayload()).isEqualTo(original.getPayload());
            assertThat(retried.getAggregateId()).isEqualTo(original.getAggregateId());
            assertThat(retried.getAggregateType()).isEqualTo(original.getAggregateType());
            assertThat(retried.getPartitionKey()).isEqualTo(original.getPartitionKey());
            assertThat(retried.getSequenceNumber()).isEqualTo(original.getSequenceNumber());
            assertThat(retried.getExpiresAt()).isEqualTo(original.getExpiresAt());
            // retryCount만 다름
            assertThat(retried.getRetryCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("순서 보장")
    class OrderingFeature {

        @Test
        @DisplayName("partitionKey 기본값은 aggregateId")
        void defaultPartitionKeyIsAggregateId() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(123L, "test@example.com");

            // when
            IntegrationEvent event = IntegrationEvent.from(domainEvent, "user-service");

            // then
            assertThat(event.getPartitionKey()).isEqualTo("123");
            assertThat(event.getPartitionKey()).isEqualTo(event.getAggregateId());
        }

        @Test
        @DisplayName("sequenceNumber 설정")
        void sequenceNumberSetting() {
            // given
            UserCreatedEvent domainEvent = new UserCreatedEvent(1L, "test@example.com");

            // when
            IntegrationEvent event = IntegrationEvent.from(
                    domainEvent, "user-service", "traceId", "spanId",
                    null, 42L, null);

            // then
            assertThat(event.getSequenceNumber()).isEqualTo(42L);
        }
    }
}