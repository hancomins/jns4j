package com.hancomins.jsn4j.json5;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.jsn4j.*;

import java.util.*;

public class Json5Object implements ObjectContainer {
    
    private final JSON5Object json5Object;
    private Json5Writer writer;
    
    public Json5Object() {
        this.json5Object = new JSON5Object();
    }
    
    public Json5Object(JSON5Object json5Object) {
        this.json5Object = json5Object;
    }
    
    /**
     * 정적 팩토리 메서드 - JSON5Object를 Json5Object로 래핑
     */
    public static Json5Object wrap(JSON5Object json5Object) {
        return new Json5Object(json5Object);
    }
    
    /**
     * JSON5Object를 직접 반환
     */
    public JSON5Object getJSON5Object() {
        return json5Object;
    }
    
    @Override
    public int size() {
        return json5Object.size();
    }
    
    @Override
    public ObjectContainer put(String key, Object value) {
        if (value == null) {
            json5Object.put(key, (Object) null);
        } else if (value instanceof ContainerValue) {
            json5Object.put(key, toJson5Value((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSON5Array array = new JSON5Array();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.add(toJson5Value((ContainerValue) item));
                } else {
                    array.add(item);
                }
            }
            json5Object.put(key, array);
        } else if (value instanceof Map) {
            JSON5Object object = new JSON5Object();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toJson5Value((ContainerValue) v));
                } else {
                    object.put(String.valueOf(k), v);
                }
            });
            json5Object.put(key, object);
        } else {
            json5Object.put(key, value);
        }
        return this;
    }
    
    @Override
    public ObjectContainer put(String key, ContainerValue value) {
        if (value == null || value.isNull()) {
            json5Object.put(key, (Object) null);
        } else {
            json5Object.put(key, toJson5Value(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject(String key) {
        JSON5Object newObject = new JSON5Object();
        json5Object.put(key, newObject);
        return new Json5Object(newObject);
    }
    
    @Override
    public ArrayContainer newAndPutArray(String key) {
        JSON5Array newArray = new JSON5Array();
        json5Object.put(key, newArray);
        return new Json5Array(newArray);
    }
    
    @Override
    public ContainerValue remove(String key) {
        Object removed = json5Object.remove(key);
        if (removed == null) {
            return null;
        }
        return Json5ContainerFactory.wrap(removed);
    }
    
    @Override
    public boolean containsKey(String key) {
        return json5Object.has(key);
    }
    
    @Override
    public void putAll(Map<String, ?> map) {
        map.forEach(this::put);
    }
    
    @Override
    public Set<Map.Entry<String, ContainerValue>> entrySet() {
        Set<Map.Entry<String, ContainerValue>> result = new LinkedHashSet<>();
        for (Map.Entry<String, Object> entry : json5Object.entrySet()) {
            result.add(new AbstractMap.SimpleEntry<>(
                entry.getKey(), 
                Json5ContainerFactory.wrap(entry.getValue())
            ));
        }
        return result;
    }
    
    @Override
    public Set<String> keySet() {
        return json5Object.keySet();
    }
    
    @Override
    public ContainerValue get(String key) {
        if (!json5Object.has(key)) {
            return null;
        }
        Object value = json5Object.get(key);
        return Json5ContainerFactory.wrap(value);
    }
    
    @Override
    public boolean has(String key) {
        return json5Object.has(key);
    }
    
    @Override
    public void clear() {
        json5Object.clear();
    }
    
    @Override
    public ContainerFactory getContainerFactory() {
        return Json5ContainerFactory.getInstance();
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
            writer = new Json5Writer(this);
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
        return json5Object.hashCode();
    }
    
    /**
     * ContainerValue를 JSON5 값으로 변환
     */
    private Object toJson5Value(ContainerValue value) {
        if (value == null || value.isNull()) {
            return null;
        } else if (value.isPrimitive()) {
            return value.raw();
        } else if (value instanceof Json5Object) {
            return ((Json5Object) value).json5Object;
        } else if (value instanceof Json5Array) {
            return ((Json5Array) value).getJSON5Array();
        } else if (value.isObject()) {
            // 다른 구현체의 ObjectContainer 변환
            JSON5Object object = new JSON5Object();
            ObjectContainer obj = value.asObject();
            for (Map.Entry<String, ContainerValue> entry : obj) {
                object.put(entry.getKey(), toJson5Value(entry.getValue()));
            }
            return object;
        } else if (value.isArray()) {
            // 다른 구현체의 ArrayContainer 변환
            JSON5Array array = new JSON5Array();
            ArrayContainer arr = value.asArray();
            for (ContainerValue item : arr) {
                array.add(toJson5Value(item));
            }
            return array;
        }
        return value.raw();
    }
}