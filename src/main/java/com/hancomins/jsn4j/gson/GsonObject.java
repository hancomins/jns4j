package com.hancomins.jsn4j.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hancomins.jsn4j.*;

import java.util.*;

public class GsonObject extends AbstractGsonContainer implements ObjectContainer {
    
    private final JsonObject jsonObject;
    
    public GsonObject() {
        this.jsonObject = new JsonObject();
    }
    
    public GsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }
    
    /**
     * 정적 팩토리 메서드 - JsonObject를 GsonObject로 래핑
     */
    public static GsonObject wrap(JsonObject jsonObject) {
        return new GsonObject(jsonObject);
    }
    
    /**
     * Gson의 JsonObject를 직접 반환
     */
    public JsonObject getJsonObject() {
        return jsonObject;
    }
    
    @Override
    public int size() {
        return jsonObject.size();
    }
    
    @Override
    public ObjectContainer put(String key, Object value) {
        if (value == null) {
            jsonObject.add(key, com.google.gson.JsonNull.INSTANCE);
        } else if (value instanceof ContainerValue) {
            jsonObject.add(key, toJsonElement((ContainerValue) value));
        } else if (value instanceof Collection) {
            GsonArray array = new GsonArray();
            for (Object item : (Collection<?>) value) {
                array.put(item);
            }
            jsonObject.add(key, array.getJsonArray());
        } else if (value instanceof Map) {
            GsonObject object = new GsonObject();
            ((Map<?, ?>) value).forEach((k, v) -> object.put(String.valueOf(k), v));
            jsonObject.add(key, object.getJsonObject());
        } else if (value instanceof Number) {
            jsonObject.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            jsonObject.addProperty(key, (Boolean) value);
        } else if (value instanceof Character) {
            jsonObject.addProperty(key, (Character) value);
        } else if (value instanceof String) {
            jsonObject.addProperty(key, (String) value);
        } else if (value instanceof byte[]) {
            jsonObject.addProperty(key, java.util.Base64.getEncoder().encodeToString((byte[]) value));
        } else {
            jsonObject.addProperty(key, String.valueOf(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer put(String key, ContainerValue value) {
        if (value == null || value.isNull()) {
            jsonObject.add(key, com.google.gson.JsonNull.INSTANCE);
        } else {
            jsonObject.add(key, toJsonElement(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject(String key) {
        GsonObject newObject = new GsonObject();
        jsonObject.add(key, newObject.getJsonObject());
        return newObject;
    }
    
    @Override
    public ArrayContainer newAndPutArray(String key) {
        GsonArray newArray = new GsonArray();
        jsonObject.add(key, newArray.getJsonArray());
        return newArray;
    }
    
    @Override
    public ContainerValue remove(String key) {
        JsonElement removed = jsonObject.remove(key);
        if (removed == null) {
            return null;
        }
        return GsonContainerFactory.wrap(removed);
    }
    
    @Override
    public boolean containsKey(String key) {
        return jsonObject.has(key);
    }
    
    @Override
    public void putAll(Map<String, ?> map) {
        map.forEach(this::put);
    }
    
    @Override
    public Set<Map.Entry<String, ContainerValue>> entrySet() {
        Set<Map.Entry<String, ContainerValue>> result = new LinkedHashSet<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            result.add(new AbstractMap.SimpleEntry<>(
                entry.getKey(),
                GsonContainerFactory.wrap(entry.getValue())
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
        JsonElement value = jsonObject.get(key);
        if (value == null) {
            return null;
        }
        return GsonContainerFactory.wrap(value);
    }
    
    @Override
    public boolean has(String key) {
        return jsonObject.has(key);
    }
    
    @Override
    public void clear() {
        // Gson JsonObject doesn't have a clear method, so we need to remove all entries
        Set<String> keys = new HashSet<>(jsonObject.keySet());
        for (String key : keys) {
            jsonObject.remove(key);
        }
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.OBJECT;
    }
    
    @Override
    public ContainerWriter<? extends Enum<?>> getWriter() {
        if (writer == null) {
            writer = new GsonWriter(this);
        }
        return writer;
    }
    
    @Override
    public Iterator<Map.Entry<String, ContainerValue>> iterator() {
        return entrySet().iterator();
    }
    
    @Override
    public int hashCode() {
        return jsonObject.hashCode();
    }
}