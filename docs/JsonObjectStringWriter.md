# JsonObjectStringWriter 상세 가이드

## 목차

1. [개요](#1-개요)
2. [주요 특징](#2-주요-특징)
3. [기본 사용법](#3-기본-사용법)
4. [고급 기능](#4-고급-기능)
5. [성능 고려사항](#5-성능-고려사항)
6. [실제 사용 예제](#6-실제-사용-예제)
7. [주의사항과 베스트 프랙티스](#7-주의사항과-베스트-프랙티스)

## 1. 개요

JsonObjectStringWriter와 JsonArrayStringWriter는 JSN4J에서 제공하는 고성능 JSON 문자열 빌더입니다. 중간 객체 생성 없이 JSON 문자열을 직접 생성하여 메모리 효율성과 성능을 극대화합니다.

## 2. 주요 특징

### 2.1 성능 최적화
- **StringBuilder 재사용**: ThreadLocal 기반 캐싱으로 StringBuilder 인스턴스 재사용
- **직접 문자열 구축**: 중간 객체 생성 없이 JSON 문자열 직접 생성
- **메모리 효율성**: 대용량 JSON 생성 시 메모리 사용량 최소화

### 2.2 Fluent API
- 메서드 체이닝을 통한 직관적인 사용
- 타입별 오버로드된 `put` 메서드 제공
- 중첩 구조 지원

### 2.3 안전성
- 자동 이스케이프 처리
- null 값 처리
- 특수 문자 인코딩
- Base64 바이트 배열 인코딩

## 3. 기본 사용법

### 3.1 JsonObjectStringWriter

#### 3.1.1 기본 사용법

```java
// 간단한 JSON 객체 생성
String json = new JsonObjectStringWriter()
    .put("name", "JSN4J")
    .put("version", 1.0)
    .put("stable", true)
    .put("downloads", 10000)
    .putNull("deprecated")
    .build();

// 결과: {"name":"JSN4J","version":1.0,"stable":true,"downloads":10000,"deprecated":null}
```

#### 3.1.2 중첩된 객체

```java
String userJson = new JsonObjectStringWriter()
    .put("id", 12345)
    .put("username", "johndoe")
    .put("profile", new JsonObjectStringWriter()
        .put("firstName", "John")
        .put("lastName", "Doe")
        .put("age", 30)
        .put("email", "john.doe@example.com"))
    .put("address", new JsonObjectStringWriter()
        .put("street", "123 Main St")
        .put("city", "Seoul")
        .put("country", "Korea")
        .put("postalCode", "12345"))
    .put("verified", true)
    .build();
```

#### 3.1.3 배열 포함

```java
String productJson = new JsonObjectStringWriter()
    .put("id", "PROD-001")
    .put("name", "노트북")
    .put("price", 1500000)
    .put("tags", new JsonArrayStringWriter()
        .put("전자제품")
        .put("컴퓨터")
        .put("노트북"))
    .put("specs", new JsonObjectStringWriter()
        .put("cpu", "Intel i7")
        .put("ram", "16GB")
        .put("storage", new JsonArrayStringWriter()
            .put(new JsonObjectStringWriter()
                .put("type", "SSD")
                .put("capacity", "512GB"))
            .put(new JsonObjectStringWriter()
                .put("type", "HDD")
                .put("capacity", "1TB"))))
    .build();
```

#### 3.1.4 Map과 Collection 처리

```java
// Map 직접 추가
Map<String, Object> config = new HashMap<>();
config.put("timeout", 30);
config.put("retries", 3);
config.put("debug", false);

// Collection 직접 추가
List<String> features = Arrays.asList("fast", "reliable", "scalable");

String appJson = new JsonObjectStringWriter()
    .put("name", "MyApp")
    .put("config", config)      // Map이 자동으로 JSON 객체로 변환
    .put("features", features)  // List가 자동으로 JSON 배열로 변환
    .build();
```

#### 3.1.5 putAll 메서드

```java
// 여러 속성을 한 번에 추가
Map<String, Object> attributes = new HashMap<>();
attributes.put("color", "blue");
attributes.put("size", "large");
attributes.put("weight", 2.5);

String itemJson = new JsonObjectStringWriter()
    .put("id", "ITEM-123")
    .put("name", "Product")
    .putAll(attributes)  // Map의 모든 항목 추가
    .build();
```

### 3.2 JsonArrayStringWriter

#### 3.2.1 기본 사용법

```java
// 다양한 타입의 배열
String mixedArray = new JsonArrayStringWriter()
    .put("text")
    .put(123)
    .put(45.67)
    .put(true)
    .putNull()
    .put(new byte[]{1, 2, 3})  // Base64로 인코딩됨
    .build();

// 결과: ["text",123,45.67,true,null,"AQID"]
```

#### 3.2.2 객체 배열

```java
String usersJson = new JsonArrayStringWriter()
    .put(new JsonObjectStringWriter()
        .put("id", 1)
        .put("name", "Alice")
        .put("role", "admin"))
    .put(new JsonObjectStringWriter()
        .put("id", 2)
        .put("name", "Bob")
        .put("role", "user"))
    .put(new JsonObjectStringWriter()
        .put("id", 3)
        .put("name", "Charlie")
        .put("role", "guest"))
    .build();
```

#### 3.2.3 다차원 배열

```java
String matrixJson = new JsonArrayStringWriter()
    .put(new JsonArrayStringWriter().put(1).put(2).put(3))
    .put(new JsonArrayStringWriter().put(4).put(5).put(6))
    .put(new JsonArrayStringWriter().put(7).put(8).put(9))
    .build();

// 결과: [[1,2,3],[4,5,6],[7,8,9]]
```

#### 3.2.4 putAll 메서드

```java
// Collection 추가
List<Integer> numbers = Arrays.asList(10, 20, 30, 40, 50);

// 가변 인자로 추가
String arrayJson = new JsonArrayStringWriter()
    .put("start")
    .putAll(numbers)           // Collection의 모든 요소 추가
    .putAll("a", "b", "c")    // 가변 인자로 여러 요소 추가
    .put("end")
    .build();

// 결과: ["start",10,20,30,40,50,"a","b","c","end"]
```

## 4. 고급 기능

### 4.1 StringBuilder 캐싱 관리

```java
// 캐시 상태 확인
System.out.println("캐시 활성화: " + StringBuilderCache.isCacheEnabled());
System.out.println("현재 캐시 크기: " + StringBuilderCache.getCacheSize());

// 캐시 설정 변경
StringBuilderCache.setMaxCacheSize(64);      // 최대 캐시 크기
StringBuilderCache.setMaxBuilderSize(2 * 1024 * 1024); // 최대 StringBuilder 크기 (2MB)

// 캐시 비활성화
StringBuilderCache.setCacheEnabled(false);

// 특정 스레드의 캐시 클리어
AbstractJsonStringWriter.clearCache();
```

### 4.2 재사용과 리셋

```java
JsonObjectStringWriter writer = new JsonObjectStringWriter();

// 첫 번째 JSON 생성
writer.put("message", "First").put("count", 1);
String first = writer.build();

// 같은 writer 재사용 - 에러 발생 (이미 build() 호출됨)
// writer.put("message", "Second"); // IllegalStateException

// reset()을 사용하여 재사용
writer.reset();
writer.put("message", "Second").put("count", 2);
String second = writer.build();
```

### 4.3 크기와 상태 확인

```java
JsonArrayStringWriter arrayWriter = new JsonArrayStringWriter();

System.out.println("비어있음: " + arrayWriter.isEmpty()); // true
System.out.println("크기: " + arrayWriter.size());        // 0

arrayWriter.put("item1").put("item2").put("item3");

System.out.println("비어있음: " + arrayWriter.isEmpty()); // false
System.out.println("크기: " + arrayWriter.size());        // 3
```

### 4.4 특수 문자 처리

```java
// 자동 이스케이프 처리
String jsonWithSpecialChars = new JsonObjectStringWriter()
    .put("quote", "She said \"Hello\"")
    .put("backslash", "C:\\Users\\test")
    .put("newline", "Line 1\nLine 2")
    .put("tab", "Col1\tCol2")
    .put("unicode", "한글 テスト 🎉")
    .put("control", "\b\f\r")
    .build();

// 모든 특수 문자가 올바르게 이스케이프됨
// {"quote":"She said \"Hello\"","backslash":"C:\\Users\\test",...}
```

### 4.5 ContainerValue와의 통합

```java
// 기존 ContainerValue 객체와 함께 사용
ObjectContainer existingObj = Jsn4j.newObject()
    .put("existing", true)
    .put("data", Arrays.asList(1, 2, 3));

ArrayContainer existingArr = Jsn4j.newArray()
    .put("a").put("b").put("c");

// JsonStringWriter에서 직접 사용
String combined = new JsonObjectStringWriter()
    .put("new", "value")
    .put("imported", existingObj)    // ContainerValue 직접 추가
    .put("array", existingArr)       // ArrayContainer 직접 추가
    .put("primitive", existingObj.get("existing")) // PrimitiveValue
    .build();
```

## 5. 성능 고려사항

### 5.1 언제 사용해야 하는가

**JsonStringWriter를 사용해야 할 때:**
- 대량의 JSON 데이터를 생성할 때
- 메모리 사용량이 중요할 때
- JSON 구조가 미리 알려져 있을 때
- 스트리밍 방식으로 JSON을 생성해야 할 때

**일반 Container를 사용해야 할 때:**
- JSON 구조를 동적으로 수정해야 할 때
- 생성된 데이터를 다시 읽어야 할 때
- 복잡한 조건부 로직이 필요할 때
- 다른 JSN4J 기능과 통합해야 할 때

### 5.2 성능 비교

실제 성능 테스트 결과 (100,000개 JSON 객체 생성, 5회 평균):

| 구현 방식 | 실행 시간 (ms) | 상대 성능 | 설명 |
|-----------|-----------------|-----------|------|
| JsonStringWriter (캐시 미사용) | 57 | 1.28x | StringBuilder 캐싱 없이 직접 생성 |
| JsonStringWriter (캐시 사용) | 73 | 1.00x | ThreadLocal 캐싱 사용 (기준) |
| Simple Container | 204 | 0.36x | 중간 객체 생성 방식 |

*테스트 환경: Java 21, 복잡한 중첩 구조 포함*

**주요 발견사항:**
- JsonStringWriter는 Simple Container 대비 약 **64% 빠른 성능**을 보여줍니다
- 단순한 JSON 구조에서는 캐싱의 이점이 크지 않을 수 있습니다

### 5.3 대용량 복잡한 데이터 성능 테스트

100명의 사용자 정보를 포함한 복잡한 JSON 구조 생성 테스트 (각 사용자당 약 13KB, 총 1.3MB):

| 구현 방식 | 실행 시간 (ms) | 메모리 사용량 (MB) | 상대 성능 | 개선율 |
|-----------|-----------------|---------------------|-----------|--------|
| JsonStringWriter | 7,002 | 89 | 1.00x | - |
| Simple Container | 8,262 | 113 | 0.85x | 15.25% 느림 |

*테스트 환경: 1,000회 반복, 5회 평균*

**복잡한 사용자 객체 구조:**
- 프로필 정보 (이름, 나이, 성별, 자기소개 등)
- 설정 정보 (테마, 언어, 알림 설정, 프라이버시 설정)
- 3개의 주소 정보 (각각 좌표 포함)
- 10개의 주문 내역 (각 주문당 1-5개 아이템)
- 20개의 활동 로그
- 15개의 태그
- 30명의 친구 목록
- 통계 정보

**대용량 테스트 주요 발견사항:**
- 대용량 복잡한 JSON에서도 JsonStringWriter가 **15.25% 더 빠른 성능** 유지
- 메모리 사용량은 **21.24% 감소**
- 1.3MB 크기의 JSON 문서를 생성하는데도 안정적인 성능 보여줌
- 중첩 깊이가 깊고 배열 요소가 많은 경우에도 성능 이점 유지

## 6. 실제 사용 예제

### 6.1 REST API 응답 생성

```java
public String createApiResponse(int statusCode, Object data, String message) {
    return new JsonObjectStringWriter()
        .put("status", statusCode)
        .put("success", statusCode >= 200 && statusCode < 300)
        .put("message", message)
        .put("data", data)
        .put("timestamp", System.currentTimeMillis())
        .put("version", "1.0")
        .build();
}

// 사용 예
String response = createApiResponse(200, 
    new JsonArrayStringWriter()
        .put(new JsonObjectStringWriter()
            .put("id", 1)
            .put("name", "Item 1"))
        .put(new JsonObjectStringWriter()
            .put("id", 2)
            .put("name", "Item 2")),
    "Success");
```

### 6.2 로그 이벤트 생성

```java
public String createLogEvent(String level, String message, 
                           Map<String, Object> context, 
                           Exception error) {
    JsonObjectStringWriter writer = new JsonObjectStringWriter()
        .put("@timestamp", Instant.now().toString())
        .put("level", level)
        .put("message", message)
        .put("thread", Thread.currentThread().getName());
    
    if (context != null && !context.isEmpty()) {
        writer.put("context", context);
    }
    
    if (error != null) {
        writer.put("error", new JsonObjectStringWriter()
            .put("type", error.getClass().getName())
            .put("message", error.getMessage())
            .put("stackTrace", Arrays.toString(error.getStackTrace())));
    }
    
    return writer.build();
}
```

### 6.3 대량 데이터 처리

```java
public String exportLargeDataset(List<Record> records) {
    JsonArrayStringWriter arrayWriter = new JsonArrayStringWriter();
    
    for (Record record : records) {
        // 각 레코드를 직접 JSON으로 변환
        arrayWriter.put(new JsonObjectStringWriter()
            .put("id", record.getId())
            .put("timestamp", record.getTimestamp())
            .put("values", record.getValues())
            .put("metadata", new JsonObjectStringWriter()
                .put("source", record.getSource())
                .put("version", record.getVersion())
                .put("processed", record.isProcessed())));
    }
    
    return new JsonObjectStringWriter()
        .put("exportDate", LocalDateTime.now().toString())
        .put("recordCount", records.size())
        .put("records", arrayWriter)
        .build();
}
```

### 6.4 동적 JSON 생성

```java
// 조건부 필드 추가
public String createUserProfile(User user, boolean includePrivate) {
    JsonObjectStringWriter writer = new JsonObjectStringWriter()
        .put("id", user.getId())
        .put("username", user.getUsername())
        .put("displayName", user.getDisplayName())
        .put("joinDate", user.getJoinDate());
    
    // 조건부로 민감한 정보 포함
    if (includePrivate) {
        writer.put("email", user.getEmail())
              .put("phone", user.getPhone())
              .put("address", new JsonObjectStringWriter()
                  .put("street", user.getStreet())
                  .put("city", user.getCity())
                  .put("postalCode", user.getPostalCode()));
    }
    
    // 선택적 필드 처리
    if (user.getBio() != null) {
        writer.put("bio", user.getBio());
    }
    
    if (user.getWebsite() != null) {
        writer.put("website", user.getWebsite());
    }
    
    // 소셜 링크가 있는 경우만 추가
    if (!user.getSocialLinks().isEmpty()) {
        JsonObjectStringWriter social = new JsonObjectStringWriter();
        user.getSocialLinks().forEach(social::put);
        writer.put("social", social);
    }
    
    return writer.build();
}

// 사용 예
User user = getUserById(123);
String publicProfile = createUserProfile(user, false);  // 공개 정보만
String fullProfile = createUserProfile(user, true);     // 전체 정보
```

### 6.5 성능 테스트 코드

```java
@Test
public void performanceTest() {
    int iterations = 100000;
    
    // JsonStringWriter 테스트
    long start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
        String json = new JsonObjectStringWriter()
            .put("id", i)
            .put("name", "User " + i)
            .put("active", i % 2 == 0)
            .put("score", i * 1.5)
            .put("tags", new JsonArrayStringWriter()
                .put("tag1").put("tag2").put("tag3"))
            .put("profile", new JsonObjectStringWriter()
                .put("age", 25 + (i % 40))
                .put("city", "Seoul"))
            .build();
    }
    long jsonWriterTime = System.nanoTime() - start;
    
    // Container 테스트
    start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
        ObjectContainer obj = Jsn4j.newObject()
            .put("id", i)
            .put("name", "User " + i)
            .put("active", i % 2 == 0)
            .put("score", i * 1.5);
        
        ArrayContainer tags = Jsn4j.newArray()
            .put("tag1").put("tag2").put("tag3");
        obj.put("tags", tags);
        
        ObjectContainer profile = Jsn4j.newObject()
            .put("age", 25 + (i % 40))
            .put("city", "Seoul");
        obj.put("profile", profile);
        
        String json = obj.getWriter().write();
    }
    long containerTime = System.nanoTime() - start;
    
    System.out.printf("JsonStringWriter: %dms%n", jsonWriterTime / 1_000_000);
    System.out.printf("Container: %dms%n", containerTime / 1_000_000);
    System.out.printf("성능 향상: %.2f%%%n", 
        (containerTime - jsonWriterTime) * 100.0 / containerTime);
}
```

## 7. 주의사항과 베스트 프랙티스

### 7.1 build() 후 재사용 금지

`build()`를 호출한 후에는 writer를 재사용할 수 없습니다. `reset()`을 호출하거나 새 인스턴스를 생성하세요.

```java
// 잘못된 사용법
JsonObjectStringWriter writer = new JsonObjectStringWriter();
String json1 = writer.put("key", "value").build();
// writer.put("another", "value"); // IllegalStateException!

// 올바른 재사용
writer.reset();
String json2 = writer.put("another", "value").build();
```

### 7.2 스레드 안전성

각 writer 인스턴스는 스레드 안전하지 않습니다. 멀티스레드 환경에서는 각 스레드마다 별도의 인스턴스를 사용하세요.

### 7.3 메모리 관리

매우 큰 JSON을 생성할 때는 StringBuilder의 크기 제한을 고려하세요.

```java
// 대용량 처리 시 캐시 설정 조정
StringBuilderCache.setMaxBuilderSize(10 * 1024 * 1024); // 10MB
```

### 7.4 예외 처리

특수한 숫자 값(NaN, Infinity)은 자동으로 null로 변환됩니다.

```java
// 올바른 사용법
try {
    String json = new JsonObjectStringWriter()
        .put("value", Double.NaN)      // null로 변환됨
        .put("infinite", Double.POSITIVE_INFINITY) // null로 변환됨
        .build();
} catch (IllegalStateException e) {
    // writer가 이미 닫혀있는 경우
}
```

### 7.5 디버깅 팁

```java
// toString()을 사용하여 현재 상태 확인 (build() 전에만)
JsonObjectStringWriter writer = new JsonObjectStringWriter()
    .put("debug", true);
System.out.println(writer.toString()); // 현재까지의 JSON 문자열
```

### 7.6 메모리 누수 방지

```java
// 장시간 실행되는 애플리케이션에서는 주기적으로 캐시 정리
@Scheduled(fixedDelay = 3600000) // 1시간마다
public void cleanupCache() {
    AbstractJsonStringWriter.clearCache();
}
```