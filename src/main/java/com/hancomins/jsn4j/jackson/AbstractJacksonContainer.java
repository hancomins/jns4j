package com.hancomins.jsn4j.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hancomins.jsn4j.*;

/**
 * Abstract base class for Jackson container implementations.
 * Provides common functionality for JacksonObject and JacksonArray.
 */
abstract class AbstractJacksonContainer implements ContainerValue, ContainerFactoryProvidable {
    protected final ObjectMapper mapper;
    protected JacksonWriter writer;
    
    protected AbstractJacksonContainer(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    public ContainerFactory getContainerFactory() {
        return JacksonContainerFactory.getInstance();
    }
    
    @Override
    public Object raw() {
        return this;
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
    
    /**
     * Converts a ContainerValue to JsonNode.
     * This method is shared between JacksonObject and JacksonArray.
     */
    protected JsonNode toJsonNode(ContainerValue value) {
        if (value == null || value.isNull()) {
            return mapper.nullNode();
        } else if (value.isPrimitive()) {
            return primitiveToJsonNode((PrimitiveValue) value);
        } else if (value instanceof JacksonObject) {
            return ((JacksonObject) value).getObjectNode();
        } else if (value instanceof JacksonArray) {
            return ((JacksonArray) value).getArrayNode();
        } else if (value.isObject()) {
            // Convert non-Jackson ObjectContainer
            ObjectNode objectNode = mapper.createObjectNode();
            ObjectContainer obj = value.asObject();
            for (java.util.Map.Entry<String, ContainerValue> entry : obj) {
                objectNode.set(entry.getKey(), toJsonNode(entry.getValue()));
            }
            return objectNode;
        } else if (value.isArray()) {
            // Convert non-Jackson ArrayContainer
            ArrayNode arrayNode = mapper.createArrayNode();
            ArrayContainer arr = value.asArray();
            for (ContainerValue item : arr) {
                arrayNode.add(toJsonNode(item));
            }
            return arrayNode;
        }
        return mapper.valueToTree(value.raw());
    }
    
    /**
     * Converts a PrimitiveValue to JsonNode.
     * This method is shared between JacksonObject and JacksonArray.
     */
    protected JsonNode primitiveToJsonNode(PrimitiveValue value) {
        Object raw = value.raw();
        if (raw == null) {
            return mapper.nullNode();
        } else if (raw instanceof String) {
            return mapper.getNodeFactory().textNode((String) raw);
        } else if (raw instanceof Integer) {
            return mapper.getNodeFactory().numberNode((Integer) raw);
        } else if (raw instanceof Long) {
            return mapper.getNodeFactory().numberNode((Long) raw);
        } else if (raw instanceof Float) {
            return mapper.getNodeFactory().numberNode((Float) raw);
        } else if (raw instanceof Double) {
            return mapper.getNodeFactory().numberNode((Double) raw);
        } else if (raw instanceof Boolean) {
            return mapper.getNodeFactory().booleanNode((Boolean) raw);
        } else if (raw instanceof byte[]) {
            return mapper.getNodeFactory().binaryNode((byte[]) raw);
        } else if (raw instanceof Number) {
            return mapper.valueToTree(raw);
        }
        return mapper.getNodeFactory().textNode(String.valueOf(raw));
    }
}