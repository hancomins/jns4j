# JSN4J

**JSN4J**는 Java 기반의 경량 JSON 구조 추상화 라이브러리입니다.  
JSON, JSON5, YAML 등 다양한 포맷을 하나의 공통 인터페이스로 다룰 수 있도록 설계되었습니다.

## 주요 특징

- `ContainerValue` 추상화: 객체, 배열, 원시값, null 모두 포함
- 통합된 `ObjectContainer`, `ArrayContainer` 인터페이스
- `copy`, `merge`, `intersection`, `diff` 유틸 제공
- Parser / Writer / Factory 구조로 확장 가능
- 외부 의존성 없는 기본 구현체(SimpleJson*) 내장

## 구성

```
com.hancomins.jsn4j
├── ContainerValue.java           // 최상위 인터페이스
├── ObjectContainer.java          // 객체 타입 컨테이너
├── ArrayContainer.java           // 배열 타입 컨테이너
├── PrimitiveValue.java           // 문자열, 숫자 등 원시값
├── ContainerValues.java          // 유틸리티 메서드 모음
├── ContainerFactory.java         // 컨테이너 생성 팩토리
├── ContainerParser.java          // 파서 인터페이스
├── ContainerWriter.java          // writer 인터페이스
├── Jsn4j.java                    // 정적 생성기 및 팩토리 레지스트리
└── simple/
    ├── SimpleObject.java
    ├── SimpleArray.java
    ├── SimpleJsonWriter.java
    ├── SimpleJsonParser.java
    ├── SimpleJsonContainerFactory.java
```

## 사용 예시

```java
ObjectContainer obj = JSN4J.newObject();

obj.put("message","안녕, JSN4J!");
// {"message":"안녕, JSN4J!"}
```

## 의도와 철학
- SLF4J가 로깅 구현을 추상화하듯,
- JSN4J는 JSON 구조를 추상화하여 Jackson, Gson, YAML 등을 통합할 수 있도록 합니다.
- 경량이며 의존성 없이도 사용 가능합니다.

## 향후 계획
- json5, Jackson 기반 구현체 추가
- XML파서 연동

## 라이선스
이 프로젝트는 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)에 따라 제공됩니다.
