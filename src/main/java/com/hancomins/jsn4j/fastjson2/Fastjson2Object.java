package com.hancomins.jsn4j.fastjson2;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hancomins.jsn4j.*;

import java.util.*;

public class Fastjson2Object implements ObjectContainer {
    
    private final JSONObject jsonObject;
    private Fastjson2Writer writer;
    
    public Fastjson2Object() {
        this.jsonObject = new JSONObject();
    }
    
    public Fastjson2Object(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
    
    /**
     * 정적 팩토리 메서드 - JSONObject를 Fastjson2Object로 래핑
     */
    public static Fastjson2Object wrap(JSONObject jsonObject) {
        return new Fastjson2Object(jsonObject);
    }
    
    /**
     * Fastjson2의 JSONObject를 직접 반환
     */
    public JSONObject getJSONObject() {
        return jsonObject;
    }
    
    @Override
    public int size() {
        return jsonObject.size();
    }
    
    @Override
    public ObjectContainer put(String key, Object value) {
        if (value == null) {
            jsonObject.put(key, null);
        } else if (value instanceof ContainerValue) {
            jsonObject.put(key, toFastjsonValue((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSONArray array = new JSONArray();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.add(toFastjsonValue((ContainerValue) item));
                } else {
                    array.add(item);
                }
            }
            jsonObject.put(key, array);
        } else if (value instanceof Map) {
            JSONObject object = new JSONObject();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toFastjsonValue((ContainerValue) v));
                } else {
                    object.put(String.valueOf(k), v);
                }
            });
            jsonObject.put(key, object);
        } else {
            jsonObject.put(key, value);
        }
        return this;
    }
    
    @Override
    public ObjectContainer put(String key, ContainerValue value) {
        if (value == null || value.isNull()) {
            jsonObject.put(key, null);
        } else {
            jsonObject.put(key, toFastjsonValue(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject(String key) {
        JSONObject newObject = new JSONObject();
        jsonObject.put(key, newObject);
        return new Fastjson2Object(newObject);
    }
    
    @Override
    public ArrayContainer newAndPutArray(String key) {
        JSONArray newArray = new JSONArray();
        jsonObject.put(key, newArray);
        return new Fastjson2Array(newArray);
    }
    
    @Override
    public ContainerValue remove(String key) {
        Object removed = jsonObject.remove(key);
        if (removed == null) {
            return null;
        }
        return Fastjson2ContainerFactory.wrap(removed);
    }
    
    @Override
    public boolean containsKey(String key) {
        return jsonObject.containsKey(key);
    }
    
    @Override
    public void putAll(Map<String, ?> map) {
        map.forEach(this::put);
    }
    
    @Override
    public Set<Map.Entry<String, ContainerValue>> entrySet() {
        Set<Map.Entry<String, ContainerValue>> result = new LinkedHashSet<>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            result.add(new AbstractMap.SimpleEntry<>(
                entry.getKey(), 
                Fastjson2ContainerFactory.wrap(entry.getValue())
            ));
        }
        return result;
    }
    
    @Override
    public Set<String> keySet() {
        return jsonObject.keySet();
    }
    
    @Override
    public ContainerValue get(String key) {
        Object value = jsonObject.get(key);
        if (value == null && !jsonObject.containsKey(key)) {
            return null;
        }
        return Fastjson2ContainerFactory.wrap(value);
    }
    
    @Override
    public boolean has(String key) {
        return jsonObject.containsKey(key);
    }
    
    @Override
    public void clear() {
        jsonObject.clear();
    }
    
    @Override
    public ContainerFactory getContainerFactory() {
        return Fastjson2ContainerFactory.getInstance();
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
        if (writer == null) {
            writer = new Fastjson2Writer(this);
        }
        return writer;
    }
    
    @Override
    public Iterator<Map.Entry<String, ContainerValue>> iterator() {
        return entrySet().iterator();
    }
    
    @Override
    public String toString() {
        return getWriter().write();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContainerValue)) {
            return false;
        }
        return ContainerValues.equals(this, (ContainerValue) o);
    }
    
    @Override
    public int hashCode() {
        return jsonObject.hashCode();
    }
    
    /**
     * ContainerValue를 Fastjson2 값으로 변환
     */
    private Object toFastjsonValue(ContainerValue value) {
        if (value == null || value.isNull()) {
            return null;
        } else if (value.isPrimitive()) {
            return value.raw();
        } else if (value instanceof Fastjson2Object) {
            return ((Fastjson2Object) value).jsonObject;
        } else if (value instanceof Fastjson2Array) {
            return ((Fastjson2Array) value).getJSONArray();
        } else if (value.isObject()) {
            // 다른 구현체의 ObjectContainer 변환
            JSONObject object = new JSONObject();
            ObjectContainer obj = value.asObject();
            for (Map.Entry<String, ContainerValue> entry : obj) {
                object.put(entry.getKey(), toFastjsonValue(entry.getValue()));
            }
            return object;
        } else if (value.isArray()) {
            // 다른 구현체의 ArrayContainer 변환
            JSONArray array = new JSONArray();
            ArrayContainer arr = value.asArray();
            for (ContainerValue item : arr) {
                array.add(toFastjsonValue(item));
            }
            return array;
        }
        return value.raw();
    }
}