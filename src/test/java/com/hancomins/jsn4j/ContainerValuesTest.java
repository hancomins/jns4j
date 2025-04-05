package com.hancomins.jsn4j;

import com.hancomins.jsn4j.simple.SimpleJsonContainerFactory;
import org.junit.jupiter.api.Test;

import java.util.*;

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




    @Test
    public void testMapToObjectContainer_nestedMapAndList() {
        ContainerFactory factory = new SimpleJsonContainerFactory();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "JSN4J");
        map.put("version", 1);

        Map<String, Object> nestedMap = new LinkedHashMap<>();
        nestedMap.put("enabled", true);
        nestedMap.put("features", Arrays.asList("copy", "merge", "diff"));

        map.put("config", nestedMap);

        ObjectContainer result = ContainerValues.mapToObjectContainer(factory.newObject(), map);

        assertEquals("JSN4J", result.get("name").raw());
        assertEquals(1, result.get("version").raw());
        assertTrue(result.get("config").isObject());

        ObjectContainer config = result.get("config").asObject();
        assertEquals(true, config.get("enabled").raw());

        ArrayContainer features = config.get("features").asArray();
        assertEquals(3, features.size());
        assertEquals("copy", features.get(0).raw());
        assertEquals("merge", features.get(1).raw());
        assertEquals("diff", features.get(2).raw());
    }

    @Test
    public void testCollectionToArrayContainer_withNestedCollectionsAndMaps() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("id", 1);
        map1.put("name", "Item1");

        Map<String, Object> map2 = new HashMap<>();
        map2.put("id", 2);
        map2.put("name", "Item2");

        List<Object> innerList = new ArrayList<>();
        innerList.add(map1);
        innerList.add(map2);

        List<Object> outerList = new ArrayList<>();
        outerList.add("Start");
        outerList.add(innerList);
        outerList.add("End");

        ArrayContainer arrayContainer = ContainerValues.collectionToArrayContainer(factory.newArray(), outerList);

        assertEquals("Start", arrayContainer.get(0).raw());

        ArrayContainer inner = arrayContainer.get(1).asArray();
        assertEquals(2, inner.size());
        assertEquals(1, inner.get(0).asObject().get("id").raw());
        assertEquals("Item2", inner.get(1).asObject().get("name").raw());

        assertEquals("End", arrayContainer.get(2).raw());
    }

}