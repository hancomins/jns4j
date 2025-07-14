# ContainerValues 클래스 가이드

## 개요

`ContainerValues`는 JSN4J 라이브러리에서 JSON 데이터를 다루기 위한 다양한 유틸리티 메서드를 제공하는 클래스입니다. 이 클래스는 JSON 객체와 배열의 비교, 복사, 병합, 차집합, 교집합 등의 작업을 수행할 수 있는 정적 메서드들을 제공합니다.

## 주요 메서드

### 1. equals(ContainerValue a, ContainerValue b)

두 ContainerValue 객체가 동일한지 비교합니다.

**특징:**
- 재귀적으로 중첩된 객체와 배열까지 비교
- 타입, 크기, 내용이 모두 동일해야 true 반환
- null 안전 처리

**예제:**
```java
ObjectContainer obj1 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25);
    
ObjectContainer obj2 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25);
    
boolean isEqual = ContainerValues.equals(obj1, obj2); // true
```

### 2. copy(ContainerValue target, ContainerValue source)

source의 내용을 target에 복사합니다. target의 기존 내용은 모두 제거됩니다.

**특징:**
- target을 완전히 비우고 source 내용으로 대체
- 같은 타입끼리만 복사 가능
- 중첩된 객체/배열은 깊은 복사 수행

**예제:**
```java
ObjectContainer target = Jsn4j.newObject().put("old", "data");
ObjectContainer source = Jsn4j.newObject()
    .put("name", "Bob")
    .put("age", 30);
    
ContainerValues.copy(target, source);
// target은 이제 {"name": "Bob", "age": 30}
```

### 3. cloneContainer(ContainerValue source)

ContainerValue의 깊은 복사본을 생성합니다.

**특징:**
- 새로운 독립적인 인스턴스 생성
- 중첩된 객체/배열도 모두 복사
- null이나 null 값은 PrimitiveValue(null)로 반환

**예제:**
```java
ObjectContainer original = Jsn4j.newObject()
    .put("data", Jsn4j.newObject().put("value", 100));
    
ContainerValue cloned = ContainerValues.cloneContainer(original);
// cloned는 original과 독립적인 복사본
```

### 4. merge(ContainerValue target, ContainerValue source)

source의 내용을 target에 병합합니다. **target을 직접 수정합니다.**

**특징:**
- **In-place 수정**: target이 직접 변경됨
- **Source 우선**: 같은 키/인덱스의 값은 source로 덮어씀
- **재귀적 병합**: 중첩된 객체는 재귀적으로 병합
- **배열 병합**: 인덱스 기반으로 병합, source가 더 길면 추가

**ObjectContainer 병합:**
```java
ObjectContainer target = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25)
    .put("city", "Seoul");
    
ObjectContainer source = Jsn4j.newObject()
    .put("age", 26)              // 덮어쓰기
    .put("country", "Korea");     // 추가

ContainerValues.merge(target, source);
// target: {"name": "Alice", "age": 26, "city": "Seoul", "country": "Korea"}
```

**ArrayContainer 병합:**
```java
ArrayContainer target = Jsn4j.newArray().put("a").put("b").put("c");
ArrayContainer source = Jsn4j.newArray().put("X").put("Y").put("Z").put("W");

ContainerValues.merge(target, source);
// target: ["X", "Y", "Z", "W"]
```

### 5. concat(ContainerValue target, ContainerValue source)

target과 source를 결합한 새로운 ContainerValue를 반환합니다. **원본은 수정되지 않습니다.**

**특징:**
- **새 객체 생성**: 원본들은 변경되지 않음
- **Target 우선**: 같은 키가 있으면 target 값 유지
- **불변성 보장**: 함수형 프로그래밍에 적합
- **null 처리**: 하나가 null이면 다른 것의 복사본 반환

**ObjectContainer 연결:**
```java
ObjectContainer target = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25);
    
ObjectContainer source = Jsn4j.newObject()
    .put("age", 30)              // 무시됨 (target 우선)
    .put("city", "Seoul");       // 추가됨

ContainerValue result = ContainerValues.concat(target, source);
// result: {"name": "Alice", "age": 25, "city": "Seoul"}
// target과 source는 변경되지 않음
```

**ArrayContainer 연결:**
```java
ArrayContainer target = Jsn4j.newArray().put("a").put("b");
ArrayContainer source = Jsn4j.newArray().put("c").put("d");

ContainerValue result = ContainerValues.concat(target, source);
// result: ["a", "b", "c", "d"]
```

### 6. intersection(ContainerValue a, ContainerValue b)

두 ContainerValue의 교집합을 계산합니다.

**특징:**
- 공통 요소만 포함하는 새 객체 반환
- 객체: 같은 키에 같은 값을 가진 필드만 포함
- 배열: 같은 인덱스에 같은 값을 가진 요소만 포함
- 중첩된 객체/배열도 재귀적으로 교집합 계산

**예제:**
```java
ObjectContainer obj1 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25)
    .put("city", "Seoul");
    
ObjectContainer obj2 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 30)
    .put("country", "Korea");

ContainerValue result = ContainerValues.intersection(obj1, obj2);
// result: {"name": "Alice"} (같은 값을 가진 필드만)
```

### 7. diff(ContainerValue a, ContainerValue b)

a를 기준으로 b와의 차이점을 계산합니다.

**특징:**
- a에만 있거나 a와 b에서 다른 값을 가진 요소 반환
- 객체: 다른 값을 가진 필드와 a에만 있는 필드 포함
- 배열: 차이가 있는 요소만 포함 (동일한 요소는 제외)
- 중첩된 구조도 재귀적으로 차이 계산

**객체 diff:**
```java
ObjectContainer obj1 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25)
    .put("city", "Seoul");
    
ObjectContainer obj2 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 26)
    .put("country", "Korea");

ContainerValue result = ContainerValues.diff(obj1, obj2);
// result: {"age": 25, "city": "Seoul"}
// name은 동일하므로 제외, age는 다른 값(a의 값), city는 a에만 있음
```

**배열 diff:**
```java
ArrayContainer arr1 = Jsn4j.newArray()
    .put(Jsn4j.newObject().put("id", 1).put("name", "Alice"))
    .put(Jsn4j.newObject().put("id", 2).put("name", "Bob"));
    
ArrayContainer arr2 = Jsn4j.newArray()
    .put(Jsn4j.newObject().put("id", 1).put("name", "Alice"))
    .put(Jsn4j.newObject().put("id", 2).put("name", "Charlie"));

ContainerValue result = ContainerValues.diff(arr1, arr2);
// result: [{"name": "Bob"}]
// 첫 번째 요소는 동일하므로 제외, 두 번째 요소만 차이점 포함
```

### 8. mapToObjectContainer(ContainerFactoryProvidable, Map<?, ?>)

Java Map을 ObjectContainer로 변환합니다.

**특징:**
- 중첩된 Map과 Collection도 재귀적으로 변환
- ContainerFactory를 통해 적절한 구현체 생성

**예제:**
```java
Map<String, Object> map = new HashMap<>();
map.put("name", "Alice");
map.put("nested", Map.of("key", "value"));
map.put("list", Arrays.asList(1, 2, 3));

ObjectContainer obj = ContainerValues.mapToObjectContainer(Jsn4j.getInstance(), map);
// obj: {"name": "Alice", "nested": {"key": "value"}, "list": [1, 2, 3]}
```

### 9. collectionToArrayContainer(ContainerFactoryProvidable, Collection<?>)

Java Collection을 ArrayContainer로 변환합니다.

**특징:**
- 중첩된 Map과 Collection도 재귀적으로 변환
- List, Set 등 모든 Collection 타입 지원

**예제:**
```java
List<Object> list = Arrays.asList(
    "text",
    123,
    Map.of("key", "value"),
    Arrays.asList(1, 2, 3)
);

ArrayContainer arr = ContainerValues.collectionToArrayContainer(Jsn4j.getInstance(), list);
// arr: ["text", 123, {"key": "value"}, [1, 2, 3]]
```

## merge() vs concat() 선택 가이드

| 특성 | merge() | concat() |
|------|---------|----------|
| **반환 타입** | void (대상 수정) | 새로운 ContainerValue |
| **원본 수정** | target을 직접 수정 | 원본 유지, 새 객체 생성 |
| **우선순위** | source가 target을 덮어씀 | target이 source보다 우선 |
| **용도** | 설정 업데이트, 패치 적용 | 불변 데이터 결합, 기본값 제공 |
| **성능** | 메모리 효율적 | 새 객체 생성으로 메모리 사용량 증가 |

## 사용 시 주의사항

1. **타입 일치**: copy, merge, concat은 같은 타입끼리만 작동
2. **null 처리**: 대부분의 메서드는 null을 안전하게 처리
3. **순환 참조**: 자기 자신과의 병합 시 무한 루프 방지를 위해 복사본 생성
4. **성능**: 대용량 데이터의 경우 merge가 concat보다 효율적
5. **불변성**: concat을 사용하면 원본 데이터의 불변성 보장

## 실제 사용 예제

### 설정 파일 병합
```java
// 기본 설정에 사용자 설정 덮어쓰기
ObjectContainer defaultConfig = loadDefaultConfig();
ObjectContainer userConfig = loadUserConfig();
ContainerValues.merge(defaultConfig, userConfig);
saveConfig(defaultConfig);
```

### API 응답 비교
```java
// 두 API 버전의 응답 차이 확인
ObjectContainer v1Response = getApiV1Response();
ObjectContainer v2Response = getApiV2Response();
ContainerValue changes = ContainerValues.diff(v1Response, v2Response);
logApiChanges(changes);
```

### 데이터 필터링
```java
// 두 데이터셋의 공통 요소만 추출
ArrayContainer dataset1 = loadDataset1();
ArrayContainer dataset2 = loadDataset2();
ContainerValue common = ContainerValues.intersection(dataset1, dataset2);
processCommonData(common);
```