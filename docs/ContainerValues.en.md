# ContainerValues Class Guide

## Overview

`ContainerValues` is a utility class in the JSN4J library that provides various static methods for handling JSON data. This class offers operations such as comparison, copying, merging, difference calculation, and intersection for JSON objects and arrays.

## Main Methods

### 1. equals(ContainerValue a, ContainerValue b)

Compares two ContainerValue objects for equality.

**Features:**
- Recursively compares nested objects and arrays
- Returns true only if type, size, and content are all identical
- Null-safe handling

**Example:**
```java
ObjectContainer obj1 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25);
    
ObjectContainer obj2 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25);
    
boolean isEqual = ContainerValues.equals(obj1, obj2); // true
```

### 2. copy(ContainerValue target, ContainerValue source)

Copies the content of source to target. The existing content of target is completely removed.

**Features:**
- Completely clears target and replaces with source content
- Only same types can be copied
- Performs deep copy for nested objects/arrays

**Example:**
```java
ObjectContainer target = Jsn4j.newObject().put("old", "data");
ObjectContainer source = Jsn4j.newObject()
    .put("name", "Bob")
    .put("age", 30);
    
ContainerValues.copy(target, source);
// target is now {"name": "Bob", "age": 30}
```

### 3. cloneContainer(ContainerValue source)

Creates a deep copy of a ContainerValue.

**Features:**
- Creates a new independent instance
- All nested objects/arrays are also copied
- Null or null values return PrimitiveValue(null)

**Example:**
```java
ObjectContainer original = Jsn4j.newObject()
    .put("data", Jsn4j.newObject().put("value", 100));
    
ContainerValue cloned = ContainerValues.cloneContainer(original);
// cloned is an independent copy of original
```

### 4. merge(ContainerValue target, ContainerValue source)

Merges the content of source into target. **Directly modifies target.**

**Features:**
- **In-place modification**: target is directly changed
- **Source priority**: Values with same key/index are overwritten by source
- **Recursive merge**: Nested objects are recursively merged
- **Array merge**: Index-based merge, appends if source is longer

**ObjectContainer merge:**
```java
ObjectContainer target = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25)
    .put("city", "Seoul");
    
ObjectContainer source = Jsn4j.newObject()
    .put("age", 26)              // Overwrite
    .put("country", "Korea");     // Add

ContainerValues.merge(target, source);
// target: {"name": "Alice", "age": 26, "city": "Seoul", "country": "Korea"}
```

**ArrayContainer merge:**
```java
ArrayContainer target = Jsn4j.newArray().put("a").put("b").put("c");
ArrayContainer source = Jsn4j.newArray().put("X").put("Y").put("Z").put("W");

ContainerValues.merge(target, source);
// target: ["X", "Y", "Z", "W"]
```

### 5. concat(ContainerValue target, ContainerValue source)

Returns a new ContainerValue combining target and source. **Originals are not modified.**

**Features:**
- **Creates new object**: Originals remain unchanged
- **Target priority**: Target values are kept when same keys exist
- **Immutability guaranteed**: Suitable for functional programming
- **Null handling**: Returns copy of the other if one is null

**ObjectContainer concatenation:**
```java
ObjectContainer target = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25);
    
ObjectContainer source = Jsn4j.newObject()
    .put("age", 30)              // Ignored (target priority)
    .put("city", "Seoul");       // Added

ContainerValue result = ContainerValues.concat(target, source);
// result: {"name": "Alice", "age": 25, "city": "Seoul"}
// target and source remain unchanged
```

**ArrayContainer concatenation:**
```java
ArrayContainer target = Jsn4j.newArray().put("a").put("b");
ArrayContainer source = Jsn4j.newArray().put("c").put("d");

ContainerValue result = ContainerValues.concat(target, source);
// result: ["a", "b", "c", "d"]
```

### 6. intersection(ContainerValue a, ContainerValue b)

Calculates the intersection of two ContainerValues.

**Features:**
- Returns new object containing only common elements
- Objects: Includes only fields with same key and same value
- Arrays: Includes only elements with same index and same value
- Recursively calculates intersection for nested objects/arrays

**Example:**
```java
ObjectContainer obj1 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25)
    .put("city", "Seoul");
    
ObjectContainer obj2 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 30)
    .put("country", "Korea");

ContainerValue result = ContainerValues.intersection(obj1, obj2);
// result: {"name": "Alice"} (only fields with same values)
```

### 7. diff(ContainerValue a, ContainerValue b)

Calculates the difference between a and b from a's perspective.

**Features:**
- Returns elements that exist only in a or have different values in a and b
- Objects: Includes fields with different values and fields only in a
- Arrays: Includes only elements with differences (excludes identical elements)
- Recursively calculates differences for nested structures

**Object diff:**
```java
ObjectContainer obj1 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 25)
    .put("city", "Seoul");
    
ObjectContainer obj2 = Jsn4j.newObject()
    .put("name", "Alice")
    .put("age", 26)
    .put("country", "Korea");

ContainerValue result = ContainerValues.diff(obj1, obj2);
// result: {"age": 25, "city": "Seoul"}
// name is excluded (identical), age has different value (a's value), city exists only in a
```

**Array diff:**
```java
ArrayContainer arr1 = Jsn4j.newArray()
    .put(Jsn4j.newObject().put("id", 1).put("name", "Alice"))
    .put(Jsn4j.newObject().put("id", 2).put("name", "Bob"));
    
ArrayContainer arr2 = Jsn4j.newArray()
    .put(Jsn4j.newObject().put("id", 1).put("name", "Alice"))
    .put(Jsn4j.newObject().put("id", 2).put("name", "Charlie"));

ContainerValue result = ContainerValues.diff(arr1, arr2);
// result: [{"name": "Bob"}]
// First element is excluded (identical), only second element with differences included
```

### 8. mapToObjectContainer(ContainerFactoryProvidable, Map<?, ?>)

Converts a Java Map to ObjectContainer.

**Features:**
- Recursively converts nested Maps and Collections
- Creates appropriate implementation through ContainerFactory

**Example:**
```java
Map<String, Object> map = new HashMap<>();
map.put("name", "Alice");
map.put("nested", Map.of("key", "value"));
map.put("list", Arrays.asList(1, 2, 3));

ObjectContainer obj = ContainerValues.mapToObjectContainer(Jsn4j.getInstance(), map);
// obj: {"name": "Alice", "nested": {"key": "value"}, "list": [1, 2, 3]}
```

### 9. collectionToArrayContainer(ContainerFactoryProvidable, Collection<?>)

Converts a Java Collection to ArrayContainer.

**Features:**
- Recursively converts nested Maps and Collections
- Supports all Collection types including List, Set, etc.

**Example:**
```java
List<Object> list = Arrays.asList(
    "text",
    123,
    Map.of("key", "value"),
    Arrays.asList(1, 2, 3)
);

ArrayContainer arr = ContainerValues.collectionToArrayContainer(Jsn4j.getInstance(), list);
// arr: ["text", 123, {"key": "value"}, [1, 2, 3]]
```

## merge() vs concat() Selection Guide

| Feature | merge() | concat() |
|---------|---------|----------|
| **Return type** | void (modifies target) | New ContainerValue |
| **Original modification** | Directly modifies target | Preserves originals, creates new object |
| **Priority** | Source overwrites target | Target has priority over source |
| **Use case** | Configuration updates, patch application | Immutable data combination, default value provision |
| **Performance** | Memory efficient | Increased memory usage due to new object creation |

## Usage Considerations

1. **Type matching**: copy, merge, concat only work between same types
2. **Null handling**: Most methods handle null safely
3. **Circular reference**: Creates copy to prevent infinite loop when merging with self
4. **Performance**: merge is more efficient than concat for large data
5. **Immutability**: concat guarantees immutability of original data

## Real-world Examples

### Configuration File Merge
```java
// Overwrite default configuration with user configuration
ObjectContainer defaultConfig = loadDefaultConfig();
ObjectContainer userConfig = loadUserConfig();
ContainerValues.merge(defaultConfig, userConfig);
saveConfig(defaultConfig);
```

### API Response Comparison
```java
// Check differences between two API versions
ObjectContainer v1Response = getApiV1Response();
ObjectContainer v2Response = getApiV2Response();
ContainerValue changes = ContainerValues.diff(v1Response, v2Response);
logApiChanges(changes);
```

### Data Filtering
```java
// Extract only common elements from two datasets
ArrayContainer dataset1 = loadDataset1();
ArrayContainer dataset2 = loadDataset2();
ContainerValue common = ContainerValues.intersection(dataset1, dataset2);
processCommonData(common);
```