package com.hancomins.jsn4j.tool;

import com.hancomins.jsn4j.Jsn4j;
import com.hancomins.jsn4j.ObjectContainer;
import com.hancomins.jsn4j.ArrayContainer;
import com.hancomins.jsn4j.simple.SimpleJsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonStringWriterTest {
    
    @AfterEach
    public void tearDown() {
        // 각 테스트 후 캐시 클리어
        JsonObjectStringWriter.clearCache();
        JsonArrayStringWriter.clearCache();
    }
    
    @Test
    public void testJsonObjectStringWriter() {
        JsonObjectStringWriter writer = new JsonObjectStringWriter();
        
        String json = writer
            .put("name", "JSN4J")
            .put("version", 1.0)
            .put("active", true)
            .put("count", 42)
            .putNull("deprecated")
            .build();
        
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"JSN4J\""));
        assertTrue(json.contains("\"version\":1.0"));
        assertTrue(json.contains("\"active\":true"));
        assertTrue(json.contains("\"count\":42"));
        assertTrue(json.contains("\"deprecated\":null"));
    }
    
    @Test
    public void testJsonArrayStringWriter() {
        JsonArrayStringWriter writer = new JsonArrayStringWriter();
        
        String json = writer
            .put(1)
            .put(2.5)
            .put("text")
            .put(true)
            .putNull()
            .build();
        
        assertEquals("[1,2.5,\"text\",true,null]", json);
    }
    
    @Test
    public void testNestedStructures() {
        JsonObjectStringWriter writer = new JsonObjectStringWriter();

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");


        // 중첩된 배열
        JsonArrayStringWriter arrayWriter = new JsonArrayStringWriter();
        arrayWriter.put(1).put(2).put(3);
        
        String json = writer
            .put("array", Arrays.asList(1, 2, 3))
            .put("object", map)
            .build();
        
        assertNotNull(json);
        assertTrue(json.contains("\"array\":[1,2,3]"));
        assertTrue(json.contains("\"object\":{\"key\":\"value\"}"));
    }


    
    @Test
    public void testContainerValueIntegration() {
        ObjectContainer obj = Jsn4j.newObject();
        obj.put("nested", "value");
        
        ArrayContainer arr = Jsn4j.newArray();
        arr.put(1).put(2).put(3);
        
        JsonObjectStringWriter writer = new JsonObjectStringWriter();
        String json = writer
            .put("object", obj)
            .put("array", arr)
            .build();
        
        assertNotNull(json);
        assertTrue(json.contains("\"object\":{\"nested\":\"value\"}"));
        assertTrue(json.contains("\"array\":[1,2,3]"));
    }
    
    @Test
    public void testSpecialCharacters() {
        JsonObjectStringWriter writer = new JsonObjectStringWriter();
        
        String json = writer
            .put("quote", "He said \"Hello\"")
            .put("backslash", "C:\\Users\\test")
            .put("newline", "Line1\nLine2")
            .put("tab", "Col1\tCol2")
            .build();
        
        assertNotNull(json);
        assertTrue(json.contains("\\\""));
        assertTrue(json.contains("\\\\"));
        assertTrue(json.contains("\\n"));
        assertTrue(json.contains("\\t"));
    }
    
    @Test
    public void testEmptyAndSize() {
        JsonObjectStringWriter objWriter = new JsonObjectStringWriter();
        assertTrue(objWriter.isEmpty());
        assertEquals(0, objWriter.size());
        
        objWriter.put("key", "value");
        assertFalse(objWriter.isEmpty());
        assertEquals(1, objWriter.size());
        
        JsonArrayStringWriter arrWriter = new JsonArrayStringWriter();
        assertTrue(arrWriter.isEmpty());
        assertEquals(0, arrWriter.size());
        
        arrWriter.put(1);
        assertFalse(arrWriter.isEmpty());
        assertEquals(1, arrWriter.size());
    }
    
    @Test
    public void testReset() {
        JsonObjectStringWriter writer = new JsonObjectStringWriter();
        writer.put("key", "value");
        assertEquals(1, writer.size());
        
        writer.reset();
        assertEquals(0, writer.size());
        assertTrue(writer.isEmpty());
        
        String json = writer.put("new", "data").build();
        assertEquals("{\"new\":\"data\"}", json);
    }
    
    @Test
    public void testPutAll() {
        JsonObjectStringWriter objWriter = new JsonObjectStringWriter();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 123);
        map.put("key3", true);
        
        objWriter.putAll(map);
        assertEquals(3, objWriter.size());
        
        JsonArrayStringWriter arrWriter = new JsonArrayStringWriter();
        arrWriter.putAll(Arrays.asList(1, 2, 3));
        assertEquals(3, arrWriter.size());
        
        arrWriter.putAll(4, 5, 6);
        assertEquals(6, arrWriter.size());
    }
    
    @Test
    public void testByteArrayAsBase64() {
        byte[] data = "Hello World".getBytes();
        
        JsonObjectStringWriter writer = new JsonObjectStringWriter();
        String json = writer.put("data", data).build();
        
        assertNotNull(json);
        assertTrue(json.contains("SGVsbG8gV29ybGQ=")); // Base64 for "Hello World"
    }
    
    @Test
    public void testCacheUsage() {
        // 캐시 활성화 상태 확인
        assertTrue(StringBuilderCache.isCacheEnabled());
        
        // 여러 writer 생성 및 사용
        for (int i = 0; i < 5; i++) {
            JsonObjectStringWriter writer = new JsonObjectStringWriter();
            writer.put("index", i).build();
        }
        
        // 캐시 크기 확인 (정확한 크기는 구현에 따라 다를 수 있음)
        assertTrue(StringBuilderCache.getCacheSize() > 0);
        
        // 캐시 비활성화
        StringBuilderCache.setCacheEnabled(false);
        assertFalse(StringBuilderCache.isCacheEnabled());
        assertEquals(0, StringBuilderCache.getCacheSize());
        
        // 캐시 재활성화
        StringBuilderCache.setCacheEnabled(true);
        assertTrue(StringBuilderCache.isCacheEnabled());
    }
    
    @Test
    public void testComplexNestedStructure() {
        // 복잡한 중첩 구조를 하나의 Writer에서 생성
        String complexJson = new JsonObjectStringWriter()
            .put("user", new JsonObjectStringWriter()
                .put("id", 12345)
                .put("username", "testuser")
                .put("email", "test@example.com")
                .put("active", true))
            .put("addresses", new JsonArrayStringWriter()
                .put(new JsonObjectStringWriter()
                    .put("type", "home")
                    .put("street", "123 Main St")
                    .put("city", "Seoul")
                    .put("country", "Korea")
                    .put("postalCode", "12345"))
                .put(new JsonObjectStringWriter()
                    .put("type", "work")
                    .put("street", "456 Office Blvd")
                    .put("city", "Busan")
                    .put("country", "Korea")
                    .put("postalCode", "67890")))
            .put("hobbies", new JsonArrayStringWriter()
                .put("programming").put("reading").put("gaming"))
            .put("settings", new JsonObjectStringWriter()
                .put("theme", "dark")
                .put("language", "ko")
                .put("notifications", new JsonObjectStringWriter()
                    .put("email", true)
                    .put("push", false)
                    .put("sms", true)))
            .put("recentActivities", new JsonArrayStringWriter()
                .put(new JsonObjectStringWriter()
                    .put("action", "login")
                    .put("timestamp", "2024-01-01T10:00:00Z")
                    .put("ip", "192.168.1.1"))
                .put(new JsonObjectStringWriter()
                    .put("action", "update_profile")
                    .put("timestamp", "2024-01-01T11:30:00Z")
                    .put("changes", new JsonArrayStringWriter()
                        .put("email")
                        .put("phone")))
                .put(new JsonObjectStringWriter()
                    .put("action", "logout")
                    .put("timestamp", "2024-01-01T18:00:00Z")))
            .put("metadata", new JsonObjectStringWriter()
                .put("version", "1.0")
                .put("generated", new Date().toString()))
            .build();
        
        // JSON이 올바르게 생성되었는지 확인
        assertNotNull(complexJson);
        System.out.println("Generated JSON: " + complexJson);
        assertTrue(complexJson.contains("\"user\":{"));
        assertTrue(complexJson.contains("\"addresses\":["));
        assertTrue(complexJson.contains("\"hobbies\":["));
        assertTrue(complexJson.contains("\"settings\":{"));
        assertTrue(complexJson.contains("\"recentActivities\":["));
        
        // SimpleJsonParser로 파싱하여 구조 검증
        SimpleJsonParser parser = new SimpleJsonParser();
        ObjectContainer parsed = (ObjectContainer) parser.parse(complexJson);
        
        // 파싱된 데이터 검증
        assertNotNull(parsed);
        assertEquals(6, parsed.size()); // user, addresses, hobbies, settings, recentActivities, metadata
        
        // user 검증
        ObjectContainer user = parsed.get("user").asObject();
        assertEquals(12345, user.getInt("id"));
        assertEquals("testuser", user.getString("username"));
        assertEquals("test@example.com", user.getString("email"));
        assertTrue(user.getBoolean("active"));
        
        // addresses 검증
        ArrayContainer addresses = parsed.get("addresses").asArray();
        assertEquals(2, addresses.size());
        assertEquals("home", addresses.get(0).asObject().getString("type"));
        assertEquals("work", addresses.get(1).asObject().getString("type"));
        
        // hobbies 검증
        ArrayContainer hobbies = parsed.get("hobbies").asArray();
        assertEquals(3, hobbies.size());
        assertEquals("programming", hobbies.getString(0));
        assertEquals("reading", hobbies.getString(1));
        assertEquals("gaming", hobbies.getString(2));
        
        // settings 검증
        ObjectContainer settings = parsed.get("settings").asObject();
        assertEquals("dark", settings.getString("theme"));
        ObjectContainer notifications = settings.get("notifications").asObject();
        assertTrue(notifications.getBoolean("email"));
        assertFalse(notifications.getBoolean("push"));
        
        // recentActivities 검증
        ArrayContainer activities = parsed.get("recentActivities").asArray();
        assertEquals(3, activities.size());
        assertEquals("login", activities.get(0).asObject().getString("action"));
        
        // 두 번째 활동의 changes 배열 확인
        ObjectContainer secondActivity = activities.get(1).asObject();
        ArrayContainer changes = secondActivity.get("changes").asArray();
        assertEquals(2, changes.size());
        assertEquals("email", changes.getString(0));
        assertEquals("phone", changes.getString(1));
    }
    
    @Test
    public void testMultipleWritersCombination() {
        // 여러 개의 독립적인 Writer를 만들어서 조합하는 테스트
        
        // 모든 정보를 하나의 루트 객체로 조합
        String finalJson = new JsonObjectStringWriter()
            .put("store", new JsonObjectStringWriter()
                .put("name", "Tech Store")
                .put("location", "Seoul")
                .put("openTime", "09:00")
                .put("closeTime", "21:00"))
            .put("products", new JsonArrayStringWriter()
                .put(new JsonObjectStringWriter()
                    .put("id", "P001")
                    .put("name", "노트북")
                    .put("price", 1200000)
                    .put("stock", 15))
                .put(new JsonObjectStringWriter()
                    .put("id", "P002")
                    .put("name", "마우스")
                    .put("price", 35000)
                    .put("stock", 50))
                .put(new JsonObjectStringWriter()
                    .put("id", "P003")
                    .put("name", "키보드")
                    .put("price", 85000)
                    .put("stock", 30)))
            .put("categories", new JsonObjectStringWriter()
                .put("electronics", new JsonArrayStringWriter()
                    .put("computers")
                    .put("accessories")
                    .put("monitors"))
                .put("furniture", new JsonArrayStringWriter()
                    .put("desks")
                    .put("chairs")
                    .put("shelves")))
            .put("lastUpdated", System.currentTimeMillis())
            .build();
        
        // 결과 검증
        assertNotNull(finalJson);
        
        // 파싱하여 구조 검증
        SimpleJsonParser parser = new SimpleJsonParser();
        ObjectContainer parsed = (ObjectContainer) parser.parse(finalJson);
        
        // store 정보 검증
        ObjectContainer store = parsed.get("store").asObject();
        assertEquals("Tech Store", store.getString("name"));
        assertEquals("Seoul", store.getString("location"));
        
        // products 배열 검증
        ArrayContainer products = parsed.get("products").asArray();
        assertEquals(3, products.size());
        
        ObjectContainer firstProduct = products.get(0).asObject();
        assertEquals("P001", firstProduct.getString("id"));
        assertEquals("노트북", firstProduct.getString("name"));
        assertEquals(1200000, firstProduct.getInt("price"));
        
        // categories 검증
        ObjectContainer categories = parsed.get("categories").asObject();
        ArrayContainer electronics = categories.get("electronics").asArray();
        assertEquals(3, electronics.size());
        assertEquals("computers", electronics.getString(0));
        
        // lastUpdated 존재 확인
        assertTrue(parsed.has("lastUpdated"));
    }
    
    @Test
    public void testDeepNestingWithMixedTypes() {
        // 매우 깊은 중첩 구조 테스트
        JsonObjectStringWriter deepWriter = new JsonObjectStringWriter();
        
        deepWriter
            .put("level1", new JsonObjectStringWriter()
                .put("level2", new JsonArrayStringWriter()
                    .put(new JsonObjectStringWriter()
                        .put("level3", new JsonObjectStringWriter()
                            .put("level4", new JsonArrayStringWriter()
                                .put(new JsonObjectStringWriter()
                                    .put("level5", new JsonObjectStringWriter()
                                        .put("deepValue", "Found me!")
                                        .put("numbers", new JsonArrayStringWriter()
                                            .put(1).put(2).put(3).put(4).put(5))))
                                .put(42))))
                    .put("level2 sibling")))
            .put("metadata", new JsonObjectStringWriter()
                .put("depth", 5)
                .put("purpose", "testing deep nesting"));
        
        String deepJson = deepWriter.build();
        
        // 파싱하여 깊은 값 찾기
        SimpleJsonParser parser = new SimpleJsonParser();
        ObjectContainer parsed = (ObjectContainer) parser.parse(deepJson);
        
        // 깊은 중첩 구조 탐색
        ObjectContainer level1 = parsed.get("level1").asObject();
        ArrayContainer level2 = level1.get("level2").asArray();
        ObjectContainer level2First = level2.get(0).asObject();
        ObjectContainer level3 = level2First.get("level3").asObject();
        ArrayContainer level4 = level3.get("level4").asArray();
        ObjectContainer level4First = level4.get(0).asObject();
        ObjectContainer level5 = level4First.get("level5").asObject();
        
        // 깊은 값 검증
        assertEquals("Found me!", level5.getString("deepValue"));
        ArrayContainer numbers = level5.get("numbers").asArray();
        assertEquals(5, numbers.size());
        assertEquals(1, numbers.getInt(0));
        assertEquals(5, numbers.getInt(4));
        
        // 형제 값들 검증
        assertEquals("level2 sibling", level2.getString(1));
        assertEquals(42, level4.getInt(1));
        
        // 메타데이터 검증
        ObjectContainer metadata = parsed.get("metadata").asObject();
        assertEquals(5, metadata.getInt("depth"));
        assertEquals("testing deep nesting", metadata.getString("purpose"));
    }
}