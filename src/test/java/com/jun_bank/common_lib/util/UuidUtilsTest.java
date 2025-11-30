package com.jun_bank.common_lib.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UuidUtils 테스트")
class UuidUtilsTest {

    @Nested
    @DisplayName("UUID 생성")
    class Generation {

        @Test
        @DisplayName("표준 UUID 생성 (36자)")
        void generateStandardUuid() {
            // when
            String uuid = UuidUtils.generate();

            // then
            assertThat(uuid).hasSize(36);
            assertThat(uuid).matches(
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
            );
        }

        @Test
        @DisplayName("Compact UUID 생성 (32자, 하이픈 없음)")
        void generateCompactUuid() {
            // when
            String uuid = UuidUtils.generateCompact();

            // then
            assertThat(uuid).hasSize(32);
            assertThat(uuid).matches("^[0-9a-fA-F]{32}$");
            assertThat(uuid).doesNotContain("-");
        }

        @Test
        @DisplayName("생성된 UUID는 매번 고유함")
        void uniqueUuids() {
            // when
            String uuid1 = UuidUtils.generate();
            String uuid2 = UuidUtils.generate();

            // then
            assertThat(uuid1).isNotEqualTo(uuid2);
        }
    }

    @Nested
    @DisplayName("UUID 유효성 검사")
    class Validation {

        @Test
        @DisplayName("유효한 표준 UUID")
        void validStandardUuid() {
            // given
            String uuid = "550e8400-e29b-41d4-a716-446655440000";

            // when & then
            assertThat(UuidUtils.isValid(uuid)).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 UUID - 잘못된 형식")
        void invalidUuidFormat() {
            assertThat(UuidUtils.isValid("invalid-uuid")).isFalse();
            assertThat(UuidUtils.isValid("550e8400e29b41d4a716446655440000")).isFalse();  // 하이픈 없음
            assertThat(UuidUtils.isValid("550e8400-e29b-41d4-a716")).isFalse();  // 너무 짧음
        }

        @Test
        @DisplayName("null 또는 빈 문자열")
        void nullOrEmpty() {
            assertThat(UuidUtils.isValid(null)).isFalse();
            assertThat(UuidUtils.isValid("")).isFalse();
            assertThat(UuidUtils.isValid("   ")).isFalse();
        }

        @Test
        @DisplayName("isValidAnyFormat - 표준 형식")
        void isValidAnyFormatStandard() {
            // given
            String uuid = "550e8400-e29b-41d4-a716-446655440000";

            // when & then
            assertThat(UuidUtils.isValidAnyFormat(uuid)).isTrue();
        }

        @Test
        @DisplayName("isValidAnyFormat - Compact 형식")
        void isValidAnyFormatCompact() {
            // given
            String uuid = "550e8400e29b41d4a716446655440000";

            // when & then
            assertThat(UuidUtils.isValidAnyFormat(uuid)).isTrue();
        }

        @Test
        @DisplayName("isValidAnyFormat - 유효하지 않은 형식")
        void isValidAnyFormatInvalid() {
            assertThat(UuidUtils.isValidAnyFormat("invalid")).isFalse();
            assertThat(UuidUtils.isValidAnyFormat(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("UUID 파싱")
    class Parsing {

        @Test
        @DisplayName("유효한 UUID 파싱")
        void parseValidUuid() {
            // given
            String uuidString = "550e8400-e29b-41d4-a716-446655440000";

            // when
            UUID uuid = UuidUtils.parse(uuidString);

            // then
            assertThat(uuid.toString()).isEqualTo(uuidString);
        }

        @Test
        @DisplayName("유효하지 않은 UUID 파싱 시 예외")
        void parseInvalidUuid() {
            // given
            String invalid = "invalid-uuid";

            // when & then
            assertThatThrownBy(() -> UuidUtils.parse(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 UUID 형식");
        }
    }

    @Nested
    @DisplayName("형식 변환")
    class FormatConversion {

        @Test
        @DisplayName("Compact → Standard 변환")
        void compactToStandard() {
            // given
            String compact = "550e8400e29b41d4a716446655440000";

            // when
            String standard = UuidUtils.toStandardFormat(compact);

            // then
            assertThat(standard).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        }

        @Test
        @DisplayName("Standard → Compact 변환")
        void standardToCompact() {
            // given
            String standard = "550e8400-e29b-41d4-a716-446655440000";

            // when
            String compact = UuidUtils.toCompactFormat(standard);

            // then
            assertThat(compact).isEqualTo("550e8400e29b41d4a716446655440000");
        }

        @Test
        @DisplayName("잘못된 Compact UUID 변환 시 예외")
        void invalidCompactToStandard() {
            // given
            String invalid = "invalid";

            // when & then
            assertThatThrownBy(() -> UuidUtils.toStandardFormat(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 compact UUID 형식");
        }

        @Test
        @DisplayName("잘못된 Standard UUID 변환 시 예외")
        void invalidStandardToCompact() {
            // given
            String invalid = "invalid-uuid";

            // when & then
            assertThatThrownBy(() -> UuidUtils.toCompactFormat(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 UUID 형식");
        }

        @Test
        @DisplayName("양방향 변환 일관성")
        void bidirectionalConversion() {
            // given
            String original = UuidUtils.generate();

            // when
            String compact = UuidUtils.toCompactFormat(original);
            String backToStandard = UuidUtils.toStandardFormat(compact);

            // then
            assertThat(backToStandard).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("도메인 ID 생성")
    class DomainIdGeneration {

        @Test
        @DisplayName("기본 도메인 ID 생성 (8자리)")
        void generateDomainIdDefault() {
            // when
            String id = UuidUtils.generateDomainId("USR");

            // then
            assertThat(id).startsWith("USR-");
            assertThat(id).hasSize(12);  // USR- (4) + 8자리
            assertThat(id.substring(4)).matches("^[0-9a-f]{8}$");
        }

        @Test
        @DisplayName("소문자 prefix도 대문자로 변환")
        void prefixUpperCase() {
            // when
            String id = UuidUtils.generateDomainId("usr");

            // then
            assertThat(id).startsWith("USR-");
        }

        @Test
        @DisplayName("길이 지정 도메인 ID 생성")
        void generateDomainIdWithLength() {
            // when
            String id4 = UuidUtils.generateDomainId("ACC", 4);
            String id12 = UuidUtils.generateDomainId("ACC", 12);
            String id32 = UuidUtils.generateDomainId("ACC", 32);

            // then
            assertThat(id4).hasSize(8);   // ACC- (4) + 4자리
            assertThat(id12).hasSize(16); // ACC- (4) + 12자리
            assertThat(id32).hasSize(36); // ACC- (4) + 32자리
        }

        @Test
        @DisplayName("잘못된 길이 지정 시 예외")
        void invalidLengthThrowsException() {
            assertThatThrownBy(() -> UuidUtils.generateDomainId("USR", 3))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("4-32 사이");

            assertThatThrownBy(() -> UuidUtils.generateDomainId("USR", 33))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("4-32 사이");
        }

        @Test
        @DisplayName("null/빈 prefix 시 예외")
        void nullOrEmptyPrefixThrowsException() {
            assertThatThrownBy(() -> UuidUtils.generateDomainId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("prefix는 필수");

            assertThatThrownBy(() -> UuidUtils.generateDomainId(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("prefix는 필수");

            assertThatThrownBy(() -> UuidUtils.generateDomainId("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("prefix는 필수");
        }

        @Test
        @DisplayName("생성된 ID는 매번 고유함")
        void uniqueDomainIds() {
            // when
            String id1 = UuidUtils.generateDomainId("USR");
            String id2 = UuidUtils.generateDomainId("USR");

            // then
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("타임스탬프 ID 생성")
    class TimestampIdGeneration {

        @Test
        @DisplayName("타임스탬프 포함 ID 생성")
        void generateTimestampId() {
            // when
            String id = UuidUtils.generateTimestampId("TXN");

            // then
            assertThat(id).startsWith("TXN-");
            // TXN-20250115143052-a1b2c3 형식
            assertThat(id).matches("^TXN-\\d{14}-[0-9a-f]{6}$");
            assertThat(id).hasSize(25);  // TXN- (4) + 14자리 타임스탬프 + - + 6자리
        }

        @Test
        @DisplayName("타임스탬프 ID는 시간순 정렬 가능")
        void timestampIdSortable() throws InterruptedException {
            // given
            String id1 = UuidUtils.generateTimestampId("TXN");
            Thread.sleep(10);  // 약간의 시간 차이
            String id2 = UuidUtils.generateTimestampId("TXN");

            // when - 문자열 정렬
            int comparison = id1.compareTo(id2);

            // then - id1이 id2보다 앞에 정렬됨
            assertThat(comparison).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("도메인 ID prefix 추출")
    class PrefixExtraction {

        @Test
        @DisplayName("prefix 추출 성공")
        void extractPrefixSuccess() {
            assertThat(UuidUtils.extractPrefix("USR-a1b2c3d4")).isEqualTo("USR");
            assertThat(UuidUtils.extractPrefix("ACC-12345678")).isEqualTo("ACC");
            assertThat(UuidUtils.extractPrefix("TXN-20250115143052-a1b2c3")).isEqualTo("TXN");
        }

        @Test
        @DisplayName("잘못된 형식은 예외")
        void extractPrefixInvalid() {
            assertThatThrownBy(() -> UuidUtils.extractPrefix(null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> UuidUtils.extractPrefix("invalidformat"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("도메인 ID 유효성 검사")
    class DomainIdValidation {

        @Test
        @DisplayName("유효한 도메인 ID")
        void validDomainId() {
            assertThat(UuidUtils.isValidDomainId("USR-a1b2c3d4", null)).isTrue();
            assertThat(UuidUtils.isValidDomainId("ACC-12345678abcd", null)).isTrue();
            assertThat(UuidUtils.isValidDomainId("TXN-20250115143052-a1b2c3", null)).isTrue();
        }

        @Test
        @DisplayName("예상 prefix 검사")
        void validateExpectedPrefix() {
            assertThat(UuidUtils.isValidDomainId("USR-a1b2c3d4", "USR")).isTrue();
            assertThat(UuidUtils.isValidDomainId("USR-a1b2c3d4", "usr")).isTrue();  // 대소문자 무시
            assertThat(UuidUtils.isValidDomainId("USR-a1b2c3d4", "ACC")).isFalse();
        }

        @Test
        @DisplayName("유효하지 않은 도메인 ID")
        void invalidDomainId() {
            assertThat(UuidUtils.isValidDomainId(null, null)).isFalse();
            assertThat(UuidUtils.isValidDomainId("", null)).isFalse();
            assertThat(UuidUtils.isValidDomainId("invalidformat", null)).isFalse();
            assertThat(UuidUtils.isValidDomainId("usr-a1b2c3d4", null)).isFalse();  // 소문자 prefix
            assertThat(UuidUtils.isValidDomainId("USR-ghijklmn", null)).isFalse();  // 비hex 문자
        }
    }

    @Nested
    @DisplayName("도메인별 편의 메서드")
    class DomainSpecificMethods {

        @Test
        @DisplayName("사용자 ID 생성")
        void generateUserId() {
            String id = UuidUtils.generateUserId();
            assertThat(id).startsWith("USR-");
            assertThat(UuidUtils.isValidDomainId(id, "USR")).isTrue();
        }

        @Test
        @DisplayName("계좌 ID 생성")
        void generateAccountId() {
            String id = UuidUtils.generateAccountId();
            assertThat(id).startsWith("ACC-");
            assertThat(UuidUtils.isValidDomainId(id, "ACC")).isTrue();
        }

        @Test
        @DisplayName("거래 ID 생성 (타임스탬프 포함)")
        void generateTransactionId() {
            String id = UuidUtils.generateTransactionId();
            assertThat(id).startsWith("TXN-");
            assertThat(id).matches("^TXN-\\d{14}-[0-9a-f]{6}$");
        }

        @Test
        @DisplayName("이체 ID 생성 (타임스탬프 포함)")
        void generateTransferId() {
            String id = UuidUtils.generateTransferId();
            assertThat(id).startsWith("TRF-");
            assertThat(id).matches("^TRF-\\d{14}-[0-9a-f]{6}$");
        }

        @Test
        @DisplayName("카드 ID 생성")
        void generateCardId() {
            String id = UuidUtils.generateCardId();
            assertThat(id).startsWith("CRD-");
            assertThat(UuidUtils.isValidDomainId(id, "CRD")).isTrue();
        }

        @Test
        @DisplayName("원장 ID 생성 (타임스탬프 포함)")
        void generateLedgerId() {
            String id = UuidUtils.generateLedgerId();
            assertThat(id).startsWith("LDG-");
            assertThat(id).matches("^LDG-\\d{14}-[0-9a-f]{6}$");
        }

        @Test
        @DisplayName("이벤트 ID 생성")
        void generateEventId() {
            String id = UuidUtils.generateEventId();
            assertThat(id).startsWith("EVT-");
            assertThat(UuidUtils.isValidDomainId(id, "EVT")).isTrue();
        }
    }

    @Nested
    @DisplayName("상수 테스트")
    class ConstantsTest {

        @Test
        @DisplayName("도메인 prefix 상수값 확인")
        void prefixConstants() {
            assertThat(UuidUtils.PREFIX_USER).isEqualTo("USR");
            assertThat(UuidUtils.PREFIX_ACCOUNT).isEqualTo("ACC");
            assertThat(UuidUtils.PREFIX_TRANSACTION).isEqualTo("TXN");
            assertThat(UuidUtils.PREFIX_TRANSFER).isEqualTo("TRF");
            assertThat(UuidUtils.PREFIX_CARD).isEqualTo("CRD");
            assertThat(UuidUtils.PREFIX_LEDGER).isEqualTo("LDG");
            assertThat(UuidUtils.PREFIX_EVENT).isEqualTo("EVT");
        }
    }
}