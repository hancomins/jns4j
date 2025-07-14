package com.hancomins.jsn4j;

import com.hancomins.jsn4j.jackson.JacksonContainerFactory;
import com.hancomins.jsn4j.fastjson2.Fastjson2ContainerFactory;
import com.hancomins.jsn4j.orgjson.OrgJsonContainerFactory;
import com.hancomins.jsn4j.json5.Json5ContainerFactory;
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
 * convertTo 메서드에 대한 종합 테스트 클래스
 * 모든 JSON 라이브러리 간의 변환을 테스트합니다.
 */
public class ContainerConvertTest {

    @BeforeAll
    public static void setupFactories() {
        // 모든 팩토리 등록
        Jsn4j.registerContainerFactory(SimpleJsonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(JacksonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(GsonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(Fastjson2ContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(OrgJsonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(Json5ContainerFactory.getInstance());
    }

    /**
     * 모든 라이브러리 조합을 제공하는 메서드
     */
    private static Stream<Arguments> libraryPairs() {
        JsonLibrary[] libraries = JsonLibrary.values();
        List<Arguments> pairs = new ArrayList<>();
        
        for (JsonLibrary from : libraries) {
            for (JsonLibrary to : libraries) {
                if (from != to) {
                    pairs.add(Arguments.of(from, to));
                }
            }
        }
        
        return pairs.stream();
    }

    @ParameterizedTest
    @MethodSource("libraryPairs")
    public void testSimpleObjectConversion(JsonLibrary from, JsonLibrary to) {
        // Given: 원본 라이브러리로 간단한 객체 생성
        ContainerFactory fromFactory = Jsn4j.getContainerFactory(from);
        ObjectContainer original = fromFactory.newObject()
                .put("name", "테스트")
                .put("age", 30)
                .put("active", true)
                .put("balance", 1234.56)
                .put("nullable", null);

        // When: 다른 라이브러리로 변환
        ObjectContainer converted = original.convertTo(to);

        // Then: 변환된 객체 검증
        assertNotNull(converted);
        assertEquals(Jsn4j.getContainerFactory(to), converted.getContainerFactory());
        assertEquals("테스트", converted.getString("name"));
        assertEquals(30, converted.getInt("age"));
        assertTrue(converted.getBoolean("active"));
        assertEquals(1234.56, converted.getDouble("balance"), 0.001);
        assertTrue(converted.get("nullable").isNull());
    }

    @ParameterizedTest
    @MethodSource("libraryPairs")
    public void testSimpleArrayConversion(JsonLibrary from, JsonLibrary to) {
        // Given: 원본 라이브러리로 간단한 배열 생성
        ContainerFactory fromFactory = Jsn4j.getContainerFactory(from);
        ArrayContainer original = fromFactory.newArray()
                .put("문자열")
                .put(42)
                .put(3.14)
                .put(true)
                .put(null);

        // When: 다른 라이브러리로 변환
        ArrayContainer converted = original.convertTo(to);

        // Then: 변환된 배열 검증
        assertNotNull(converted);
        assertEquals(Jsn4j.getContainerFactory(to), converted.getContainerFactory());
        assertEquals(5, converted.size());
        assertEquals("문자열", converted.getString(0));
        assertEquals(42, converted.getInt(1));
        assertEquals(3.14, converted.getDouble(2), 0.001);
        assertTrue(converted.getBoolean(3));
        assertTrue(converted.get(4).isNull());
    }

    @Test
    public void testComplexNestedStructureConversion() {
        // Given: 복잡한 중첩 구조 생성
        ObjectContainer ecommerce = createEcommerceData();

        // When: 모든 라이브러리로 순차적으로 변환
        ObjectContainer simpleVersion = ecommerce.convertTo(JsonLibrary.SIMPLE);
        ObjectContainer jacksonVersion = simpleVersion.convertTo(JsonLibrary.JACKSON);
        ObjectContainer gsonVersion = jacksonVersion.convertTo(JsonLibrary.GSON);
        ObjectContainer fastjsonVersion = gsonVersion.convertTo(JsonLibrary.FASTJSON2);
        ObjectContainer orgjsonVersion = fastjsonVersion.convertTo(JsonLibrary.ORG_JSON);
        ObjectContainer json5Version = orgjsonVersion.convertTo(JsonLibrary.JSON5);

        // Then: 최종 변환 결과가 원본과 동일한지 검증
        verifyEcommerceData(json5Version);
    }

    @Test
    public void testSnsDataConversion() {
        // Given: SNS 피드 데이터 생성
        ObjectContainer snsData = createSnsData();

        // When: 여러 라이브러리로 변환
        for (JsonLibrary library : JsonLibrary.values()) {
            ObjectContainer converted = snsData.convertTo(library);
            
            // Then: 변환된 데이터 검증
            verifySnsData(converted);
            assertEquals(Jsn4j.getContainerFactory(library), converted.getContainerFactory());
        }
    }

    @Test
    public void testGeoDataConversion() {
        // Given: 지리정보 데이터 생성
        ObjectContainer geoData = createGeoData();

        // When & Then: 각 라이브러리로 변환하며 검증
        for (JsonLibrary library : JsonLibrary.values()) {
            ObjectContainer converted = geoData.convertTo(library);
            verifyGeoData(converted);
        }
    }

    @Test
    public void testSpecialCharactersAndUnicode() {
        // Given: 특수 문자와 유니코드를 포함한 데이터
        ObjectContainer original = Jsn4j.newObject()
                .put("korean", "한글 테스트")
                .put("japanese", "日本語テスト")
                .put("emoji", "🎉🎊✨")
                .put("special", "\"quotes\" and \\backslash\\ and /slash/")
                .put("control", "line\nfeed\ttab\rreturn")
                .put("unicode", "\u0048\u0065\u006C\u006C\u006F"); // Hello

        // When & Then: 모든 라이브러리로 변환 테스트
        for (JsonLibrary library : JsonLibrary.values()) {
            ObjectContainer converted = original.convertTo(library);
            
            assertEquals("한글 테스트", converted.getString("korean"));
            assertEquals("日本語テスト", converted.getString("japanese"));
            assertEquals("🎉🎊✨", converted.getString("emoji"));
            assertEquals("\"quotes\" and \\backslash\\ and /slash/", converted.getString("special"));
            assertEquals("line\nfeed\ttab\rreturn", converted.getString("control"));
            assertEquals("Hello", converted.getString("unicode"));
        }
    }

    @Test
    public void testLargeNumbersAndPrecision() {
        // Given: 큰 숫자와 정밀도 테스트 데이터
        ObjectContainer original = Jsn4j.newObject()
                .put("maxLong", Long.MAX_VALUE)
                .put("minLong", Long.MIN_VALUE)
                .put("maxDouble", Double.MAX_VALUE)
                .put("minDouble", Double.MIN_VALUE)
                .put("pi", Math.PI)
                .put("e", Math.E)
                .put("verySmall", 0.000000000000001)
                .put("veryLarge", 999999999999999.0);

        // When & Then: 변환 후 정밀도 유지 확인
        for (JsonLibrary library : JsonLibrary.values()) {
            ObjectContainer converted = original.convertTo(library);
            
            assertEquals(Long.MAX_VALUE, converted.getLong("maxLong"));
            assertEquals(Long.MIN_VALUE, converted.getLong("minLong"));
            // Double의 경우 라이브러리마다 처리가 다를 수 있으므로 근사값 비교
            assertEquals(Math.PI, converted.getDouble("pi"), 0.0000000000001);
            assertEquals(Math.E, converted.getDouble("e"), 0.0000000000001);
        }
    }

    @Test
    public void testByteArrayConversion() {
        // Given: 바이트 배열 데이터
        byte[] imageData = new byte[]{0x00, 0x01, 0x02, 0x03, (byte)0xFF, (byte)0xFE};
        ObjectContainer original = Jsn4j.newObject()
                .put("image", imageData)
                .put("description", "Test image data");

        // When & Then: Base64 인코딩이 유지되는지 확인
        for (JsonLibrary library : JsonLibrary.values()) {
            ObjectContainer converted = original.convertTo(library);
            
            byte[] convertedData = converted.getByteArray("image");
            assertArrayEquals(imageData, convertedData);
            assertEquals("Test image data", converted.getString("description"));
        }
    }

    @Test
    public void testSelfConversion() {
        // Given: 각 라이브러리로 객체 생성
        for (JsonLibrary library : JsonLibrary.values()) {
            ContainerFactory factory = Jsn4j.getContainerFactory(library);
            ObjectContainer original = factory.newObject()
                    .put("library", library.name())
                    .put("test", "self conversion");

            // When: 같은 라이브러리로 변환
            ObjectContainer converted = original.convertTo(library);

            // Then: 같은 인스턴스여야 함 (최적화)
            assertSame(original, converted);
        }
    }

    @Test
    public void testDeepNestingLevels() {
        // Given: 10단계 이상 깊이 중첩된 구조
        ObjectContainer deepNested = createDeepNestedStructure(15);

        // When: 변환
        for (JsonLibrary library : JsonLibrary.values()) {
            ObjectContainer converted = deepNested.convertTo(library);
            
            // Then: 깊이 검증
            verifyDeepNestedStructure(converted, 15);
        }
    }

    @Test
    public void testMixedTypeArrays() {
        // Given: 다양한 타입이 섞인 배열
        ArrayContainer mixedArray = Jsn4j.newArray()
                .put("string")
                .put(123)
                .put(45.67)
                .put(true)
                .put(null)
                .put(Jsn4j.newObject().put("nested", "object"))
                .put(Jsn4j.newArray().put("nested").put("array"))
                .put(new byte[]{1, 2, 3}); // Base64로 인코딩될 것

        // When & Then: 모든 라이브러리로 변환 테스트
        for (JsonLibrary library : JsonLibrary.values()) {
            ArrayContainer converted = mixedArray.convertTo(library);
            
            assertEquals(8, converted.size());
            assertEquals("string", converted.getString(0));
            assertEquals(123, converted.getInt(1));
            assertEquals(45.67, converted.getDouble(2), 0.001);
            assertTrue(converted.getBoolean(3));
            assertTrue(converted.get(4).isNull());
            assertEquals("object", converted.get(5).asObject().getString("nested"));
            assertEquals("nested", converted.get(6).asArray().getString(0));
            assertArrayEquals(new byte[]{1, 2, 3}, converted.getByteArray(7));
        }
    }

    // 헬퍼 메서드들

    private ObjectContainer createEcommerceData() {
        return Jsn4j.newObject()
                .put("orderId", "ORD-2024-001234")
                .put("customer", Jsn4j.newObject()
                        .put("id", 98765)
                        .put("name", "김철수")
                        .put("email", "chulsoo@example.com")
                        .put("addresses", Jsn4j.newArray()
                                .put(Jsn4j.newObject()
                                        .put("type", "billing")
                                        .put("street", "서울시 강남구 테헤란로 123")
                                        .put("isDefault", true))
                                .put(Jsn4j.newObject()
                                        .put("type", "shipping")
                                        .put("street", "서울시 서초구 서초대로 456"))))
                .put("items", Jsn4j.newArray()
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-001")
                                .put("name", "노트북 Pro 15")
                                .put("quantity", 1)
                                .put("unitPrice", 1850000))
                        .put(Jsn4j.newObject()
                                .put("productId", "PROD-002")
                                .put("name", "무선 마우스")
                                .put("quantity", 2)
                                .put("unitPrice", 45000)));
    }

    private void verifyEcommerceData(ObjectContainer data) {
        assertEquals("ORD-2024-001234", data.getString("orderId"));
        
        ObjectContainer customer = data.get("customer").asObject();
        assertEquals(98765, customer.getInt("id"));
        assertEquals("김철수", customer.getString("name"));
        
        ArrayContainer addresses = customer.get("addresses").asArray();
        assertEquals(2, addresses.size());
        assertEquals("billing", addresses.get(0).asObject().getString("type"));
        
        ArrayContainer items = data.get("items").asArray();
        assertEquals(2, items.size());
        assertEquals("노트북 Pro 15", items.get(0).asObject().getString("name"));
    }

    private ObjectContainer createSnsData() {
        return Jsn4j.newObject()
                .put("feedId", "feed-2024-001")
                .put("posts", Jsn4j.newArray()
                        .put(Jsn4j.newObject()
                                .put("postId", "post-123456")
                                .put("author", Jsn4j.newObject()
                                        .put("userId", "user-789")
                                        .put("username", "tech_lover")
                                        .put("verified", true))
                                .put("content", Jsn4j.newObject()
                                        .put("text", "새로운 JSN4J 라이브러리 정말 편리하네요! 🎉")
                                        .put("hashtags", Jsn4j.newArray()
                                                .put("#JSN4J")
                                                .put("#Java")
                                                .put("#JSON")))
                                .put("engagement", Jsn4j.newObject()
                                        .put("likes", 1542)
                                        .put("comments", 89)
                                        .put("shares", 234))));
    }

    private void verifySnsData(ObjectContainer data) {
        assertEquals("feed-2024-001", data.getString("feedId"));
        
        ArrayContainer posts = data.get("posts").asArray();
        assertFalse(posts.isEmpty());
        
        ObjectContainer firstPost = posts.get(0).asObject();
        assertEquals("post-123456", firstPost.getString("postId"));
        
        ObjectContainer author = firstPost.get("author").asObject();
        assertTrue(author.getBoolean("verified"));
        
        ObjectContainer engagement = firstPost.get("engagement").asObject();
        assertEquals(1542, engagement.getInt("likes"));
    }

    private ObjectContainer createGeoData() {
        return Jsn4j.newObject()
                .put("mapData", Jsn4j.newObject()
                        .put("region", "서울특별시")
                        .put("districts", Jsn4j.newArray()
                                .put(Jsn4j.newObject()
                                        .put("name", "강남구")
                                        .put("population", 545000)
                                        .put("area", 39.5)
                                        .put("boundaries", Jsn4j.newObject()
                                                .put("type", "Polygon")
                                                .put("coordinates", Jsn4j.newArray()
                                                        .put(Jsn4j.newArray()
                                                                .put(Jsn4j.newArray().put(127.0164).put(37.5172))
                                                                .put(Jsn4j.newArray().put(127.0832).put(37.5172))))))));
    }

    private void verifyGeoData(ObjectContainer data) {
        ObjectContainer mapData = data.get("mapData").asObject();
        assertEquals("서울특별시", mapData.getString("region"));
        
        ArrayContainer districts = mapData.get("districts").asArray();
        assertFalse(districts.isEmpty());
        
        ObjectContainer district = districts.get(0).asObject();
        assertEquals("강남구", district.getString("name"));
        assertEquals(545000, district.getInt("population"));
        
        ObjectContainer boundaries = district.get("boundaries").asObject();
        assertEquals("Polygon", boundaries.getString("type"));
    }

    private ObjectContainer createDeepNestedStructure(int depth) {
        ObjectContainer root = Jsn4j.newObject().put("level", 0);
        ObjectContainer current = root;
        
        for (int i = 1; i <= depth; i++) {
            ObjectContainer next = Jsn4j.newObject()
                    .put("level", i)
                    .put("data", "Level " + i + " data");
            current.put("nested", next);
            current = next;
        }
        
        return root;
    }

    private void verifyDeepNestedStructure(ObjectContainer obj, int expectedDepth) {
        ObjectContainer current = obj;
        
        for (int i = 0; i <= expectedDepth; i++) {
            assertNotNull(current);
            assertEquals(i, current.getInt("level"));
            
            if (i < expectedDepth) {
                current = current.get("nested").asObject();
            }
        }
    }
}