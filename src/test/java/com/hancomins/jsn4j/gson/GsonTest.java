package com.hancomins.jsn4j.gson;

import com.hancomins.jsn4j.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GsonTest {
    
    private ContainerFactory factory;
    
    @BeforeEach
    public void setUp() {
        factory = GsonContainerFactory.getInstance();
        // Register factory for cross-implementation tests
        Jsn4j.registerContainerFactory(factory);
    }
    
    @Test
    public void testObjectCreation() {
        ObjectContainer obj = factory.newObject();
        assertNotNull(obj);
        assertTrue(obj instanceof GsonObject);
        assertEquals(0, obj.size());
    }
    
    @Test
    public void testArrayCreation() {
        ArrayContainer arr = factory.newArray();
        assertNotNull(arr);
        assertTrue(arr instanceof GsonArray);
        assertEquals(0, arr.size());
    }
    
    @Test
    public void testObjectPutAndGet() {
        ObjectContainer obj = factory.newObject();
        
        obj.put("string", "test");
        obj.put("number", 42);
        obj.put("boolean", true);
        obj.put("null", null);
        
        assertEquals("test", obj.getString("string"));
        assertEquals(42, obj.getInt("number"));
        assertTrue(obj.getBoolean("boolean"));
        assertTrue(obj.get("null").isNull());
        assertEquals(4, obj.size());
    }
    
    @Test
    public void testArrayPutAndGet() {
        ArrayContainer arr = factory.newArray();
        
        arr.put("test");
        arr.put(42);
        arr.put(true);
        arr.put(null);
        
        assertEquals("test", arr.getString(0));
        assertEquals(42, arr.getInt(1));
        assertTrue(arr.getBoolean(2));
        assertTrue(arr.get(3).isNull());
        assertEquals(4, arr.size());
    }
    
    @Test
    public void testNestedStructures() {
        ObjectContainer root = factory.newObject();
        
        ObjectContainer nested = root.newAndPutObject("nested");
        nested.put("value", "test");
        
        ArrayContainer array = root.newAndPutArray("array");
        array.put(1).put(2).put(3);
        
        assertEquals("test", root.get("nested").asObject().getString("value"));
        assertEquals(3, root.get("array").asArray().size());
    }
    
    @Test
    public void testJsonParsing() {
        String json = "{\"name\":\"Gson\",\"version\":2.8,\"features\":[\"fast\",\"simple\"]}";
        ContainerValue parsed = factory.getParser().parse(json);
        
        assertTrue(parsed.isObject());
        ObjectContainer obj = parsed.asObject();
        assertEquals("Gson", obj.getString("name"));
        assertEquals(2.8, obj.getDouble("version"), 0.001);
        
        ArrayContainer features = obj.get("features").asArray();
        assertEquals(2, features.size());
        assertEquals("fast", features.getString(0));
        assertEquals("simple", features.getString(1));
    }
    
    @Test
    public void testJsonWriting() {
        ObjectContainer obj = factory.newObject();
        obj.put("name", "test");
        obj.put("value", 123);
        
        String json = obj.getWriter().write();
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":123"));
    }
    
    @Test
    public void testPrettyPrint() {
        ObjectContainer obj = factory.newObject();
        obj.put("name", "test");
        obj.put("nested", factory.newObject().put("key", "value"));
        
        GsonWriter writer = (GsonWriter) obj.getWriter();
        writer.enable(GsonWriteOption.PRETTY_PRINT);
        
        String json = writer.write();
        assertTrue(json.contains("\n")); // Pretty print adds newlines
        assertTrue(json.contains("  ")); // Pretty print adds indentation
    }
    
    @Test
    public void testMapAndCollectionSupport() {
        ObjectContainer obj = factory.newObject();
        
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 123);
        
        obj.put("map", map);
        obj.put("list", Arrays.asList("a", "b", "c"));
        
        ObjectContainer mapObj = obj.get("map").asObject();
        assertEquals("value1", mapObj.getString("key1"));
        assertEquals(123, mapObj.getInt("key2"));
        
        ArrayContainer listArr = obj.get("list").asArray();
        assertEquals(3, listArr.size());
        assertEquals("a", listArr.getString(0));
    }
    
    @Test
    public void testByteArrayHandling() {
        ObjectContainer obj = factory.newObject();
        byte[] data = {1, 2, 3, 4, 5};
        
        obj.put("data", data);
        
        // Gson serializes byte arrays as base64
        String json = obj.getWriter().write();
        // Gson may escape the = sign as \u003d
        assertTrue(json.contains("AQIDBAU=") || json.contains("AQIDBAU\\u003d")); // Base64 of [1,2,3,4,5]
    }
    
    @Test
    public void testCrossImplementationCompatibility() {
        // Create with Gson
        ObjectContainer gsonObj = factory.newObject();
        gsonObj.put("source", "gson");
        gsonObj.put("value", 42);
        
        // Convert to simple implementation
        ObjectContainer simpleObj = Jsn4j.newObject();
        for (Map.Entry<String, ContainerValue> entry : gsonObj.entrySet()) {
            simpleObj.put(entry.getKey(), entry.getValue());
        }
        
        // Verify data is preserved
        assertEquals("gson", simpleObj.getString("source"));
        assertEquals(42, simpleObj.getInt("value"));
    }
}