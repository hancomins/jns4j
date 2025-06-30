package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleJsonWriterTest {

    @Test
    public void testCompactJsonWrite() {
        ObjectContainer obj = new SimpleObject();
        obj.put("name", "BlinkBin");
        obj.put("active", true);
        obj.put("value", 42);

        SimpleJsonWriter writer = new SimpleJsonWriter(obj);
        String json = writer.write();

        assertEquals("{\"name\":\"BlinkBin\",\"active\":true,\"value\":42}", json);
    }

    @Test
    public void testPrettyPrint() {
        ObjectContainer obj = new SimpleObject();
        obj.put("name", "BlinkBin");
        obj.put("array", new SimpleArray().put(1).put(2).put(3));

        SimpleJsonWriter writer = new SimpleJsonWriter(obj);
        writer.putOption(SimpleJsonWriteOption.PRETTY_PRINT, true);
        String pretty = writer.write();

        assertTrue(pretty.contains("\n"));
        assertTrue(pretty.contains("  \"name\""));
        assertTrue(pretty.contains("[\n"));
    }

    @Test
    public void testPrettyPrintWithIndentLevel() {
        ObjectContainer obj = new SimpleObject();
        obj.put("level", new SimpleObject().put("inner", "value"));

        SimpleJsonWriter writer = new SimpleJsonWriter(obj);
        writer.putOption(SimpleJsonWriteOption.PRETTY_PRINT, true);
        writer.putOption(SimpleJsonWriteOption.INDENT_OUTPUT, 2); // 2단계 들여쓰기

        String result = writer.write();
        assertTrue(result.contains("    \"inner\"")); // 4칸 들여쓰기 기대
    }

    @Test
    public void testOutputStreamWrite() throws IOException {
        ObjectContainer obj = new SimpleObject();
        obj.put("x", 123);

        SimpleJsonWriter writer = new SimpleJsonWriter(obj);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.write(baos);

        String output = new String(baos.toByteArray(), "UTF-8");
        assertEquals("{\"x\":123}", output);
    }
}
