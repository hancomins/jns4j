package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import java.util.ArrayList;
import java.util.Iterator;

public class SimpleArray implements ArrayContainer {
    private final ArrayList<ContainerValue> values = new ArrayList<>();
    private SimpleJsonWriter jsonWriter;

    @Override
    public ArrayContainer put(int index, Object value) {
        if(value instanceof ContainerValue) {
            ensure(index + 1);
            values.set(index, (ContainerValue)value);
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
    public ContainerFactory getFactory() {
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
}
