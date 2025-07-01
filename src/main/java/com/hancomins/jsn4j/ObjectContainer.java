package com.hancomins.jsn4j;

import java.util.Map;
import java.util.Set;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface ObjectContainer extends ContainerValue, Iterable<Map.Entry<String, ContainerValue>>, ContainerFactoryProvidable {

    int size();




    ObjectContainer put(String key, Object value);


    default ObjectContainer putIfAbsent(String key, Object value) {
        if (!containsKey(key)) {
            return put(key, value);
        }
        return this;
    }

    default ObjectContainer putIfPresent(String key, Object value) {
        if (containsKey(key)) {
            return put(key, value);
        }
        return this;
    }

    default ObjectContainer put(String key, char value) {
        return put(key, getContainerFactory().newPrimitive(value));
    }
    default ObjectContainer put(String key, byte value) {
        return put(key, getContainerFactory().newPrimitive(value));
    }
    default ObjectContainer put(String key, short value) {
        return put(key, getContainerFactory().newPrimitive(value));
    }


    /**
     * 객체를 생성하고, 키에 매핑하여 반환합니다.
     * @param key 키
     * @return 생성된 객체. ⚠️ 반환되는 객체는 자기 자신이 아닌 자식 객체입니다.
     */
    ObjectContainer newAndPutObject(String key);

    /**
     * Array 타입의 객체를 생성하고, 키에 매핑하여 반환합니다.
     * @param key 키
     * @return 생성된 객체. ⚠️ 반환되는 객체는 자기 자신이 아닌 자식 객체입니다.
     */
    ArrayContainer newAndPutArray(String key);


    default ObjectContainer put(String key, int value) {
        return put(key, getContainerFactory().newPrimitive(value));
    }
    default ObjectContainer put(String key, long value) {
        return put(key, getContainerFactory().newPrimitive(value));
    }
    default ObjectContainer put(String key, float value) {
        return put(key, getContainerFactory().newPrimitive(value));
    }
    default ObjectContainer put(String key, double value) {
        return put(key, getContainerFactory().newPrimitive(value));
    }
    default ObjectContainer put(String key, boolean value) {
        return put(key, getContainerFactory().newPrimitive(value));
    }
    ObjectContainer put(String key, ContainerValue value);



    ContainerValue remove(String key);
    boolean containsKey(String key);
    void putAll(Map<String, ?> map);

    default boolean isEmpty() {
        return size() == 0;
    }






    Set<Map.Entry<String, ContainerValue>> entrySet();

    Set<String> keySet();


    /**
     * 문자열 키로 값 접근 (내부적으로 key table index 매핑 필요)
     */
    ContainerValue get(String key);

    /**
     * 필드 존재 여부
     */
    boolean has(String key);

    // ---- 편의 메서드 (문자열) ----

    default String getString(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return null;
        }
        else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asString();
        } else {
            return String.valueOf(ContainerValue);
        }
    }



    default ContainerValue getOrDefault(String key, ContainerValue defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        }
        return ContainerValue;
    }

    default String getString(String key, String defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asString();
        } else {
            return defaultValue;
        }
    }

    // ---- 편의 메서드 (불리언) ----

    default boolean getBoolean(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return false;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asBoolean();
        } else {
            return Boolean.parseBoolean(String.valueOf(ContainerValue));
        }
    }

    default boolean getBoolean(String key, boolean defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asBooleanOr(defaultValue);
        } else {
            return defaultValue;
        }
    }

    // ---- 숫자형 ----

    default int getInt(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return Integer.MIN_VALUE;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asInt();
        } else {
            return Integer.MIN_VALUE;
        }
    }

    default int getInt(String key, int defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asIntOr(defaultValue);
        } else {
            return defaultValue;
        }
    }

    default long getLong(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return Long.MIN_VALUE;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asLong();
        } else {
            return Long.MIN_VALUE;
        }
    }

    default long getLong(String key, long defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asLongOr(defaultValue);
        } else {
            return defaultValue;
        }
    }

    default float getFloat(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return Float.NaN;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asFloat();
        } else {
            return Float.NaN;
        }
    }
    default float getFloat(String key, float defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asFloatOr(defaultValue);
        } else {
            return defaultValue;
        }
    }

    default double getDouble(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return Double.NaN;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asDouble();
        } else {
            return Double.NaN;
        }
    }
    default double getDouble(String key, double defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asDoubleOr(defaultValue);
        } else {
            return defaultValue;
        }
    }

    // ---- 바이트 배열 ----

    default byte[] getByteArray(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return null;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asByteArray();
        } else {
            return null;
        }
    }
    default byte[] getByteArray(String key, byte[] defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isPrimitive()) {
            return ((PrimitiveValue)ContainerValue).asByteArrayOr(defaultValue);
        } else {
            return defaultValue;
        }
    }


    default ObjectContainer getObject(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return null;
        } else if(ContainerValue.isObject()) {
            return (ObjectContainer) ContainerValue;
        } else {
            return null;
        }
    }

    default ObjectContainer getObject(String key, ObjectContainer defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isObject()) {
            return (ObjectContainer) ContainerValue;
        } else {
            return defaultValue;
        }
    }

    default ObjectContainer getObjectOrNew(String key) {
        ObjectContainer objectContainer = getObject(key);
        if(objectContainer == null) {
            objectContainer = this.newAndPutObject(key);
        }
        return objectContainer;
    }


    default ArrayContainer getArray(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return null;
        } else if(ContainerValue.isArray()) {
            return (ArrayContainer) ContainerValue;
        } else {
            return null;
        }
    }

    default ArrayContainer getArray(String key, ArrayContainer defaultValue) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return defaultValue;
        } else if(ContainerValue.isArray()) {
            return (ArrayContainer) ContainerValue;
        } else {
            return defaultValue;
        }
    }

    default ArrayContainer getArrayOrNew(String key) {
        ArrayContainer arrayContainer = getArray(key);
        if(arrayContainer == null) {
            arrayContainer = this.newAndPutArray(key);
        }
        return arrayContainer;
    }

    default Object getRaw(String key) {
        ContainerValue ContainerValue = get(key);
        if(ContainerValue == null) {
            return null;
        }
        return ContainerValue.raw();
    }

    default Map<String, Object> toRawMap() {
        Map<String, Object> result = new java.util.HashMap<>();
        for (Map.Entry<String, ContainerValue> entry : entrySet()) {
            String key = entry.getKey();
            ContainerValue value = entry.getValue();
            Object raw = value.raw();
            if (raw instanceof ObjectContainer) {
                result.put(key, ((ObjectContainer) raw).toRawMap());
            } else if (raw instanceof ArrayContainer) {
                result.put(key, ((ArrayContainer) raw).toRawList());
            } else {
                result.put(key, raw);
            }
        }
        return result;

    }

    void clear();


    default ObjectContainer putCopy(String key, ObjectContainer source) {
        ObjectContainer child = this.newAndPutObject(key);
        ContainerValues.copy(child, source);
        return child;
    }

    default ArrayContainer putCopy(String key, ArrayContainer source) {
        ArrayContainer child = this.newAndPutArray(key);
        ContainerValues.copy(child, source);
        return child;
    }


}
