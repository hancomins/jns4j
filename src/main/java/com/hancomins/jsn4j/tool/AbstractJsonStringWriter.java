package com.hancomins.jsn4j.tool;

import com.hancomins.jsn4j.ContainerValue;

import java.util.Base64;

/**
 * JSON String Writer의 공통 기능을 제공하는 추상 클래스
 * @param <T> 구체적인 Writer 타입 (fluent interface를 위함)
 */
public abstract class AbstractJsonStringWriter<T extends AbstractJsonStringWriter<T>> {
    
    protected final StringBuilder builder;
    protected boolean first = true;
    protected final boolean ownsBuilder;
    protected boolean closed = false;
    
    /**
     * 캐시된 StringBuilder를 사용하여 Writer를 생성합니다.
     * @param openChar 시작 문자 ('{' 또는 '[')
     */
    protected AbstractJsonStringWriter(char openChar) {
        this.builder = StringBuilderCache.acquire();
        this.ownsBuilder = true;
        this.builder.append(openChar);
    }
    
    /**
     * 외부에서 제공한 StringBuilder를 사용하여 Writer를 생성합니다.
     * @param builder 사용할 StringBuilder
     * @param openChar 시작 문자 ('{' 또는 '[')
     */
    protected AbstractJsonStringWriter(StringBuilder builder, char openChar) {
        this.builder = builder;
        this.ownsBuilder = false;
        this.builder.append(openChar);
    }
    
    /**
     * Writer가 비어있는지 확인
     * @return 비어있으면 true
     */
    public abstract boolean isEmpty();
    
    /**
     * 현재 요소 개수 반환
     * @return 요소 개수
     */
    public abstract int size();
    
    /**
     * Writer를 초기 상태로 리셋
     * @return this (method chaining)
     */
    @SuppressWarnings("unchecked")
    public T reset() {
        checkClosed();
        doReset();
        return (T) this;
    }
    
    /**
     * 구체적인 리셋 동작 구현
     */
    protected abstract void doReset();
    
    /**
     * JSON을 완성하고 문자열을 반환합니다.
     * @param closeChar 닫는 문자 ('}' 또는 ']')
     * @return 완성된 JSON 문자열
     */
    protected String buildInternal(char closeChar) {
        if (!closed) {
            builder.append(closeChar);
            closed = true;
        }
        
        String result = builder.toString();
        
        if (ownsBuilder) {
            StringBuilderCache.release(builder);
        }
        
        return result;
    }
    

    abstract String build(boolean checkClosed);


    /**
     * JSON을 완성하고 문자열을 반환합니다.
     * @return 완성된 JSON 문자열
     */
    public String build() {
        return build(true);
    }

    
    /**
     * toString()은 build()를 호출합니다.
     */
    @Override
    public String toString() {
        return build(false);
    }
    
    /**
     * 캐시 클리어 정적 메서드
     */
    public static void clearCache() {
        StringBuilderCache.clearCurrentThreadCache();
    }
    
    /**
     * Writer가 닫혔는지 확인
     */
    protected void checkClosed() {
        if (closed) {
            throw new IllegalStateException("Writer has been closed");
        }
    }
    
    /**
     * 필요한 경우 콤마 추가
     */
    protected void appendCommaIfNeeded() {
        if (!first) {
            builder.append(',');
        }
        first = false;
    }
    
    /**
     * 중첩된 Writer에서 사용. StringBuilder를 반환하지 않고 닫기만 함
     * @param closeChar 닫는 문자
     */
    void closeWithoutRelease(char closeChar) {
        if (!closed) {
            builder.append(closeChar);
            closed = true;
        }
    }
    
    // 공통 타입 처리 메서드들
    
    /**
     * char 값을 문자열로 추가
     */
    protected void appendChar(char value) {
        builder.append('"').append(value).append('"');
    }
    
    /**
     * float 값 추가 (NaN, Infinite 처리)
     */
    protected void appendFloat(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            builder.append("null");
        } else {
            builder.append(value);
        }
    }
    
    /**
     * double 값 추가 (NaN, Infinite 처리)
     */
    protected void appendDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            builder.append("null");
        } else {
            builder.append(value);
        }
    }
    
    /**
     * byte 배열을 Base64로 인코딩하여 추가
     */
    protected void appendByteArray(byte[] value) {
        if (value == null) {
            builder.append("null");
        } else {
            appendString(Base64.getEncoder().encodeToString(value));
        }
    }
    
    /**
     * ContainerValue 추가
     */
    protected void appendContainerValue(ContainerValue value) {
        if (value == null || value.isNull()) {
            builder.append("null");
        } else if (value.isPrimitive()) {
            Object raw = value.raw();
            if (raw instanceof String) {
                appendString((String) raw);
            } else {
                builder.append(raw);
            }
        } else {
            // ContainerValue의 Writer를 사용하여 JSON 문자열 생성
            String json = value.getWriter().write();
            builder.append(json);
        }
    }
    
    /**
     * JSON 문자열 이스케이프 처리
     */
    protected void appendString(String value) {
        if (value == null) {
            builder.append("null");
            return;
        }
        
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': builder.append("\\\""); break;
                case '\\': builder.append("\\\\"); break;
                case '/': builder.append("\\/"); break;
                case '\b': builder.append("\\b"); break;
                case '\f': builder.append("\\f"); break;
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                case '\t': builder.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
            }
        }
        builder.append('"');
    }
    
    /**
     * Object 타입 값을 적절한 JSON 표현으로 변환
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    protected T appendObject(Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof Number) {
            Number num = (Number) value;
            if (value instanceof Float) {
                appendFloat(num.floatValue());
            } else if (value instanceof Double) {
                appendDouble(num.doubleValue());
            } else if (value instanceof Long) {
                builder.append(num.longValue());
            } else {
                builder.append(num.intValue());
            }
        } else if (value instanceof Boolean) {
            builder.append(value);
        } else if (value instanceof Character) {
            appendChar((char) value);
        } else if (value instanceof String) {
            appendString((String) value);
        } else if (value instanceof byte[]) {
            appendByteArray((byte[]) value);
        } else if (value instanceof ContainerValue) {
            appendContainerValue((ContainerValue) value);
        } else if (value instanceof JsonObjectStringWriter) {
            // JsonObjectStringWriter를 직접 처리
            JsonObjectStringWriter writer = (JsonObjectStringWriter) value;
            // build()를 호출하여 JSON 문자열을 얻되, 이스케이프하지 않고 직접 추가
            String json = writer.build();
            builder.append(json);
        } else if (value instanceof JsonArrayStringWriter) {
            // JsonArrayStringWriter를 직접 처리
            JsonArrayStringWriter writer = (JsonArrayStringWriter) value;
            // build()를 호출하여 JSON 문자열을 얻되, 이스케이프하지 않고 직접 추가
            String json = writer.build();
            builder.append(json);
        } else {
            appendString(value.toString());
        }
        return (T) this;
    }
}