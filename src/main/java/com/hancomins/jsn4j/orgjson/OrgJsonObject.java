package com.hancomins.jsn4j.orgjson;

import org.json.JSONArray;
import org.json.JSONObject;
import com.hancomins.jsn4j.*;

import java.util.*;

public class OrgJsonObject implements ObjectContainer {
    
    private final JSONObject jsonObject;
    private OrgJsonWriter writer;
    
    public OrgJsonObject() {
        this.jsonObject = new JSONObject();
    }
    
    public OrgJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
    
    /**
     * 정적 팩토리 메서드 - JSONObject를 OrgJsonObject로 래핑
     */
    public static OrgJsonObject wrap(JSONObject jsonObject) {
        return new OrgJsonObject(jsonObject);
    }
    
    /**
     * org.json의 JSONObject를 직접 반환
     */
    public JSONObject getJSONObject() {
        return jsonObject;
    }
    
    @Override
    public int size() {
        return jsonObject.length();
    }
    
    @Override
    public ObjectContainer put(String key, Object value) {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else if (value instanceof ContainerValue) {
            jsonObject.put(key, toOrgJsonValue((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSONArray array = new JSONArray();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.put(toOrgJsonValue((ContainerValue) item));
                } else {
                    array.put(item);
                }
            }
            jsonObject.put(key, array);
        } else if (value instanceof Map) {
            JSONObject object = new JSONObject();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toOrgJsonValue((ContainerValue) v));
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
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, toOrgJsonValue(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject(String key) {
        JSONObject newObject = new JSONObject();
        jsonObject.put(key, newObject);
        return new OrgJsonObject(newObject);
    }
    
    @Override
    public ArrayContainer newAndPutArray(String key) {
        JSONArray newArray = new JSONArray();
        jsonObject.put(key, newArray);
        return new OrgJsonArray(newArray);
    }
    
    @Override
    public ContainerValue remove(String key) {
        Object removed = jsonObject.remove(key);
        if (removed == null) {
            return null;
        }
        return OrgJsonContainerFactory.wrap(removed);
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
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            result.add(new AbstractMap.SimpleEntry<>(
                key, 
                OrgJsonContainerFactory.wrap(value)
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
        if (!jsonObject.has(key)) {
            return null;
        }
        Object value = jsonObject.get(key);
        return OrgJsonContainerFactory.wrap(value);
    }
    
    @Override
    public boolean has(String key) {
        return jsonObject.has(key);
    }
    
    @Override
    public void clear() {
        // org.json의 JSONObject는 clear 메서드가 없으므로 모든 키 제거
        for (String key : new ArrayList<>(jsonObject.keySet())) {
            jsonObject.remove(key);
        }
    }
    
    @Override
    public ContainerFactory getContainerFactory() {
        return OrgJsonContainerFactory.getInstance();
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
            writer = new OrgJsonWriter(this);
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
        return jsonObject.toString().hashCode();
    }
    
    /**
     * ContainerValue를 org.json 값으로 변환
     */
    private Object toOrgJsonValue(ContainerValue value) {
        if (value == null || value.isNull()) {
            return JSONObject.NULL;
        } else if (value.isPrimitive()) {
            return value.raw();
        } else if (value instanceof OrgJsonObject) {
            return ((OrgJsonObject) value).jsonObject;
        } else if (value instanceof OrgJsonArray) {
            return ((OrgJsonArray) value).getJSONArray();
        } else if (value.isObject()) {
            // 다른 구현체의 ObjectContainer 변환
            JSONObject object = new JSONObject();
            ObjectContainer obj = value.asObject();
            for (Map.Entry<String, ContainerValue> entry : obj) {
                object.put(entry.getKey(), toOrgJsonValue(entry.getValue()));
            }
            return object;
        } else if (value.isArray()) {
            // 다른 구현체의 ArrayContainer 변환
            JSONArray array = new JSONArray();
            ArrayContainer arr = value.asArray();
            for (ContainerValue item : arr) {
                array.put(toOrgJsonValue(item));
            }
            return array;
        }
        return value.raw();
    }
}