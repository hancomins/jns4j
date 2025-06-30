package com.hancomins.jsn4j.orgjson;

import org.json.JSONArray;
import org.json.JSONObject;
import com.hancomins.jsn4j.ContainerValue;
import com.hancomins.jsn4j.ContainerWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class OrgJsonWriter implements ContainerWriter<OrgJsonWriteOption> {
    
    private final ContainerValue containerValue;
    private final Set<OrgJsonWriteOption> options;
    private int indentSize = 4; // 기본 들여쓰기 크기
    
    public OrgJsonWriter(ContainerValue containerValue) {
        this.containerValue = containerValue;
        this.options = EnumSet.noneOf(OrgJsonWriteOption.class);
    }
    
    @Override
    public void putOption(OrgJsonWriteOption option, Object value) {
        if (option == null) {
            return;
        }
        
        if (option == OrgJsonWriteOption.INDENT_SIZE && value instanceof Number) {
            indentSize = ((Number) value).intValue();
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
            OrgJsonWriteOption option = OrgJsonWriteOption.valueOf(optionName.toUpperCase());
            putOption(option, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public boolean removeOption(String optionName) {
        try {
            OrgJsonWriteOption option = OrgJsonWriteOption.valueOf(optionName.toUpperCase());
            removeOption(option);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public void removeOption(OrgJsonWriteOption option) {
        options.remove(option);
    }
    
    @Override
    public String write() {
        Object jsonValue = getOrgJsonValue();
        
        if (options.contains(OrgJsonWriteOption.PRETTY_PRINT) || 
            options.contains(OrgJsonWriteOption.INDENT_OUTPUT)) {
            if (jsonValue instanceof JSONObject) {
                return ((JSONObject) jsonValue).toString(indentSize);
            } else if (jsonValue instanceof JSONArray) {
                return ((JSONArray) jsonValue).toString(indentSize);
            }
        }
        
        return jsonValue.toString();
    }
    
    @Override
    public void write(OutputStream outputStream) throws IOException {
        String json = write();
        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * ContainerValue에서 org.json 값 추출
     */
    private Object getOrgJsonValue() {
        if (containerValue instanceof OrgJsonObject) {
            return ((OrgJsonObject) containerValue).getJSONObject();
        } else if (containerValue instanceof OrgJsonArray) {
            return ((OrgJsonArray) containerValue).getJSONArray();
        } else if (containerValue.isPrimitive()) {
            return containerValue.raw();
        } else {
            // 다른 구현체인 경우 변환
            if (containerValue.isObject()) {
                JSONObject obj = new JSONObject();
                for (Map.Entry<String, ContainerValue> entry : containerValue.asObject()) {
                    obj.put(entry.getKey(), convertToOrgJson(entry.getValue()));
                }
                return obj;
            } else if (containerValue.isArray()) {
                JSONArray arr = new JSONArray();
                for (ContainerValue item : containerValue.asArray()) {
                    arr.put(convertToOrgJson(item));
                }
                return arr;
            }
            return containerValue.raw();
        }
    }
    
    /**
     * ContainerValue를 org.json 값으로 변환
     */
    private Object convertToOrgJson(ContainerValue value) {
        if (value == null || value.isNull()) {
            return JSONObject.NULL;
        } else if (value.isPrimitive()) {
            return value.raw();
        } else if (value.isObject()) {
            JSONObject obj = new JSONObject();
            for (Map.Entry<String, ContainerValue> entry : value.asObject()) {
                obj.put(entry.getKey(), convertToOrgJson(entry.getValue()));
            }
            return obj;
        } else if (value.isArray()) {
            JSONArray arr = new JSONArray();
            for (ContainerValue item : value.asArray()) {
                arr.put(convertToOrgJson(item));
            }
            return arr;
        }
        return value.raw();
    }
    
    @Override
    public String toString() {
        return write();
    }
}