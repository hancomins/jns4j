# JSN4J 기능 및 구조 분석

## 개요
JSN4J는 Java 기반의 경량 JSON 구조 추상화 라이브러리로, 다양한 JSON 라이브러리(Jackson, Gson, YAML 등)를 통합할 수 있는 공통 인터페이스를 제공합니다.

## 핵심 인터페이스 구조

### 1. ContainerValue (최상위 인터페이스)
- **역할**: 모든 JSON 값(Object, Array, Primitive, Null)의 공통 인터페이스
- **주요 메서드**:
  - `ValueType getValueType()`: 값의 타입 반환
  - `Object raw()`: 원시 값 반환
  - `boolean isNull()`, `isObject()`, `isArray()`, `isPrimitive()`: 타입 체크
  - `ObjectContainer asObject()`, `ArrayContainer asArray()`: 타입 캐스팅

### 2. ObjectContainer (JSON 객체)
- **구조**: Map<String, ContainerValue> 기반
- **주요 기능**:
  - 다양한 타입의 put 메서드 (오버로딩)
  - 타입별 getter 메서드 (`getString`, `getInt`, `getBoolean` 등)
  - 기본값 반환 메서드 (`getString(key, defaultValue)`)
  - 중첩 구조 생성: `newAndPutObject(key)`, `newAndPutArray(key)`
  - Map 변환: `toRawMap()`
  - 깊은 복사: `putCopy(key, source)`

### 3. ArrayContainer (JSON 배열)
- **구조**: List<ContainerValue> 기반
- **주요 기능**:
  - 인덱스 기반 접근 및 수정
  - append 방식의 put 메서드
  - 타입별 getter 메서드 (인덱스 기반)
  - 중첩 구조 생성: `newAndPutObject()`, `newAndPutArray()`
  - List 변환: `toRawList()`, `toList()`

### 4. PrimitiveValue (원시 값)
- **지원 타입**: String, Number, Boolean, byte[]
- **타입 변환 메서드**:
  - `asInt()`, `asLong()`, `asFloat()`, `asDouble()`
  - `asBoolean()`, `asString()`, `asByteArray()`
  - 기본값 반환: `asIntOr(defaultValue)` 등
- **특징**: 자동 타입 변환 지원 (예: String "123" → int 123)

## 팩토리 시스템

### ContainerFactory 인터페이스
```java
public interface ContainerFactory {
    String getJsn4jModuleName();
    ObjectContainer newObject();
    ArrayContainer newArray();
    PrimitiveValue newPrimitive(Object value);
    ContainerParser getParser();
}
```

### SimpleJsonContainerFactory (기본 구현체)
- **특징**: 외부 의존성 없는 순수 Java 구현
- **패턴**: Singleton 패턴 적용
- **모듈명**: "simple"

## 파서/라이터 시스템

### ContainerParser
- **입력 지원**: String, Reader, InputStream
- **구현체**: SimpleJsonParser
  - JsonTokenizer 사용하여 토큰 기반 파싱
  - 재귀적 파싱 구조
  - 이스케이프 문자 처리

### ContainerWriter
- **옵션 시스템**: Enum 기반 옵션 설정
- **구현체**: SimpleJsonWriter
  - Pretty Print 지원
  - 이스케이프 처리
  - OutputStream 직접 출력

## 유틸리티 기능 (ContainerValues)

### 1. 비교 및 조작
- `equals(a, b)`: 깊은 비교
- `copy(target, source)`: 깊은 복사
- `merge(target, source)`: 병합 (기존 값 덮어쓰기)

### 2. 집합 연산
- `intersection(a, b)`: 교집합
- `diff(a, b)`: 차집합 (a - b)

### 3. 변환 기능
- `mapToObjectContainer()`: Map → ObjectContainer
- `collectionToArrayContainer()`: Collection → ArrayContainer
- 중첩 구조 자동 변환 지원

## 레지스트리 시스템

### Jsn4j (정적 팩토리 매니저)
- **팩토리 관리**:
  - 이름 기반 레지스트리: `ConcurrentHashMap<String, ContainerFactory>`
  - 클래스 기반 레지스트리: `ConcurrentHashMap<Class, ContainerFactory>`
- **기본 팩토리 설정**: 시스템 프로퍼티 `jsn4j.container.factory`
- **정적 편의 메서드**: `newObject()`, `newArray()`, `parse()`

### Jsn4jThreadContext (스레드별 컨텍스트)
- **ThreadLocal 기반**: 스레드별 독립적인 팩토리 설정
- **용도**: 동일 스레드 내에서 일관된 팩토리 사용
- **기능**:
  - 팩토리 설정/해제
  - 기본 루트 값 설정
  - 스레드별 파싱/생성

## 설계 패턴

1. **Abstract Factory Pattern**
   - ContainerFactory로 관련 객체군(Object, Array, Primitive) 생성

2. **Factory Method Pattern**
   - 각 팩토리 구현체별 생성 메서드 제공

3. **Singleton Pattern**
   - SimpleJsonContainerFactory의 getInstance()

4. **Builder Pattern**
   - 메서드 체이닝을 통한 유창한 인터페이스
   - 예: `obj.put("key", value).put("key2", value2)`

5. **Strategy Pattern**
   - Parser/Writer 교체 가능한 구조

6. **Registry Pattern**
   - 팩토리 등록 및 조회 시스템

## 사용 예시

### 기본 사용
```java
// 객체 생성
ObjectContainer obj = Jsn4j.newObject();
obj.put("name", "John")
   .put("age", 30)
   .put("active", true);

// 중첩 구조
ObjectContainer address = obj.newAndPutObject("address");
address.put("city", "Seoul")
       .put("zipcode", "12345");

// 배열
ArrayContainer hobbies = obj.newAndPutArray("hobbies");
hobbies.put("reading").put("gaming");

// JSON 출력
obj.getWriter().enable("PRETTY_PRINT");
String json = obj.toString();
```

### 파싱
```java
// 문자열 파싱
String json = "{\"name\":\"John\",\"age\":30}";
ObjectContainer parsed = Jsn4j.parse(json).asObject();

// 값 접근
String name = parsed.getString("name");
int age = parsed.getInt("age", 0);  // 기본값 지원
```

### 스레드별 팩토리
```java
// 스레드별 팩토리 설정
Jsn4jThreadContext.setContainerFactory("custom");
ObjectContainer obj = Jsn4jThreadContext.newObject();

// 사용 후 정리
Jsn4jThreadContext.clearContainerFactory();
```

## 확장 방법

### 커스텀 팩토리 구현
1. ContainerFactory 인터페이스 구현
2. ObjectContainer, ArrayContainer 구현체 작성
3. ContainerParser 구현
4. Jsn4j.registerContainerFactory()로 등록

### 예시
```java
public class JacksonContainerFactory implements ContainerFactory {
    @Override
    public String getJsn4jModuleName() {
        return "jackson";
    }
    
    @Override
    public ObjectContainer newObject() {
        return new JacksonObjectContainer();
    }
    // ... 기타 메서드 구현
}
```

## 주요 특징

1. **확장성**: 새로운 JSON 라이브러리 쉽게 통합
2. **타입 안전성**: 제네릭과 타입별 메서드 제공
3. **편의성**: 풍부한 유틸리티 메서드와 기본값 지원
4. **독립성**: 외부 의존성 없는 기본 구현체 내장
5. **유연성**: 스레드별 팩토리 설정 가능
6. **성능**: ConcurrentHashMap 사용으로 멀티스레드 환경 지원

## 향후 확장 가능성
- JSON5 파서 추가
- Jackson 기반 구현체
- YAML 파서 연동
- XML 변환 지원