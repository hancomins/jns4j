package com.hancomins.jsn4j.orgjson;

import org.json.JSONArray;
import org.json.JSONObject;
import com.hancomins.jsn4j.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OrgJsonContainerFactoryTest {
    
    private OrgJsonContainerFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = OrgJsonContainerFactory.getInstance();
        Jsn4j.registerContainerFactory(factory);
    }
    
    @Test
    void testFactoryCreation() {
        assertNotNull(factory);
        assertEquals("orgjson", factory.getJsn4jModuleName());
        assertNotNull(factory.getParser());
    }
    
    @Test
    void testNewObject() {
        ObjectContainer obj = factory.newObject();
        assertNotNull(obj);
        assertTrue(obj instanceof OrgJsonObject);
        assertEquals(0, obj.size());
    }
    
    @Test
    void testNewArray() {
        ArrayContainer arr = factory.newArray();
        assertNotNull(arr);
        assertTrue(arr instanceof OrgJsonArray);
        assertEquals(0, arr.size());
    }
    
    @Test
    void testWrapJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "John");
        jsonObject.put("age", 30);
        
        OrgJsonObject wrapped = OrgJsonObject.wrap(jsonObject);
        assertEquals("John", wrapped.getString("name"));
        assertEquals(30, wrapped.getInt("age"));
        assertSame(jsonObject, wrapped.getJSONObject());
    }
    
    @Test
    void testWrapJSONArray() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("item1");
        jsonArray.put(42);
        jsonArray.put(true);
        
        OrgJsonArray wrapped = OrgJsonArray.wrap(jsonArray);
        assertEquals(3, wrapped.size());
        assertEquals("item1", wrapped.getString(0));
        assertEquals(42, wrapped.getInt(1));
        assertTrue(wrapped.getBoolean(2));
        assertSame(jsonArray, wrapped.getJSONArray());
    }
    
    @Test
    void testWrapGenericValue() {
        // 객체
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "value");
        ContainerValue wrappedObj = OrgJsonContainerFactory.wrap(jsonObject);
        assertTrue(wrappedObj.isObject());
        
        // 배열
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(123);
        ContainerValue wrappedArr = OrgJsonContainerFactory.wrap(jsonArray);
        assertTrue(wrappedArr.isArray());
        
        // 원시 값들
        ContainerValue wrappedText = OrgJsonContainerFactory.wrap("hello");
        assertTrue(wrappedText.isPrimitive());
        assertEquals("hello", wrappedText.raw());
        
        ContainerValue wrappedNumber = OrgJsonContainerFactory.wrap(42);
        assertTrue(wrappedNumber.isPrimitive());
        assertEquals(42, wrappedNumber.raw());
        
        ContainerValue wrappedBool = OrgJsonContainerFactory.wrap(true);
        assertTrue(wrappedBool.isPrimitive());
        assertEquals(true, wrappedBool.raw());
        
        ContainerValue wrappedNull = OrgJsonContainerFactory.wrap(JSONObject.NULL);
        assertTrue(wrappedNull.isNull());
    }
    
    @Test
    void testComplexObjectOperations() {
        ObjectContainer obj = factory.newObject();
        
        // 기본 타입 추가
        obj.put("string", "Hello World");
        obj.put("integer", 42);
        obj.put("double", 3.14);
        obj.put("boolean", true);
        obj.put("nullValue", (Object) null);
        
        // 중첩 객체 추가
        ObjectContainer nested = obj.newAndPutObject("nested");
        nested.put("innerKey", "innerValue");
        
        // 배열 추가
        ArrayContainer array = obj.newAndPutArray("array");
        array.put(1).put(2).put(3);
        
        // 검증
        assertEquals(7, obj.size());
        assertEquals("Hello World", obj.getString("string"));
        assertEquals(42, obj.getInt("integer"));
        assertEquals(3.14, obj.getDouble("double"), 0.001);
        assertTrue(obj.getBoolean("boolean"));
        assertTrue(obj.get("nullValue").isNull());
        
        ObjectContainer retrievedNested = obj.getObject("nested");
        assertNotNull(retrievedNested);
        assertEquals("innerValue", retrievedNested.getString("innerKey"));
        
        ArrayContainer retrievedArray = obj.getArray("array");
        assertNotNull(retrievedArray);
        assertEquals(3, retrievedArray.size());
        assertEquals(1, retrievedArray.getInt(0));
    }
    
    @Test
    void testComplexArrayOperations() {
        ArrayContainer arr = factory.newArray();
        
        // 다양한 타입 추가
        arr.put("string");
        arr.put(123);
        arr.put(45.67);
        arr.put(true);
        arr.put((Object) null);
        
        // 중첩 객체 추가
        ObjectContainer nestedObj = arr.newAndPutObject();
        nestedObj.put("key", "value");
        
        // 중첩 배열 추가
        ArrayContainer nestedArr = arr.newAndPutArray();
        nestedArr.put("nested1").put("nested2");
        
        // 검증
        assertEquals(7, arr.size());
        assertEquals("string", arr.getString(0));
        assertEquals(123, arr.getInt(1));
        assertEquals(45.67, arr.getDouble(2), 0.001);
        assertTrue(arr.getBoolean(3));
        assertTrue(arr.get(4).isNull());
        
        ObjectContainer retrievedObj = arr.getObject(5);
        assertNotNull(retrievedObj);
        assertEquals("value", retrievedObj.getString("key"));
        
        ArrayContainer retrievedArr = arr.getArray(6);
        assertNotNull(retrievedArr);
        assertEquals(2, retrievedArr.size());
    }
    
    @Test
    void testParsingFromString() {
        String json = "{\"name\":\"John\",\"age\":30,\"active\":true,\"scores\":[85,90,95]}";
        
        ContainerValue parsed = factory.getParser().parse(json);
        assertTrue(parsed.isObject());
        
        ObjectContainer obj = parsed.asObject();
        assertEquals("John", obj.getString("name"));
        assertEquals(30, obj.getInt("age"));
        assertTrue(obj.getBoolean("active"));
        
        ArrayContainer scores = obj.getArray("scores");
        assertNotNull(scores);
        assertEquals(3, scores.size());
        assertEquals(85, scores.getInt(0));
        assertEquals(90, scores.getInt(1));
        assertEquals(95, scores.getInt(2));
    }
    
    @Test
    void testParsingFromReader() {
        String json = "[1,2,3,{\"key\":\"value\"}]";
        StringReader reader = new StringReader(json);
        
        ContainerValue parsed = factory.getParser().parse(reader);
        assertTrue(parsed.isArray());
        
        ArrayContainer arr = parsed.asArray();
        assertEquals(4, arr.size());
        assertEquals(1, arr.getInt(0));
        assertEquals(2, arr.getInt(1));
        assertEquals(3, arr.getInt(2));
        
        ObjectContainer obj = arr.getObject(3);
        assertNotNull(obj);
        assertEquals("value", obj.getString("key"));
    }
    
    @Test
    void testParsingFromInputStream() {
        String json = "{\"message\":\"Hello from InputStream\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
        
        ContainerValue parsed = factory.getParser().parse(inputStream);
        assertTrue(parsed.isObject());
        
        ObjectContainer obj = parsed.asObject();
        assertEquals("Hello from InputStream", obj.getString("message"));
    }
    
    @Test
    void testWriting() {
        ObjectContainer obj = factory.newObject();
        obj.put("name", "Test");
        obj.put("value", 123);
        
        ArrayContainer arr = obj.newAndPutArray("items");
        arr.put("a").put("b").put("c");
        
        String json = obj.toString();
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Test\""));
        assertTrue(json.contains("\"value\":123"));
        assertTrue(json.contains("\"items\":[\"a\",\"b\",\"c\"]"));
    }
    
    @Test
    void testPrettyPrint() {
        ObjectContainer obj = factory.newObject();
        obj.put("key", "value");
        obj.newAndPutObject("nested").put("inner", "data");
        
        obj.getWriter().enable("PRETTY_PRINT");
        String prettyJson = obj.toString();
        
        assertTrue(prettyJson.contains("\n"));
        assertTrue(prettyJson.contains("    ") || prettyJson.contains("\t"));
    }
    
    @Test
    void testInteroperabilityWithSimple() {
        // SimpleObject를 생성
        ObjectContainer simpleObj = Jsn4j.newObject();
        simpleObj.put("fromSimple", "value");
        
        // OrgJson 객체에 Simple 객체 추가
        OrgJsonObject orgJsonObj = (OrgJsonObject) factory.newObject();
        orgJsonObj.put("simple", simpleObj);
        
        // 검증
        ContainerValue retrieved = orgJsonObj.get("simple");
        assertTrue(retrieved.isObject());
        assertEquals("value", retrieved.asObject().getString("fromSimple"));
        
        // JSON 출력 검증
        String json = orgJsonObj.toString();
        assertTrue(json.contains("\"fromSimple\":\"value\""));
    }
    
    @Test
    void testCollectionAndMapConversion() {
        ObjectContainer obj = factory.newObject();
        
        // List 추가
        List<String> list = Arrays.asList("item1", "item2", "item3");
        obj.put("list", list);
        
        // Map 추가
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        obj.put("map", map);
        
        // 검증
        ArrayContainer retrievedList = obj.getArray("list");
        assertNotNull(retrievedList);
        assertEquals(3, retrievedList.size());
        assertEquals("item1", retrievedList.getString(0));
        
        ObjectContainer retrievedMap = obj.getObject("map");
        assertNotNull(retrievedMap);
        assertEquals("value1", retrievedMap.getString("key1"));
        assertEquals(42, retrievedMap.getInt("key2"));
    }
    
    @Test
    void testJsn4jIntegration() {
        // OrgJson 팩토리를 통한 파싱
        String json = "{\"type\":\"orgjson\",\"version\":3}";
        ContainerValue parsed = Jsn4j.parse("orgjson", json);
        
        assertTrue(parsed.isObject());
        assertEquals("orgjson", parsed.asObject().getString("type"));
        assertEquals(3, parsed.asObject().getInt("version"));
    }
    
    @Test
    void testArrayIndexOperations() {
        ArrayContainer arr = factory.newArray();
        
        // 인덱스를 통한 직접 설정
        arr.put(0, "first");
        arr.put(2, "third");  // 인덱스 1은 null로 채워짐
        
        assertEquals(3, arr.size());
        assertEquals("first", arr.getString(0));
        assertTrue(arr.get(1).isNull());
        assertEquals("third", arr.getString(2));
        
        // 기존 인덱스 덮어쓰기
        arr.put(1, "second");
        assertEquals("second", arr.getString(1));
    }
    
    @Test
    void testClearOperations() {
        // Object clear 테스트
        ObjectContainer obj = factory.newObject();
        obj.put("key1", "value1");
        obj.put("key2", "value2");
        assertEquals(2, obj.size());
        
        obj.clear();
        assertEquals(0, obj.size());
        assertFalse(obj.has("key1"));
        
        // Array clear 테스트
        ArrayContainer arr = factory.newArray();
        arr.put("item1").put("item2").put("item3");
        assertEquals(3, arr.size());
        
        arr.clear();
        assertEquals(0, arr.size());
    }
}