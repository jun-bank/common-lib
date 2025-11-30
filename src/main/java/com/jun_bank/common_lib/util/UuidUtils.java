package com.jun_bank.common_lib.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * UUID 유틸리티
 * UUID 생성 및 검증
 */
public final class UuidUtils {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private UuidUtils() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /**
     * 새 UUID 생성 (표준 36자)
     *
     * @return UUID 문자열 (예: "550e8400-e29b-41d4-a716-446655440000")
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }

    /**
     * 하이픈 없는 UUID 생성 (32자)
     *
     * @return UUID 문자열 (예: "550e8400e29b41d4a716446655440000")
     */
    public static String generateCompact() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * UUID 유효성 검사
     *
     * @param uuid 검사할 UUID 문자열
     * @return 유효한 UUID면 true
     */
    public static boolean isValid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid).matches();
    }

    /**
     * UUID 유효성 검사 (하이픈 없는 형식 포함)
     *
     * @param uuid 검사할 UUID 문자열
     * @return 유효한 UUID면 true
     */
    public static boolean isValidAnyFormat(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }

        // 표준 형식 검사
        if (UUID_PATTERN.matcher(uuid).matches()) {
            return true;
        }

        // 하이픈 없는 형식 검사 (32자 hex)
        if (uuid.length() == 32 && uuid.matches("^[0-9a-fA-F]{32}$")) {
            return true;
        }

        return false;
    }

    /**
     * 문자열을 UUID로 파싱
     *
     * @param uuid UUID 문자열
     * @return UUID 객체
     * @throws IllegalArgumentException 유효하지 않은 UUID인 경우
     */
    public static UUID parse(String uuid) {
        if (!isValid(uuid)) {
            throw new IllegalArgumentException("유효하지 않은 UUID 형식입니다: " + uuid);
        }
        return UUID.fromString(uuid);
    }

    /**
     * 하이픈 없는 UUID를 표준 형식으로 변환
     *
     * @param compactUuid 하이픈 없는 UUID (32자)
     * @return 표준 UUID 문자열 (36자)
     * @throws IllegalArgumentException 유효하지 않은 형식인 경우
     */
    public static String toStandardFormat(String compactUuid) {
        if (compactUuid == null || compactUuid.length() != 32) {
            throw new IllegalArgumentException("유효하지 않은 compact UUID 형식입니다: " + compactUuid);
        }

        return String.format("%s-%s-%s-%s-%s",
                compactUuid.substring(0, 8),
                compactUuid.substring(8, 12),
                compactUuid.substring(12, 16),
                compactUuid.substring(16, 20),
                compactUuid.substring(20, 32)
        );
    }

    /**
     * 표준 UUID를 하이픈 없는 형식으로 변환
     *
     * @param uuid 표준 UUID 문자열 (36자)
     * @return 하이픈 없는 UUID (32자)
     */
    public static String toCompactFormat(String uuid) {
        if (!isValid(uuid)) {
            throw new IllegalArgumentException("유효하지 않은 UUID 형식입니다: " + uuid);
        }
        return uuid.replace("-", "");
    }

    // ========================================
    // 도메인별 ID 생성
    // ========================================

    /**
     * 도메인 식별자가 포함된 ID 생성
     * 형식: {prefix}-{uuid 8자리}
     * 예: USR-a1b2c3d4, ACC-e5f6g7h8
     *
     * @param prefix 도메인 식별자 (예: USR, ACC, TXN)
     * @return 도메인 ID (예: USR-a1b2c3d4)
     */
    public static String generateDomainId(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix는 필수입니다.");
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return prefix.toUpperCase() + "-" + uuid.substring(0, 8);
    }

    /**
     * 도메인 식별자가 포함된 ID 생성 (길이 지정)
     * 형식: {prefix}-{uuid n자리}
     *
     * @param prefix 도메인 식별자
     * @param length UUID 부분 길이 (4-32)
     * @return 도메인 ID
     */
    public static String generateDomainId(String prefix, int length) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix는 필수입니다.");
        }
        if (length < 4 || length > 32) {
            throw new IllegalArgumentException("length는 4-32 사이여야 합니다.");
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return prefix.toUpperCase() + "-" + uuid.substring(0, length);
    }

    /**
     * 타임스탬프 기반 도메인 ID 생성
     * 형식: {prefix}-{yyyyMMddHHmmss}-{uuid 6자리}
     * 정렬 가능하고 생성 시간 파악 용이
     *
     * @param prefix 도메인 식별자
     * @return 타임스탬프 포함 도메인 ID (예: TXN-20250115143052-a1b2c3)
     */
    public static String generateTimestampId(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix는 필수입니다.");
        }
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return prefix.toUpperCase() + "-" + timestamp + "-" + uuid;
    }

    /**
     * 도메인 ID에서 prefix 추출
     *
     * @param domainId 도메인 ID (예: USR-a1b2c3d4)
     * @return prefix (예: USR)
     */
    public static String extractPrefix(String domainId) {
        if (domainId == null || !domainId.contains("-")) {
            throw new IllegalArgumentException("유효하지 않은 도메인 ID 형식입니다: " + domainId);
        }
        return domainId.substring(0, domainId.indexOf("-"));
    }

    /**
     * 도메인 ID 유효성 검사
     * 형식: {PREFIX}-{hex문자열}
     *
     * @param domainId 검사할 도메인 ID
     * @param expectedPrefix 예상되는 prefix (null이면 prefix 검사 안함)
     * @return 유효하면 true
     */
    public static boolean isValidDomainId(String domainId, String expectedPrefix) {
        if (domainId == null || domainId.isBlank()) {
            return false;
        }

        if (!domainId.contains("-")) {
            return false;
        }

        String[] parts = domainId.split("-", 2);
        if (parts.length != 2) {
            return false;
        }

        String prefix = parts[0];
        String idPart = parts[1];

        // prefix 검사
        if (expectedPrefix != null && !prefix.equalsIgnoreCase(expectedPrefix)) {
            return false;
        }

        // prefix는 영문 대문자만
        if (!prefix.matches("^[A-Z]+$")) {
            return false;
        }

        // idPart는 hex 문자열 (타임스탬프 포함 가능)
        // 단순 hex: a1b2c3d4
        // 타임스탬프 포함: 20250115143052-a1b2c3
        return idPart.matches("^[0-9a-fA-F]+$") ||
                idPart.matches("^\\d{14}-[0-9a-fA-F]+$");
    }

    // ========================================
    // 도메인별 상수 (편의용)
    // ========================================

    /** 사용자 ID prefix */
    public static final String PREFIX_USER = "USR";
    /** 계좌 ID prefix */
    public static final String PREFIX_ACCOUNT = "ACC";
    /** 거래 ID prefix */
    public static final String PREFIX_TRANSACTION = "TXN";
    /** 이체 ID prefix */
    public static final String PREFIX_TRANSFER = "TRF";
    /** 카드 ID prefix */
    public static final String PREFIX_CARD = "CRD";
    /** 원장 ID prefix */
    public static final String PREFIX_LEDGER = "LDG";
    /** 이벤트 ID prefix */
    public static final String PREFIX_EVENT = "EVT";

    /**
     * 사용자 ID 생성
     * @return USR-xxxxxxxx
     */
    public static String generateUserId() {
        return generateDomainId(PREFIX_USER);
    }

    /**
     * 계좌 ID 생성
     * @return ACC-xxxxxxxx
     */
    public static String generateAccountId() {
        return generateDomainId(PREFIX_ACCOUNT);
    }

    /**
     * 거래 ID 생성 (타임스탬프 포함, 정렬 용이)
     * @return TXN-yyyyMMddHHmmss-xxxxxx
     */
    public static String generateTransactionId() {
        return generateTimestampId(PREFIX_TRANSACTION);
    }

    /**
     * 이체 ID 생성 (타임스탬프 포함, 정렬 용이)
     * @return TRF-yyyyMMddHHmmss-xxxxxx
     */
    public static String generateTransferId() {
        return generateTimestampId(PREFIX_TRANSFER);
    }

    /**
     * 카드 ID 생성
     * @return CRD-xxxxxxxx
     */
    public static String generateCardId() {
        return generateDomainId(PREFIX_CARD);
    }

    /**
     * 원장 ID 생성 (타임스탬프 포함)
     * @return LDG-yyyyMMddHHmmss-xxxxxx
     */
    public static String generateLedgerId() {
        return generateTimestampId(PREFIX_LEDGER);
    }

    /**
     * 이벤트 ID 생성
     * @return EVT-xxxxxxxx
     */
    public static String generateEventId() {
        return generateDomainId(PREFIX_EVENT);
    }
}