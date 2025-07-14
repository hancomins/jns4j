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
 * ContainerValues 클래스의 새로운 정적 메서드들에 대한 테스트
 * - cloneContainer
 * - concat (정적 메서드)
 */
public class ContainerValuesExtendedTest {

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

    // cloneContainer 테스트

    @Test
    public void testClonePrimitiveValue() {
        // Given
        PrimitiveValue original = new PrimitiveValue("test string");
        
        // When
        ContainerValue cloned = ContainerValues.cloneContainer(original);
        
        // Then
        assertNotSame(original, cloned);
        assertTrue(cloned.isPrimitive());
        assertEquals("test string", ((PrimitiveValue)cloned).asString());
    }

    @Test
    public void testCloneNull() {
        // Given
        PrimitiveValue nullValue = new PrimitiveValue(null);
        
        // When
        ContainerValue cloned1 = ContainerValues.cloneContainer(null);
        ContainerValue cloned2 = ContainerValues.cloneContainer(nullValue);
        
        // Then
        assertNotNull(cloned1);
        assertTrue(cloned1.isNull());
        assertTrue(cloned2.isNull());
    }

    @ParameterizedTest
    @MethodSource("containerFactories")
    public void testCloneSimpleObject(ContainerFactory factory) {
        // Given
        ObjectContainer original = factory.newObject()
                .put("name", "John")
                .put("age", 30)
                .put("active", true)
                .put("balance", 1234.56)
                .put("nullValue", null);
        
        // When
        ContainerValue clonedValue = ContainerValues.cloneContainer(original);
        ObjectContainer cloned = clonedValue.asObject();
        
        // Then
        assertNotSame(original, cloned);
        assertEquals(original.getContainerFactory().getClass(), cloned.getContainerFactory().getClass());
        
        // 값 확인
        assertEquals("John", cloned.getString("name"));
        assertEquals(30, cloned.getInt("age"));
        assertTrue(cloned.getBoolean("active"));
        assertEquals(1234.56, cloned.getDouble("balance"), 0.001);
        assertTrue(cloned.get("nullValue").isNull());
        
        // 깊은 복사 확인 - 원본 수정이 복사본에 영향 없음
        original.put("name", "Jane");
        assertEquals("John", cloned.getString("name"));
    }

    @ParameterizedTest
    @MethodSource("containerFactories")
    public void testCloneSimpleArray(ContainerFactory factory) {
        // Given
        ArrayContainer original = factory.newArray()
                .put("string")
                .put(123)
                .put(45.67)
                .put(true)
                .put(null);
        
        // When
        ContainerValue clonedValue = ContainerValues.cloneContainer(original);
        ArrayContainer cloned = clonedValue.asArray();
        
        // Then
        assertNotSame(original, cloned);
        assertEquals(original.size(), cloned.size());
        
        assertEquals("string", cloned.getString(0));
        assertEquals(123, cloned.getInt(1));
        assertEquals(45.67, cloned.getDouble(2), 0.001);
        assertTrue(cloned.getBoolean(3));
        assertTrue(cloned.get(4).isNull());
        
        // 깊은 복사 확인
        original.put(0, "modified");
        assertEquals("string", cloned.getString(0));
    }

    @Test
    public void testCloneDeepNestedStructure() {
        // Given: 깊은 중첩 구조
        ObjectContainer original = Jsn4j.newObject()
                .put("level1", Jsn4j.newObject()
                        .put("level2", Jsn4j.newObject()
                                .put("level3", Jsn4j.newObject()
                                        .put("level4", Jsn4j.newObject()
                                                .put("level5", Jsn4j.newArray()
                                                        .put("deep")
                                                        .put("nested")
                                                        .put("array"))))));
        
        // When
        ObjectContainer cloned = ContainerValues.cloneContainer(original).asObject();
        
        // Then
        ArrayContainer deepArray = cloned.get("level1").asObject()
                .get("level2").asObject()
                .get("level3").asObject()
                .get("level4").asObject()
                .get("level5").asArray();
        
        assertEquals(3, deepArray.size());
        assertEquals("deep", deepArray.getString(0));
        
        // 원본 수정이 복사본에 영향 없음
        original.get("level1").asObject()
                .get("level2").asObject()
                .get("level3").asObject()
                .get("level4").asObject()
                .get("level5").asArray()
                .put(0, "modified");
        
        assertEquals("deep", deepArray.getString(0));
    }

    @Test
    public void testCloneComplexData() {
        // Given: 복잡한 전자상거래 데이터
        ObjectContainer original = createComplexEcommerceData();
        
        // When
        ObjectContainer cloned = ContainerValues.cloneContainer(original).asObject();
        
        // Then
        verifyComplexEcommerceData(cloned);
        
        // 원본과 복사본이 독립적인지 확인
        original.put("orderId", "MODIFIED");
        assertEquals("ORD-2024-001234", cloned.getString("orderId"));
        
        // 중첩된 객체도 독립적인지 확인
        original.get("customer").asObject().put("name", "Modified Name");
        assertEquals("김철수", cloned.get("customer").asObject().getString("name"));
    }

    /*@Test
    public void testCloneWithByteArray() {
        // Given
        byte[] imageData = {0x00, 0x01, 0x02, (byte)0xFF};
        ObjectContainer original = Jsn4j.newObject()
                .put("image", imageData)
                .put("type", "png");
        
        // When
        ObjectContainer cloned = ContainerValues.cloneContainer(original).asObject();
        
        // Then
        byte[] clonedData = cloned.getByteArray("image");
        assertArrayEquals(imageData, clonedData);
        assertNotSame(imageData, clonedData); // 배열도 복사되어야 함
        
        // 원본 배열 수정이 복사본에 영향 없음
        imageData[0] = (byte)0xAA;
        assertEquals((byte)0x00, clonedData[0]);
    }*/

    // concat 정적 메서드 테스트

    @Test
    public void testConcatNullHandling() {
        // Given
        ObjectContainer obj = Jsn4j.newObject().put("key", "value");
        ArrayContainer arr = Jsn4j.newArray().put("element");
        
        // When & Then: 둘 다 null인 경우
        assertThrows(NullPointerException.class, () -> {
            ContainerValues.concat(null, null);
        });
        
        // null과 객체
        ContainerValue result1 = ContainerValues.concat(null, obj);
        assertTrue(result1.isObject());
        assertEquals("value", result1.asObject().getString("key"));
        
        ContainerValue result2 = ContainerValues.concat(obj, null);
        assertTrue(result2.isObject());
        assertEquals("value", result2.asObject().getString("key"));
        
        // null과 배열
        ContainerValue result3 = ContainerValues.concat(null, arr);
        assertTrue(result3.isArray());
        assertEquals("element", result3.asArray().getString(0));
    }

    @Test
    public void testConcatTypeMismatch() {
        // Given
        ObjectContainer obj = Jsn4j.newObject().put("key", "value");
        ArrayContainer arr = Jsn4j.newArray().put("element");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ContainerValues.concat(obj, arr);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ContainerValues.concat(arr, obj);
        });
    }

    @Test
    public void testConcatPrimitiveValues() {
        // Given
        PrimitiveValue prim1 = new PrimitiveValue("string");
        PrimitiveValue prim2 = new PrimitiveValue(123);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ContainerValues.concat(prim1, prim2);
        });
    }

    @Test
    public void testConcatSimpleObjects() {
        // Given
        ObjectContainer obj1 = Jsn4j.newObject()
                .put("a", 1)
                .put("b", 2);
        
        ObjectContainer obj2 = Jsn4j.newObject()
                .put("b", 3)  // 충돌
                .put("c", 4);
        
        // When
        ObjectContainer result = ContainerValues.concat(obj1, obj2).asObject();
        
        // Then
        assertNotSame(obj1, result);
        assertNotSame(obj2, result);
        
        // concat은 첫 번째 인자의 값이 우선
        assertEquals(1, result.getInt("a"));
        assertEquals(2, result.getInt("b"));  // obj1의 값
        assertEquals(4, result.getInt("c"));
    }

    @Test
    public void testConcatSimpleArrays() {
        // Given
        ArrayContainer arr1 = Jsn4j.newArray()
                .put("a")
                .put("b");
        
        ArrayContainer arr2 = Jsn4j.newArray()
                .put("c")
                .put("d");
        
        // When
        ArrayContainer result = ContainerValues.concat(arr1, arr2).asArray();
        
        // Then
        assertNotSame(arr1, result);
        assertNotSame(arr2, result);
        
        assertEquals(4, result.size());
        assertEquals("a", result.getString(0));
        assertEquals("b", result.getString(1));
        assertEquals("c", result.getString(2));
        assertEquals("d", result.getString(3));
    }

    @Test
    public void testConcatComplexStructures() {
        // Given: 복잡한 SNS 데이터
        ObjectContainer user1Data = createUserProfile("user1", "Alice", 1000);
        ObjectContainer user2Data = createUserProfile("user2", "Bob", 2000);
        
        // When
        ObjectContainer combined = ContainerValues.concat(user1Data, user2Data).asObject();
        
        // Then
        assertEquals("user1", combined.getString("userId"));  // user1 우선
        assertEquals("Alice", combined.getString("name"));
        
        // posts는 user1의 것이 유지됨
        ArrayContainer posts = combined.get("posts").asArray();
        assertEquals(2, posts.size());
        assertEquals("post-user1-1", posts.get(0).asObject().getString("id"));
        
        // user2에만 있는 필드는 추가됨
        if (user2Data.has("specialField")) {
            assertTrue(combined.has("specialField"));
        }
    }

    @Test
    public void testConcatLargeData() {
        // Given: 대용량 데이터
        ObjectContainer large1 = Jsn4j.newObject();
        ObjectContainer large2 = Jsn4j.newObject();
        
        for (int i = 0; i < 1000; i++) {
            large1.put("key" + i, "value1-" + i);
            large2.put("key" + (i + 500), "value2-" + i);  // 일부 겹침
        }
        
        // When
        long startTime = System.currentTimeMillis();
        ObjectContainer result = ContainerValues.concat(large1, large2).asObject();
        long endTime = System.currentTimeMillis();
        
        // Then
        assertTrue(result.size() >= 1000 && result.size() <= 1500);
        
        // 겹치는 키는 large1 값 유지
        for (int i = 500; i < 1000; i++) {
            assertEquals("value1-" + i, result.getString("key" + i));
        }
        
        // large2에만 있는 키
        for (int i = 1000; i < 1500; i++) {
            assertEquals("value2-" + (i - 500), result.getString("key" + i));
        }
        
        // 성능 확인
        assertTrue((endTime - startTime) < 1000, "Concat took too long: " + (endTime - startTime) + "ms");
    }

    @Test
    public void testConcatCrossLibrary() {
        // Given: 서로 다른 라이브러리의 컨테이너
        ObjectContainer simpleObj = SimpleJsonContainerFactory.getInstance().newObject()
                .put("lib", "simple")
                .put("data", 1);
        
        ObjectContainer jacksonObj = JacksonContainerFactory.getInstance().newObject()
                .put("lib", "jackson")
                .put("extra", 2);
        
        // When
        ObjectContainer result = ContainerValues.concat(simpleObj, jacksonObj).asObject();
        
        // Then
        assertEquals("simple", result.getString("lib"));  // 첫 번째 우선
        assertEquals(1, result.getInt("data"));
        assertEquals(2, result.getInt("extra"));
        
        // 결과는 첫 번째 인자의 팩토리 사용
        assertEquals(simpleObj.getContainerFactory().getClass(), 
                    result.getContainerFactory().getClass());
    }

    @Test
    public void testCloneAndModifyScenario() {
        // Given: 원본 주문 데이터
        ObjectContainer originalOrder = createOrder("ORD-001", "pending");
        
        // When: 복제 후 수정
        ObjectContainer clonedOrder = ContainerValues.cloneContainer(originalOrder).asObject();
        clonedOrder.put("orderId", "ORD-002");
        clonedOrder.put("status", "confirmed");
        clonedOrder.get("items").asArray().put(
            Jsn4j.newObject()
                .put("productId", "PROD-C")
                .put("quantity", 1)
        );
        
        // Then: 원본은 변경되지 않음
        assertEquals("ORD-001", originalOrder.getString("orderId"));
        assertEquals("pending", originalOrder.getString("status"));
        assertEquals(2, originalOrder.get("items").asArray().size());
        
        // 복제본은 변경됨
        assertEquals("ORD-002", clonedOrder.getString("orderId"));
        assertEquals("confirmed", clonedOrder.getString("status"));
        assertEquals(3, clonedOrder.get("items").asArray().size());
    }

    // 헬퍼 메서드들

    private ObjectContainer createComplexEcommerceData() {
        return Jsn4j.newObject()
                .put("orderId", "ORD-2024-001234")
                .put("customer", Jsn4j.newObject()
                        .put("id", 98765)
                        .put("name", "김철수")
                        .put("email", "chulsoo@example.com")
                        .put("addresses", Jsn4j.newArray()
                                .put(Jsn4j.newObject()
                                        .put("type", "billing")
                                        .put("street", "서울시 강남구 테헤란로 123"))
                                .put(Jsn4j.newObject()
                                        .put("type", "shipping")
                                        .put("street", "서울시 서초구 서초대로 456"))))
                .put("items", Jsn4j.newArray()
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-001")
                                .put("name", "노트북")
                                .put("quantity", 1)
                                .put("price", 1500000))
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-002")
                                .put("name", "마우스")
                                .put("quantity", 2)
                                .put("price", 50000)))
                .put("totalAmount", 1600000)
                .put("status", "confirmed");
    }

    private void verifyComplexEcommerceData(ObjectContainer data) {
        assertEquals("ORD-2024-001234", data.getString("orderId"));
        assertEquals(98765, data.get("customer").asObject().getInt("id"));
        assertEquals(2, data.get("customer").asObject().get("addresses").asArray().size());
        assertEquals(2, data.get("items").asArray().size());
        assertEquals(1600000, data.getInt("totalAmount"));
    }

    private ObjectContainer createUserProfile(String userId, String name, int followers) {
        return Jsn4j.newObject()
                .put("userId", userId)
                .put("name", name)
                .put("followers", followers)
                .put("posts", Jsn4j.newArray()
                        .put(Jsn4j.newObject()
                                .put("id", "post-" + userId + "-1")
                                .put("content", "First post by " + name))
                        .put(Jsn4j.newObject()
                                .put("id", "post-" + userId + "-2")
                                .put("content", "Second post by " + name)))
                .put("joinDate", new Date().toString());
    }

    private ObjectContainer createOrder(String orderId, String status) {
        return Jsn4j.newObject()
                .put("orderId", orderId)
                .put("status", status)
                .put("items", Jsn4j.newArray()
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-A")
                                .put("quantity", 2))
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-B")
                                .put("quantity", 1)))
                .put("createdAt", new Date().toString());
    }
}