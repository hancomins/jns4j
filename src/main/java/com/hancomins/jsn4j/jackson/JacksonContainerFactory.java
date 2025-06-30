package com.hancomins.jsn4j.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hancomins.jsn4j.*;

import java.io.IOException;

public class JacksonContainerFactory implements ContainerFactory {
    
    private static final JacksonContainerFactory INSTANCE = new JacksonContainerFactory();
    private static final String MODULE_NAME = "jackson";
    private final ObjectMapper objectMapper;
    private final JacksonParser parser;
    
    private JacksonContainerFactory() {
        this.objectMapper = new ObjectMapper();
        this.parser = new JacksonParser(objectMapper);
    }
    
    public static JacksonContainerFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getJsn4jModuleName() {
        return MODULE_NAME;
    }
    
    @Override
    public ObjectContainer newObject() {
        return new JacksonObject(objectMapper);
    }
    
    @Override
    public ArrayContainer newArray() {
        return new JacksonArray(objectMapper);
    }
    
    @Override
    public ObjectContainer newObject(ContainerValue rootContainer) {
        return new JacksonObject(objectMapper);
    }
    
    @Override
    public ArrayContainer newArray(ContainerValue rootContainer) {
        return new JacksonArray(objectMapper);
    }
    
    @Override
    public ContainerParser getParser() {
        return parser;
    }
    
    /**
     * Jackson JsonNode를 JSN4J ContainerValue로 래핑합니다.
     */
    public static ContainerValue wrap(JsonNode node) {
        return wrap(node, getInstance().objectMapper);
    }
    
    /**
     * Jackson JsonNode를 JSN4J ContainerValue로 래핑합니다.
     */
    public static ContainerValue wrap(JsonNode node, ObjectMapper mapper) {
        if (node == null || node.isNull()) {
            return new PrimitiveValue(null);
        } else if (node.isObject()) {
            return JacksonObject.wrap((ObjectNode) node, mapper);
        } else if (node.isArray()) {
            return JacksonArray.wrap((ArrayNode) node, mapper);
        } else if (node.isTextual()) {
            return new PrimitiveValue(node.textValue());
        } else if (node.isNumber()) {
            if (node.isInt()) {
                return new PrimitiveValue(node.intValue());
            } else if (node.isLong()) {
                return new PrimitiveValue(node.longValue());
            } else if (node.isFloat()) {
                return new PrimitiveValue(node.floatValue());
            } else if (node.isDouble()) {
                return new PrimitiveValue(node.doubleValue());
            } else {
                return new PrimitiveValue(node.numberValue());
            }
        } else if (node.isBoolean()) {
            return new PrimitiveValue(node.booleanValue());
        } else if (node.isBinary()) {
            try {
                return new PrimitiveValue(node.binaryValue());
            } catch (IOException e) {
                throw new RuntimeException("Failed to extract binary value", e);
            }
        }
        return new PrimitiveValue(null);
    }
    
    /**
     * 기본 ObjectMapper를 반환합니다.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}