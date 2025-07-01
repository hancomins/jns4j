package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import java.util.*;

public class SimpleObject extends AbstractSimpleContainer implements ObjectContainer {



    private final HashMap<String, ContainerValue> objectMap;

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
        objectMap.put(key, convertValue(value));
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
    public Iterator<Map.Entry<String, ContainerValue>> iterator() {
        return objectMap.entrySet().iterator();
    }


}
