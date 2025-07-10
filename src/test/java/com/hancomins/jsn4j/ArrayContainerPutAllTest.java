package com.hancomins.jsn4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ArrayContainerPutAllTest {
    
    private ArrayContainer array;
    
    @BeforeEach
    void setUp() {
        array = Jsn4j.newArray();
    }
    
    /*@Test
    void testPutAllWithNull() {
        ArrayContainer result = array.putAll(null);
        assertSame(array, result);
        assertEquals(0, array.size());
    }*/
    
    @Test
    void testPutAllWithEmptyCollection() {
        List<String> emptyList = new ArrayList<>();
        ArrayContainer result = array.putAll(emptyList);
        assertSame(array, result);
        assertEquals(0, array.size());
    }
    
    @Test
    void testPutAllWithStrings() {
        List<String> strings = Arrays.asList("one", "two", "three");
        array.putAll(strings);
        
        assertEquals(3, array.size());
        assertEquals("one", array.getString(0));
        assertEquals("two", array.getString(1));
        assertEquals("three", array.getString(2));
    }
    
    @Test
    void testPutAllWithNumbers() {
        List<Number> numbers = Arrays.asList(1, 2.5, 3L, 4.0f);
        array.putAll(numbers);
        
        assertEquals(4, array.size());
        assertEquals(1, array.getInt(0));
        assertEquals(2.5, array.getDouble(1));
        assertEquals(3L, array.getLong(2));
        assertEquals(4.0f, array.getFloat(3));
    }
    
    @Test
    void testPutAllWithMixedTypes() {
        List<Object> mixed = Arrays.asList("string", 123, true, null, 45.67);
        array.putAll(mixed);
        
        assertEquals(5, array.size());
        assertEquals("string", array.getString(0));
        assertEquals(123, array.getInt(1));
        assertTrue(array.getBoolean(2));
        assertTrue(array.get(3).isNull());
        assertEquals(45.67, array.getDouble(4));
    }
    
    @Test
    void testPutAllWithNullElements() {
        List<String> withNulls = Arrays.asList("first", null, "third");
        array.putAll(withNulls);
        
        assertEquals(3, array.size());
        assertEquals("first", array.getString(0));
        assertTrue(array.get(1).isNull());
        assertEquals("third", array.getString(2));
    }
    
    @Test
    void testPutAllWithNestedCollections() {
        List<Object> nested = new ArrayList<>();
        nested.add("outer");
        nested.add(Arrays.asList("inner1", "inner2"));
        nested.add(new HashSet<>(Arrays.asList("set1", "set2")));
        
        array.putAll(nested);
        
        assertEquals(3, array.size());
        assertEquals("outer", array.getString(0));
        assertTrue(array.get(1).isArray());
        assertTrue(array.get(2).isArray());
    }
    
    @Test
    void testPutAllPreservesExistingElements() {
        array.put("existing1");
        array.put("existing2");
        
        List<String> newElements = Arrays.asList("new1", "new2");
        array.putAll(newElements);
        
        assertEquals(4, array.size());
        assertEquals("existing1", array.getString(0));
        assertEquals("existing2", array.getString(1));
        assertEquals("new1", array.getString(2));
        assertEquals("new2", array.getString(3));
    }
    
    @Test
    void testPutAllChaining() {
        ArrayContainer result = array
            .put("first")
            .putAll(Arrays.asList("second", "third"))
            .put("fourth");
            
        assertSame(array, result);
        assertEquals(4, array.size());
        assertEquals("first", array.getString(0));
        assertEquals("second", array.getString(1));
        assertEquals("third", array.getString(2));
        assertEquals("fourth", array.getString(3));
    }
    
    @Test
    void testPutAllWithLargeCollection() {
        List<Integer> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(i);
        }
        
        array.putAll(largeList);
        
        assertEquals(1000, array.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, array.getInt(i));
        }
    }
    
    @Test
    void testPutAllWithCustomObjects() {
        class CustomObject {
            private final String value;
            CustomObject(String value) { this.value = value; }
            @Override
            public String toString() { return value; }
        }
        
        List<CustomObject> customList = Arrays.asList(
            new CustomObject("custom1"),
            new CustomObject("custom2")
        );
        
        // CustomObject는 PrimitiveValue가 지원하지 않는 타입이므로 예외 발생
        assertThrows(IllegalArgumentException.class, () -> array.putAll(customList));
    }
    
    @Test
    void testPutAllWithDifferentCollectionTypes() {
        Set<String> set = new LinkedHashSet<>(Arrays.asList("set1", "set2", "set3"));
        Queue<String> queue = new LinkedList<>(Arrays.asList("queue1", "queue2"));
        
        array.putAll(set);
        array.putAll(queue);
        
        assertEquals(5, array.size());
    }
    
    @Test
    void testPutAllExceptionHandling() {
        List<Object> listWithProblem = new ArrayList<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
                    private int count = 0;
                    
                    @Override
                    public boolean hasNext() {
                        return count < 3;
                    }
                    
                    @Override
                    public Object next() {
                        if (count++ == 1) {
                            throw new RuntimeException("Iterator error");
                        }
                        return "item" + count;
                    }
                };
            }
        };
        
        assertThrows(RuntimeException.class, () -> array.putAll(listWithProblem));
    }
    
    @Test
    void testPutAllReturnsSelfForChaining() {
        ArrayContainer result1 = array.putAll(Arrays.asList("a", "b"));
        ArrayContainer result2 = result1.putAll(Arrays.asList("c", "d"));
        
        assertSame(array, result1);
        assertSame(array, result2);
        assertEquals(4, array.size());
    }
    
    @Test
    void testPutAllWithSupportedTypes() {
        // PrimitiveValue가 지원하는 모든 타입 테스트
        List<Object> supportedTypes = Arrays.asList(
            "string",           // String (CharSequence)
            123,                // Integer (Number)
            45.67,              // Double (Number)
            true,               // Boolean
            new byte[]{1, 2, 3} // byte array
        );
        
        array.putAll(supportedTypes);
        
        assertEquals(5, array.size());
        assertEquals("string", array.getString(0));
        assertEquals(123, array.getInt(1));
        assertEquals(45.67, array.getDouble(2));
        assertTrue(array.getBoolean(3));
        assertArrayEquals(new byte[]{1, 2, 3}, array.getByteArray(4));
    }
}