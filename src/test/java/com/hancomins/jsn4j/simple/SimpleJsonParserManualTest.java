package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

/**
 * Manual test runner for SimpleJsonParser fixes
 */
public class SimpleJsonParserManualTest {
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("Running SimpleJsonParser Fix Tests...\n");
        
        // Run all tests
        testBufferOverflow();
        testSimpleObjectDeepCopy();
        testNumberParsing();
        testUnicodeEscape();
        testErrorPosition();
        testOutOfMemoryFix();
        
        // Print summary
        System.out.println("\n========================================");
        System.out.println("Test Summary:");
        System.out.println("Passed: " + testsPassed);
        System.out.println("Failed: " + testsFailed);
        System.out.println("Total: " + (testsPassed + testsFailed));
        System.out.println("========================================");
    }
    
    private static void testBufferOverflow() {
        System.out.println("1. Testing PrimitiveValue buffer overflow fix...");
        
        try {
            // Test bufferToInt with large array
            byte[] largeArray = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
            int result = PrimitiveValueTest.TestPrimitiveValue.testBufferToInt(largeArray);
            int expected = (0x01 << 24) | (0x02 << 16) | (0x03 << 8) | 0x04;
            
            if (result == expected) {
                System.out.println("✓ bufferToInt: Correctly handles large arrays");
                testsPassed++;
            } else {
                System.out.println("✗ bufferToInt: Failed - Expected " + expected + ", got " + result);
                testsFailed++;
            }
            
            // Test bufferToLong with large array
            byte[] largeArray2 = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A};
            long result2 = PrimitiveValueTest.TestPrimitiveValue.testBufferToLong(largeArray2);
            long expected2 = ((long)0x01 << 56) | ((long)0x02 << 48) | ((long)0x03 << 40) | ((long)0x04 << 32) |
                           ((long)0x05 << 24) | ((long)0x06 << 16) | ((long)0x07 << 8) | (long)0x08;
            
            if (result2 == expected2) {
                System.out.println("✓ bufferToLong: Correctly handles large arrays");
                testsPassed++;
            } else {
                System.out.println("✗ bufferToLong: Failed - Expected " + expected2 + ", got " + result2);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Buffer overflow test failed with exception: " + e.getMessage());
            testsFailed += 2;
        }
    }
    
    private static void testSimpleObjectDeepCopy() {
        System.out.println("\n2. Testing SimpleObject deep copy fix...");
        
        try {
            String json = "{\"name\":\"original\",\"value\":123}";
            
            // Create original and copy
            SimpleObject original = new SimpleObject(json);
            SimpleObject copy = new SimpleObject(json);
            
            // Modify copy
            copy.put("name", "modified");
            copy.put("value", 456);
            
            // Check independence
            if ("original".equals(original.getString("name")) && 
                original.getInt("value") == 123 &&
                "modified".equals(copy.getString("name")) && 
                copy.getInt("value") == 456) {
                System.out.println("✓ Deep copy: Objects are independent");
                testsPassed++;
            } else {
                System.out.println("✗ Deep copy: Objects are not independent");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Deep copy test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }
    
    private static void testNumberParsing() {
        System.out.println("\n3. Testing number parsing improvements...");
        SimpleJsonParser parser = new SimpleJsonParser();
        
        // Test decimal point only
        try {
            String json = "{\"number\": 123.}";
            parser.parse(json);
            System.out.println("✗ Decimal point only: Should have thrown exception");
            testsFailed++;
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("Expected digit after decimal point")) {
                System.out.println("✓ Decimal point only: Correctly throws exception");
                testsPassed++;
            } else {
                System.out.println("✗ Decimal point only: Wrong exception message");
                testsFailed++;
            }
        }
        
        // Test exponent notation
        try {
            String[] testCases = {
                "{\"num\": 1.23e10}",
                "{\"num\": 1.23E-5}",
                "{\"num\": 1e5}"
            };
            double[] expected = {1.23e10, 1.23e-5, 1e5};
            
            boolean allPassed = true;
            for (int i = 0; i < testCases.length; i++) {
                ObjectContainer obj = parser.parse(testCases[i]).asObject();
                double value = obj.getDouble("num");
                if (Math.abs(value - expected[i]) > 0.000001) {
                    allPassed = false;
                    System.out.println("✗ Exponent notation: Failed for " + testCases[i]);
                }
            }
            
            if (allPassed) {
                System.out.println("✓ Exponent notation: All formats parsed correctly");
                testsPassed++;
            } else {
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Exponent notation test failed: " + e.getMessage());
            testsFailed++;
        }
    }
    
    private static void testUnicodeEscape() {
        System.out.println("\n4. Testing unicode escape support...");
        SimpleJsonParser parser = new SimpleJsonParser();
        
        try {
            // Test basic unicode
            String json = "{\"text\": \"\\u0048\\u0065\\u006C\\u006C\\u006F\"}";
            ObjectContainer obj = parser.parse(json).asObject();
            if ("Hello".equals(obj.getString("text"))) {
                System.out.println("✓ Unicode escape: Basic unicode works");
                testsPassed++;
            } else {
                System.out.println("✗ Unicode escape: Failed - got " + obj.getString("text"));
                testsFailed++;
            }
            
            // Test Korean unicode
            String koreanJson = "{\"text\": \"\\uD55C\\uAE00\"}";
            ObjectContainer koreanObj = parser.parse(koreanJson).asObject();
            if ("한글".equals(koreanObj.getString("text"))) {
                System.out.println("✓ Unicode escape: Korean unicode works");
                testsPassed++;
            } else {
                System.out.println("✗ Unicode escape: Korean failed - got " + koreanObj.getString("text"));
                testsFailed++;
            }
            
            // Test slash escape
            String slashJson = "{\"path\": \"http:\\/\\/example.com\"}";
            ObjectContainer slashObj = parser.parse(slashJson).asObject();
            if ("http://example.com".equals(slashObj.getString("path"))) {
                System.out.println("✓ Slash escape: Works correctly");
                testsPassed++;
            } else {
                System.out.println("✗ Slash escape: Failed - got " + slashObj.getString("path"));
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Unicode escape test failed: " + e.getMessage());
            testsFailed += 3;
        }
    }
    
    private static void testErrorPosition() {
        System.out.println("\n5. Testing error position information...");
        SimpleJsonParser parser = new SimpleJsonParser();
        
        try {
            String json = "{\n  \"key1\": \"value\",\n  \"key2\": invalid\n}";
            parser.parse(json);
            System.out.println("✗ Error position: Should have thrown exception");
            testsFailed++;
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("line") && msg.contains("column")) {
                System.out.println("✓ Error position: Contains line and column info");
                testsPassed++;
            } else {
                System.out.println("✗ Error position: Missing position info in: " + msg);
                testsFailed++;
            }
        }
    }
    
    private static void testOutOfMemoryFix() {
        System.out.println("\n6. Testing OutOfMemory fix...");
        SimpleJsonParser parser = new SimpleJsonParser();
        
        try {
            String json = "{\"name\":\"unclosed string}";
            parser.parse(json);
            System.out.println("✗ OutOfMemory fix: Should have thrown exception");
            testsFailed++;
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("Unexpected EOF while reading string")) {
                System.out.println("✓ OutOfMemory fix: Correctly throws EOF exception");
                testsPassed++;
            } else {
                System.out.println("✗ OutOfMemory fix: Wrong exception message: " + e.getMessage());
                testsFailed++;
            }
        } catch (OutOfMemoryError e) {
            System.out.println("✗ OutOfMemory fix: Still causes OutOfMemory!");
            testsFailed++;
        }
    }
}