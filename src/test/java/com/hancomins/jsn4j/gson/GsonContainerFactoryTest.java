package com.hancomins.jsn4j.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hancomins.jsn4j.*;
import com.hancomins.jsn4j.simple.SimpleJsonContainerFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class GsonContainerFactoryTest {

    @Test
    public void testSingleton() {
        GsonContainerFactory factory1 = GsonContainerFactory.getInstance();
        GsonContainerFactory factory2 = GsonContainerFactory.getInstance();
        assertSame(factory1, factory2);
    }

    @Test
    public void testNewObject() {
        GsonContainerFactory factory = GsonContainerFactory.getInstance();
        ObjectContainer obj = factory.newObject();
        assertNotNull(obj);
        assertTrue(obj instanceof GsonObject);
    }

    @Test
    public void testNewArray() {
        GsonContainerFactory factory = GsonContainerFactory.getInstance();
        ArrayContainer arr = factory.newArray();
        assertNotNull(arr);
        assertTrue(arr instanceof GsonArray);
    }

    @Test
    public void testWrapJsonObject() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("name", "test");
        jsonObj.addProperty("value", 123);
        
        ContainerValue wrapped = GsonContainerFactory.wrap(jsonObj);
        assertNotNull(wrapped);
        assertTrue(wrapped.isObject());
        assertEquals("test", wrapped.asObject().getString("name"));
        assertEquals(123, wrapped.asObject().getInt("value"));
    }

    @Test
    public void testWrapJsonArray() {
        JsonArray jsonArr = new JsonArray();
        jsonArr.add("item1");
        jsonArr.add(42);
        jsonArr.add(true);
        
        ContainerValue wrapped = GsonContainerFactory.wrap(jsonArr);
        assertNotNull(wrapped);
        assertTrue(wrapped.isArray());
        assertEquals(3, wrapped.asArray().size());
        assertEquals("item1", wrapped.asArray().getString(0));
        assertEquals(42, wrapped.asArray().getInt(1));
        assertEquals(true, wrapped.asArray().getBoolean(2));
    }

    @Test
    public void testWrapPrimitives() {
        // wrap() only accepts JsonElement, not primitives directly
        JsonObject obj = new JsonObject();
        obj.addProperty("test", "value");
        assertTrue(GsonContainerFactory.wrap(obj).isObject());
        
        assertTrue(GsonContainerFactory.wrap(com.google.gson.JsonNull.INSTANCE).isNull());
    }

    @Test
    public void testComplexObject() {
        ObjectContainer obj = GsonContainerFactory.getInstance().newObject();
        obj.put("name", "Test Object");
        obj.put("version", 1.0);
        obj.put("active", true);
        
        ArrayContainer arr = GsonContainerFactory.getInstance().newArray();
        arr.put("item1");
        arr.put(100);
        arr.put(false);
        
        obj.put("items", arr);
        
        ObjectContainer nested = GsonContainerFactory.getInstance().newObject();
        nested.put("key", "value");
        obj.put("nested", nested);
        
        // Verify structure
        assertEquals("Test Object", obj.getString("name"));
        assertEquals(1.0, obj.getDouble("version"));
        assertTrue(obj.getBoolean("active"));
        
        ContainerValue items = obj.get("items");
        assertTrue(items.isArray());
        assertEquals(3, items.asArray().size());
        
        ContainerValue nestedValue = obj.get("nested");
        assertTrue(nestedValue.isObject());
        assertEquals("value", nestedValue.asObject().getString("key"));
    }

    @Test
    public void testArrayOperations() {
        ArrayContainer arr = GsonContainerFactory.getInstance().newArray();
        
        arr.put("string");
        arr.put(123);
        arr.put(45.67);
        arr.put(true);
        arr.put((Object) null);
        
        assertEquals(5, arr.size());
        assertEquals("string", arr.getString(0));
        assertEquals(123, arr.getInt(1));
        assertEquals(45.67, arr.getDouble(2));
        assertTrue(arr.getBoolean(3));
        assertTrue(arr.get(4).isNull());
    }

    @Test
    public void testToJsonString() {
        ObjectContainer obj = GsonContainerFactory.getInstance().newObject();
        obj.put("message", "Hello, Gson!");
        obj.put("number", 42);
        obj.put("active", true);
        
        String json = obj.getWriter().write();
        assertNotNull(json);
        assertTrue(json.contains("\"message\":\"Hello, Gson!\""));
        assertTrue(json.contains("\"number\":42"));
        assertTrue(json.contains("\"active\":true"));
    }

    @Test
    public void testWriter() {
        ObjectContainer obj = GsonContainerFactory.getInstance().newObject();
        obj.put("test", "value");
        
        ContainerWriter<? extends Enum<?>> writer = obj.getWriter();
        assertTrue(writer instanceof GsonWriter);
        
        String json = writer.write();
        assertNotNull(json);
        assertTrue(json.contains("\"test\":\"value\""));
    }

    @Test
    public void testParser() {
        ContainerParser parser = GsonContainerFactory.getInstance().getParser();
        assertNotNull(parser);
        
        // Test parsing from string
        String json = "{\"name\":\"test\",\"value\":123,\"array\":[1,2,3]}";
        ContainerValue parsed = parser.parse(json);
        assertNotNull(parsed);
        assertTrue(parsed.isObject());
        assertEquals("test", parsed.asObject().getString("name"));
        assertEquals(123, parsed.asObject().getInt("value"));
        
        ContainerValue array = parsed.asObject().get("array");
        assertTrue(array.isArray());
        assertEquals(3, array.asArray().size());
        
        // Test parsing from Reader
        StringReader reader = new StringReader(json);
        ContainerValue parsedFromReader = parser.parse(reader);
        assertNotNull(parsedFromReader);
        assertTrue(parsedFromReader.isObject());
        
        // Test parsing from InputStream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
        ContainerValue parsedFromStream = parser.parse(inputStream);
        assertNotNull(parsedFromStream);
        assertTrue(parsedFromStream.isObject());
    }

    @Test
    public void testCrossImplementationCompatibility() {
        // Create Gson object with nested structures
        ObjectContainer gsonObj = GsonContainerFactory.getInstance().newObject();
        gsonObj.put("type", "gson");
        
        // Add array from SimpleJson
        ArrayContainer simpleArr = SimpleJsonContainerFactory.getInstance().newArray();
        simpleArr.put("from simple");
        simpleArr.put(999);
        
        gsonObj.put("simpleArray", simpleArr);
        
        // Convert to JSON and back
        String json = gsonObj.getWriter().write();
        assertNotNull(json);
        
        // Parse with Gson parser
        ContainerValue parsed = GsonContainerFactory.getInstance().getParser().parse(json);
        assertTrue(parsed.isObject());
        assertEquals("gson", parsed.asObject().getString("type"));
        
        ContainerValue parsedArray = parsed.asObject().get("simpleArray");
        assertTrue(parsedArray.isArray());
        assertEquals("from simple", parsedArray.asArray().getString(0));
        assertEquals(999, parsedArray.asArray().getInt(1));
    }
}