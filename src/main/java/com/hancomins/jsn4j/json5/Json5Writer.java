package com.hancomins.jsn4j.json5;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.jsn4j.ContainerValue;
import com.hancomins.jsn4j.ContainerWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Json5Writer implements ContainerWriter<Json5WriteOption> {
    
    private final ContainerValue containerValue;
    private final Set<Json5WriteOption> options;
    
    public Json5Writer(ContainerValue containerValue) {
        this.containerValue = containerValue;
        this.options = EnumSet.noneOf(Json5WriteOption.class);
    }
    
    @Override
    public void putOption(Json5WriteOption option, Object value) {
        if (option == null) {
            return;
        }
        
        boolean enabled = false;
        if (value instanceof Boolean) {
            enabled = (Boolean) value;
        } else if (value instanceof Number) {
            enabled = ((Number) value).intValue() > 0;
        } else if (value instanceof String) {
            String str = ((String) value).trim();
            enabled = "true".equalsIgnoreCase(str) || "1".equals(str);
        }
        
        if (enabled) {
            options.add(option);
        } else {
            options.remove(option);
        }
    }
    
    @Override
    public boolean putOption(String optionName, Object value) {
        try {
            Json5WriteOption option = Json5WriteOption.valueOf(optionName.toUpperCase());
            putOption(option, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public boolean removeOption(String optionName) {
        try {
            Json5WriteOption option = Json5WriteOption.valueOf(optionName.toUpperCase());
            removeOption(option);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public void removeOption(Json5WriteOption option) {
        options.remove(option);
    }
    
    @Override
    public String write() {
        Object json5Value = getJson5Value();
        
        // JSON5는 기본적으로 JSON5 특징(주석, 후행 쉼표, 작은따옴표 등)을 지원
        // JSON5Object와 JSON5Array는 toString()으로 JSON5 형식을 출력
        if (json5Value instanceof JSON5Object || json5Value instanceof JSON5Array) {
            // Pretty print 옵션은 JSON5 라이브러리가 자체적으로 처리
            return json5Value.toString();
        }
        
        // 원시 값인 경우
        if (json5Value == null) {
            return "null";
        } else if (json5Value instanceof String) {
            return "\"" + escapeString(json5Value.toString()) + "\"";
        } else {
            return json5Value.toString();
        }
    }
    
    @Override
    public void write(OutputStream outputStream) throws IOException {
        String json = write();
        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * ContainerValue에서 JSON5 값 추출
     */
    private Object getJson5Value() {
        if (containerValue instanceof Json5Object) {
            return ((Json5Object) containerValue).getJSON5Object();
        } else if (containerValue instanceof Json5Array) {
            return ((Json5Array) containerValue).getJSON5Array();
        } else if (containerValue.isPrimitive()) {
            return containerValue.raw();
        } else {
            // 다른 구현체인 경우 변환
            if (containerValue.isObject()) {
                JSON5Object obj = new JSON5Object();
                for (Map.Entry<String, ContainerValue> entry : containerValue.asObject()) {
                    obj.put(entry.getKey(), convertToJson5(entry.getValue()));
                }
                return obj;
            } else if (containerValue.isArray()) {
                JSON5Array arr = new JSON5Array();
                for (ContainerValue item : containerValue.asArray()) {
                    arr.add(convertToJson5(item));
                }
                return arr;
            }
            return containerValue.raw();
        }
    }
    
    /**
     * ContainerValue를 JSON5 값으로 변환
     */
    private Object convertToJson5(ContainerValue value) {
        if (value == null || value.isNull()) {
            return null;
        } else if (value.isPrimitive()) {
            return value.raw();
        } else if (value.isObject()) {
            JSON5Object obj = new JSON5Object();
            for (Map.Entry<String, ContainerValue> entry : value.asObject()) {
                obj.put(entry.getKey(), convertToJson5(entry.getValue()));
            }
            return obj;
        } else if (value.isArray()) {
            JSON5Array arr = new JSON5Array();
            for (ContainerValue item : value.asArray()) {
                arr.add(convertToJson5(item));
            }
            return arr;
        }
        return value.raw();
    }
    
    @Override
    public String toString() {
        return write();
    }
    
    /**
     * 문자열 이스케이프 처리
     */
    private String escapeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}