package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.PrimitiveValue;

public class PrimitiveValueTest {
    
    public static class TestPrimitiveValue extends PrimitiveValue {
        public TestPrimitiveValue(Object raw) {
            super(raw);
        }
        
        public static int testBufferToInt(byte[] b) {
            return bufferToInt(b);
        }
        
        public static long testBufferToLong(byte[] b) {
            return bufferToLong(b);
        }
    }
}