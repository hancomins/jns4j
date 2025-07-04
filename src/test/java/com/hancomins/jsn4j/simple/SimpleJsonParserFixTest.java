package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the fixes applied to SimpleJsonParser and related classes
 */
public class SimpleJsonParserFixTest {

    // 1. PrimitiveValue 버퍼 오버플로우 테스트
    @Test
    public void testBufferToIntWithLargeArray() {
        // Test with array larger than 4 bytes
        byte[] largeArray = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        int result = PrimitiveValueTest.TestPrimitiveValue.testBufferToInt(largeArray);
        
        // Should only use first 4 bytes
        int expected = (0x01 << 24) | (0x02 << 16) | (0x03 << 8) | 0x04;
        assertEquals(expected, result);
    }
    
    @Test  
    public void testBufferToLongWithLargeArray() {
        // Test with array larger than 8 bytes
        byte[] largeArray = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A};
        long result = PrimitiveValueTest.TestPrimitiveValue.testBufferToLong(largeArray);
        
        // Should only use first 8 bytes
        long expected = ((long)0x01 << 56) | ((long)0x02 << 48) | ((long)0x03 << 40) | ((long)0x04 << 32) |
                       ((long)0x05 << 24) | ((long)0x06 << 16) | ((long)0x07 << 8) | (long)0x08;
        assertEquals(expected, result);
    }

    // 2. SimpleObject 깊은 복사 테스트
    @Test
    public void testSimpleObjectDeepCopy() {
        String json = "{\"name\":\"original\",\"value\":123}";
        
        // Create original object from JSON
        SimpleObject original = new SimpleObject(json);
        
        // Create copy using constructor
        SimpleObject copy = new SimpleObject(json);
        
        // Modify the copy
        copy.put("name", "modified");
        copy.put("value", 456);
        
        // Original should remain unchanged
        assertEquals("original", original.getString("name"));
        assertEquals(123, original.getInt("value"));
        
        // Copy should have new values
        assertEquals("modified", copy.getString("name"));
        assertEquals(456, copy.getInt("value"));
    }

    // 3. 숫자 파싱 개선 테스트
    @Test
    public void testNumberParsingWithDecimalPointOnly() {
        String json = "{\"number\": 123.}";
        SimpleJsonParser parser = new SimpleJsonParser();
        
        // Should throw exception for decimal point without digits
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> parser.parse(json));
        assertTrue(exception.getMessage().contains("Expected digit after decimal point"));
    }
    
    @Test
    public void testNumberParsingWithExponent() {
        SimpleJsonParser parser = new SimpleJsonParser();
        
        // Test various exponent formats
        String[] testCases = {
            "{\"num\": 1.23e10}",
            "{\"num\": 1.23E10}",
            "{\"num\": 1.23e+10}",
            "{\"num\": 1.23e-10}",
            "{\"num\": 1e5}",
            "{\"num\": 1E-5}"
        };
        
        double[] expected = {
            1.23e10,
            1.23e10,
            1.23e10,
            1.23e-10,
            1e5,
            1e-5
        };
        
        for (int i = 0; i < testCases.length; i++) {
            ObjectContainer obj = parser.parse(testCases[i]).asObject();
            assertEquals(expected[i], obj.getDouble("num"), 0.000001, 
                "Failed for: " + testCases[i]);
        }
        
        // Test invalid exponent
        String invalidExponent = "{\"num\": 1.23e}";
        assertThrows(IllegalStateException.class, () -> parser.parse(invalidExponent));
    }

    // 4. 유니코드 이스케이프 테스트
    @Test
    public void testUnicodeEscapeSequence() {
        SimpleJsonParser parser = new SimpleJsonParser();
        
        // Test unicode escape
        String json = "{\"text\": \"\\u0048\\u0065\\u006C\\u006C\\u006F\"}";
        ObjectContainer obj = parser.parse(json).asObject();
        assertEquals("Hello", obj.getString("text"));
        
        // Test Korean unicode
        String koreanJson = "{\"text\": \"\\uD55C\\uAE00\"}";
        ObjectContainer koreanObj = parser.parse(koreanJson).asObject();
        assertEquals("한글", koreanObj.getString("text"));
        
        // Test invalid unicode sequence
        String invalidUnicode = "{\"text\": \"\\u123G\"}";
        assertThrows(IllegalStateException.class, () -> parser.parse(invalidUnicode));
    }
    
    @Test
    public void testSlashEscape() {
        SimpleJsonParser parser = new SimpleJsonParser();
        
        String json = "{\"path\": \"http:\\/\\/example.com\\/path\"}";
        ObjectContainer obj = parser.parse(json).asObject();
        assertEquals("http://example.com/path", obj.getString("path"));
    }

    // 5. 에러 위치 정보 테스트
    @Test
    public void testErrorPositionInfo() {
        SimpleJsonParser parser = new SimpleJsonParser();
        
        // Test error on first line
        String json1 = "{\"key\": invalid}";
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, 
            () -> parser.parse(json1));
        assertTrue(ex1.getMessage().contains("line 1"));
        
        // Test error on multiple lines
        String json2 = "{\n  \"key1\": \"value\",\n  \"key2\": invalid\n}";
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, 
            () -> parser.parse(json2));
        assertTrue(ex2.getMessage().contains("line 3"));
        
        // Test error with column info
        assertTrue(ex1.getMessage().contains("column"));
        assertTrue(ex2.getMessage().contains("column"));
    }

    // 6. OutOfMemory 수정 확인 테스트
    @Test
    public void testParseErrorWithUnclosedString() {
        String json = "{\"name\":\"unclosed string}";
        SimpleJsonParser parser = new SimpleJsonParser();
        
        // Should throw IllegalStateException instead of OutOfMemory
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> parser.parse(json));
        assertTrue(exception.getMessage().contains("Unexpected EOF while reading string"));
        
        // Test with escape sequence at EOF
        String jsonWithEscape = "{\"name\":\"test\\";
        IllegalStateException exception2 = assertThrows(IllegalStateException.class, 
            () -> parser.parse(jsonWithEscape));
        assertTrue(exception2.getMessage().contains("Unexpected EOF while reading escape sequence"));
    }
}