# Jun-Bank Common Library

Jun-Bank MSA 프로젝트의 공통 라이브러리입니다.

모든 비즈니스 서비스에서 공유하는 API 응답, 예외 처리, 이벤트, 유틸리티 클래스를 제공합니다.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.jun-bank/common-lib.svg)](https://central.sonatype.com/artifact/io.github.jun-bank/common-lib)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

---

## 📦 설치

### Gradle

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.jun-bank:common-lib:0.0.1'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.jun-bank</groupId>
    <artifactId>common-lib</artifactId>
    <version>0.0.1</version>
</dependency>
```

---

## 📁 패키지 구조

```
common-lib/
├── .github/workflows/publish.yml   # GitHub Actions 배포
├── README.md                        # 문서
├── build.gradle                     # 빌드 설정
├── settings.gradle                  # 프로젝트 설정
└── src/
    ├── main/java/com/jun_bank/common_lib/
    │   ├── api/
    │   │   ├── ApiResponse.java     # 통합 API 응답
    │   │   ├── PageInfo.java        # 페이징 메타데이터
    │   │   └── PageResponse.java    # 페이징 응답
    │   ├── event/
    │   │   ├── DomainEvent.java     # 도메인 이벤트
    │   │   └── IntegrationEvent.java # Kafka 통합 이벤트
    │   ├── exception/
    │   │   ├── BusinessException.java    # 비즈니스 예외
    │   │   ├── ErrorCode.java            # 에러 코드 인터페이스
    │   │   ├── GlobalErrorCode.java      # 공통 에러 코드
    │   │   └── GlobalExceptionHandler.java # 전역 예외 핸들러
    │   └── util/
    │       ├── JsonUtils.java       # JSON 유틸리티
    │       └── UuidUtils.java       # UUID + 도메인 ID 유틸리티
    └── test/java/com/jun_bank/common_lib/
        ├── api/
        │   ├── ApiResponseTest.java
        │   └── PageResponseTest.java
        ├── event/
        │   ├── DomainEventTest.java
        │   └── IntegrationEventTest.java
        ├── exception/
        │   └── BusinessExceptionTest.java
        └── util/
            ├── JsonUtilsTest.java
            └── UuidUtilsTest.java
```

---

## 🔧 주요 기능

### 1. API 응답 표준화

```java
// 성공 응답
@GetMapping("/users/{id}")
public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
    return ApiResponse.success(userService.findById(id));
}

// 메시지 포함
@PostMapping("/users")
public ApiResponse<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    return ApiResponse.success(userService.create(request), "사용자가 생성되었습니다.");
}

// 데이터 없음
@DeleteMapping("/users/{id}")
public ApiResponse<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ApiResponse.success();
}
```

**응답 형식:**
```json
{
  "success": true,
  "message": "사용자가 생성되었습니다.",
  "data": {
    "id": "USR-a1b2c3d4",
    "email": "user@example.com",
    "name": "홍길동"
  },
  "timestamp": "2025-01-15T10:30:00Z",
  "traceId": "abc123def456"
}
```

### 2. 페이징 응답

```java
@GetMapping("/users")
public ApiResponse<PageResponse<UserResponse>> getUsers(Pageable pageable) {
    Page<User> page = userRepository.findAll(pageable);
    return ApiResponse.success(PageResponse.from(page, UserResponse::from));
}
```

**응답 형식:**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageInfo": {
      "page": 0,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5,
      "first": true,
      "last": false,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

### 3. 예외 처리

**도메인별 에러 코드 정의:**
```java
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", 404),
    DUPLICATE_EMAIL("USER_002", "이미 존재하는 이메일입니다.", 409),
    INVALID_PASSWORD("USER_003", "비밀번호가 올바르지 않습니다.", 400);

    private final String code;
    private final String message;
    private final int status;
}
```

**예외 발생:**
```java
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
}

// 커스텀 메시지
throw new BusinessException(UserErrorCode.USER_NOT_FOUND, 
    String.format("ID가 %d인 사용자를 찾을 수 없습니다.", id));
```

**에러 응답 형식:**
```json
{
  "success": false,
  "error": {
    "code": "USER_001",
    "message": "사용자를 찾을 수 없습니다.",
    "status": 404,
    "path": "/api/v1/users/999"
  },
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### 4. 이벤트 기반 통신

**멱등성, 순서 보장, 재시도, TTL** 기본 지원

**도메인 이벤트 정의:**
```java
@Getter
public class UserCreatedEvent extends DomainEvent {
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
}
```

**Kafka로 이벤트 발행:**
```java
IntegrationEvent event = IntegrationEvent.from(domainEvent, "user-service", traceId);
kafkaTemplate.send("user-events", event.getPartitionKey(), event);
```

**TTL 포함 이벤트:**
```java
IntegrationEvent tempEvent = IntegrationEvent.createWithTtl(
    "VerificationCodeSent", "auth-server", payload, 300  // 5분 만료
);
```

**Consumer에서 처리:**
```java
@KafkaListener(topics = "user-events")
public void handle(IntegrationEvent event) {
    // 만료 체크
    if (event.isExpired()) {
        log.warn("만료된 이벤트 무시: {}", event.getEventId());
        return;
    }
    
    // 중복 체크 (멱등성)
    if (processedEventRepository.existsByEventId(event.getEventId())) {
        return;
    }
    
    // 처리
    processEvent(event);
    
    // 실패 시 재시도
    if (shouldRetry && event.getRetryCount() < 3) {
        kafkaTemplate.send("user-events", event.retry());
    }
}
```

**Kafka 메시지 형식:**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "UserCreatedEvent",
  "occurredAt": "2025-01-15T10:30:00Z",
  "sourceService": "user-service",
  "traceId": "abc123def456",
  "partitionKey": "1",
  "sequenceNumber": 1,
  "retryCount": 0,
  "expiresAt": null,
  "payload": { ... }
}
```

### 5. 도메인 ID 생성

```java
// 기본 ID (8자리)
String userId = UuidUtils.generateUserId();       // USR-a1b2c3d4
String accountId = UuidUtils.generateAccountId(); // ACC-e5f6g7h8
String cardId = UuidUtils.generateCardId();       // CRD-i9j0k1l2

// 타임스탬프 포함 ID (정렬 가능)
String txnId = UuidUtils.generateTransactionId(); // TXN-20250115143052-a1b2c3
String trfId = UuidUtils.generateTransferId();    // TRF-20250115143052-d4e5f6
String ledgerId = UuidUtils.generateLedgerId();   // LDG-20250115143052-g7h8i9

// 커스텀 도메인 ID
String customId = UuidUtils.generateDomainId("ORD");        // ORD-a1b2c3d4
String longId = UuidUtils.generateDomainId("ORD", 12);      // ORD-a1b2c3d4e5f6
String timestampId = UuidUtils.generateTimestampId("ORD");  // ORD-20250115143052-a1b2c3

// 유효성 검사
UuidUtils.isValidDomainId("USR-a1b2c3d4", "USR");  // true
UuidUtils.extractPrefix("USR-a1b2c3d4");           // "USR"
```

### 6. JSON 유틸리티

```java
// 직렬화
String json = JsonUtils.toJson(object);
byte[] bytes = JsonUtils.toBytes(object);

// 역직렬화
User user = JsonUtils.fromJson(json, User.class);
List<User> users = JsonUtils.fromJsonToList(json, User.class);
Map<String, Object> map = JsonUtils.fromJsonToMap(json);

// 유효성 검사
boolean valid = JsonUtils.isValidJson(json);

// 타입 변환
UserDto dto = JsonUtils.convert(entity, UserDto.class);
```

---

## 📋 GlobalErrorCode

| 코드 | 메시지 | HTTP |
|------|--------|------|
| **서버 에러** |||
| GLOBAL_001 | 서버 내부 오류가 발생했습니다. | 500 |
| GLOBAL_002 | 서비스를 일시적으로 사용할 수 없습니다. | 503 |
| GLOBAL_003 | 데이터베이스 오류가 발생했습니다. | 500 |
| **클라이언트 에러** |||
| GLOBAL_100 | 잘못된 요청입니다. | 400 |
| GLOBAL_101 | 유효하지 않은 입력값입니다. | 400 |
| GLOBAL_104 | JSON 형식이 올바르지 않습니다. | 400 |
| **인증/인가** |||
| GLOBAL_200 | 인증이 필요합니다. | 401 |
| GLOBAL_201 | 유효하지 않은 토큰입니다. | 401 |
| GLOBAL_210 | 접근 권한이 없습니다. | 403 |
| **리소스** |||
| GLOBAL_300 | 요청한 리소스를 찾을 수 없습니다. | 404 |
| GLOBAL_310 | 리소스 충돌이 발생했습니다. | 409 |
| **서비스 통신** |||
| GLOBAL_500 | 서비스 간 통신 오류가 발생했습니다. | 503 |
| GLOBAL_501 | 서비스가 일시적으로 차단되었습니다. | 503 |

---

## 🏗️ 의존성

| 라이브러리 | 용도 |
|-----------|------|
| spring-boot-starter-validation | Bean Validation |
| spring-boot-starter-data-jpa | Page, Pageable |
| spring-web, spring-webmvc | Web MVC |
| jackson-databind, jackson-datatype-jsr310 | JSON 직렬화 |
| spring-kafka | Kafka 이벤트 |

---

## 🧪 테스트

```bash
./gradlew test
```

| 클래스 | 테스트 수 |
|--------|----------|
| ApiResponseTest | 12 |
| PageResponseTest | 10 |
| BusinessExceptionTest | 12 |
| DomainEventTest | 8 |
| IntegrationEventTest | 17 |
| UuidUtilsTest | 37 |
| JsonUtilsTest | 17 |
| **Total** | **113** |

---

## 📝 사용 서비스

| 서비스 | 설명 |
|--------|------|
| user-service | 사용자 관리 |
| auth-server | 인증/인가 |
| account-service | 계좌 관리 |
| transaction-service | 입출금 처리 |
| transfer-service | 이체 처리 |
| card-service | 카드 관리 |
| ledger-service | 원장 기록 |

---

## 🚀 배포

### 자동 배포 (GitHub Actions)

main 브랜치에 push 시 자동으로 Maven Central에 배포됩니다.

### 수동 배포

```bash
./gradlew publishToMavenCentralPortal
```

### 배포 확인

- [Maven Central에서 검색](https://central.sonatype.com/search?q=io.github.jun-bank)
- [아티팩트 직접 링크](https://central.sonatype.com/artifact/io.github.jun-bank/common-lib)

---

## 🔐 Maven Central 배포 설정

이 라이브러리를 Fork하여 본인의 Maven Central에 배포하려면 [Maven Central 배포 가이드](./MAVEN_CENTRAL_PUBLISH_GUIDE.md)를 참고하세요.

### 필요한 GitHub Secrets

| Secret | 설명 |
|--------|------|
| `OSSRH_USERNAME` | Sonatype 토큰 Username |
| `OSSRH_PASSWORD` | Sonatype 토큰 Password |
| `GPG_KEY_ID` | GPG 키 ID (8자리) |
| `GPG_PASSPHRASE` | GPG 비밀번호 |
| `GPG_PRIVATE_KEY` | GPG 비밀키 (armor 형식) |
| `MAVEN_CENTRAL_TOKEN` | Base64 인코딩된 `username:password` |

---

## 📌 버전

| 버전 | Spring Boot | Java | 배포일 |
|------|-------------|------|--------|
| 0.0.1 | 4.0.0 | 21 | 2025-11-30 |

---

## 📄 라이선스

이 프로젝트는 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 라이선스를 따릅니다.