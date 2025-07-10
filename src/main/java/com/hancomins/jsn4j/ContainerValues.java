package com.hancomins.jsn4j;



import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class ContainerValues {

    /**
     * 두 ContainerValue 객체를 비교하여 동일한지 확인합니다.
     *
     * @param a 비교할 첫 번째 ContainerValue
     * @param b 비교할 두 번째 ContainerValue
     * @return 두 ContainerValue 객체가 동일하면 true, 그렇지 않으면 false
     */
    public static boolean equals(ContainerValue a, ContainerValue b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getValueType() != b.getValueType()) return false;

        switch (a.getValueType()) {

            case PRIMITIVE:
                return Objects.equals(a.raw(), b.raw());
            case OBJECT:
                ObjectContainer objA = a.asObject();
                ObjectContainer objB = b.asObject();
                if (objA.size() != objB.size()) return false;
                for (String key : objA.keySet()) {
                    if (!equals(objA.get(key), objB.get(key))) return false;
                }
                return true;
            case ARRAY:
                ArrayContainer arrA = a.asArray();
                ArrayContainer arrB = b.asArray();
                if (arrA.size() != arrB.size()) return false;
                for (int i = 0; i < arrA.size(); i++) {
                    if (!equals(arrA.get(i), arrB.get(i))) return false;
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 하나의 ContainerValue의 내용을 다른 ContainerValue에 복사합니다.
     *
     * @param target 복사 대상 ContainerValue
     * @param source 복사 원본 ContainerValue
     * @throws IllegalArgumentException 대상과 원본의 값 유형이 일치하지 않는 경우
     * @throws UnsupportedOperationException 값 유형에 대해 복사 작업이 지원되지 않는 경우
     */
    public static void copy(ContainerValue target, ContainerValue source) {
        if (target == null || source == null) return;
        if (target.getValueType() != source.getValueType()) {
            throw new IllegalArgumentException("Cannot copy between different value types: "
                    + target.getValueType() + " and " + source.getValueType());
        }

        switch (target.getValueType()) {
            case OBJECT:
                ObjectContainer tgtObj = target.asObject();
                ObjectContainer srcObj = source.asObject();
                tgtObj.clear();
                for (String key : srcObj.keySet()) {
                    ContainerValue val = srcObj.get(key);
                    addValue(tgtObj, key, val);
                }
                break;
            case ARRAY:
                ArrayContainer tgtArr = target.asArray();
                ArrayContainer srcArr = source.asArray();
                tgtArr.clear();
                for (int i = 0; i < srcArr.size(); i++) {
                    ContainerValue val = srcArr.get(i);
                    addValue(tgtArr, val);
                }
                break;
            default:
                throw new UnsupportedOperationException("Copy not supported for type: " + target.getValueType());
        }
    }

    public static ContainerValue cloneContainer(ContainerValue source) {
        if (source == null || source.isNull()) return new PrimitiveValue(null);
        switch (source.getValueType()) {
            case PRIMITIVE:
                return new PrimitiveValue(source.raw());
            case OBJECT:
                ObjectContainer objectContainer = source.asObject().getContainerFactory().newObject();
                ContainerValues.copy(objectContainer, source.asObject());
                return objectContainer;
            case ARRAY:
                ArrayContainer arrayContainer = source.asArray().getContainerFactory().newArray();
                ContainerValues.copy(arrayContainer, source.asArray());
                return arrayContainer;
            default:
                throw new UnsupportedOperationException("Clone not supported for type: " + source.getValueType());
        }

    }

    public static ContainerValue concat(ContainerValue target, ContainerValue source) {
        if ((target == null || target.isNull()) && (source == null || source.isNull())) {
            throw new NullPointerException("Both target and source cannot be null or null value");
        }
        if(target == source) {
            source = ContainerValues.cloneContainer(source);
            if(source.getValueType() == ValueType.OBJECT) {
                return source;
            }
        }



        ValueType targetType = (target != null) ? target.getValueType() : null;
        ValueType sourceType = (source != null) ? source.getValueType() : null;
        if(targetType != null && sourceType == null) {
            return ContainerValues.cloneContainer(target);
        }
        else if(sourceType != null && targetType == null) {
            return ContainerValues.cloneContainer(source);
        }
        ValueType mergeableType = getValueTypeOfMergeable(targetType, sourceType);
        switch (mergeableType) {
            case ARRAY:
                ArrayContainer targetArray = target.asArray();
                ArrayContainer sourceArray = source.asArray();
                ArrayContainer resultArray = targetArray.getContainerFactory().newArray(target);
                resultArray.putAll(targetArray);
                resultArray.putAll(sourceArray);
                return resultArray;
            case OBJECT:
                ObjectContainer targetObject = target.asObject();
                ObjectContainer sourceObject = source.asObject();
                ObjectContainer resultObject = targetObject.getContainerFactory().newObject();
                ContainerValues.merge(resultObject, sourceObject);
                ContainerValues.merge(resultObject, targetObject);
                return resultObject;
            default:
                throw new IllegalArgumentException("Unexpected value type: " + targetType);
        }
    }

    private static ValueType getValueTypeOfMergeable(ValueType targetType, ValueType sourceType) {

        if(targetType != null && (targetType != ValueType.OBJECT && targetType != ValueType.ARRAY)) {
            throw new IllegalArgumentException("Target must be an Object or Array type, but was: " + targetType);
        }
        if(sourceType != null && (sourceType != ValueType.OBJECT && sourceType != ValueType.ARRAY)) {
            throw new IllegalArgumentException("Source must be an Object or Array type, but was: " + sourceType);
        }
        if(targetType != sourceType) {
            throw new IllegalArgumentException("Target and source must be of the same type, but were: "
                    + targetType + " and " + sourceType);
        }
        return sourceType;
    }

    /**
     * 하나의 ContainerValue의 내용을 다른 ContainerValue에 병합합니다.
     *
     * @param target 병합할 대상 ContainerValue
     * @param source 병합할 원본 ContainerValue
     * @throws IllegalArgumentException 대상과 원본의 값 유형이 일치하지 않는 경우
     * @throws UnsupportedOperationException 값 유형에 대해 병합 작업이 지원되지 않는 경우
     */
    public static void merge(ContainerValue target, ContainerValue source) {
        if (target == null || source == null || source.isNull()) return;
        if (target.getValueType() != source.getValueType()) {
            throw new IllegalArgumentException("Cannot merge different value types: "
                    + target.getValueType() + " vs " + source.getValueType());
        }

        if(target == source) {
            source = ContainerValues.cloneContainer(source);
        }

        switch (target.getValueType()) {
            case OBJECT:
                ObjectContainer tgtObj = target.asObject();
                ObjectContainer srcObj = source.asObject();
                mergeObjectContainer(tgtObj, srcObj);
                break;
            case ARRAY:
                ArrayContainer tgtArr = target.asArray();
                ArrayContainer srcArr = source.asArray();
                mergeArrayContainer(tgtArr, srcArr);
                break;
            default:
                throw new UnsupportedOperationException("Merge not supported for type: " + target.getValueType());
        }
    }

    private static void mergeArrayContainer(ArrayContainer target, ArrayContainer source) {
        int targetSize = target.size();
        int sourceSize = source.size();
        int size =  Math.min(targetSize, sourceSize);
        for (int i = 0; i < size; i++) {
            ContainerValue val = source.get(i);
            if(val instanceof ObjectContainer) {
                ContainerValue targetVal = target.get(i);
                if(targetVal instanceof ObjectContainer) {
                    mergeObjectContainer((ObjectContainer)targetVal, (ObjectContainer)val);
                } else {
                    target.put(i, val);
                }
            } else if(val instanceof ArrayContainer) {
                ContainerValue targetVal = target.get(i);
                if(targetVal instanceof ArrayContainer) {
                    mergeArrayContainer((ArrayContainer)targetVal, (ArrayContainer)val);
                } else {
                    target.put(i, val);
                }
            } else {
                target.put(i, val);
            }
        }
        if(targetSize < sourceSize) {
            for (int i = targetSize; i < sourceSize; i++) {
                ContainerValue val = source.get(i);
                if(val instanceof ObjectContainer) {
                    target.putCopy((ObjectContainer)val);
                } else if(val instanceof ArrayContainer) {
                    target.putCopy((ArrayContainer)val);
                } else {
                    addValue(target, val);
                }
            }
        }
    }

    private static void mergeObjectContainer(ObjectContainer target, ObjectContainer source) {
        for (String key : source.keySet()) {
            ContainerValue val = source.get(key);
            if(val instanceof ObjectContainer) {
                ContainerValue targetVal = target.get(key);
                if(targetVal instanceof ObjectContainer) {
                    mergeObjectContainer((ObjectContainer)targetVal, (ObjectContainer)val);
                    continue;
                }
            }
            addValue(target, key, val);
        }

    }

    /**
     * 두 ContainerValue 객체의 교집합을 계산합니다.
     *
     * @param a 첫 번째 ContainerValue
     * @param b 두 번째 ContainerValue
     * @return 두 입력 값의 교집합을 나타내는 새로운 ContainerValue
     */
    public static ContainerValue intersection(ContainerValue a, ContainerValue b) {
        if (a == null || b == null || a.isNull() || b.isNull()) return new PrimitiveValue(null);
        if (a.getValueType() != b.getValueType()) return new PrimitiveValue(null);

        switch (a.getValueType()) {
            case PRIMITIVE:
                return Objects.equals(a.raw(), b.raw()) ? new PrimitiveValue(a.raw()) : new PrimitiveValue(null);
            case OBJECT:
                ObjectContainer objA = a.asObject();
                ObjectContainer objB = b.asObject();
                ObjectContainer result = objA.getContainerFactory().newObject();
                for (String key : objA.keySet()) {
                    if (objB.containsKey(key)) {
                        ContainerValue valA = objA.get(key);
                        ContainerValue valB = objB.get(key);
                        
                        if (valA != null && valB != null) {
                            if (valA.getValueType() == valB.getValueType()) {
                                if (valA.isObject()) {
                                    // Deep intersection for nested objects
                                    ContainerValue intersected = intersection(valA, valB);
                                    if (intersected != null && intersected.isObject() && !intersected.asObject().isEmpty()) {
                                        result.put(key, intersected);
                                    }
                                } else if (valA.isArray()) {
                                    // Deep intersection for nested arrays
                                    ContainerValue intersected = intersection(valA, valB);
                                    if (intersected != null && intersected.isArray()) {
                                        result.put(key, intersected);
                                    }
                                } else if (equals(valA, valB)) {
                                    // For primitives and equal values
                                    addValue(result, key, valA);
                                }
                            }
                        }
                    }
                }
                return result;
            case ARRAY:
                ArrayContainer arrA = a.asArray();
                ArrayContainer arrB = b.asArray();
                ArrayContainer resultArr = arrA.getContainerFactory().newArray();
                int size = Math.min(arrA.size(), arrB.size());
                for (int i = 0; i < size; i++) {
                    ContainerValue valA = arrA.get(i);
                    ContainerValue valB = arrB.get(i);
                    
                    if (valA != null && valB != null) {
                        if (valA.getValueType() == valB.getValueType()) {
                            if (valA.isObject()) {
                                // Deep intersection for nested objects
                                ContainerValue intersected = intersection(valA, valB);
                                if (intersected != null && intersected.isObject() && !intersected.asObject().isEmpty()) {
                                    resultArr.put(intersected);
                                }
                            } else if (valA.isArray()) {
                                // Deep intersection for nested arrays
                                ContainerValue intersected = intersection(valA, valB);
                                if (intersected != null && intersected.isArray() && !intersected.asArray().isEmpty()) {
                                    resultArr.put(intersected);
                                }
                            } else if (equals(valA, valB)) {
                                // For primitives, only include if equal
                                addValue(resultArr, valA);
                            }
                        }
                    }
                }
                return resultArr;
            default:
                return new PrimitiveValue(null);
        }
    }

    /**
     * 두 ContainerValue 객체의 차집합을 계산합니다.
     *
     * @param a 첫 번째 ContainerValue
     * @param b 두 번째 ContainerValue
     * @return 두 입력 값의 차집합을 나타내는 새로운 ContainerValue
     */
    public static ContainerValue diff(ContainerValue a, ContainerValue b) {
        if (a == null || a.isNull()) return new PrimitiveValue(null);
        if (b == null || b.isNull()) return a;
        if (a.getValueType() != b.getValueType()) return a;

        switch (a.getValueType()) {
            case PRIMITIVE:
                return Objects.equals(a.raw(), b.raw()) ? new PrimitiveValue(null) : new PrimitiveValue(a.raw());
            case OBJECT:
                ObjectContainer objA = a.asObject();
                ObjectContainer objB = b.asObject();
                ObjectContainer result = objA.getContainerFactory().newObject();
                for (String key : objA.keySet()) {
                    if (!objB.containsKey(key)) {
                        // Key exists only in a
                        ContainerValue val = objA.get(key);
                        addValue(result, key, val);
                    } else {
                        ContainerValue valA = objA.get(key);
                        ContainerValue valB = objB.get(key);
                        
                        // Deep diff for nested objects and arrays
                        if (valA != null && valB != null) {
                            if (valA.isObject() && valB.isObject()) {
                                ContainerValue diffed = diff(valA, valB);
                                if (diffed != null && diffed.isObject() && !diffed.asObject().isEmpty()) {
                                    result.put(key, diffed);
                                }
                            } else if (valA.isArray() && valB.isArray()) {
                                ContainerValue diffed = diff(valA, valB);
                                if (diffed != null && diffed.isArray() && !diffed.asArray().isEmpty()) {
                                    result.put(key, diffed);
                                }
                            } else if (!equals(valA, valB)) {
                                addValue(result, key, valA);
                            }
                        } else if (!equals(valA, valB)) {
                            addValue(result, key, valA);
                        }
                    }
                }
                return result;
            case ARRAY:
                ArrayContainer arrA = a.asArray();
                ArrayContainer arrB = b.asArray();
                ArrayContainer resultArr = arrA.getContainerFactory().newArray();
                int size = Math.min(arrA.size(), arrB.size());
                for (int i = 0; i < size; i++) {
                    ContainerValue valA = arrA.get(i);
                    ContainerValue valB = arrB.get(i);
                    
                    if (valA != null && valB != null) {
                        if (valA.getValueType() == valB.getValueType()) {
                            if (valA.isObject()) {
                                // Deep diff for nested objects
                                ContainerValue diffed = diff(valA, valB);
                                if (diffed != null && diffed.isObject() && !diffed.asObject().isEmpty()) {
                                    resultArr.put(diffed);
                                }
                            } else if (valA.isArray()) {
                                // Deep diff for nested arrays
                                ContainerValue diffed = diff(valA, valB);
                                if (diffed != null && diffed.isArray() && !diffed.asArray().isEmpty()) {
                                    resultArr.put(diffed);
                                }
                            } else if (!equals(valA, valB)) {
                                // For primitives, only include if different
                                addValue(resultArr, valA);
                            }
                        } else {
                            // Different types
                            addValue(resultArr, valA);
                        }
                    } else if (valA != null && valB == null) {
                        // valB is null
                        addValue(resultArr, valA);
                    }
                }
                // Include elements that exist only in a
                for (int i = arrB.size(), arrASize = arrA.size(); i < arrASize; i++) {
                    ContainerValue val = arrA.get(i);
                    addValue(resultArr, val);
                }
                return resultArr;
            default:
                return new PrimitiveValue(null);
        }
    }

    /**
     * ArrayContainer에 값을 추가합니다.
     *
     * @param resultArr 값을 추가할 ArrayContainer
     * @param val 추가할 값
     */
    private static void addValue(ArrayContainer resultArr, ContainerValue val) {
        if(val == null) {
            resultArr.put(new PrimitiveValue(null));
        }
        else if (val.isPrimitive()) {
            resultArr.put(new PrimitiveValue(val.raw()));
        } else if (val.isObject()) {
            resultArr.putCopy(val.asObject());
        } else if (val.isArray()) {
            resultArr.putCopy(val.asArray());
        }
    }





    /**
     * ObjectContainer에 값을 추가합니다.
     *
     * @param resultObj 값을 추가할 ObjectContainer
     * @param key 값을 추가할 키
     * @param val 추가할 값
     */
    private static void addValue(ObjectContainer resultObj,String key, ContainerValue val) {
        if(val == null) {
            resultObj.put(key, new PrimitiveValue(null));
        }
        else if (val.isPrimitive()) {
            resultObj.put(key,new PrimitiveValue(val.raw()));
        } else if (val.isObject()) {
            resultObj.putCopy(key,val.asObject());
        } else if (val.isArray()) {
            resultObj.putCopy(key,val.asArray());
        }
    }


    public static ObjectContainer mapToObjectContainer(ContainerFactoryProvidable containerFactoryProvidable, Map<?, ?> map) {
        ContainerFactory containerFactory = containerFactoryProvidable.getContainerFactory();
        ObjectContainer objectContainer;
        if(containerFactoryProvidable instanceof ContainerValue) {
            objectContainer = containerFactory.newObject((ContainerValue)containerFactoryProvidable);
        } else {
            objectContainer = containerFactory.newObject();
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) {
                objectContainer.put(key, mapToObjectContainer(containerFactoryProvidable, (Map<?, ?>)value));
            } else if (value instanceof Collection) {
                objectContainer.put(key, collectionToArrayContainer(containerFactoryProvidable,(Collection<?>)value));
            } else {
                objectContainer.put(key, value);
            }
        }
        return objectContainer;
    }


    public static ArrayContainer collectionToArrayContainer(ContainerFactoryProvidable containerFactoryProvidable, Collection<?> list) {
        ContainerFactory containerFactory = containerFactoryProvidable.getContainerFactory();
        ArrayContainer arrayContainer;
        if(containerFactoryProvidable instanceof ContainerValue) {
            arrayContainer = containerFactory.newArray((ContainerValue)containerFactoryProvidable);
        } else {
            arrayContainer = containerFactory.newArray();
        }

        for (Object value : list) {
            if (value instanceof Map) {
                arrayContainer.put(mapToObjectContainer(containerFactoryProvidable, (Map<?, ?>)value));
            } else if (value instanceof Collection) {
                arrayContainer.put(collectionToArrayContainer(containerFactoryProvidable, (Collection<?>)value));
            } else {
                arrayContainer.put(value);
            }
        }
        return arrayContainer;
    }



}
