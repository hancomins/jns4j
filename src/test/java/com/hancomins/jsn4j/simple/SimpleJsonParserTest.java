package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleJsonParserTest {


    private final String json = "{\n" +
            "  \"string\": \"hello \\n world\",\n" +
            "  \"number\": 12345,\n" +
            "  \"decimal\": -12.345,\n" +
            "  \"booleanTrue\": true,\n" +
            "  \"booleanFalse\": false,\n" +
            "  \"nullValue\": null,\n" +
            "  \"nestedObject\": {\n" +
            "    \"level1\": {\n" +
            "      \"level2\": {\n" +
            "        \"message\": \"deep\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"array\": [1, 2, 3, {\"a\": true}, [null, \"end\"]],\n" +
            "  \"escapeTest\": \"\\\"\\\\/\\b\\f\\n\\r\\t\"\n" +
            "}";


    @Test
    public void testComplexJsonParsing() {
        ContainerParser parser = new SimpleJsonParser();
        ContainerValue result = parser.parse(new StringReader(json));

        assertTrue(result.isObject());
        ObjectContainer obj = result.asObject();

        assertEquals("hello \n world", obj.getString("string"));
        assertEquals(12345, obj.getInt("number"));
        assertEquals(-12.345, obj.getDouble("decimal"), 0.00001);
        assertTrue(obj.getBoolean("booleanTrue"));
        assertFalse(obj.getBoolean("booleanFalse"));
        assertTrue(obj.get("nullValue").isNull());

        ObjectContainer nested = obj.getObject("nestedObject")
                .getObject("level1")
                .getObject("level2");
        assertEquals("deep", nested.getString("message"));

        ArrayContainer arr = obj.getArray("array");
        assertEquals(1, arr.getInt(0));
        assertEquals(2, arr.getInt(1));
        assertEquals(3, arr.getInt(2));
        assertTrue(arr.get(3).isObject());
        assertTrue(arr.getObject(3).getBoolean("a"));

        ArrayContainer innerArr = arr.getArray(4);
        assertTrue(innerArr.get(0).isNull());
        assertEquals("end", innerArr.getString(1));

        assertEquals("\"\\/\b\f\n\r\t", obj.getString("escapeTest"));

    }

    @Test
    public void testJSONArray() {
        String jsonArray = "[1, 2, 3, {\"a\": true}, [null, \"end\"]]";
        ContainerParser parser = new SimpleJsonParser();
        ContainerValue result = parser.parse(new StringReader(jsonArray));

        assertTrue(result.isArray());
        ArrayContainer arr = result.asArray();

        assertEquals(1, arr.getInt(0));
        assertEquals(2, arr.getInt(1));
        assertEquals(3, arr.getInt(2));
        assertTrue(arr.get(3).isObject());
        assertTrue(arr.getObject(3).getBoolean("a"));

        ArrayContainer innerArr = arr.getArray(4);
        assertTrue(innerArr.get(0).isNull());
        assertEquals("end", innerArr.getString(1));

        System.out.println(arr.toString());

    }
}