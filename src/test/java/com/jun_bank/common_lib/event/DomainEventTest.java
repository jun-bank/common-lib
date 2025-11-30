package com.jun_bank.common_lib.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DomainEvent 테스트")
class DomainEventTest {

    // 테스트용 구체 클래스
    static class TestEvent extends DomainEvent {
        private final String data;

        public TestEvent(String data) {
            super();
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    static class TestEventWithAggregate extends DomainEvent {
        private final Long userId;

        public TestEventWithAggregate(Long userId) {
            super();
            this.userId = userId;
        }

        @Override
        public String getAggregateId() {
            return userId.toString();
        }

        @Override
        public String getAggregateType() {
            return "User";
        }
    }

    static class VersionedEvent extends DomainEvent {
        public VersionedEvent(int version) {
            super(version);
        }
    }

    @Nested
    @DisplayName("기본 생성")
    class BasicCreation {

        @Test
        @DisplayName("기본 생성자로 이벤트 생성")
        void defaultConstructor() {
            // when
            TestEvent event = new TestEvent("test data");

            // then
            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getEventId()).matches(
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
            );
            assertThat(event.getEventType()).isEqualTo("TestEvent");
            assertThat(event.getOccurredAt()).isNotNull();
            assertThat(event.getVersion()).isEqualTo(1);
            assertThat(event.getData()).isEqualTo("test data");
        }

        @Test
        @DisplayName("이벤트 ID는 매번 고유함")
        void uniqueEventId() {
            // when
            TestEvent event1 = new TestEvent("data1");
            TestEvent event2 = new TestEvent("data2");

            // then
            assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
        }

        @Test
        @DisplayName("occurredAt이 현재 시간에 가까움")
        void occurredAtIsNow() {
            // given
            Instant before = Instant.now();

            // when
            TestEvent event = new TestEvent("data");

            // then
            Instant after = Instant.now();
            assertThat(event.getOccurredAt()).isBetween(before, after.plusMillis(100));
        }
    }

    @Nested
    @DisplayName("버전 지정")
    class VersionSpecification {

        @Test
        @DisplayName("버전 지정 생성자로 이벤트 생성")
        void versionedConstructor() {
            // when
            VersionedEvent event = new VersionedEvent(2);

            // then
            assertThat(event.getVersion()).isEqualTo(2);
            assertThat(event.getEventType()).isEqualTo("VersionedEvent");
        }
    }

    @Nested
    @DisplayName("Aggregate 정보")
    class AggregateInfo {

        @Test
        @DisplayName("기본 Aggregate 정보는 null")
        void defaultAggregateInfoIsNull() {
            // when
            TestEvent event = new TestEvent("data");

            // then
            assertThat(event.getAggregateId()).isNull();
            assertThat(event.getAggregateType()).isNull();
        }

        @Test
        @DisplayName("오버라이드된 Aggregate 정보")
        void overriddenAggregateInfo() {
            // when
            TestEventWithAggregate event = new TestEventWithAggregate(123L);

            // then
            assertThat(event.getAggregateId()).isEqualTo("123");
            assertThat(event.getAggregateType()).isEqualTo("User");
        }
    }

    @Nested
    @DisplayName("이벤트 타입")
    class EventType {

        @Test
        @DisplayName("이벤트 타입은 클래스명")
        void eventTypeIsClassName() {
            // when
            TestEvent event = new TestEvent("data");

            // then
            assertThat(event.getEventType()).isEqualTo("TestEvent");
        }

        @Test
        @DisplayName("다른 이벤트는 다른 타입")
        void differentEventsHaveDifferentTypes() {
            // when
            TestEvent event1 = new TestEvent("data");
            TestEventWithAggregate event2 = new TestEventWithAggregate(1L);

            // then
            assertThat(event1.getEventType()).isEqualTo("TestEvent");
            assertThat(event2.getEventType()).isEqualTo("TestEventWithAggregate");
        }
    }
}