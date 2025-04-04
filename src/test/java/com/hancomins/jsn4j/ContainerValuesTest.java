package com.hancomins.jsn4j;

import com.hancomins.jsn4j.simple.SimpleJsonContainerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContainerValuesTest {

    private final ContainerFactory factory = new SimpleJsonContainerFactory();

    @Test
    public void testEquals_Primitives() {
        PrimitiveValue a = new PrimitiveValue("hello");
        PrimitiveValue b = new PrimitiveValue("hello");
        PrimitiveValue c = new PrimitiveValue("world");

        assertTrue(ContainerValues.equals(a, b));
        assertFalse(ContainerValues.equals(a, c));
    }

    @Test
    public void testEquals_Object() {
        ObjectContainer a = factory.newObject()
                .put("key", new PrimitiveValue("value"));

        ObjectContainer b = factory.newObject()
                .put("key", new PrimitiveValue("value"));

        ObjectContainer c = factory.newObject()
                .put("key", new PrimitiveValue("different"));

        assertTrue(ContainerValues.equals(a, b));
        assertFalse(ContainerValues.equals(a, c));
    }

    @Test
    public void testEquals_Array() {
        ArrayContainer a = factory.newArray()
                .put(new PrimitiveValue(1))
                .put(new PrimitiveValue(2));

        ArrayContainer b = factory.newArray()
                .put(new PrimitiveValue(1))
                .put(new PrimitiveValue(2));

        ArrayContainer c = factory.newArray()
                .put(new PrimitiveValue(1))
                .put(new PrimitiveValue(3));

        assertTrue(ContainerValues.equals(a, b));
        assertFalse(ContainerValues.equals(a, c));
    }

    @Test
    public void testCopy_Object() {
        ObjectContainer src = factory.newObject()
                .put("name", new PrimitiveValue("BlinkBin"))
                .put("version", new PrimitiveValue(1));

        ObjectContainer target = factory.newObject();
        ContainerValues.copy(target, src);

        assertTrue(ContainerValues.equals(src, target));
    }

    @Test
    public void testCopy_Array() {
        ArrayContainer src = factory.newArray()
                .put(new PrimitiveValue("a"))
                .put(new PrimitiveValue("b"));

        ArrayContainer target = factory.newArray();
        ContainerValues.copy(target, src);

        assertTrue(ContainerValues.equals(src, target));
    }

    @Test
    public void testMerge_Object() {
        ObjectContainer base = factory.newObject()
                .put("a", new PrimitiveValue(1));

        ObjectContainer patch = factory.newObject()
                .put("b", new PrimitiveValue(2))
                .put("a", new PrimitiveValue(3));

        ContainerValues.merge(base, patch);

        assertEquals(new PrimitiveValue(3), base.get("a"));
        assertEquals(new PrimitiveValue(2), base.get("b"));
    }

    @Test
    public void testIntersection_Object() {
        ObjectContainer a = factory.newObject()
                .put("x", new PrimitiveValue(1))
                .put("y", new PrimitiveValue(true));

        ObjectContainer b = factory.newObject()
                .put("x", new PrimitiveValue(1))
                .put("y", new PrimitiveValue(false));

        ContainerValue intersect = ContainerValues.intersection(a, b);

        assertTrue(intersect.isObject());
        ObjectContainer result = intersect.asObject();
        assertEquals(1, result.size());
        assertEquals(new PrimitiveValue(1), result.get("x"));
    }

    @Test
    public void testDiff_Object() {
        ObjectContainer a = factory.newObject()
                .put("name", new PrimitiveValue("BlinkBin"))
                .put("version", new PrimitiveValue(1));

        ObjectContainer b = factory.newObject()
                .put("name", new PrimitiveValue("BlinkBin"))
                .put("version", new PrimitiveValue(2));

        ContainerValue diff = ContainerValues.diff(a, b);

        assertTrue(diff.isObject());
        ObjectContainer result = diff.asObject();
        assertEquals(1, result.size());
        assertEquals(new PrimitiveValue(1), result.get("version"));
    }
}