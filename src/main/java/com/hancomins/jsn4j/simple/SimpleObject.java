package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import java.util.*;

public class SimpleObject implements ObjectContainer {



    private final HashMap<String, ContainerValue> objectMap;
    private SimpleJsonWriter jsonWriter;


    public SimpleObject() {
        this.objectMap = new HashMap<>();
    }

    public SimpleObject(String jsonObject) {
        SimpleJsonParser parser = new SimpleJsonParser();
        ContainerValue containerValue = parser.parse(jsonObject);
        if(!containerValue.isObject()) {
            throw new IllegalArgumentException("Invalid JSON object: " + jsonObject);
        }
        this.objectMap = ((SimpleObject)containerValue.asObject()).objectMap;
    }


    @Override
    public int size() {
        return objectMap.size();
    }

    @Override
    public ObjectContainer put(String key, Object value) {
        if (value instanceof ContainerValue) {
            objectMap.put(key, (ContainerValue) value);
        } else if(value instanceof Collection) {
            objectMap.put(key, ContainerValues.collectionToArrayContainer(this, (Collection<?>) value));
        } else if(value instanceof Map) {
            objectMap.put(key, ContainerValues.mapToObjectContainer(this, (Map<?, ?>) value));
        }
        else {
            PrimitiveValue primitiveValue = new PrimitiveValue(value);
            objectMap.put(key, primitiveValue);
        }
        return this;
    }

    @Override
    public ObjectContainer newAndPutObject(String key) {
        SimpleObject newObject = new SimpleObject();
        objectMap.put(key, newObject);
        return newObject;
    }

    @Override
    public ArrayContainer newAndPutArray(String key) {
        SimpleArray array = new SimpleArray();
        objectMap.put(key, array);
        return array;
    }

    @Override
    public ObjectContainer put(String key, ContainerValue value) {
        objectMap.put(key, value);
        return this;
    }

    @Override
    public ContainerValue remove(String key) {
        return objectMap.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return objectMap.containsKey(key);
    }

    @Override
    public void putAll(Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            put(key, value);
        }
    }



    @Override
    public Set<Map.Entry<String, ContainerValue>> entrySet() {
        return objectMap.entrySet();
    }

    @Override
    public Set<String> keySet() {
        return objectMap.keySet();
    }


    @Override
    public ContainerValue get(String key) {
        return objectMap.get(key);
    }

    @Override
    public boolean has(String key) {
        return objectMap.containsKey(key);
    }

    @Override
    public void clear() {
        this.objectMap.clear();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.OBJECT;
    }

    @Override
    public Object raw() {
        return this;
    }

    @Override
    public ContainerWriter<? extends Enum<?>> getWriter() {
        if(jsonWriter == null) {
            jsonWriter = new SimpleJsonWriter(this);
        }
        return jsonWriter;
    }

    @Override
    public Iterator<Map.Entry<String, ContainerValue>> iterator() {
        return objectMap.entrySet().iterator();
    }


    @Override
    public String toString() {
        return getWriter().write();
    }

    @Override
    public ContainerFactory getContainerFactory() {
        return SimpleJsonContainerFactory.getInstance();
    }


    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ContainerValue)) {
            return false;
        }
        return ContainerValues.equals(this, (ContainerValue) o);
    }


}
