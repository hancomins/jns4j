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
 * concat 메서드에 대한 종합 테스트 클래스
 * ObjectContainer와 ArrayContainer의 연결 기능을 테스트합니다.
 */
public class ContainerConcatTest {

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

    // ObjectContainer concat 테스트

    @ParameterizedTest
    @MethodSource("containerFactories")
    public void testSimpleObjectConcat(ContainerFactory factory) {
        // Given
        ObjectContainer obj1 = factory.newObject()
                .put("a", 1)
                .put("b", 2)
                .put("c", 3);

        ObjectContainer obj2 = factory.newObject()
                .put("d", 4)
                .put("e", 5)
                .put("f", 6);

        // When
        ObjectContainer result = obj1.concat(obj2);

        // Then
        assertNotSame(obj1, result);  // 새로운 객체여야 함
        assertNotSame(obj2, result);
        
        // 원본은 변경되지 않아야 함
        assertEquals(3, obj1.size());
        assertEquals(3, obj2.size());
        
        // 결과는 모든 키를 포함해야 함
        assertEquals(6, result.size());
        assertEquals(1, result.getInt("a"));
        assertEquals(2, result.getInt("b"));
        assertEquals(3, result.getInt("c"));
        assertEquals(4, result.getInt("d"));
        assertEquals(5, result.getInt("e"));
        assertEquals(6, result.getInt("f"));
    }

    @Test
    public void testObjectConcatWithKeyConflict() {
        // Given: 키가 겹치는 경우
        ObjectContainer obj1 = Jsn4j.newObject()
                .put("name", "First")
                .put("value", 100)
                .put("unique1", "only in obj1");

        ObjectContainer obj2 = Jsn4j.newObject()
                .put("name", "Second")  // 겹치는 키
                .put("value", 200)      // 겹치는 키
                .put("unique2", "only in obj2");

        // When
        ObjectContainer result = obj1.concat(obj2);

        // Then: obj1의 값이 우선
        assertEquals("First", result.getString("name"));
        assertEquals(100, result.getInt("value"));
        assertEquals("only in obj1", result.getString("unique1"));
        assertEquals("only in obj2", result.getString("unique2"));
    }

    @Test
    public void testDeepNestedObjectConcat() {
        // Given: 깊은 중첩 구조
        ObjectContainer obj1 = Jsn4j.newObject()
                .put("level1", Jsn4j.newObject()
                        .put("level2", Jsn4j.newObject()
                                .put("level3", Jsn4j.newObject()
                                        .put("data", "deep1")
                                        .put("value", 1))));

        ObjectContainer obj2 = Jsn4j.newObject()
                .put("level1", Jsn4j.newObject()
                        .put("level2", Jsn4j.newObject()
                                .put("level3", Jsn4j.newObject()
                                        .put("data", "deep2")  // 덮어쓰여지지 않아야 함
                                        .put("extra", "added"))));

        // When
        ObjectContainer result = obj1.concat(obj2);

        // Then: obj1의 전체 구조가 우선
        ObjectContainer deep = result.get("level1").asObject()
                .get("level2").asObject()
                .get("level3").asObject();
        
        assertEquals("deep1", deep.getString("data"));
        assertEquals(1, deep.getInt("value"));
        assertTrue(deep.has("extra"));  // obj2의 깊은 값은 병합되지 않음
    }

    @Test
    public void testObjectConcatWithArrays() {
        // Given
        ObjectContainer obj1 = Jsn4j.newObject()
                .put("users", Jsn4j.newArray()
                        .put("Alice")
                        .put("Bob"))
                .put("count", 2);

        ObjectContainer obj2 = Jsn4j.newObject()
                .put("users", Jsn4j.newArray()
                        .put("Charlie")
                        .put("David"))
                .put("tags", Jsn4j.newArray()
                        .put("tag1")
                        .put("tag2"));

        // When
        ObjectContainer result = obj1.concat(obj2);

        // Then
        ArrayContainer users = result.get("users").asArray();
        assertEquals(2, users.size());  // obj1의 배열이 유지됨
        assertEquals("Alice", users.getString(0));
        assertEquals("Bob", users.getString(1));
        
        ArrayContainer tags = result.get("tags").asArray();
        assertEquals(2, tags.size());
        assertEquals("tag1", tags.getString(0));
    }


    @Test
    public void testEmptyObjectConcat() {
        // Given
        ObjectContainer empty = Jsn4j.newObject();
        ObjectContainer nonEmpty = Jsn4j.newObject()
                .put("a", 1)
                .put("b", 2);

        // When
        ObjectContainer result1 = empty.concat(nonEmpty);
        ObjectContainer result2 = nonEmpty.concat(empty);

        // Then
        assertEquals(2, result1.size());
        assertEquals(1, result1.getInt("a"));
        assertEquals(2, result1.getInt("b"));

        assertEquals(2, result2.size());
        assertEquals(1, result2.getInt("a"));
        assertEquals(2, result2.getInt("b"));
    }

    // ArrayContainer concat 테스트

    @ParameterizedTest
    @MethodSource("containerFactories")
    public void testSimpleArrayConcat(ContainerFactory factory) {
        // Given
        ArrayContainer arr1 = factory.newArray()
                .put("a")
                .put("b")
                .put("c");

        ArrayContainer arr2 = factory.newArray()
                .put("d")
                .put("e")
                .put("f");

        // When
        ArrayContainer result = arr1.concat(arr2);

        // Then
        assertNotSame(arr1, result);  // 새로운 배열이어야 함
        assertNotSame(arr2, result);
        
        // 원본은 변경되지 않아야 함
        assertEquals(3, arr1.size());
        assertEquals(3, arr2.size());
        
        // 결과는 두 배열의 연결
        assertEquals(6, result.size());
        assertEquals("a", result.getString(0));
        assertEquals("b", result.getString(1));
        assertEquals("c", result.getString(2));
        assertEquals("d", result.getString(3));
        assertEquals("e", result.getString(4));
        assertEquals("f", result.getString(5));
    }

    @Test
    public void testMixedTypeArrayConcat() {
        // Given
        ArrayContainer arr1 = Jsn4j.newArray()
                .put("string")
                .put(123)
                .put(true)
                .put(null);

        ArrayContainer arr2 = Jsn4j.newArray()
                .put(45.67)
                .put(Jsn4j.newObject().put("key", "value"))
                .put(Jsn4j.newArray().put(1).put(2))
                .put(new byte[]{1, 2, 3});

        // When
        ArrayContainer result = arr1.concat(arr2);

        // Then
        assertEquals(8, result.size());
        
        // arr1 요소들
        assertEquals("string", result.getString(0));
        assertEquals(123, result.getInt(1));
        assertTrue(result.getBoolean(2));
        assertTrue(result.get(3).isNull());
        
        // arr2 요소들
        assertEquals(45.67, result.getDouble(4), 0.001);
        assertEquals("value", result.get(5).asObject().getString("key"));
        assertEquals(2, result.get(6).asArray().size());
        assertArrayEquals(new byte[]{1, 2, 3}, result.getByteArray(7));
    }

    @Test
    public void testMultiDimensionalArrayConcat() {
        // Given: 다차원 배열
        ArrayContainer matrix1 = Jsn4j.newArray()
                .put(Jsn4j.newArray().put(1).put(2))
                .put(Jsn4j.newArray().put(3).put(4));

        ArrayContainer matrix2 = Jsn4j.newArray()
                .put(Jsn4j.newArray().put(5).put(6))
                .put(Jsn4j.newArray().put(7).put(8));

        // When
        ArrayContainer result = matrix1.concat(matrix2);

        // Then
        assertEquals(4, result.size());
        
        assertEquals(2, result.get(0).asArray().getInt(1));
        assertEquals(4, result.get(1).asArray().getInt(1));
        assertEquals(6, result.get(2).asArray().getInt(1));
        assertEquals(8, result.get(3).asArray().getInt(1));
    }

    @Test
    public void testArrayConcatWithComplexObjects() {
        // Given
        ArrayContainer products1 = Jsn4j.newArray()
                .put(createProduct("Laptop", 1500000, Arrays.asList("Electronics", "Computers")))
                .put(createProduct("Mouse", 50000, Arrays.asList("Electronics", "Accessories")));

        ArrayContainer products2 = Jsn4j.newArray()
                .put(createProduct("Keyboard", 100000, Arrays.asList("Electronics", "Accessories")))
                .put(createProduct("Monitor", 300000, Arrays.asList("Electronics", "Display")));

        // When
        ArrayContainer allProducts = products1.concat(products2);

        // Then
        assertEquals(4, allProducts.size());
        
        String[] expectedNames = {"Laptop", "Mouse", "Keyboard", "Monitor"};
        for (int i = 0; i < expectedNames.length; i++) {
            ObjectContainer product = allProducts.get(i).asObject();
            assertEquals(expectedNames[i], product.getString("name"));
        }
    }

    @Test
    public void testEmptyArrayConcat() {
        // Given
        ArrayContainer empty = Jsn4j.newArray();
        ArrayContainer nonEmpty = Jsn4j.newArray()
                .put("a")
                .put("b")
                .put("c");

        // When
        ArrayContainer result1 = empty.concat(nonEmpty);
        ArrayContainer result2 = nonEmpty.concat(empty);

        // Then
        assertEquals(3, result1.size());
        assertEquals("a", result1.getString(0));
        assertEquals("b", result1.getString(1));
        assertEquals("c", result1.getString(2));

        assertEquals(3, result2.size());
        assertEquals("a", result2.getString(0));
        assertEquals("b", result2.getString(1));
        assertEquals("c", result2.getString(2));
    }

    @Test
    public void testLargeArrayConcat() {
        // Given: 대용량 배열
        ArrayContainer arr1 = Jsn4j.newArray();
        ArrayContainer arr2 = Jsn4j.newArray();

        for (int i = 0; i < 10000; i++) {
            arr1.put("element1-" + i);
            arr2.put("element2-" + i);
        }

        // When
        long startTime = System.currentTimeMillis();
        ArrayContainer result = arr1.concat(arr2);
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals(20000, result.size());
        
        // 첫 번째 배열 요소 확인
        for (int i = 0; i < 10; i++) {
            assertEquals("element1-" + i, result.getString(i));
        }
        
        // 두 번째 배열 요소 확인
        for (int i = 0; i < 10; i++) {
            assertEquals("element2-" + i, result.getString(10000 + i));
        }

        // 성능 확인
        assertTrue((endTime - startTime) < 1000, "Concat took too long: " + (endTime - startTime) + "ms");
    }

    // 복잡한 시나리오 테스트

    @Test
    public void testSnsDataConcat() {
        // Given: 두 개의 SNS 피드
        ObjectContainer feed1 = Jsn4j.newObject()
                .put("feedId", "feed-001")
                .put("posts", Jsn4j.newArray()
                        .put(createPost("post-001", "First post", 100))
                        .put(createPost("post-002", "Second post", 200)))
                .put("lastUpdated", "2024-01-01");

        ObjectContainer feed2 = Jsn4j.newObject()
                .put("feedId", "feed-002")
                .put("posts", Jsn4j.newArray()
                        .put(createPost("post-003", "Third post", 150)))
                .put("newFeature", "stories")
                .put("lastUpdated", "2024-01-02");

        // When: 피드 병합
        ObjectContainer combinedFeed = feed1.concat(feed2);

        // Then
        assertEquals("feed-001", combinedFeed.getString("feedId"));  // feed1 우선
        assertEquals("2024-01-01", combinedFeed.getString("lastUpdated"));  // feed1 우선
        assertEquals("stories", combinedFeed.getString("newFeature"));  // feed2에서 추가
        
        ArrayContainer posts = combinedFeed.get("posts").asArray();
        assertEquals(2, posts.size());  // feed1의 posts가 유지됨
    }

    @Test
    public void testGeoDataConcat() {
        // Given: 지역 정보
        ObjectContainer seoul = Jsn4j.newObject()
                .put("city", "Seoul")
                .put("districts", Jsn4j.newArray()
                        .put(createDistrict("Gangnam", 545000))
                        .put(createDistrict("Songpa", 660000)))
                .put("totalPopulation", 9700000);

        ObjectContainer busan = Jsn4j.newObject()
                .put("city", "Busan")
                .put("districts", Jsn4j.newArray()
                        .put(createDistrict("Haeundae", 420000)))
                .put("port", true)
                .put("totalPopulation", 3400000);

        // When
        ObjectContainer combined = seoul.concat(busan);

        // Then
        assertEquals("Seoul", combined.getString("city"));
        assertEquals(9700000, combined.getInt("totalPopulation"));
        assertTrue(combined.getBoolean("port"));  // busan에서 추가된 필드
        
        ArrayContainer districts = combined.get("districts").asArray();
        assertEquals(2, districts.size());  // seoul의 districts 유지
    }

    @Test
    public void testCrossLibraryConcat() {
        // Given: 서로 다른 라이브러리의 컨테이너
        ObjectContainer simpleObj = SimpleJsonContainerFactory.getInstance().newObject()
                .put("lib", "simple")
                .put("data", 1);

        ObjectContainer jacksonObj = JacksonContainerFactory.getInstance().newObject()
                .put("lib", "jackson")
                .put("extra", 2);

        ArrayContainer simpleArr = SimpleJsonContainerFactory.getInstance().newArray()
                .put("simple1")
                .put("simple2");

        ArrayContainer gsonArr = GsonContainerFactory.getInstance().newArray()
                .put("gson1")
                .put("gson2");

        // When
        ObjectContainer objResult = simpleObj.concat(jacksonObj);
        ArrayContainer arrResult = simpleArr.concat(gsonArr);

        // Then
        assertEquals("simple", objResult.getString("lib"));
        assertEquals(1, objResult.getInt("data"));
        assertEquals(2, objResult.getInt("extra"));

        assertEquals(4, arrResult.size());
        assertEquals("simple1", arrResult.getString(0));
        assertEquals("gson2", arrResult.getString(3));
    }

    // 헬퍼 메서드들

    private ObjectContainer createProduct(String name, int price, List<String> categories) {
        ArrayContainer categoriesArr = Jsn4j.newArray();
        categories.forEach(categoriesArr::put);
        
        return Jsn4j.newObject()
                .put("name", name)
                .put("price", price)
                .put("categories", categoriesArr)
                .put("inStock", true);
    }

    private ObjectContainer createPost(String id, String content, int likes) {
        return Jsn4j.newObject()
                .put("postId", id)
                .put("content", content)
                .put("likes", likes)
                .put("timestamp", new Date().toString());
    }

    private ObjectContainer createDistrict(String name, int population) {
        return Jsn4j.newObject()
                .put("name", name)
                .put("population", population)
                .put("area", Math.random() * 50 + 10);
    }
}