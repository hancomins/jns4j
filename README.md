# JSN4J - Java JSON Abstraction Library

**JSN4J**는 Java 기반의 경량 JSON 구조 추상화 라이브러리입니다. SLF4J가 로깅 구현을 추상화하듯이, JSN4J는 다양한 JSON 라이브러리(Jackson, Gson, Fastjson2, org.json, JSON5 등)를 하나의 통합된 인터페이스로 다룰 수 있게 해줍니다.

## 목차

1. [튜토리얼과 사용법](#1-튜토리얼과-사용법)
2. [지원하는 JSON 라이브러리 타입과 의존성](#2-지원하는-json-라이브러리-타입과-의존성)
3. [ContainerValues 클래스와 메서드 소개](#3-containervalues-클래스와-메서드-소개)
4. [JsonArrayStringWriter와 JsonObjectStringWriter 특징과 사용법](#4-jsonarraystringwriter와-jsonobjectstringwriter-특징과-사용법)

## 1. 튜토리얼과 사용법

### 1.1 기본 개념

JSN4J는 JSON 데이터를 다루기 위한 4가지 핵심 인터페이스를 제공합니다:

- **`ContainerValue`**: 모든 JSON 값의 최상위 인터페이스
- **`ObjectContainer`**: JSON 객체 (key-value 쌍)
- **`ArrayContainer`**: JSON 배열
- **`PrimitiveValue`**: 원시값 (문자열, 숫자, 불린, null)

### 1.2 시작하기

#### 1.2.0 의존성 추가

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.hancomins</groupId>
    <artifactId>jsn4j</artifactId>
    <version>{version}</version>
</dependency>
```

```gradle
// Gradle
implementation 'io.github.hancomins:jsn4j:{version}'
```


#### 1.2.1 객체 생성 및 조작

```java
import com.hancomins.jsn4j.*;

// JSON 객체 생성
ObjectContainer obj = Jsn4j.newObject();

// 값 추가 - 체이닝 가능
obj.put("name", "JSN4J")
   .put("version", 1.0)
   .put("active", true)
   .put("tags", Arrays.asList("json", "java", "library"));

// 중첩된 객체 생성
obj.put("author", Jsn4j.newObject()
    .put("name", "Hancomins")
    .put("email", "contact@hancomins.com"));

// JSON 문자열로 변환
String json = obj.getWriter().write();
System.out.println(json);
```

#### 1.2.2 배열 생성 및 조작

```java
// JSON 배열 생성
ArrayContainer arr = Jsn4j.newArray();

// 다양한 타입의 값 추가
arr.put("string")
   .put(123)
   .put(45.67)
   .put(true)
   .put(null);

// 객체를 배열에 추가
arr.put(Jsn4j.newObject()
    .put("id", 1)
    .put("name", "Item 1"));

// 인덱스를 지정하여 값 추가/변경
arr.put(0, "replaced");

// JSON 문자열로 변환
String json = arr.getWriter().write();
```

#### 1.2.3 JSON 파싱

```java
// JSON 문자열 파싱
String jsonStr = "{\"name\":\"JSN4J\",\"version\":1.0,\"features\":[\"lightweight\",\"extensible\"]}";
ContainerValue parsed = Jsn4j.parse(jsonStr);

// 타입 확인 및 변환
if (parsed.isObject()) {
    ObjectContainer obj = parsed.asObject();
    String name = obj.getString("name");
    double version = obj.getDouble("version");
    
    // 중첩된 배열 접근
    ArrayContainer features = obj.get("features").asArray();
    for (int i = 0; i < features.size(); i++) {
        String feature = features.getString(i);
        System.out.println("Feature: " + feature);
    }
}

// 파일에서 파싱
try (FileReader reader = new FileReader("data.json")) {
    ContainerValue data = Jsn4j.parse(reader);
    // 데이터 처리...
}
```

### 1.3 고급 기능

#### 1.3.1 타입 안전한 접근

JSN4J는 기본값을 지원하는 타입 안전한 getter 메서드들을 제공합니다:

```java
ObjectContainer obj = Jsn4j.parse(jsonStr).asObject();

// 기본값 없이 접근 (값이 없으면 null 또는 기본 primitive 값 반환)
String name = obj.getString("name");     // null 반환
int count = obj.getInt("count");         // Integer.MIN_VALUE 반환
float price = obj.getFloat("price");     // Float.NaN 반환
double amount = obj.getDouble("amount"); // Double.NaN 반환
boolean active = obj.getBoolean("active"); // false 반환

// 기본값과 함께 접근 (값이 없으면 기본값 반환)
String description = obj.getString("description", "No description");
int priority = obj.getInt("priority", 0);
boolean enabled = obj.getBoolean("enabled", true);

// null 체크와 타입 변환
ContainerValue value = obj.get("someKey");
if (value != null && !value.isNull()) {
    if (value.isPrimitive()) {
        PrimitiveValue primitive = (PrimitiveValue) value;
        String strValue = primitive.asStringOr("default");
    }
}
```

#### 1.3.2 중첩된 구조 생성 헬퍼

```java
ObjectContainer root = Jsn4j.newObject();

// newAndPutObject - 새 객체를 생성하고 추가한 후 반환
ObjectContainer user = root.newAndPutObject("user");
user.put("id", 12345)
    .put("username", "testuser");

// newAndPutArray - 새 배열을 생성하고 추가한 후 반환
ArrayContainer addresses = root.newAndPutArray("addresses");
addresses.put(Jsn4j.newObject()
    .put("type", "home")
    .put("street", "123 Main St")
    .put("city", "Seoul"));

// 더 복잡한 중첩 구조
root.newAndPutObject("settings")
    .put("theme", "dark")
    .newAndPutObject("notifications")
        .put("email", true)
        .put("push", false);
```

#### 1.3.3 Raw 타입 변환

```java
// ObjectContainer를 Java Map으로 변환
ObjectContainer obj = Jsn4j.newObject()
    .put("key1", "value1")
    .put("key2", 123);
Map<String, Object> map = obj.toRawMap();

// ArrayContainer를 Java List로 변환
ArrayContainer arr = Jsn4j.newArray()
    .put("item1")
    .put(123)
    .put(true);
List<Object> list = arr.toRawList();

// Map을 ObjectContainer로 변환
Map<String, Object> javaMap = new HashMap<>();
javaMap.put("name", "test");
javaMap.put("count", 10);
ObjectContainer fromMap = ContainerValues.mapToObjectContainer(Jsn4j.newObject(), javaMap);

// Collection을 ArrayContainer로 변환
List<String> javaList = Arrays.asList("a", "b", "c");
ArrayContainer fromList = ContainerValues.collectionToArrayContainer(Jsn4j.newArray(), javaList);
```

#### 1.3.4 바이트 배열 처리

JSN4J는 바이트 배열을 자동으로 Base64로 인코딩/디코딩합니다:

```java
// 바이트 배열 저장
byte[] imageData = Files.readAllBytes(Paths.get("image.png"));
ObjectContainer obj = Jsn4j.newObject()
    .put("filename", "image.png")
    .put("data", imageData);  // 자동으로 Base64 인코딩됨

// 바이트 배열 읽기
byte[] retrievedData = obj.getByteArray("data");
// 또는 기본값과 함께
byte[] data = obj.getByteArray("data", new byte[0]);
```

### 1.4 에러 처리

```java
// 존재하지 않는 키 접근 시 null 반환
String value = obj.getString("nonexistent"); // null 반환

// 안전한 접근 방법
if (obj.has("key")) {
    String value = obj.getString("key");
}

// 또는 기본값 사용
String value = obj.getString("key", "default");
```

## 2. 지원하는 JSON 라이브러리 타입과 의존성

JSN4J는 플러그인 방식으로 다양한 JSON 라이브러리를 지원합니다. 각 구현체는 별도의 모듈로 제공되며, 필요한 것만 선택하여 사용할 수 있습니다.

### 2.1 Simple (기본 내장)

외부 의존성이 없는 기본 구현체입니다. JSN4J 코어에 포함되어 있어 추가 의존성 없이 바로 사용할 수 있습니다.

사용법:
```java
// 기본적으로 Simple 구현체가 사용됨
ObjectContainer obj = Jsn4j.newObject();

// 명시적으로 지정하려면
ContainerFactory factory = Jsn4j.getContainerFactory(JsonLibrary.SIMPLE);
ObjectContainer obj = factory.newObject();

// Parser 사용
SimpleJsonParser parser = new SimpleJsonParser();
ContainerValue parsed = parser.parse(jsonString);

// Writer 옵션 설정
SimpleJsonWriter writer = (SimpleJsonWriter) obj.getWriter();
writer.enable(SimpleJsonWriteOption.PRETTY_PRINT);
writer.putOption(SimpleJsonWriteOption.INDENT, "    ");
String prettyJson = writer.write();
```

### 2.2 Jackson

가장 널리 사용되는 JSON 라이브러리인 Jackson을 지원합니다.

```xml
<!-- Maven -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

```gradle
// Gradle
implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
```

사용법:
```java
// Jackson 팩토리 등록 (한 번만 수행)
Jsn4j.registerContainerFactory(JacksonContainerFactory.getInstance());

// Jackson을 기본으로 설정
ContainerFactory jacksonFactory = Jsn4j.getContainerFactory(JsonLibrary.JACKSON);
Jsn4j.setDefaultContainerFactory(jacksonFactory);

// 이제 모든 Jsn4j 호출이 Jackson을 사용
ObjectContainer obj = Jsn4j.newObject();

// 또는 명시적으로 Jackson 사용
ContainerFactory jacksonFactory = Jsn4j.getContainerFactory(JsonLibrary.JACKSON);
ObjectContainer obj = jacksonFactory.newObject();

// Jackson 특화 기능 사용
JacksonWriter writer = (JacksonWriter) obj.getWriter();
writer.enable(JacksonWriteOption.PRETTY_PRINT);
writer.enable(JacksonWriteOption.WRITE_DATES_AS_TIMESTAMPS);
```

### 2.3 Fastjson2

알리바바의 고성능 JSON 라이브러리 Fastjson2를 지원합니다.

```xml
<!-- Maven -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.40</version>
</dependency>
```

```gradle
// Gradle
implementation 'com.alibaba:fastjson2:2.0.40'
```

사용법:
```java

// Fastjson2 팩토리 등록
Jsn4j.registerContainerFactory(Fastjson2ContainerFactory.getInstance());

// Fastjson2 사용
ContainerFactory fastjsonFactory = Jsn4j.getContainerFactory(JsonLibrary.FASTJSON2);
ObjectContainer obj = fastjsonFactory.newObject();

// Fastjson2 특화 옵션
Fastjson2Writer writer = (Fastjson2Writer) obj.getWriter();
writer.enable(Fastjson2WriteOption.PRETTY_FORMAT);
writer.enable(Fastjson2WriteOption.WRITE_MAP_NULL_VALUE);
```

### 2.4 org.json

 org.json 라이브러리를 지원합니다.

```xml
<!-- Maven -->
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20231013</version>
</dependency>
```

```gradle
// Gradle
implementation 'org.json:json:20231013'
```

사용법:
```java

// org.json 팩토리 등록
Jsn4j.registerContainerFactory(OrgJsonContainerFactory.getInstance());

// org.json 사용
ContainerFactory orgJsonFactory = Jsn4j.getContainerFactory(JsonLibrary.ORG_JSON);
ObjectContainer obj = orgJsonFactory.newObject();

// org.json Writer 옵션
OrgJsonWriter writer = (OrgJsonWriter) obj.getWriter();
writer.putOption(OrgJsonWriteOption.INDENT_FACTOR, 2);
```

### 2.5 Gson

Google의 인기 있는 JSON 라이브러리인 Gson을 지원합니다.

```xml
<!-- Maven -->
<dependency>
    <groupId>com.google.code</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

```gradle
// Gradle
implementation 'com.google.code.gson:2.10.1'
```

사용법:
```java

// Gson 팩토리 등록
Jsn4j.registerContainerFactory(GsonContainerFactory.getInstance());

// Gson 사용
ContainerFactory gsonFactory = Jsn4j.getContainerFactory(JsonLibrary.GSON);
ObjectContainer obj = gsonFactory.newObject();

// Gson 특화 옵션
GsonWriter writer = (GsonWriter) obj.getWriter();
writer.enable(GsonWriteOption.PRETTY_PRINT);
writer.enable(GsonWriteOption.SERIALIZE_NULLS);
writer.enable(GsonWriteOption.ESCAPE_HTML);
```

### 2.6 JSON5

JSON5 형식(주석, 따옴표 없는 키 등)을 지원합니다.

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.hancomins</groupId>
    <artifactId>json5</artifactId>
    <version>1.1.1</version>
</dependency>
```

```gradle
// Gradle
implementation 'io.github.hancomins:json5:1.1.1'
```

사용법:
```java
// JSON5 팩토리 등록
Jsn4j.registerContainerFactory(Json5ContainerFactory.getInstance());

// JSON5 파싱 (주석, 따옴표 없는 키 지원)
String json5 = """
    {
        // 이것은 주석입니다
        name: "JSN4J",  // 키에 따옴표 없음
        version: 1.0,
        features: [
            'lightweight',
            'extensible',
        ],  // 후행 콤마 허용
    }
    """;

ContainerFactory json5Factory = Jsn4j.getContainerFactory(JsonLibrary.JSON5);
Json5Parser parser = json5Factory.getParser(); // new Json5Parser(); 둘 다 사용 가능.
ContainerValue parsed = parser.parse(json5);

// JSON5 형식으로 출력
Json5Writer writer = (Json5Writer) parsed.getWriter();
```

### 2.7 구현체 선택 가이드

| 구현체 | 장점 | 단점                            | 추천 사용 케이스 |
|--------|------|-------------------------------|------------------|
| **Simple** | • 의존성 없음<br>• 가볍고 빠름<br>• JSN4J 코어에 포함 | • 고급 기능 부족<br>• 대용량 처리 최적화 부족 | • 간단한 애플리케이션<br>• 외부 의존성 최소화가 필요한 경우 |
| **Jackson** | • 가장 많은 기능<br>• 뛰어난 성능<br>• 광범위한 커뮤니티 지원 | • 큰 라이브러리 크기<br>• 복잡한 API     | • 엔터프라이즈 애플리케이션<br>• Spring Boot 프로젝트 |
| **Gson** | • 간단하고 직관적인 API<br>• Google 지원<br>• 좋은 성능 | • Jackson보다 기능 적음<br>• 커스터마이징 제한적 | • Android 애플리케이션<br>• Google 생태계 프로젝트 |
| **Fastjson2** | • 매우 빠른 성능<br>• 간단한 API | • 보안 이슈 히스토리<br>• 문서가 주로 중국어  | • 성능이 중요한 경우<br>• 대용량 데이터 처리 |
| **org.json** | • Android 기본 포함<br>• 간단하고 직관적 | • 성능이 상대적으로 느림<br>• 기능 제한적    | • Android 애플리케이션<br>• 레거시 시스템 |
| **JSON5** | • 사람이 읽기 쉬운 형식<br>• 주석 지원 | • 성능이 상대적으로 느림                | • 설정 파일<br>• 사람이 직접 편집하는 JSON |

### 2.8 런타임 구현체 전환

JSN4J의 강력한 기능 중 하나는 런타임에 JSON 구현체를 전환할 수 있다는 것입니다. 

#### JsonLibrary 열거형 사용

각 구현체는 JsonLibrary 열거형으로 정의되어 있습니다:
- `JsonLibrary.SIMPLE` - SimpleJsonContainerFactory (기본 내장)
- `JsonLibrary.JACKSON` - JacksonContainerFactory
- `JsonLibrary.GSON` - GsonContainerFactory
- `JsonLibrary.FASTJSON2` - Fastjson2ContainerFactory
- `JsonLibrary.ORG_JSON` - OrgJsonContainerFactory
- `JsonLibrary.JSON5` - Json5ContainerFactory

```java
// JsonLibrary 열거형으로 팩토리 가져오기
ContainerFactory simpleFactory = Jsn4j.getContainerFactory(JsonLibrary.SIMPLE);
Jsn4j.setDefaultContainerFactory(simpleFactory);

// 특정 작업에만 다른 구현체 사용
ContainerFactory jacksonFactory = Jsn4j.getContainerFactory(JsonLibrary.JACKSON);
ObjectContainer complexData = jacksonFactory.newObject();
// Jackson의 고급 기능을 활용한 복잡한 데이터 처리...

// 성능이 중요한 부분에서는 Fastjson2 사용
ContainerFactory fastjsonFactory = Jsn4j.getContainerFactory(JsonLibrary.FASTJSON2);
ArrayContainer bigArray = fastjsonFactory.newArray();
// 대용량 배열 처리...

// 사람이 읽기 쉬운 형식이 필요한 경우 JSON5 사용
ContainerFactory json5Factory = Jsn4j.getContainerFactory(JsonLibrary.JSON5);
ObjectContainer config = json5Factory.newObject();
// 주석과 유연한 구문을 지원하는 설정 파일 처리...

// 문자열 이름으로도 접근 가능 (레거시 지원)
ContainerFactory factory = Jsn4j.getContainerFactoryByName("jackson");
```

**주의사항:**
- JsonLibrary 열거형 사용이 권장됩니다 (타입 안전성)
- 문자열 이름으로 접근하는 방식도 여전히 지원됩니다
- 등록되지 않은 팩토리를 요청하면 예외가 발생할 수 있습니다
- 각 팩토리는 싱글톤 패턴으로 구현되어 있어 `getInstance()` 메서드를 통해서도 접근 가능합니다

### 2.9 커스텀 팩토리 등록

자체 JSON 라이브러리나 특별한 요구사항을 위한 커스텀 팩토리를 만들어 등록할 수 있습니다:

```java
// 커스텀 팩토리 구현
public class MyCustomContainerFactory extends ContainerFactory {
    @Override
    public String getName() {
        return "mycustom";  // 팩토리 이름
    }
    
    @Override
    public ObjectContainer newObject() {
        return new MyCustomObjectContainer(this);
    }
    
    @Override
    public ArrayContainer newArray() {
        return new MyCustomArrayContainer(this);
    }
    
    // 다른 필수 메서드들 구현...
}

// 커스텀 팩토리 등록 및 사용
MyCustomContainerFactory customFactory = new MyCustomContainerFactory();
Jsn4j.registerContainerFactory(customFactory);

// 이제 다른 팩토리처럼 사용 가능
ContainerFactory factory = Jsn4j.getContainerFactoryByName("mycustom");
ObjectContainer obj = factory.newObject();
```

## 3. ContainerValues 클래스와 메서드 소개

`ContainerValues`는 JSN4J의 유틸리티 클래스로, 컨테이너를 조작하는 다양한 정적 메서드를 제공합니다.

### 주요 메서드

| 메서드 | 설명 | 반환 타입 |
|--------|------|-----------|
| **equals(a, b)** | 두 ContainerValue의 깊은 동등성 비교 | boolean |
| **copy(target, source)** | source를 target으로 완전 복사 (target 내용 삭제) | void |
| **cloneContainer(source)** | source의 독립적인 복사본 생성 | ContainerValue |
| **merge(target, source)** | source를 target에 병합 (source 우선, target 수정) | void |
| **concat(target, source)** | 새로운 결합 객체 생성 (target 우선, 원본 유지) | ContainerValue |
| **intersection(a, b)** | 두 컨테이너의 교집합 | ContainerValue |
| **diff(a, b)** | a 기준으로 b와의 차이점 | ContainerValue |
| **mapToObjectContainer(factory, map)** | Java Map을 ObjectContainer로 변환 | ObjectContainer |
| **collectionToArrayContainer(factory, collection)** | Java Collection을 ArrayContainer로 변환 | ArrayContainer |

### merge() vs concat() 

두 메서드의 핵심 차이점:

```java
// merge() - target을 직접 수정, source 값이 우선
ObjectContainer target = Jsn4j.newObject().put("a", 1).put("b", 2);
ObjectContainer source = Jsn4j.newObject().put("b", 3).put("c", 4);
ContainerValues.merge(target, source);
// target이 수정됨: {"a": 1, "b": 3, "c": 4}

// concat() - 새 객체 생성, target 값이 우선
ObjectContainer target2 = Jsn4j.newObject().put("a", 1).put("b", 2);
ObjectContainer source2 = Jsn4j.newObject().put("b", 3).put("c", 4);
ContainerValue result = ContainerValues.concat(target2, source2);
// 새 객체 반환: {"a": 1, "b": 2, "c": 4}
// target2와 source2는 변경되지 않음
```

### 간단한 사용 예제

```java
// 깊은 복사
ObjectContainer original = Jsn4j.newObject().put("data", "value");
ContainerValue cloned = ContainerValues.cloneContainer(original);

// 교집합
ObjectContainer obj1 = Jsn4j.newObject().put("a", 1).put("b", 2);
ObjectContainer obj2 = Jsn4j.newObject().put("b", 2).put("c", 3);
ContainerValue common = ContainerValues.intersection(obj1, obj2); // {"b": 2}

// 차이점
ContainerValue diff = ContainerValues.diff(obj1, obj2); // {"a": 1}

// Map 변환
Map<String, Object> map = Map.of("key", "value", "nested", Map.of("inner", 123));
ObjectContainer obj = ContainerValues.mapToObjectContainer(Jsn4j.getInstance(), map);
```

📖 **자세한 설명과 더 많은 예제는 [ContainerValues 문서](docs/ContainerValues.md)를 참고하세요.**

## 4. JsonArrayStringWriter와 JsonObjectStringWriter 특징과 사용법

`JsonArrayStringWriter`와 `JsonObjectStringWriter`는 중간 객체 생성 없이 JSON 문자열을 직접 구축하는 고성능 빌더 클래스입니다. StringBuilder를 기반으로 하며, ThreadLocal 캐싱을 통해 메모리 할당을 최소화합니다.

### 4.1 주요 특징

#### 4.1.1 성능 최적화
- **StringBuilder 재사용**: ThreadLocal 기반 캐싱으로 StringBuilder 인스턴스 재사용
- **직접 문자열 구축**: 중간 객체 생성 없이 JSON 문자열 직접 생성
- **메모리 효율성**: 대용량 JSON 생성 시 메모리 사용량 최소화

#### 4.1.2 Fluent API
- 메서드 체이닝을 통한 직관적인 사용
- 타입별 오버로드된 `put` 메서드 제공
- 중첩 구조 지원

#### 4.1.3 안전성
- 자동 이스케이프 처리
- null 값 처리
- 특수 문자 인코딩
- Base64 바이트 배열 인코딩

### 4.2 JsonObjectStringWriter 사용법

JsonObjectStringWriter는 중간 객체 생성 없이 JSON 문자열을 직접 생성하는 고성능 빌더입니다.

```java
// 기본 사용법
String json = new JsonObjectStringWriter()
    .put("name", "JSN4J")
    .put("version", 1.0)
    .put("stable", true)
    .put("tags", new JsonArrayStringWriter()
        .put("json").put("java").put("library"))
    .put("author", new JsonObjectStringWriter()
        .put("name", "Hancomins")
        .put("email", "contact@hancomins.com"))
    .build();
```

**주요 특징:**
- 메모리 효율적인 직접 문자열 생성
- Fluent API로 가독성 높은 코드
- Map, Collection 자동 변환 지원
- ThreadLocal 기반 StringBuilder 재사용

자세한 사용법과 예제는 [JsonObjectStringWriter 가이드](docs/JsonObjectStringWriter.md)를 참조하세요.

### 4.3 JsonArrayStringWriter 사용법

#### 4.3.1 기본 사용법

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

#### 4.3.2 객체 배열

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

#### 4.3.3 다차원 배열

```java
String matrixJson = new JsonArrayStringWriter()
    .put(new JsonArrayStringWriter().put(1).put(2).put(3))
    .put(new JsonArrayStringWriter().put(4).put(5).put(6))
    .put(new JsonArrayStringWriter().put(7).put(8).put(9))
    .build();

// 결과: [[1,2,3],[4,5,6],[7,8,9]]
```

#### 4.3.4 putAll 메서드

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

### 4.4 고급 기능

#### 4.4.1 StringBuilder 캐싱 관리

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

#### 4.4.2 재사용과 리셋

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

#### 4.4.3 크기와 상태 확인

```java
JsonArrayStringWriter arrayWriter = new JsonArrayStringWriter();

System.out.println("비어있음: " + arrayWriter.isEmpty()); // true
System.out.println("크기: " + arrayWriter.size());        // 0

arrayWriter.put("item1").put("item2").put("item3");

System.out.println("비어있음: " + arrayWriter.isEmpty()); // false
System.out.println("크기: " + arrayWriter.size());        // 3
```

#### 4.4.4 특수 문자 처리

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

#### 4.4.5 ContainerValue와의 통합

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

### 4.5 실제 사용 예제

#### 4.5.1 REST API 응답 생성

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

#### 4.5.2 로그 이벤트 생성

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

#### 4.5.3 대량 데이터 처리

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

#### 4.5.4 동적 JSON 생성

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

### 4.6 성능 고려사항

#### 4.6.1 언제 사용해야 하는가

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

#### 4.6.2 성능 비교

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

#### 4.6.3 대용량 복잡한 데이터 성능 테스트

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

```java
// 성능 테스트 코드 예제
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

### 4.7 주의사항과 베스트 프랙티스

1. **build() 후 재사용 금지**: `build()`를 호출한 후에는 writer를 재사용할 수 없습니다. `reset()`을 호출하거나 새 인스턴스를 생성하세요.

2. **스레드 안전성**: 각 writer 인스턴스는 스레드 안전하지 않습니다. 멀티스레드 환경에서는 각 스레드마다 별도의 인스턴스를 사용하세요.

3. **메모리 관리**: 매우 큰 JSON을 생성할 때는 StringBuilder의 크기 제한을 고려하세요.

4. **예외 처리**: 특수한 숫자 값(NaN, Infinity)은 자동으로 null로 변환됩니다.

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

// 잘못된 사용법
JsonObjectStringWriter writer = new JsonObjectStringWriter();
String json1 = writer.put("key", "value").build();
// writer.put("another", "value"); // IllegalStateException!

// 올바른 재사용
writer.reset();
String json2 = writer.put("another", "value").build();
```

