package com.hancomins.jsn4j.orgjson;

import org.json.JSONArray;
import org.json.JSONObject;
import com.hancomins.jsn4j.*;

import java.util.*;

public class OrgJsonArray implements ArrayContainer {
    
    private final JSONArray jsonArray;
    private OrgJsonWriter writer;
    
    public OrgJsonArray() {
        this.jsonArray = new JSONArray();
    }
    
    public OrgJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }
    
    /**
     * 정적 팩토리 메서드 - JSONArray를 OrgJsonArray로 래핑
     */
    public static OrgJsonArray wrap(JSONArray jsonArray) {
        return new OrgJsonArray(jsonArray);
    }
    
    /**
     * org.json의 JSONArray를 직접 반환
     */
    public JSONArray getJSONArray() {
        return jsonArray;
    }
    
    @Override
    public ArrayContainer put(int index, Object value) {
        ensureCapacity(index + 1);
        if (value == null) {
            jsonArray.put(index, JSONObject.NULL);
        } else if (value instanceof ContainerValue) {
            jsonArray.put(index, toOrgJsonValue((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSONArray array = new JSONArray();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.put(toOrgJsonValue((ContainerValue) item));
                } else {
                    array.put(item);
                }
            }
            jsonArray.put(index, array);
        } else if (value instanceof Map) {
            JSONObject object = new JSONObject();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toOrgJsonValue((ContainerValue) v));
                } else {
                    object.put(String.valueOf(k), v);
                }
            });
            jsonArray.put(index, object);
        } else {
            jsonArray.put(index, value);
        }
        return this;
    }
    
    @Override
    public ArrayContainer put(Object value) {
        if (value == null) {
            jsonArray.put(JSONObject.NULL);
        } else if (value instanceof ContainerValue) {
            jsonArray.put(toOrgJsonValue((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSONArray array = new JSONArray();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.put(toOrgJsonValue((ContainerValue) item));
                } else {
                    array.put(item);
                }
            }
            jsonArray.put(array);
        } else if (value instanceof Map) {
            JSONObject object = new JSONObject();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toOrgJsonValue((ContainerValue) v));
                } else {
                    object.put(String.valueOf(k), v);
                }
            });
            jsonArray.put(object);
        } else {
            jsonArray.put(value);
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject() {
        JSONObject newObject = new JSONObject();
        jsonArray.put(newObject);
        return new OrgJsonObject(newObject);
    }
    
    @Override
    public ArrayContainer newAndPutArray() {
        JSONArray newArray = new JSONArray();
        jsonArray.put(newArray);
        return new OrgJsonArray(newArray);
    }
    
    @Override
    public int size() {
        return jsonArray.length();
    }
    
    @Override
    public ContainerValue remove(int index) {
        if (index < 0 || index >= jsonArray.length()) {
            return null;
        }
        Object removed = jsonArray.get(index);
        jsonArray.remove(index);
        if (removed == null) {
            return null;
        }
        return OrgJsonContainerFactory.wrap(removed);
    }
    
    @Override
    public ContainerValue get(int index) {
        if (index < 0 || index >= jsonArray.length()) {
            return null;
        }
        Object value = jsonArray.get(index);
        return OrgJsonContainerFactory.wrap(value);
    }
    
    @Override
    public void clear() {
        // org.json의 JSONArray는 clear 메서드가 없으므로 모든 요소 제거
        while (jsonArray.length() > 0) {
            jsonArray.remove(0);
        }
    }
    
    @Override
    public ContainerFactory getContainerFactory() {
        return OrgJsonContainerFactory.getInstance();
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
            writer = new OrgJsonWriter(this);
        }
        return writer;
    }
    
    @Override
    public Iterator<ContainerValue> iterator() {
        return new Iterator<ContainerValue>() {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < jsonArray.length();
            }
            
            @Override
            public ContainerValue next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Object value = jsonArray.get(index++);
                return OrgJsonContainerFactory.wrap(value);
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
        return jsonArray.toString().hashCode();
    }
    
    /**
     * 배열 크기를 지정된 용량만큼 확장
     */
    private void ensureCapacity(int minCapacity) {
        while (jsonArray.length() < minCapacity) {
            jsonArray.put(JSONObject.NULL);
        }
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
            return ((OrgJsonObject) value).getJSONObject();
        } else if (value instanceof OrgJsonArray) {
            return ((OrgJsonArray) value).jsonArray;
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