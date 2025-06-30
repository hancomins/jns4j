package com.hancomins.jsn4j;

public interface ObjectContainerBuilder extends  ContainerValue {
    ObjectContainer put(String key, char value);
    ObjectContainer put(String key, byte value);
    ObjectContainer put(String key, short value);
    ObjectContainer put(String key, int value);
    ObjectContainer put(String key, long value);
    ObjectContainer put(String key, float value);
    ObjectContainer put(String key, double value);
    ObjectContainer put(String key, boolean value);
    ObjectContainer put(String key, ContainerValue value);

    default ValueType getValueType() {
        return ValueType.OBJECT;
    }
}
