package com.hancomins.jsn4j;

import com.hancomins.jsn4j.simple.SimpleJsonContainerFactory;
import com.hancomins.jsn4j.jackson.JacksonContainerFactory;
import com.hancomins.jsn4j.gson.GsonContainerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deep diff and intersection tests for ContainerValues
 */
public class ContainerDiffIntersectionTest {

    @BeforeAll
    public static void setupFactories() {
        Jsn4j.registerContainerFactory(SimpleJsonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(JacksonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(GsonContainerFactory.getInstance());
    }

    private static Stream<Arguments> containerFactories() {
        return Stream.of(
                Arguments.of(SimpleJsonContainerFactory.getInstance()),
                Arguments.of(JacksonContainerFactory.getInstance()),
                Arguments.of(GsonContainerFactory.getInstance())
        );
    }

    // Deep Intersection Tests

    @ParameterizedTest
    @MethodSource("containerFactories")
    public void testDeepObjectIntersection(ContainerFactory factory) {
        // Given: Two objects with nested structures
        ObjectContainer objA = factory.newObject()
                .put("name", "Product")
                .put("details", factory.newObject()
                        .put("color", "red")
                        .put("size", "large")
                        .put("price", 100))
                .put("tags", factory.newArray().put("new").put("sale"));

        ObjectContainer objB = factory.newObject()
                .put("name", "Product")
                .put("details", factory.newObject()
                        .put("color", "red")
                        .put("size", "medium")  // Different
                        .put("weight", 500))    // Only in B
                .put("tags", factory.newArray().put("new").put("featured")); // Different array

        // When
        ContainerValue result = ContainerValues.intersection(objA, objB);

        // Then
        assertTrue(result.isObject());
        ObjectContainer resultObj = result.asObject();
        
        // Name should be included (same value)
        assertEquals("Product", resultObj.getString("name"));
        
        // Details should have deep intersection
        assertTrue(resultObj.has("details"));
        ObjectContainer details = resultObj.get("details").asObject();
        assertEquals("red", details.getString("color")); // Same in both
        assertFalse(details.has("size"));    // Different values
        assertFalse(details.has("price"));   // Only in A
        assertFalse(details.has("weight"));  // Only in B
        
        // Tags should be included with partial intersection
        assertTrue(resultObj.has("tags"));
        ArrayContainer tags = resultObj.get("tags").asArray();
        assertEquals(1, tags.size());
        assertEquals("new", tags.getString(0)); // Only "new" is common
    }

    @Test
    public void testVeryDeepObjectIntersection() {
        // Given: Very deep nested structure
        ObjectContainer objA = Jsn4j.newObject()
                .put("level1", Jsn4j.newObject()
                        .put("level2", Jsn4j.newObject()
                                .put("level3", Jsn4j.newObject()
                                        .put("common", "value")
                                        .put("differentA", "A"))
                                .put("common2", "value2")));

        ObjectContainer objB = Jsn4j.newObject()
                .put("level1", Jsn4j.newObject()
                        .put("level2", Jsn4j.newObject()
                                .put("level3", Jsn4j.newObject()
                                        .put("common", "value")
                                        .put("differentB", "B"))
                                .put("common2", "value2")));

        // When
        ContainerValue result = ContainerValues.intersection(objA, objB);

        // Then
        ObjectContainer level3 = result.asObject()
                .get("level1").asObject()
                .get("level2").asObject()
                .get("level3").asObject();
        
        assertEquals("value", level3.getString("common"));
        assertFalse(level3.has("differentA"));
        assertFalse(level3.has("differentB"));
    }

    @Test
    public void testArrayWithNestedObjectsIntersection() {
        // Given: Arrays containing objects
        ArrayContainer arrA = Jsn4j.newArray()
                .put(Jsn4j.newObject()
                        .put("id", 1)
                        .put("name", "Item1")
                        .put("price", 100))
                .put(Jsn4j.newObject()
                        .put("id", 2)
                        .put("name", "Item2"));

        ArrayContainer arrB = Jsn4j.newArray()
                .put(Jsn4j.newObject()
                        .put("id", 1)
                        .put("name", "Item1")
                        .put("stock", 50))  // Different field
                .put(Jsn4j.newObject()
                        .put("id", 2)
                        .put("name", "ItemX")); // Different name

        // When
        ContainerValue result = ContainerValues.intersection(arrA, arrB);

        // Then
        assertTrue(result.isArray());
        ArrayContainer resultArr = result.asArray();
        assertEquals(2, resultArr.size()); // Both objects have some intersection
        
        // First object has id and name in common
        ObjectContainer firstItem = resultArr.get(0).asObject();
        assertEquals(1, firstItem.getInt("id"));
        assertEquals("Item1", firstItem.getString("name"));
        assertFalse(firstItem.has("price"));  // Only in A
        assertFalse(firstItem.has("stock"));  // Only in B
        
        // Second object only has id in common
        ObjectContainer secondItem = resultArr.get(1).asObject();
        assertEquals(2, secondItem.getInt("id"));
        assertFalse(secondItem.has("name"));  // Different values
    }

    // Deep Diff Tests

    @ParameterizedTest
    @MethodSource("containerFactories")
    public void testDeepObjectDiff(ContainerFactory factory) {
        // Given: Two objects with nested structures
        ObjectContainer objA = factory.newObject()
                .put("name", "Original")
                .put("version", 1)
                .put("config", factory.newObject()
                        .put("timeout", 30)
                        .put("retries", 3)
                        .put("debug", true))
                .put("unchanged", "same");

        ObjectContainer objB = factory.newObject()
                .put("name", "Original")
                .put("version", 2)  // Changed
                .put("config", factory.newObject()
                        .put("timeout", 60)  // Changed
                        .put("retries", 3)   // Same
                        .put("cache", true)) // Added in B
                .put("unchanged", "same");

        // When
        ContainerValue result = ContainerValues.diff(objA, objB);

        // Then
        assertTrue(result.isObject());
        ObjectContainer resultObj = result.asObject();
        
        // Should not include unchanged fields
        assertFalse(resultObj.has("name"));
        assertFalse(resultObj.has("unchanged"));
        
        // Should include changed fields
        assertEquals(1, resultObj.getInt("version"));
        
        // Should include deep diff of config
        assertTrue(resultObj.has("config"));
        ObjectContainer configDiff = resultObj.get("config").asObject();
        assertEquals(30, configDiff.getInt("timeout")); // Value from A
        assertTrue(configDiff.getBoolean("debug"));      // Only in A
        assertFalse(configDiff.has("retries"));          // Same in both
        assertFalse(configDiff.has("cache"));            // Only in B
    }

    @Test
    public void testComplexNestedDiff() {
        // Given: Complex nested structure
        ObjectContainer objA = Jsn4j.newObject()
                .put("users", Jsn4j.newArray()
                        .put(Jsn4j.newObject()
                                .put("id", 1)
                                .put("profile", Jsn4j.newObject()
                                        .put("name", "Alice")
                                        .put("age", 25)
                                        .put("city", "Seoul")))
                        .put(Jsn4j.newObject()
                                .put("id", 2)
                                .put("profile", Jsn4j.newObject()
                                        .put("name", "Bob")
                                        .put("age", 30))));

        ObjectContainer objB = Jsn4j.newObject()
                .put("users", Jsn4j.newArray()
                        .put(Jsn4j.newObject()
                                .put("id", 1)
                                .put("profile", Jsn4j.newObject()
                                        .put("name", "Alice")
                                        .put("age", 26)      // Changed
                                        .put("country", "Korea"))) // Added
                        .put(Jsn4j.newObject()
                                .put("id", 2)
                                .put("profile", Jsn4j.newObject()
                                        .put("name", "Robert") // Changed
                                        .put("age", 30))));

        // When
        ContainerValue result = ContainerValues.diff(objA, objB);

        // Then
        assertTrue(result.isObject());
        ObjectContainer resultObj = result.asObject();
        
        assertTrue(resultObj.has("users"));
        ArrayContainer usersDiff = resultObj.get("users").asArray();
        assertEquals(2, usersDiff.size());
        
        // First user diff - contains only the differences
        ObjectContainer user1Diff = usersDiff.get(0).asObject();
        assertFalse(user1Diff.has("id"));     // id is same in both, so not in diff
        assertTrue(user1Diff.has("profile")); // Has differences in profile
        ObjectContainer profile1 = user1Diff.get("profile").asObject();
        
        assertEquals(25, profile1.getInt("age"));     // Original value from A
        assertEquals("Seoul", profile1.getString("city")); // Only in A
        assertFalse(profile1.has("name"));            // Same in both
        assertFalse(profile1.has("country"));         // Only in B, not in diff
        
        // Second user diff - contains only the differences
        ObjectContainer user2Diff = usersDiff.get(1).asObject();
        assertFalse(user2Diff.has("id"));     // id is same in both, so not in diff
        assertTrue(user2Diff.has("profile")); // Has differences in profile
        ObjectContainer profile2 = user2Diff.get("profile").asObject();
        assertEquals("Bob", profile2.getString("name")); // Original value from A
        assertFalse(profile2.has("age"));               // Same in both
    }

    @Test
    public void testEmptyResultsForIdenticalStructures() {
        // Given: Identical complex structures
        ObjectContainer obj1 = Jsn4j.newObject()
                .put("data", Jsn4j.newObject()
                        .put("items", Jsn4j.newArray()
                                .put(Jsn4j.newObject().put("id", 1).put("value", "A"))
                                .put(Jsn4j.newObject().put("id", 2).put("value", "B"))));

        ObjectContainer obj2 = Jsn4j.newObject()
                .put("data", Jsn4j.newObject()
                        .put("items", Jsn4j.newArray()
                                .put(Jsn4j.newObject().put("id", 1).put("value", "A"))
                                .put(Jsn4j.newObject().put("id", 2).put("value", "B"))));

        // When
        ContainerValue diffResult = ContainerValues.diff(obj1, obj2);
        ContainerValue intersectionResult = ContainerValues.intersection(obj1, obj2);

        // Then
        // Diff should be empty
        assertTrue(diffResult.isObject());
        assertTrue(diffResult.asObject().isEmpty());
        
        // Intersection should contain everything
        assertTrue(intersectionResult.isObject());
        assertEquals(obj1.size(), intersectionResult.asObject().size());
    }

    @Test
    public void testMixedTypeHandling() {
        // Given: Objects with mixed types
        ObjectContainer objA = Jsn4j.newObject()
                .put("nested", Jsn4j.newObject().put("key", "value"))
                .put("array", Jsn4j.newArray().put(1).put(2))
                .put("primitive", "text")
                .put("number", 42);

        ObjectContainer objB = Jsn4j.newObject()
                .put("nested", Jsn4j.newArray().put("different"))  // Different type
                .put("array", Jsn4j.newObject().put("key", "val")) // Different type
                .put("primitive", "text")  // Same
                .put("number", 99);        // Different value

        // When
        ContainerValue intersection = ContainerValues.intersection(objA, objB);
        ContainerValue diff = ContainerValues.diff(objA, objB);

        // Then
        ObjectContainer intersectionObj = intersection.asObject();
        assertEquals(1, intersectionObj.size());
        assertEquals("text", intersectionObj.getString("primitive"));
        
        ObjectContainer diffObj = diff.asObject();
        assertTrue(diffObj.has("nested"));
        assertTrue(diffObj.has("array"));
        assertEquals(42, diffObj.getInt("number"));
        assertFalse(diffObj.has("primitive")); // Same in both
    }

    @Test
    public void testPerformanceWithLargeStructures() {
        // Given: Large nested structures
        ObjectContainer largeObjA = Jsn4j.newObject();
        ObjectContainer largeObjB = Jsn4j.newObject();
        
        for (int i = 0; i < 100; i++) {
            ObjectContainer nestedA = Jsn4j.newObject();
            ObjectContainer nestedB = Jsn4j.newObject();
            
            for (int j = 0; j < 10; j++) {
                nestedA.put("field" + j, "value" + j);
                nestedB.put("field" + j, (j % 2 == 0) ? "value" + j : "different" + j);
            }
            
            largeObjA.put("object" + i, nestedA);
            largeObjB.put("object" + i, nestedB);
        }

        // When
        long startTime = System.currentTimeMillis();
        ContainerValue diffResult = ContainerValues.diff(largeObjA, largeObjB);
        ContainerValue intersectionResult = ContainerValues.intersection(largeObjA, largeObjB);
        long endTime = System.currentTimeMillis();

        // Then
        assertTrue(diffResult.isObject());
        assertTrue(intersectionResult.isObject());
        
        // Performance check
        assertTrue((endTime - startTime) < 1000, 
                "Operations took too long: " + (endTime - startTime) + "ms");
        
        // Verify some results
        ObjectContainer diffObj = diffResult.asObject();
        assertEquals(100, diffObj.size()); // All objects have differences
        
        ObjectContainer intersectionObj = intersectionResult.asObject();
        assertEquals(100, intersectionObj.size()); // All objects have some common fields
    }

    // Edge Cases

    @Test
    public void testNullAndEmptyHandling() {
        ObjectContainer obj = Jsn4j.newObject().put("key", "value");
        ObjectContainer empty = Jsn4j.newObject();
        
        // Diff with null/empty
        ContainerValue diffWithNull = ContainerValues.diff(obj, null);
        assertEquals(obj, diffWithNull);
        
        ContainerValue diffWithEmpty = ContainerValues.diff(obj, empty);
        assertEquals(obj.size(), diffWithEmpty.asObject().size());
        
        // Intersection with null/empty
        ContainerValue intersectionWithNull = ContainerValues.intersection(obj, null);
        assertTrue(intersectionWithNull.isNull());
        
        ContainerValue intersectionWithEmpty = ContainerValues.intersection(obj, empty);
        assertTrue(intersectionWithEmpty.asObject().isEmpty());
    }

    @Test
    public void testDeepStructureWithEmptyObjects() {
        // Given: Structures with empty nested objects
        ObjectContainer objA = Jsn4j.newObject()
                .put("data", Jsn4j.newObject()
                        .put("empty", Jsn4j.newObject())
                        .put("filled", Jsn4j.newObject().put("key", "value")));

        ObjectContainer objB = Jsn4j.newObject()
                .put("data", Jsn4j.newObject()
                        .put("empty", Jsn4j.newObject())
                        .put("filled", Jsn4j.newObject().put("key", "different")));

        // When
        ContainerValue intersection = ContainerValues.intersection(objA, objB);
        ContainerValue diff = ContainerValues.diff(objA, objB);

        // Then
        // When nested objects have no common non-empty fields, the parent is not included
        assertTrue(intersection.isObject());
        assertFalse(intersection.asObject().has("data")); // No common data fields
        
        ObjectContainer diffData = diff.asObject().get("data").asObject();
        assertFalse(diffData.has("empty")); // Same empty objects
        assertTrue(diffData.has("filled")); // Different values
    }

    @Test
    public void testRealWorldScenario() {
        // Given: API response comparison scenario
        ObjectContainer apiV1Response = Jsn4j.newObject()
                .put("status", "success")
                .put("data", Jsn4j.newObject()
                        .put("user", Jsn4j.newObject()
                                .put("id", 123)
                                .put("name", "John Doe")
                                .put("email", "john@example.com")
                                .put("preferences", Jsn4j.newObject()
                                        .put("theme", "dark")
                                        .put("notifications", true)))
                        .put("permissions", Jsn4j.newArray()
                                .put("read")
                                .put("write")))
                .put("timestamp", 1234567890);

        ObjectContainer apiV2Response = Jsn4j.newObject()
                .put("status", "success")
                .put("data", Jsn4j.newObject()
                        .put("user", Jsn4j.newObject()
                                .put("id", 123)
                                .put("name", "John Doe")
                                .put("emailAddress", "john@example.com") // Field renamed
                                .put("preferences", Jsn4j.newObject()
                                        .put("theme", "light") // Changed
                                        .put("notifications", true)
                                        .put("language", "en"))) // Added
                        .put("permissions", Jsn4j.newArray()
                                .put("read")
                                .put("write")
                                .put("admin"))) // Added permission
                .put("timestamp", 1234567999)
                .put("version", "2.0"); // New field

        // When: Find what changed between API versions
        ContainerValue changes = ContainerValues.diff(apiV1Response, apiV2Response);

        // Then
        ObjectContainer changesObj = changes.asObject();
        
        // Timestamp changed
        assertEquals(1234567890, changesObj.getInt("timestamp"));
        
        // Data changes
        ObjectContainer dataChanges = changesObj.get("data").asObject();
        
        // User changes
        ObjectContainer userChanges = dataChanges.get("user").asObject();
        assertEquals("john@example.com", userChanges.getString("email")); // V1 had this field
        
        // Preferences changes
        ObjectContainer prefChanges = userChanges.get("preferences").asObject();
        assertEquals("dark", prefChanges.getString("theme")); // V1 value
        
        // Permissions array has no differences in the overlapping elements
        // so it's not included in the diff result
        assertFalse(dataChanges.has("permissions"));
    }
}