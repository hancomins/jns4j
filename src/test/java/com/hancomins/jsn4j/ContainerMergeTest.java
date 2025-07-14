package com.hancomins.jsn4j;

import com.hancomins.jsn4j.jackson.JacksonContainerFactory;
import com.hancomins.jsn4j.gson.GsonContainerFactory;
import com.hancomins.jsn4j.simple.SimpleJsonContainerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * merge 메서드에 대한 종합 테스트 클래스
 * ObjectContainer와 ArrayContainer의 병합 기능을 테스트합니다.
 */
public class ContainerMergeTest {

    @BeforeAll
    public static void setupFactories() {
        Jsn4j.registerContainerFactory(SimpleJsonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(JacksonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(GsonContainerFactory.getInstance());
    }

    private static Stream<Arguments> containerFactories() {
        return Stream.of(
                Arguments.of(SimpleJsonContainerFactory.getInstance()),
                Arguments.of(JacksonContainerFactory.getInstance()),
                Arguments.of(GsonContainerFactory.getInstance())
        );
    }

    // ObjectContainer merge 테스트

    @ParameterizedTest
    @MethodSource("containerFactories")
    public void testSimpleObjectMerge(ContainerFactory factory) {
        // Given
        ObjectContainer target = factory.newObject()
                .put("name", "Original")
                .put("age", 25)
                .put("city", "Seoul");

        ObjectContainer source = factory.newObject()
                .put("age", 30)  // 덮어쓰기
                .put("email", "test@example.com")  // 새 필드
                .put("country", "Korea");  // 새 필드

        // When
        target.merge(source);

        // Then
        assertEquals("Original", target.getString("name"));  // 유지
        assertEquals(30, target.getInt("age"));  // 덮어쓰기
        assertEquals("Seoul", target.getString("city"));  // 유지
        assertEquals("test@example.com", target.getString("email"));  // 추가
        assertEquals("Korea", target.getString("country"));  // 추가
    }

    @Test
    public void testDeepNestedObjectMerge() {
        // Given: 깊은 중첩 구조
        ObjectContainer target = Jsn4j.newObject()
                .put("user", Jsn4j.newObject()
                        .put("profile", Jsn4j.newObject()
                                .put("name", "John")
                                .put("age", 25)
                                .put("preferences", Jsn4j.newObject()
                                        .put("theme", "dark")
                                        .put("language", "en"))))
                .put("settings", Jsn4j.newObject()
                        .put("notifications", true));

        ObjectContainer source = Jsn4j.newObject()
                .put("user", Jsn4j.newObject()
                        .put("profile", Jsn4j.newObject()
                                .put("age", 26)  // 덮어쓰기
                                .put("email", "john@example.com")  // 추가
                                .put("preferences", Jsn4j.newObject()
                                        .put("language", "ko")  // 덮어쓰기
                                        .put("timezone", "Asia/Seoul"))))  // 추가
                .put("settings", Jsn4j.newObject()
                        .put("privacy", "public"));  // 추가

        // When
        target.merge(source);

        // Then
        ObjectContainer userProfile = target.get("user").asObject().get("profile").asObject();
        assertEquals("John", userProfile.getString("name"));  // 유지
        assertEquals(26, userProfile.getInt("age"));  // 덮어쓰기
        assertEquals("john@example.com", userProfile.getString("email"));  // 추가

        ObjectContainer preferences = userProfile.get("preferences").asObject();
        assertEquals("dark", preferences.getString("theme"));  // 유지
        assertEquals("ko", preferences.getString("language"));  // 덮어쓰기
        assertEquals("Asia/Seoul", preferences.getString("timezone"));  // 추가

        ObjectContainer settings = target.get("settings").asObject();
        assertTrue(settings.getBoolean("notifications"));  // 유지
        assertEquals("public", settings.getString("privacy"));  // 추가
    }

    @Test
    public void testObjectMergeWithArrays() {
        // Given: 배열을 포함한 객체
        ObjectContainer target = Jsn4j.newObject()
                .put("name", "Project")
                .put("tags", Jsn4j.newArray()
                        .put("java")
                        .put("json"))
                .put("members", Jsn4j.newArray()
                        .put(Jsn4j.newObject().put("id", 1).put("name", "Alice")));

        ObjectContainer source = Jsn4j.newObject()
                .put("tags", Jsn4j.newArray()
                        .put("library")
                        .put("opensource"))  // 배열 전체 교체
                .put("members", Jsn4j.newArray()
                        .put(Jsn4j.newObject().put("id", 2).put("name", "Bob"))
                        .put(Jsn4j.newObject().put("id", 3).put("name", "Charlie")))
                .put("version", "1.0.0");

        // When
        target.merge(source);

        // Then
        assertEquals("Project", target.getString("name"));
        
        ArrayContainer tags = target.get("tags").asArray();
        assertEquals(2, tags.size());
        assertEquals("library", tags.getString(0));
        assertEquals("opensource", tags.getString(1));

        ArrayContainer members = target.get("members").asArray();
        assertEquals(2, members.size());
        assertEquals("Bob", members.get(0).asObject().getString("name"));
        
        assertEquals("1.0.0", target.getString("version"));
    }

    @Test
    public void testObjectMergeWithNullValues() {
        // Given
        ObjectContainer target = Jsn4j.newObject()
                .put("a", "value")
                .put("b", 123)
                .put("c", true);

        ObjectContainer source = Jsn4j.newObject()
                .put("b", null)  // null로 덮어쓰기
                .put("d", null)  // null 추가
                .put("e", "new");

        // When
        target.merge(source);

        // Then
        assertEquals("value", target.getString("a"));
        assertTrue(target.get("b").isNull());
        assertTrue(target.has("c"));
        assertTrue(target.has("d"));
        assertTrue(target.get("d").isNull());
        assertEquals("new", target.getString("e"));
    }

    @Test
    public void testCrossLibraryObjectMerge() {
        // Given: 서로 다른 라이브러리의 객체
        ObjectContainer simpleTarget = SimpleJsonContainerFactory.getInstance().newObject()
                .put("lib", "simple")
                .put("data", Jsn4j.newObject().put("value", 1));

        ObjectContainer jacksonSource = JacksonContainerFactory.getInstance().newObject()
                .put("lib", "jackson")
                .put("data", JacksonContainerFactory.getInstance().newObject().put("extra", 2))
                .put("newField", "added");

        // When
        simpleTarget.merge(jacksonSource);

        // Then
        assertEquals("jackson", simpleTarget.getString("lib"));  // 덮어쓰기
        assertEquals("added", simpleTarget.getString("newField"));
        
        ObjectContainer data = simpleTarget.get("data").asObject();
        assertEquals(2, data.getInt("extra"));  // 원본 data는 완전히 교체됨
    }

    @Test
    public void testLargeObjectMerge() {
        // Given: 1000개 이상의 키를 가진 대용량 객체
        ObjectContainer target = Jsn4j.newObject();
        ObjectContainer source = Jsn4j.newObject();

        for (int i = 0; i < 1000; i++) {
            target.put("key" + i, "target" + i);
            if (i % 2 == 0) {
                source.put("key" + i, "source" + i);  // 짝수 키 덮어쓰기
            }
        }

        for (int i = 1000; i < 1500; i++) {
            source.put("key" + i, "source" + i);  // 새 키 추가
        }

        // When
        long startTime = System.currentTimeMillis();
        target.merge(source);
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals(1500, target.size());
        
        // 덮어쓰기 확인
        for (int i = 0; i < 1000; i++) {
            if (i % 2 == 0) {
                assertEquals("source" + i, target.getString("key" + i));
            } else {
                assertEquals("target" + i, target.getString("key" + i));
            }
        }
        
        // 새 키 확인
        for (int i = 1000; i < 1500; i++) {
            assertEquals("source" + i, target.getString("key" + i));
        }

        // 성능 확인 (1초 이내)
        assertTrue((endTime - startTime) < 1000, "Merge took too long: " + (endTime - startTime) + "ms");
    }

    // ArrayContainer merge 테스트

    @ParameterizedTest
    @MethodSource("containerFactories")
    public void testSimpleArrayMerge(ContainerFactory factory) {
        // Given
        ArrayContainer target = factory.newArray()
                .put("a")
                .put("b")
                .put("c");

        ArrayContainer source = factory.newArray()
                .put("d")
                .put("e")
                .put("f")
                .put("g");  // 추가 요소

        // When
        target.merge(source);

        // Then
        assertEquals(4, target.size());
        assertEquals("d", target.getString(0));
        assertEquals("e", target.getString(1));
        assertEquals("f", target.getString(2));
        assertEquals("g", target.getString(3));

    }

    @Test
    public void testMixedTypeArrayMerge() {
        // Given
        ArrayContainer target = Jsn4j.newArray()
                .put("string")
                .put(123)
                .put(true);

        ArrayContainer source = Jsn4j.newArray()
                .put(45.67)
                .put(null)
                .put(Jsn4j.newObject().put("key", "value"))
                .put(Jsn4j.newArray().put(1).put(2).put(3));

        // When
        target.merge(source);

        // Then
        assertEquals(4, target.size());
        assertEquals(45.67, target.getDouble(0), 0.001);
        assertTrue(target.get(1).isNull());
        assertEquals("value", target.get(2).asObject().getString("key"));
        assertEquals(3, target.get(3).asArray().size());
    }

    @Test
    public void testNestedArrayMerge() {
        // Given: 중첩된 배열 구조
        ArrayContainer target = Jsn4j.newArray()
                .put(Jsn4j.newArray().put(1).put(2))
                .put(Jsn4j.newArray().put("a").put("b"));

        ArrayContainer source = Jsn4j.newArray()
                .put(Jsn4j.newArray().put(3).put(4))
                .put(Jsn4j.newArray().put("c").put("d"));

        // When
        target.merge(source);

        // Then
        assertEquals(2, target.size());
        // First array was merged (index 0)
        ArrayContainer firstArray = target.get(0).asArray();
        assertEquals(2, firstArray.size());
        assertEquals(3, firstArray.getInt(0));
        assertEquals(4, firstArray.getInt(1));
        // Second array was merged (index 1)
        ArrayContainer secondArray = target.get(1).asArray();
        assertEquals(2, secondArray.size());
        assertEquals("c", secondArray.getString(0));
        assertEquals("d", secondArray.getString(1));
    }

    @Test
    public void testArrayMergeWithComplexObjects() {
        // Given
        ArrayContainer target = Jsn4j.newArray()
                .put(createUserObject("Alice", 25, "alice@example.com"))
                .put(createUserObject("Bob", 30, "bob@example.com"));

        ArrayContainer source = Jsn4j.newArray()
                .put(createUserObject("Charlie", 35, "charlie@example.com"))
                .put(createUserObject("David", 40, "david@example.com"))
                .put(createUserObject("Eve", 28, "eve@example.com"));

        // When
        target.merge(source);

        // Then
        assertEquals(3, target.size());
        
        // First two elements are replaced, third is appended
        assertEquals("Charlie", target.get(0).asObject().getString("name"));
        assertEquals("David", target.get(1).asObject().getString("name"));
        assertEquals("Eve", target.get(2).asObject().getString("name"));
    }

    @Test
    public void testLargeArrayMerge() {
        // Given: 대용량 배열
        ArrayContainer target = Jsn4j.newArray();
        ArrayContainer source = Jsn4j.newArray();

        for (int i = 0; i < 5000; i++) {
            target.put("target-" + i);
            source.put("source-" + i);
        }

        // When
        long startTime = System.currentTimeMillis();
        target.merge(source);
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals(5000, target.size());
        
        // 순서 확인
        for (int i = 0; i < 5000; i++) {
            assertEquals("source-" + i, target.getString(i ));
        }

        // 성능 확인
        assertTrue((endTime - startTime) < 1000, "Array merge took too long: " + (endTime - startTime) + "ms");
    }

    @Test
    public void testEmptyContainerMerge() {
        // Given
        ObjectContainer emptyObjTarget = Jsn4j.newObject();
        ObjectContainer objSource = Jsn4j.newObject().put("key", "value");

        ArrayContainer emptyArrTarget = Jsn4j.newArray();
        ArrayContainer arrSource = Jsn4j.newArray().put("element");

        // When
        emptyObjTarget.merge(objSource);
        emptyArrTarget.merge(arrSource);

        // Then
        assertEquals(1, emptyObjTarget.size());
        assertEquals("value", emptyObjTarget.getString("key"));

        assertEquals(1, emptyArrTarget.size());
        assertEquals("element", emptyArrTarget.getString(0));
    }

    @Test
    public void testSelfMerge() {
        // Given
        ObjectContainer obj = Jsn4j.newObject()
                .put("key1", "value1")
                .put("key2", "value2");

        ArrayContainer arr = Jsn4j.newArray()
                .put("a")
                .put("b");

        // When
        obj.merge(obj);
        arr.merge(arr);

        // Then
        assertEquals(2, obj.size());  // 같은 키는 덮어쓰기되므로 크기 유지
        assertEquals(2, arr.size());  // 배열은 요소가 추가됨
        
        assertEquals("value1", obj.getString("key1"));
        assertEquals("a", arr.getString(0));
        assertEquals("b", arr.getString(1));

    }

    // 복잡한 시나리오

    @Test
    public void testEcommerceMergeScenario() {
        // Given: 기존 주문 데이터
        ObjectContainer existingOrder = Jsn4j.newObject()
                .put("orderId", "ORD-001")
                .put("status", "pending")
                .put("items", Jsn4j.newArray()
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-A")
                                .put("quantity", 2)))
                .put("totalAmount", 50000);

        // 업데이트할 데이터
        ObjectContainer updates = Jsn4j.newObject()
                .put("status", "confirmed")  // 상태 변경
                .put("items", Jsn4j.newArray()  // 아이템 교체
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-A")
                                .put("quantity", 3))
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-B")
                                .put("quantity", 1)))
                .put("totalAmount", 75000)  // 금액 업데이트
                .put("paymentMethod", "creditCard")  // 새 필드
                .put("shippingAddress", Jsn4j.newObject()  // 새 필드
                        .put("street", "123 Main St")
                        .put("city", "Seoul"));

        // When
        existingOrder.merge(updates);

        // Then
        assertEquals("ORD-001", existingOrder.getString("orderId"));  // 유지
        assertEquals("confirmed", existingOrder.getString("status"));  // 업데이트
        assertEquals(75000, existingOrder.getInt("totalAmount"));  // 업데이트
        assertEquals("creditCard", existingOrder.getString("paymentMethod"));  // 추가

        ArrayContainer items = existingOrder.get("items").asArray();
        assertEquals(2, items.size());
        assertEquals(3, items.get(0).asObject().getInt("quantity"));
        assertEquals("PROD-B", items.get(1).asObject().getString("productId"));
    }

    // 헬퍼 메서드
    private ObjectContainer createUserObject(String name, int age, String email) {
        return Jsn4j.newObject()
                .put("name", name)
                .put("age", age)
                .put("email", email)
                .put("registeredAt", new Date().toString());
    }
}