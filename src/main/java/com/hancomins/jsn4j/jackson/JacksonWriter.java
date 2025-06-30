package com.hancomins.jsn4j.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hancomins.jsn4j.ContainerValue;
import com.hancomins.jsn4j.ContainerWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Set;

public class JacksonWriter implements ContainerWriter<JacksonWriteOption> {
    
    private final ContainerValue containerValue;
    private final ObjectMapper mapper;
    private final Set<JacksonWriteOption> options;
    
    public JacksonWriter(ContainerValue containerValue, ObjectMapper mapper) {
        this.containerValue = containerValue;
        this.mapper = mapper;
        this.options = EnumSet.noneOf(JacksonWriteOption.class);
    }
    
    @Override
    public void putOption(JacksonWriteOption option, Object value) {
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
            JacksonWriteOption option = JacksonWriteOption.valueOf(optionName.toUpperCase());
            putOption(option, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public boolean removeOption(String optionName) {
        try {
            JacksonWriteOption option = JacksonWriteOption.valueOf(optionName.toUpperCase());
            removeOption(option);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public void removeOption(JacksonWriteOption option) {
        options.remove(option);
    }
    
    @Override
    public String write() {
        try {
            ObjectWriter writer = createWriter();
            JsonNode node = getJsonNode();
            return writer.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write JSON", e);
        }
    }
    
    @Override
    public void write(OutputStream outputStream) throws IOException {
        ObjectWriter writer = createWriter();
        JsonNode node = getJsonNode();
        writer.writeValue(outputStream, node);
    }
    
    /**
     * 현재 옵션에 따른 ObjectWriter 생성
     */
    private ObjectWriter createWriter() {
        ObjectMapper configuredMapper = mapper.copy();
        
        // Pretty print 설정
        if (options.contains(JacksonWriteOption.PRETTY_PRINT) || 
            options.contains(JacksonWriteOption.INDENT_OUTPUT)) {
            configuredMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        
        // 날짜 타임스탬프 설정
        if (options.contains(JacksonWriteOption.WRITE_DATES_AS_TIMESTAMPS)) {
            configuredMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        } else {
            configuredMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        
        ObjectWriter writer = configuredMapper.writer();
        
        if (options.contains(JacksonWriteOption.PRETTY_PRINT)) {
            writer = writer.withDefaultPrettyPrinter();
        }
        
        return writer;
    }
    
    /**
     * ContainerValue에서 JsonNode 추출
     */
    private JsonNode getJsonNode() {
        if (containerValue instanceof JacksonObject) {
            return ((JacksonObject) containerValue).getObjectNode();
        } else if (containerValue instanceof JacksonArray) {
            return ((JacksonArray) containerValue).getArrayNode();
        } else {
            // 다른 구현체인 경우 변환
            return mapper.valueToTree(containerValue.raw());
        }
    }
    
    @Override
    public String toString() {
        return write();
    }
}