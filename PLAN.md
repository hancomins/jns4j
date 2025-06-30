# Jackson Wrapper 구현 계획

## 개요
`com.hancomins.jsn4j.jackson` 패키지에 Jackson 라이브러리를 래핑하여 JSN4J의 `ContainerFactory`, `ObjectContainer`, `ArrayContainer` 인터페이스를 구현합니다.

## 의존성 추가
```gradle
dependencies {
    // Jackson 의존성 추가 (compileOnly로 선택적 의존성 처리)
    compileOnly 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    
    // 테스트 시에는 필요
    testImplementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
}
```

## 구현 클래스 구조

### 1. JacksonContainerFactory
```java
package com.hancomins.jsn4j.jackson;

public class JacksonContainerFactory implements ContainerFactory {
    private static final JacksonContainerFactory INSTANCE = new JacksonContainerFactory();
    private final ObjectMapper objectMapper;
    private final JacksonParser parser;
    
    private JacksonContainerFactory() {
        this.objectMapper = new ObjectMapper();
        this.parser = new JacksonParser(objectMapper);
    }
    
    public static JacksonContainerFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getJsn4jModuleName() {
        return "jackson";
    }
}
```

### 2. JacksonObject (ObjectContainer 구현체)

#### 핵심 설계
- **내부 저장소**: `JsonNode` (ObjectNode) 사용
- **지연 변환**: Jackson의 JsonNode를 직접 래핑하여 성능 최적화
- **타입 변환**: Jackson의 자동 타입 변환 기능 활용

#### 주요 구현 사항
```java
public class JacksonObject implements ObjectContainer {
    private final ObjectNode node;
    private final ObjectMapper mapper;
    private JacksonWriter writer;
    
    // 생성자
    public JacksonObject(ObjectMapper mapper) {
        this.mapper = mapper;
        this.node = mapper.createObjectNode();
    }
    
    // 기존 ObjectNode 래핑
    public JacksonObject(ObjectNode node, ObjectMapper mapper) {
        this.node = node;
        this.mapper = mapper;
    }
    
    // ContainerValue 변환 메서드
    private JsonNode toJsonNode(ContainerValue value) {
        if (value == null || value.isNull()) {
            return node.nullNode();
        } else if (value.isPrimitive()) {
            return primitiveToJsonNode((PrimitiveValue) value);
        } else if (value instanceof JacksonObject) {
            return ((JacksonObject) value).node;
        } else if (value instanceof JacksonArray) {
            return ((JacksonArray) value).node;
        } else {
            // 다른 구현체는 raw 값으로 변환
            return mapper.valueToTree(value.raw());
        }
    }
}
```

#### 메서드 구현 전략

1. **put 메서드들**
   - Jackson의 ObjectNode.put() 메서드 활용
   - ContainerValue는 JsonNode로 변환 후 설정
   - 다른 팩토리의 ContainerValue도 지원

2. **get 메서드들**
   - JsonNode의 타입별 접근 메서드 활용
   - null 체크 및 기본값 처리
   - 타입 불일치 시 변환 시도

3. **newAndPut 메서드들**
   - 새로운 JacksonObject/JacksonArray 생성
   - ObjectNode/ArrayNode 생성 후 래핑

4. **순회 기능**
   - Jackson의 Iterator<Map.Entry<String, JsonNode>> 활용
   - ContainerValue로 래핑하여 반환

### 3. JacksonArray (ArrayContainer 구현체)

#### 핵심 설계
- **내부 저장소**: `JsonNode` (ArrayNode) 사용
- **인덱스 관리**: ArrayNode의 size() 및 get() 활용
- **동적 크기 조정**: ensure() 메서드로 구현

#### 주요 구현 사항
```java
public class JacksonArray implements ArrayContainer {
    private final ArrayNode node;
    private final ObjectMapper mapper;
    private JacksonWriter writer;
    
    // put(index, value) 구현 시 고려사항
    @Override
    public ArrayContainer put(int index, Object value) {
        ensureCapacity(index + 1);
        JsonNode jsonNode = valueToJsonNode(value);
        node.set(index, jsonNode);
        return this;
    }
    
    // 크기 보장
    private void ensureCapacity(int minCapacity) {
        while (node.size() < minCapacity) {
            node.addNull();
        }
    }
}
```

### 4. JacksonParser (ContainerParser 구현체)

#### 구현 전략
```java
public class JacksonParser implements ContainerParser {
    private final ObjectMapper mapper;
    
    @Override
    public ContainerValue parse(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            return wrapJsonNode(node);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }
    
    private ContainerValue wrapJsonNode(JsonNode node) {
        if (node.isObject()) {
            return new JacksonObject((ObjectNode) node, mapper);
        } else if (node.isArray()) {
            return new JacksonArray((ArrayNode) node, mapper);
        } else if (node.isNull()) {
            return new PrimitiveValue(null);
        } else {
            // 원시 타입 처리
            return nodeToPrimitive(node);
        }
    }
}
```

### 5. JacksonWriter (ContainerWriter 구현체)

#### 구현 전략
- Jackson의 SerializationFeature 활용
- Pretty Print는 `writerWithDefaultPrettyPrinter()` 사용
- 커스텀 옵션은 ObjectMapper 설정으로 매핑

### 6. 정적 팩토리 메서드 - wrap()

#### 구현 전략
기존 Jackson JsonNode를 JSN4J ContainerValue로 래핑하는 정적 메서드를 각 구현체에 제공합니다.

```java
public class JacksonObject implements ObjectContainer {
    // 정적 팩토리 메서드
    public static JacksonObject wrap(ObjectNode node) {
        return new JacksonObject(node, defaultMapper());
    }
    
    public static JacksonObject wrap(ObjectNode node, ObjectMapper mapper) {
        return new JacksonObject(node, mapper);
    }
}

public class JacksonArray implements ArrayContainer {
    // 정적 팩토리 메서드
    public static JacksonArray wrap(ArrayNode node) {
        return new JacksonArray(node, defaultMapper());
    }
    
    public static JacksonArray wrap(ArrayNode node, ObjectMapper mapper) {
        return new JacksonArray(node, mapper);
    }
}

// 범용 wrap 메서드
public class JacksonContainerFactory {
    // 모든 JsonNode 타입을 적절한 ContainerValue로 변환
    public static ContainerValue wrap(JsonNode node) {
        return wrap(node, new ObjectMapper());
    }
    
    public static ContainerValue wrap(JsonNode node, ObjectMapper mapper) {
        if (node == null || node.isNull()) {
            return new PrimitiveValue(null);
        } else if (node.isObject()) {
            return JacksonObject.wrap((ObjectNode) node, mapper);
        } else if (node.isArray()) {
            return JacksonArray.wrap((ArrayNode) node, mapper);
        } else if (node.isTextual()) {
            return new PrimitiveValue(node.textValue());
        } else if (node.isNumber()) {
            return new PrimitiveValue(node.numberValue());
        } else if (node.isBoolean()) {
            return new PrimitiveValue(node.booleanValue());
        } else if (node.isBinary()) {
            try {
                return new PrimitiveValue(node.binaryValue());
            } catch (IOException e) {
                throw new RuntimeException("Failed to extract binary value", e);
            }
        }
        return new PrimitiveValue(null);
    }
}

## 타입 변환 전략

### PrimitiveValue ↔ JsonNode 변환
```java
private JsonNode primitiveToJsonNode(PrimitiveValue value) {
    Object raw = value.raw();
    if (raw == null) return NullNode.instance;
    if (raw instanceof String) return new TextNode((String) raw);
    if (raw instanceof Integer) return new IntNode((Integer) raw);
    if (raw instanceof Long) return new LongNode((Long) raw);
    if (raw instanceof Double) return new DoubleNode((Double) raw);
    if (raw instanceof Boolean) return BooleanNode.valueOf((Boolean) raw);
    if (raw instanceof byte[]) return new BinaryNode((byte[]) raw);
    // 기타 Number 타입 처리
    return mapper.valueToTree(raw);
}
```

### 다른 팩토리와의 상호 운용성
1. **SimpleObject/SimpleArray → Jackson**
   - `toRawMap()`/`toRawList()` 활용
   - `mapper.valueToTree()` 변환

2. **Jackson → Simple**
   - JsonNode를 일반 Java 객체로 변환
   - `mapper.treeToValue()` 활용

## 특수 기능 구현

### 1. JsonNode 직접 접근
```java
public class JacksonObject implements ObjectContainer {
    // Jackson 특화 메서드
    public ObjectNode getObjectNode() {
        return node;
    }
    
    // JsonNode로 직접 설정
    public void putJsonNode(String key, JsonNode value) {
        node.set(key, value);
    }
}
```

### 2. 고급 Jackson 기능 지원
- TypeReference 지원
- Custom Serializer/Deserializer
- Jackson Annotation 처리

## 테스트 계획

### 1. 기본 기능 테스트
- 모든 ContainerValue 인터페이스 메서드
- 타입 변환 정확성
- null 처리

### 2. Jackson 특화 테스트
- 복잡한 객체 구조 직렬화/역직렬화
- 대용량 JSON 처리 성능
- Jackson 어노테이션 동작

### 3. 상호 운용성 테스트
- Simple ↔ Jackson 변환
- 혼합 사용 시나리오
- 스레드 안전성

## 성능 최적화 방안

1. **지연 초기화**
   - Writer는 필요 시점에 생성
   - ObjectMapper 인스턴스 재사용

2. **직접 변환 최소화**
   - 가능한 Jackson 내부 타입 유지
   - 불필요한 복사 방지

3. **메모리 효율성**
   - JsonNode 재사용
   - 큰 데이터는 스트리밍 처리

## 구현 우선순위

1. **Phase 1**: 핵심 기능
   - JacksonContainerFactory
   - JacksonObject 기본 메서드
   - JacksonArray 기본 메서드
   - JacksonParser

2. **Phase 2**: 완성도
   - 모든 편의 메서드 구현
   - JacksonWriter
   - 타입 변환 최적화

3. **Phase 3**: 고급 기능
   - Jackson 특화 기능
   - 성능 최적화
   - 확장 기능

## 예상 사용 예시

```java
// Jackson 팩토리 등록
Jsn4j.registerContainerFactory(JacksonContainerFactory.getInstance());

// Jackson으로 파싱
ObjectContainer obj = Jsn4j.parse("jackson", jsonString).asObject();

// Jackson 특화 기능 사용
if (obj instanceof JacksonObject) {
    ObjectNode node = ((JacksonObject) obj).getObjectNode();
    // Jackson API 직접 사용
}

// 기존 Jackson JsonNode를 JSN4J로 래핑
ObjectMapper mapper = new ObjectMapper();
JsonNode jacksonNode = mapper.readTree(jsonString);

// wrap 메서드로 변환
ContainerValue wrapped = JacksonContainerFactory.wrap(jacksonNode, mapper);

// 특정 타입으로 래핑
ObjectNode objectNode = mapper.createObjectNode();
objectNode.put("name", "John");
JacksonObject jsn4jObject = JacksonObject.wrap(objectNode, mapper);

// 다른 팩토리와 혼용
ObjectContainer simpleObj = Jsn4j.newObject();
simpleObj.put("data", jsn4jObject); // Jackson 객체를 Simple에 저장
```