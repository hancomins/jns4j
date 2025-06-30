package com.hancomins.jsn4j.fastjson2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.hancomins.jsn4j.ContainerValue;
import com.hancomins.jsn4j.ContainerWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Fastjson2Writer implements ContainerWriter<Fastjson2WriteOption> {
    
    private final ContainerValue containerValue;
    private final Set<Fastjson2WriteOption> options;
    
    public Fastjson2Writer(ContainerValue containerValue) {
        this.containerValue = containerValue;
        this.options = EnumSet.noneOf(Fastjson2WriteOption.class);
    }
    
    @Override
    public void putOption(Fastjson2WriteOption option, Object value) {
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
            Fastjson2WriteOption option = Fastjson2WriteOption.valueOf(optionName.toUpperCase());
            putOption(option, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public boolean removeOption(String optionName) {
        try {
            Fastjson2WriteOption option = Fastjson2WriteOption.valueOf(optionName.toUpperCase());
            removeOption(option);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public void removeOption(Fastjson2WriteOption option) {
        options.remove(option);
    }
    
    @Override
    public String write() {
        Object jsonValue = getFastjsonValue();
        JSONWriter.Feature[] features = getFeatures();
        
        if (features.length > 0) {
            return JSON.toJSONString(jsonValue, features);
        } else {
            return JSON.toJSONString(jsonValue);
        }
    }
    
    @Override
    public void write(OutputStream outputStream) throws IOException {
        String json = write();
        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 현재 옵션에 따른 JSONWriter.Feature 배열 생성
     */
    private JSONWriter.Feature[] getFeatures() {
        List<JSONWriter.Feature> features = new ArrayList<>();
        
        if (options.contains(Fastjson2WriteOption.PRETTY_PRINT) || 
            options.contains(Fastjson2WriteOption.INDENT_OUTPUT)) {
            features.add(JSONWriter.Feature.PrettyFormat);
        }
        
        if (options.contains(Fastjson2WriteOption.WRITE_NULL_VALUES)) {
            features.add(JSONWriter.Feature.WriteNulls);
        }
        
        if (options.contains(Fastjson2WriteOption.WRITE_MAP_NULL_VALUE)) {
            features.add(JSONWriter.Feature.WriteNullStringAsEmpty);
        }
        
        if (options.contains(Fastjson2WriteOption.WRITE_NULL_LIST_AS_EMPTY)) {
            features.add(JSONWriter.Feature.WriteNullListAsEmpty);
        }
        
        if (options.contains(Fastjson2WriteOption.WRITE_CLASS_NAME)) {
            features.add(JSONWriter.Feature.WriteClassName);
        }
        
        return features.toArray(new JSONWriter.Feature[0]);
    }
    
    /**
     * ContainerValue에서 Fastjson2 값 추출
     */
    private Object getFastjsonValue() {
        if (containerValue instanceof Fastjson2Object) {
            return ((Fastjson2Object) containerValue).getJSONObject();
        } else if (containerValue instanceof Fastjson2Array) {
            return ((Fastjson2Array) containerValue).getJSONArray();
        } else if (containerValue.isPrimitive()) {
            return containerValue.raw();
        } else {
            // 다른 구현체인 경우 변환
            if (containerValue.isObject()) {
                return ((Fastjson2Object) Fastjson2ContainerFactory.wrap(containerValue)).getJSONObject();
            } else if (containerValue.isArray()) {
                return ((Fastjson2Array) Fastjson2ContainerFactory.wrap(containerValue)).getJSONArray();
            }
            return containerValue.raw();
        }
    }
    
    @Override
    public String toString() {
        return write();
    }
}