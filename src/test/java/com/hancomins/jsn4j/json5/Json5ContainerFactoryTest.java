package com.hancomins.jsn4j.json5;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.jsn4j.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Json5ContainerFactoryTest {
    
    @Test
    public void testGetInstance() {
        Json5ContainerFactory factory = Json5ContainerFactory.getInstance();
        assertNotNull(factory);
        assertEquals("json5", factory.getJsn4jModuleName());
    }
    
    @Test
    public void testNewObject() {
        Json5ContainerFactory factory = Json5ContainerFactory.getInstance();
        ObjectContainer obj = factory.newObject();
        assertNotNull(obj);
        assertTrue(obj instanceof Json5Object);
        assertEquals(0, obj.size());
    }
    
    @Test
    public void testNewArray() {
        Json5ContainerFactory factory = Json5ContainerFactory.getInstance();
        ArrayContainer arr = factory.newArray();
        assertNotNull(arr);
        assertTrue(arr instanceof Json5Array);
        assertEquals(0, arr.size());
    }
    
    @Test
    public void testWrapJSON5Object() {
        JSON5Object json5Obj = new JSON5Object();
        json5Obj.put("name", "test");
        json5Obj.put("value", 123);
        
        ContainerValue wrapped = Json5ContainerFactory.wrap(json5Obj);
        assertNotNull(wrapped);
        assertTrue(wrapped.isObject());
        assertEquals("test", wrapped.asObject().getString("name"));
        assertEquals(123, wrapped.asObject().getInt("value"));
    }
    
    @Test
    public void testWrapJSON5Array() {
        JSON5Array json5Arr = new JSON5Array();
        json5Arr.add("test");
        json5Arr.add(123);
        json5Arr.add(true);
        
        ContainerValue wrapped = Json5ContainerFactory.wrap(json5Arr);
        assertNotNull(wrapped);
        assertTrue(wrapped.isArray());
        assertEquals(3, wrapped.asArray().size());
        assertEquals("test", wrapped.asArray().getString(0));
        assertEquals(123, wrapped.asArray().getInt(1));
        assertTrue(wrapped.asArray().getBoolean(2));
    }
    
    @Test
    public void testWrapPrimitives() {
        assertTrue(Json5ContainerFactory.wrap("test").isPrimitive());
        assertTrue(Json5ContainerFactory.wrap(123).isPrimitive());
        assertTrue(Json5ContainerFactory.wrap(45.67).isPrimitive());
        assertTrue(Json5ContainerFactory.wrap(true).isPrimitive());
        assertTrue(Json5ContainerFactory.wrap(null).isNull());
    }
    
    @Test
    public void testJson5ObjectOperations() {
        ObjectContainer obj = Json5ContainerFactory.getInstance().newObject();
        
        // Test put operations
        obj.put("string", "value");
        obj.put("number", 42);
        obj.put("boolean", true);
        obj.put("null", null);
        
        // Test get operations
        assertEquals("value", obj.getString("string"));
        assertEquals(42, obj.getInt("number"));
        assertTrue(obj.getBoolean("boolean"));
        assertTrue(obj.get("null").isNull());
        
        // Test size and containsKey
        assertEquals(4, obj.size());
        assertTrue(obj.containsKey("string"));
        assertFalse(obj.containsKey("notexist"));
    }
    
    @Test
    public void testJson5ArrayOperations() {
        ArrayContainer arr = Json5ContainerFactory.getInstance().newArray();
        
        // Test add operations
        arr.put("string");
        arr.put(123);
        arr.put(true);
        arr.put((Object) null);
        
        // Test get operations
        assertEquals("string", arr.getString(0));
        assertEquals(123, arr.getInt(1));
        assertTrue(arr.getBoolean(2));
        assertTrue(arr.get(3).isNull());
        
        // Test size
        assertEquals(4, arr.size());
    }
    
    @Test
    public void testJson5Writer() throws IOException {
        ObjectContainer obj = Json5ContainerFactory.getInstance().newObject();
        obj.put("name", "JSON5");
        obj.put("version", "1.1.1");
        obj.put("features", obj.getContainerFactory().newArray()
            .put("trailing commas")
            .put("single quotes")
            .put("comments"));
        
        // Test write to string
        String json = obj.getWriter().write();
        assertNotNull(json);
        assertTrue(json.contains("JSON5"));
        assertTrue(json.contains("1.1.1"));
        
        // Test write to stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        obj.getWriter().write(baos);
        String streamJson = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(json, streamJson);
    }
    
    @Test
    public void testJson5WriterPrettyPrint() {
        ObjectContainer obj = Json5ContainerFactory.getInstance().newObject();
        obj.put("name", "test");
        obj.put("nested", obj.getContainerFactory().newObject()
            .put("value", 123));
        
        // Enable pretty print
        obj.getWriter().putOption("PRETTY_PRINT", true);
        String json = obj.getWriter().write();
        
        // Pretty printed JSON should contain newlines and indentation
        assertTrue(json.contains("\n") || json.contains("  "));
    }
    
    @Test
    public void testJson5Parser() {
        String json5String = "{name: 'test', value: 123, enabled: true}";
        
        ContainerParser parser = Json5ContainerFactory.getInstance().getParser();
        ContainerValue parsed = parser.parse(json5String);
        
        assertNotNull(parsed);
        assertTrue(parsed.isObject());
        assertEquals("test", parsed.asObject().getString("name"));
        assertEquals(123, parsed.asObject().getInt("value"));
        assertTrue(parsed.asObject().getBoolean("enabled"));
    }
    
    @Test
    public void testCrossImplementationCompatibility() {
        // Create with simple implementation
        ObjectContainer simpleObj = Jsn4j.newObject();
        simpleObj.put("type", "simple");
        simpleObj.put("data", Jsn4j.newArray().put(1).put(2).put(3));
        
        // Convert to JSON5
        ObjectContainer json5Obj = Json5ContainerFactory.getInstance().newObject();
        json5Obj.put("converted", simpleObj);
        
        // Verify
        ContainerValue converted = json5Obj.get("converted");
        assertTrue(converted.isObject());
        assertEquals("simple", converted.asObject().getString("type"));
        assertTrue(converted.asObject().get("data").isArray());
        assertEquals(3, converted.asObject().get("data").asArray().size());
    }
}