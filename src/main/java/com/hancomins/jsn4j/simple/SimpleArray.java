package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import java.util.*;

public class SimpleArray implements ArrayContainer {
    private final ArrayList<ContainerValue> values;
    private SimpleJsonWriter jsonWriter;

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
        if(value instanceof ContainerValue) {
            ensure(index + 1);
            values.set(index, (ContainerValue)value);
            return this;
        } else if(value instanceof Collection) {
            ensure(index + 1);
            values.set(index,ContainerValues.collectionToArrayContainer(this, (Collection<?>)value));
            return this;
        } else if(value instanceof Map) {
            ensure(index + 1);
            values.set(index,ContainerValues.mapToObjectContainer(this, (Map<?,?>)value));
            return this;
        }
        PrimitiveValue containerValue = new PrimitiveValue(value);
        ensure(index + 1);
        values.set(index, containerValue);
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
        if(value instanceof ContainerValue) {
            values.add((ContainerValue)value);
            return this;
        } else if(value instanceof Collection) {
            values.add(ContainerValues.collectionToArrayContainer(this, (Collection<?>)value));
        } else if(value instanceof Map) {
            values.add(ContainerValues.mapToObjectContainer(this, (Map<?,?>)value));
        }
        PrimitiveValue containerValue = new PrimitiveValue(value);
        values.add(containerValue);
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
    public ContainerFactory getContainerFactory() {
        return SimpleJsonContainerFactory.getInstance();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    @Override
    public ContainerWriter<? extends Enum<?>> getWriter() {
        if(jsonWriter == null) {
            jsonWriter = new SimpleJsonWriter(this);
        }
        return jsonWriter;
    }

    @Override
    public Iterator<ContainerValue> iterator() {
        return values.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ContainerValue)) {
            return false;
        }
        return ContainerValues.equals(this, (ContainerValue) o);
    }

    @Override
    public String toString() {
        return getWriter().write();
    }
}
