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
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonLibrary enum과 관련된 기능 테스트
 */
public class JsonLibraryEnumTest {

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

    @Test
    public void testEnumValues() {
        // Given & When
        JsonLibrary[] libraries = JsonLibrary.values();

        // Then
        assertEquals(6, libraries.length);

        // 모든 enum 값 확인
        assertTrue(containsLibrary(libraries, JsonLibrary.SIMPLE));
        assertTrue(containsLibrary(libraries, JsonLibrary.JACKSON));
        assertTrue(containsLibrary(libraries, JsonLibrary.GSON));
        assertTrue(containsLibrary(libraries, JsonLibrary.FASTJSON2));
        assertTrue(containsLibrary(libraries, JsonLibrary.JSON5));
        assertTrue(containsLibrary(libraries, JsonLibrary.ORG_JSON));
    }

    @ParameterizedTest
    @EnumSource(JsonLibrary.class)
    public void testGetContainerFactory(JsonLibrary library) {
        // When
        ContainerFactory factory = Jsn4j.getContainerFactory(library);

        // Then
        assertNotNull(factory, "Factory should not be null for " + library);

        // 올바른 팩토리 타입인지 확인
        switch (library) {
            case SIMPLE:
                assertTrue(factory instanceof SimpleJsonContainerFactory);
                break;
            case JACKSON:
                assertTrue(factory instanceof JacksonContainerFactory);
                break;
            case GSON:
                assertTrue(factory instanceof GsonContainerFactory);
                break;
            case FASTJSON2:
                assertTrue(factory instanceof Fastjson2ContainerFactory);
                break;
            case JSON5:
                assertTrue(factory instanceof Json5ContainerFactory);
                break;
            case ORG_JSON:
                assertTrue(factory instanceof OrgJsonContainerFactory);
                break;
        }
    }

    @Test
    public void testGetContainerFactoryWithNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            Jsn4j.getContainerFactory(null);
        });
    }

    @ParameterizedTest
    @EnumSource(JsonLibrary.class)
    public void testSetDefaultContainerFactory(JsonLibrary library) {
        // Given: 현재 기본 팩토리 저장
        ContainerFactory originalDefault = Jsn4j.getDefaultContainerFactory();

        try {
            // When
            Jsn4j.setDefaultContainerFactory(library);

            // Then
            ContainerFactory currentDefault = Jsn4j.getDefaultContainerFactory();
            assertNotNull(currentDefault);
            assertEquals(Jsn4j.getContainerFactory(library), currentDefault);

            // 새로 생성되는 컨테이너가 올바른 팩토리를 사용하는지 확인
            ObjectContainer obj = Jsn4j.newObject();
            assertEquals(currentDefault, obj.getContainerFactory());

            ArrayContainer arr = Jsn4j.newArray();
            assertEquals(currentDefault, arr.getContainerFactory());
        } finally {
            // 원래 기본 팩토리로 복원
            Jsn4j.setDefaultContainerFactory(originalDefault);
        }
    }

    @Test
    public void testSetDefaultContainerFactoryWithNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            Jsn4j.setDefaultContainerFactory((JsonLibrary) null);
        });
    }

    @Test
    public void testFactoryConsistency() {
        // Given & When: 각 enum에 대해 팩토리를 여러 번 가져옴
        for (JsonLibrary library : JsonLibrary.values()) {
            ContainerFactory factory1 = Jsn4j.getContainerFactory(library);
            ContainerFactory factory2 = Jsn4j.getContainerFactory(library);

            // Then: 같은 인스턴스여야 함 (싱글톤)
            assertSame(factory1, factory2,
                "Factory instances should be the same for " + library);
        }
    }

    @Test
    public void testCreateContainersWithEnum() {
        // Given & When & Then: 각 라이브러리로 컨테이너 생성
        for (JsonLibrary library : JsonLibrary.values()) {
            ContainerFactory factory = Jsn4j.getContainerFactory(library);

            // 객체 생성
            ObjectContainer obj = factory.newObject()
                    .put("library", library.name())
                    .put("test", true);

            assertNotNull(obj);
            assertEquals(library.name(), obj.getString("library"));
            assertTrue(obj.getBoolean("test"));

            // 배열 생성
            ArrayContainer arr = factory.newArray()
                    .put(library.name())
                    .put(123)
                    .put(true);

            assertNotNull(arr);
            assertEquals(3, arr.size());
            assertEquals(library.name(), arr.getString(0));
        }
    }

    @Test
    public void testEnumStringConversion() {
        // Given
        String simpleStr = "SIMPLE";
        String jacksonStr = "JACKSON";

        // When
        JsonLibrary simple = JsonLibrary.valueOf(simpleStr);
        JsonLibrary jackson = JsonLibrary.valueOf(jacksonStr);

        // Then
        assertEquals(JsonLibrary.SIMPLE, simple);
        assertEquals(JsonLibrary.JACKSON, jackson);
        assertEquals(simpleStr, simple.toString());
        assertEquals(jacksonStr, jackson.toString());
    }

    @Test
    public void testInvalidEnumString() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            JsonLibrary.valueOf("INVALID_LIBRARY");
        });
    }

    @Test
    public void testParseWithDifferentLibraries() {
        // Given
        String jsonString = "{\"name\":\"test\",\"value\":123,\"active\":true}";

        // When & Then: 각 라이브러리로 파싱
        for (JsonLibrary library : JsonLibrary.values()) {
            ContainerFactory factory = Jsn4j.getContainerFactory(library);
            ContainerValue parsed = factory.getParser().parse(jsonString);

            assertNotNull(parsed);
            assertTrue(parsed.isObject());

            ObjectContainer obj = parsed.asObject();
            assertEquals("test", obj.getString("name"));
            assertEquals(123, obj.getInt("value"));
            assertTrue(obj.getBoolean("active"));
        }
    }

    @Test
    public void testWriteWithDifferentLibraries() {
        // Given
        ObjectContainer testObj = Jsn4j.newObject()
                .put("library", "test")
                .put("number", 42)
                .put("array", Jsn4j.newArray().put(1).put(2).put(3));

        // When & Then: 각 라이브러리로 변환하여 쓰기
        for (JsonLibrary library : JsonLibrary.values()) {
            ObjectContainer converted = testObj.convertTo(library);
            String json = converted.getWriter().write();


            assertNotNull(json);
            if (library == JsonLibrary.JSON5) {
                 json = converted.convertTo(JsonLibrary.SIMPLE).getWriter().write();

            }
                assertTrue(json.contains("\"library\""));
                assertTrue(json.contains("\"test\""));
                assertTrue(json.contains("42"));
                assertTrue(json.contains("[1,2,3]"));

        }


    }

    @Test
    public void testComplexScenarioWithEnum() {
        // Given: 복잡한 시나리오 - 여러 라이브러리를 순차적으로 사용
        String originalJson = "{\n" +
            "    \"project\": \"JSN4J\",\n" +
            "    \"features\": [\"abstraction\", \"multi-library\", \"performance\"],\n" +
            "    \"metadata\": {\n" +
            "        \"version\": \"1.0.0\",\n" +
            "        \"author\": \"Hancomins\",\n" +
            "        \"stats\": {\n" +
            "            \"libraries\": 6,\n" +
            "            \"tests\": 1000\n" +
            "        }\n" +
            "    }\n" +
            "}";

        // When: 각 라이브러리로 순환하며 파싱하고 변환
        ContainerValue current = Jsn4j.parse(originalJson);
        
        for (JsonLibrary library : JsonLibrary.values()) {
            // 현재 데이터를 해당 라이브러리로 변환
            current = current.asObject().convertTo(library);
            
            // 검증
            ObjectContainer obj = current.asObject();
            assertEquals("JSN4J", obj.getString("project"));
            
            ArrayContainer features = obj.get("features").asArray();
            assertEquals(3, features.size());
            
            ObjectContainer metadata = obj.get("metadata").asObject();
            assertEquals("1.0.0", metadata.getString("version"));
            
            ObjectContainer stats = metadata.get("stats").asObject();
            assertEquals(6, stats.getInt("libraries"));
        }
    }

    @Test
    public void testRuntimeLibrarySwitch() {
        // Given: 초기 설정
        ContainerFactory originalDefault = Jsn4j.getDefaultContainerFactory();
        
        try {
            // When & Then: 런타임에 기본 라이브러리 변경
            Jsn4j.setDefaultContainerFactory(JsonLibrary.SIMPLE);
            ObjectContainer simpleObj = Jsn4j.newObject().put("lib", "simple");
            assertTrue(simpleObj.getContainerFactory() instanceof SimpleJsonContainerFactory);
            
            Jsn4j.setDefaultContainerFactory(JsonLibrary.JACKSON);
            ObjectContainer jacksonObj = Jsn4j.newObject().put("lib", "jackson");
            assertTrue(jacksonObj.getContainerFactory() instanceof JacksonContainerFactory);
            
            Jsn4j.setDefaultContainerFactory(JsonLibrary.GSON);
            ObjectContainer gsonObj = Jsn4j.newObject().put("lib", "gson");
            assertTrue(gsonObj.getContainerFactory() instanceof GsonContainerFactory);
            
            // 이전에 생성된 객체들은 여전히 각자의 팩토리를 유지
            assertTrue(simpleObj.getContainerFactory() instanceof SimpleJsonContainerFactory);
            assertTrue(jacksonObj.getContainerFactory() instanceof JacksonContainerFactory);
        } finally {
            // 원래 설정으로 복원
            Jsn4j.setDefaultContainerFactory(originalDefault);
        }
    }

    // 헬퍼 메서드
    private boolean containsLibrary(JsonLibrary[] libraries, JsonLibrary target) {
        for (JsonLibrary lib : libraries) {
            if (lib == target) return true;
        }
        return false;
    }
}