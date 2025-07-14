package com.hancomins.jsn4j.tool;

import com.hancomins.jsn4j.ArrayContainer;
import com.hancomins.jsn4j.ContainerValue;
import com.hancomins.jsn4j.ObjectContainer;
import com.hancomins.jsn4j.ValueWriter;

import java.util.*;

/**
 * JSON 배열을 문자열로 직접 작성하는 StringBuilder 기반 Writer
 * ThreadLocal 캐싱을 통해 StringBuilder를 재사용합니다.
 */
@SuppressWarnings("unchecked")
public class JsonArrayStringWriter extends AbstractJsonStringWriter<JsonArrayStringWriter> implements ValueWriter {
    
    private int elementCount = 0;
    
    /**
     * 캐시된 StringBuilder를 사용하여 Writer를 생성합니다.
     */
    public JsonArrayStringWriter() {
        super('[');
    }
    
    /**
     * 외부에서 제공한 StringBuilder를 사용하여 Writer를 생성합니다.
     * @param builder 사용할 StringBuilder
     */
    public JsonArrayStringWriter(StringBuilder builder) {
        super(builder, '[');
    }
    
    public JsonArrayStringWriter put(char value) {
        checkClosed();
        appendCommaIfNeeded();
        appendChar(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(byte value) {
        checkClosed();
        appendCommaIfNeeded();
        builder.append(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(short value) {
        checkClosed();
        appendCommaIfNeeded();
        builder.append(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(int value) {
        checkClosed();
        appendCommaIfNeeded();
        builder.append(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(long value) {
        checkClosed();
        appendCommaIfNeeded();
        builder.append(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(float value) {
        checkClosed();
        appendCommaIfNeeded();
        appendFloat(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(double value) {
        checkClosed();
        appendCommaIfNeeded();
        appendDouble(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(boolean value) {
        checkClosed();
        appendCommaIfNeeded();
        builder.append(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(String value) {
        checkClosed();
        appendCommaIfNeeded();
        appendString(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(Object value) {
        checkClosed();
        appendCommaIfNeeded();
        
        if (value instanceof Map) {
            appendMap((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            appendCollection((Collection<?>) value);
        } else if (value instanceof ObjectContainer) {
            appendObjectContainer((ObjectContainer) value);
        } else if (value instanceof ArrayContainer) {
            appendArrayContainer((ArrayContainer) value);
        } else {
            appendObject(value);
        }
        
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(byte[] value) {
        checkClosed();
        appendCommaIfNeeded();
        appendByteArray(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(ContainerValue value) {
        checkClosed();
        appendCommaIfNeeded();
        appendContainerValue(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(ObjectContainer value) {
        checkClosed();
        appendCommaIfNeeded();
        appendObjectContainer(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(ArrayContainer value) {
        checkClosed();
        appendCommaIfNeeded();
        appendArrayContainer(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(Map<?, ?> value) {
        checkClosed();
        appendCommaIfNeeded();
        appendMap(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter put(Collection<?> value) {
        checkClosed();
        appendCommaIfNeeded();
        appendCollection(value);
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter putNull() {
        checkClosed();
        appendCommaIfNeeded();
        builder.append("null");
        elementCount++;
        return this;
    }
    
    public JsonArrayStringWriter putAll(Collection<?> values) {
        checkClosed();
        if (values != null) {
            for (Object value : values) {
                put(value);
            }
        }
        return this;
    }
    
    public JsonArrayStringWriter putAll(Object... values) {
        checkClosed();
        if (values != null) {
            for (Object value : values) {
                put(value);
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
        builder.append('[');
        elementCount = 0;
        first = true;
    }
    
    @Override
    protected String build(boolean checkClosed) {
        if(checkClosed) {
            return buildInternal(']');
        }
        return builder.toString() + ']';
    }
    
    /**
     * 중첩된 Writer에서 사용. StringBuilder를 반환하지 않고 닫기만 함
     */
    void closeWithoutRelease() {
        closeWithoutRelease(']');
    }
    
    // 내부 헬퍼 메서드들
    
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