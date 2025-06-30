package com.hancomins.jsn4j.fastjson2;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hancomins.jsn4j.*;

import java.util.*;

public class Fastjson2Array implements ArrayContainer {
    
    private final JSONArray jsonArray;
    private Fastjson2Writer writer;
    
    public Fastjson2Array() {
        this.jsonArray = new JSONArray();
    }
    
    public Fastjson2Array(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }
    
    /**
     * 정적 팩토리 메서드 - JSONArray를 Fastjson2Array로 래핑
     */
    public static Fastjson2Array wrap(JSONArray jsonArray) {
        return new Fastjson2Array(jsonArray);
    }
    
    /**
     * Fastjson2의 JSONArray를 직접 반환
     */
    public JSONArray getJSONArray() {
        return jsonArray;
    }
    
    @Override
    public ArrayContainer put(int index, Object value) {
        ensureCapacity(index + 1);
        if (value == null) {
            jsonArray.set(index, null);
        } else if (value instanceof ContainerValue) {
            jsonArray.set(index, toFastjsonValue((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSONArray array = new JSONArray();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.add(toFastjsonValue((ContainerValue) item));
                } else {
                    array.add(item);
                }
            }
            jsonArray.set(index, array);
        } else if (value instanceof Map) {
            JSONObject object = new JSONObject();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toFastjsonValue((ContainerValue) v));
                } else {
                    object.put(String.valueOf(k), v);
                }
            });
            jsonArray.set(index, object);
        } else {
            jsonArray.set(index, value);
        }
        return this;
    }
    
    @Override
    public ArrayContainer put(Object value) {
        if (value == null) {
            jsonArray.add(null);
        } else if (value instanceof ContainerValue) {
            jsonArray.add(toFastjsonValue((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSONArray array = new JSONArray();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.add(toFastjsonValue((ContainerValue) item));
                } else {
                    array.add(item);
                }
            }
            jsonArray.add(array);
        } else if (value instanceof Map) {
            JSONObject object = new JSONObject();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toFastjsonValue((ContainerValue) v));
                } else {
                    object.put(String.valueOf(k), v);
                }
            });
            jsonArray.add(object);
        } else {
            jsonArray.add(value);
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject() {
        JSONObject newObject = new JSONObject();
        jsonArray.add(newObject);
        return new Fastjson2Object(newObject);
    }
    
    @Override
    public ArrayContainer newAndPutArray() {
        JSONArray newArray = new JSONArray();
        jsonArray.add(newArray);
        return new Fastjson2Array(newArray);
    }
    
    @Override
    public int size() {
        return jsonArray.size();
    }
    
    @Override
    public ContainerValue remove(int index) {
        if (index < 0 || index >= jsonArray.size()) {
            return null;
        }
        Object removed = jsonArray.remove(index);
        if (removed == null) {
            return null;
        }
        return Fastjson2ContainerFactory.wrap(removed);
    }
    
    @Override
    public ContainerValue get(int index) {
        if (index < 0 || index >= jsonArray.size()) {
            return null;
        }
        Object value = jsonArray.get(index);
        return Fastjson2ContainerFactory.wrap(value);
    }
    
    @Override
    public void clear() {
        jsonArray.clear();
    }
    
    @Override
    public ContainerFactory getContainerFactory() {
        return Fastjson2ContainerFactory.getInstance();
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
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
    public Iterator<ContainerValue> iterator() {
        return new Iterator<ContainerValue>() {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < jsonArray.size();
            }
            
            @Override
            public ContainerValue next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Object value = jsonArray.get(index++);
                return Fastjson2ContainerFactory.wrap(value);
            }
        };
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
        return jsonArray.hashCode();
    }
    
    /**
     * 배열 크기를 지정된 용량만큼 확장
     */
    private void ensureCapacity(int minCapacity) {
        while (jsonArray.size() < minCapacity) {
            jsonArray.add(null);
        }
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
            return ((Fastjson2Object) value).getJSONObject();
        } else if (value instanceof Fastjson2Array) {
            return ((Fastjson2Array) value).jsonArray;
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