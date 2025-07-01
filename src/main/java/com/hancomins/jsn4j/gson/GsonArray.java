package com.hancomins.jsn4j.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.hancomins.jsn4j.*;

import java.util.*;

public class GsonArray extends AbstractGsonContainer implements ArrayContainer {
    
    private final JsonArray jsonArray;
    
    public GsonArray() {
        this.jsonArray = new JsonArray();
    }
    
    public GsonArray(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }
    
    /**
     * 정적 팩토리 메서드 - JsonArray를 GsonArray로 래핑
     */
    public static GsonArray wrap(JsonArray jsonArray) {
        return new GsonArray(jsonArray);
    }
    
    /**
     * Gson의 JsonArray를 직접 반환
     */
    public JsonArray getJsonArray() {
        return jsonArray;
    }
    
    @Override
    public ArrayContainer put(int index, Object value) {
        ensureCapacity(index + 1);
        if (value == null) {
            jsonArray.set(index, com.google.gson.JsonNull.INSTANCE);
        } else if (value instanceof ContainerValue) {
            jsonArray.set(index, toJsonElement((ContainerValue) value));
        } else if (value instanceof Collection) {
            GsonArray array = new GsonArray();
            for (Object item : (Collection<?>) value) {
                array.put(item);
            }
            jsonArray.set(index, array.getJsonArray());
        } else if (value instanceof Map) {
            GsonObject object = new GsonObject();
            ((Map<?, ?>) value).forEach((k, v) -> object.put(String.valueOf(k), v));
            jsonArray.set(index, object.getJsonObject());
        } else if (value instanceof Number) {
            jsonArray.set(index, new JsonPrimitive((Number) value));
        } else if (value instanceof Boolean) {
            jsonArray.set(index, new JsonPrimitive((Boolean) value));
        } else if (value instanceof Character) {
            jsonArray.set(index, new JsonPrimitive((Character) value));
        } else if (value instanceof String) {
            jsonArray.set(index, new JsonPrimitive((String) value));
        } else if (value instanceof byte[]) {
            jsonArray.set(index, new JsonPrimitive(java.util.Base64.getEncoder().encodeToString((byte[]) value)));
        } else {
            jsonArray.set(index, new JsonPrimitive(String.valueOf(value)));
        }
        return this;
    }
    
    @Override
    public ArrayContainer put(Object value) {
        if (value == null) {
            jsonArray.add(com.google.gson.JsonNull.INSTANCE);
        } else if (value instanceof ContainerValue) {
            jsonArray.add(toJsonElement((ContainerValue) value));
        } else if (value instanceof Collection) {
            GsonArray array = new GsonArray();
            for (Object item : (Collection<?>) value) {
                array.put(item);
            }
            jsonArray.add(array.getJsonArray());
        } else if (value instanceof Map) {
            GsonObject object = new GsonObject();
            ((Map<?, ?>) value).forEach((k, v) -> object.put(String.valueOf(k), v));
            jsonArray.add(object.getJsonObject());
        } else if (value instanceof Number) {
            jsonArray.add((Number) value);
        } else if (value instanceof Boolean) {
            jsonArray.add((Boolean) value);
        } else if (value instanceof Character) {
            jsonArray.add((Character) value);
        } else if (value instanceof String) {
            jsonArray.add((String) value);
        } else if (value instanceof byte[]) {
            jsonArray.add(java.util.Base64.getEncoder().encodeToString((byte[]) value));
        } else {
            jsonArray.add(String.valueOf(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject() {
        GsonObject object = new GsonObject();
        jsonArray.add(object.getJsonObject());
        return object;
    }
    
    @Override
    public ArrayContainer newAndPutArray() {
        GsonArray array = new GsonArray();
        jsonArray.add(array.getJsonArray());
        return array;
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
        JsonElement removed = jsonArray.remove(index);
        return GsonContainerFactory.wrap(removed);
    }
    
    @Override
    public ContainerValue get(int index) {
        if (index < 0 || index >= jsonArray.size()) {
            return null;
        }
        JsonElement value = jsonArray.get(index);
        return GsonContainerFactory.wrap(value);
    }
    
    @Override
    public void clear() {
        // Gson JsonArray doesn't have a clear method, so we need to remove all elements
        while (jsonArray.size() > 0) {
            jsonArray.remove(0);
        }
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }
    
    @Override
    public ContainerWriter<? extends Enum<?>> getWriter() {
        if (writer == null) {
            writer = new GsonWriter(this);
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
                JsonElement element = jsonArray.get(index++);
                return GsonContainerFactory.wrap(element);
            }
        };
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
            jsonArray.add(com.google.gson.JsonNull.INSTANCE);
        }
    }
}