package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import java.util.*;

public class SimpleArray extends AbstractSimpleContainer implements ArrayContainer {
    private final ArrayList<ContainerValue> values;

    public SimpleArray() {
        this.values = new ArrayList<>();
    }

    public SimpleArray(String jsonArray) {
        ContainerValue containerValue = getContainerFactory().getParser().parse(jsonArray);
        if(!containerValue.isArray()) {
            throw new IllegalArgumentException("Invalid JSON array: " + jsonArray);
        }
        this.values = new ArrayList<>( ((SimpleArray)containerValue.asArray()).values);
    }

    @Override
    public ArrayContainer put(int index, Object value) {
        ensure(index + 1);
        values.set(index, convertValue(value));
        return this;
    }


    private void ensure(int capacity) {
        if (capacity > values.size()) {
            values.ensureCapacity(capacity);
            for (int i = values.size(); i < capacity; i++) {
                values.add(null);
            }
        }

    }

    @Override
    public ArrayContainer put(Object value) {
        values.add(convertValue(value));
        return this;
    }

    @Override
    public ObjectContainer newAndPutObject() {
        SimpleObject object = new SimpleObject();
        values.add(object);
        return object;
    }

    @Override
    public ArrayContainer newAndPutArray() {
        SimpleArray array = new SimpleArray();
        values.add(array);
        return array;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public ContainerValue remove(int index) {
        if(index < 0 || index >= values.size()) {
            return null;
        }
        ContainerValue value = values.get(index);
        values.remove(index);
        return value;
    }

    @Override
    public ContainerValue get(int index) {
        return values.get(index);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    @Override
    public Iterator<ContainerValue> iterator() {
        return values.iterator();
    }


}
