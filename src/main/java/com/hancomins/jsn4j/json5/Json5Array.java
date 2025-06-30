package com.hancomins.jsn4j.json5;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.jsn4j.*;

import java.util.*;

public class Json5Array implements ArrayContainer {
    
    private final JSON5Array json5Array;
    private Json5Writer writer;
    
    public Json5Array() {
        this.json5Array = new JSON5Array();
    }
    
    public Json5Array(JSON5Array json5Array) {
        this.json5Array = json5Array;
    }
    
    /**
     * 정적 팩토리 메서드 - JSON5Array를 Json5Array로 래핑
     */
    public static Json5Array wrap(JSON5Array json5Array) {
        return new Json5Array(json5Array);
    }
    
    /**
     * JSON5Array를 직접 반환
     */
    public JSON5Array getJSON5Array() {
        return json5Array;
    }
    
    @Override
    public ArrayContainer put(int index, Object value) {
        ensureCapacity(index + 1);
        if (value == null) {
            json5Array.set(index, null);
        } else if (value instanceof ContainerValue) {
            json5Array.set(index, toJson5Value((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSON5Array array = new JSON5Array();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.add(toJson5Value((ContainerValue) item));
                } else {
                    array.add(item);
                }
            }
            json5Array.set(index, array);
        } else if (value instanceof Map) {
            JSON5Object object = new JSON5Object();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toJson5Value((ContainerValue) v));
                } else {
                    object.put(String.valueOf(k), v);
                }
            });
            json5Array.set(index, object);
        } else {
            json5Array.set(index, value);
        }
        return this;
    }
    
    @Override
    public ArrayContainer put(Object value) {
        if (value == null) {
            json5Array.add(null);
        } else if (value instanceof ContainerValue) {
            json5Array.add(toJson5Value((ContainerValue) value));
        } else if (value instanceof Collection) {
            JSON5Array array = new JSON5Array();
            for (Object item : (Collection<?>) value) {
                if (item instanceof ContainerValue) {
                    array.add(toJson5Value((ContainerValue) item));
                } else {
                    array.add(item);
                }
            }
            json5Array.add(array);
        } else if (value instanceof Map) {
            JSON5Object object = new JSON5Object();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof ContainerValue) {
                    object.put(String.valueOf(k), toJson5Value((ContainerValue) v));
                } else {
                    object.put(String.valueOf(k), v);
                }
            });
            json5Array.add(object);
        } else {
            json5Array.add(value);
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject() {
        JSON5Object newObject = new JSON5Object();
        json5Array.add(newObject);
        return new Json5Object(newObject);
    }
    
    @Override
    public ArrayContainer newAndPutArray() {
        JSON5Array newArray = new JSON5Array();
        json5Array.add(newArray);
        return new Json5Array(newArray);
    }
    
    @Override
    public int size() {
        return json5Array.size();
    }
    
    @Override
    public ContainerValue remove(int index) {
        if (index < 0 || index >= json5Array.size()) {
            return null;
        }
        Object removed = json5Array.remove(index);
        if (removed == null) {
            return null;
        }
        return Json5ContainerFactory.wrap(removed);
    }
    
    @Override
    public ContainerValue get(int index) {
        if (index < 0 || index >= json5Array.size()) {
            return null;
        }
        Object value = json5Array.get(index);
        return Json5ContainerFactory.wrap(value);
    }
    
    @Override
    public void clear() {
        json5Array.clear();
    }
    
    @Override
    public ContainerFactory getContainerFactory() {
        return Json5ContainerFactory.getInstance();
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
            writer = new Json5Writer(this);
        }
        return writer;
    }
    
    @Override
    public Iterator<ContainerValue> iterator() {
        return new Iterator<ContainerValue>() {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < json5Array.size();
            }
            
            @Override
            public ContainerValue next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Object value = json5Array.get(index++);
                return Json5ContainerFactory.wrap(value);
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
        return json5Array.hashCode();
    }
    
    /**
     * 배열 크기를 지정된 용량만큼 확장
     */
    private void ensureCapacity(int minCapacity) {
        while (json5Array.size() < minCapacity) {
            json5Array.add(null);
        }
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
            return ((Json5Object) value).getJSON5Object();
        } else if (value instanceof Json5Array) {
            return ((Json5Array) value).json5Array;
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