package com.hancomins.jsn4j.tool;

import com.hancomins.jsn4j.ArrayContainer;
import com.hancomins.jsn4j.ContainerValue;
import com.hancomins.jsn4j.KeyValueWriter;
import com.hancomins.jsn4j.ObjectContainer;

import java.util.*;

/**
 * JSON 객체를 문자열로 직접 작성하는 StringBuilder 기반 Writer
 * ThreadLocal 캐싱을 통해 StringBuilder를 재사용합니다.
 */
@SuppressWarnings("unchecked")
public class JsonObjectStringWriter extends AbstractJsonStringWriter<JsonObjectStringWriter> implements KeyValueWriter {
    
    private int elementCount = 0; // 현재 요소 개수

    /**
     * 캐시된 StringBuilder를 사용하여 Writer를 생성합니다.
     */
    public JsonObjectStringWriter() {
        super('{');

    }
    
    /**
     * 외부에서 제공한 StringBuilder를 사용하여 Writer를 생성합니다.
     * @param builder 사용할 StringBuilder
     */
    public JsonObjectStringWriter(StringBuilder builder) {
        super(builder, '{');

    }
    
    public JsonObjectStringWriter put(String key, char value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendChar(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, byte value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        builder.append(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, short value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        builder.append(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, int value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        builder.append(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, long value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        builder.append(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, float value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendFloat(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, double value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendDouble(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, boolean value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        builder.append(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, String value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendString(value);
        ++elementCount;
        return this;
    }


    public JsonObjectStringWriter put(String key, Object value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);

        //noinspection DuplicatedCode
        if (value instanceof Map) {
            appendMap((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            appendCollection((Collection<?>) value);
        } else if (value instanceof ObjectContainer) {
            appendObjectContainer((ObjectContainer) value);
        } else if (value instanceof ArrayContainer) {
            appendArrayContainer((ArrayContainer) value);
        }
        else {
            appendObject(value);
        }

        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, byte[] value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendByteArray(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, ContainerValue value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendContainerValue(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, ObjectContainer value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendObjectContainer(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, ArrayContainer value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendArrayContainer(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, Map<?, ?> value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendMap(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter put(String key, Collection<?> value) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        appendCollection(value);
        ++elementCount;
        return this;
    }

    public JsonObjectStringWriter putNull(String key) {
        checkClosed();
        appendCommaIfNeeded();
        appendKey(key);
        builder.append("null");
        ++elementCount;
        return this;
    }
    
    public JsonObjectStringWriter putAll(Map<String, ?> map) {
        checkClosed();
        if (map != null) {
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }
    
    @Override
    public boolean isEmpty() {
        return elementCount == 0;
    }
    
    @Override
    public int size() {
        return elementCount;
    }
    
    @Override
    protected void doReset() {
        builder.setLength(0);
        builder.append('{');
        elementCount = 0;
        first = true;
    }
    
    @Override
    public String build(boolean checkClosed) {
        if(checkClosed) {
            return buildInternal('}');
        }
        return builder.toString() + '}';

    }
    
    /**
     * 중첩된 Writer에서 사용. StringBuilder를 반환하지 않고 닫기만 함
     */
    void closeWithoutRelease() {
        closeWithoutRelease('}');
    }
    
    // 내부 헬퍼 메서드들
    
    private void appendKey(String key) {
        appendString(key);
        builder.append(':');
    }
    
    private void appendObjectContainer(ObjectContainer value) {
        if (value == null) {
            builder.append("null");
        } else {
            JsonObjectStringWriter nested = new JsonObjectStringWriter(builder);
            for (Map.Entry<String, ContainerValue> entry : value) {
                nested.put(entry.getKey(), entry.getValue());
            }
            nested.closeWithoutRelease();
        }
    }
    
    private void appendArrayContainer(ArrayContainer value) {
        if (value == null) {
            builder.append("null");
        } else {
            JsonArrayStringWriter nested = new JsonArrayStringWriter(builder);
            for (ContainerValue item : value) {
                nested.put(item);
            }
            nested.closeWithoutRelease();
        }
    }
    
    private void appendMap(Map<?, ?> value) {
        if (value == null) {
            builder.append("null");
        } else {
            JsonObjectStringWriter nested = new JsonObjectStringWriter(builder);
            for (Map.Entry<?, ?> entry : value.entrySet()) {
                nested.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            nested.closeWithoutRelease();
        }
    }
    
    private void appendCollection(Collection<?> value) {
        if (value == null) {
            builder.append("null");
        } else {
            JsonArrayStringWriter nested = new JsonArrayStringWriter(builder);
            for (Object item : value) {
                nested.put(item);
            }
            nested.closeWithoutRelease();
        }
    }
}