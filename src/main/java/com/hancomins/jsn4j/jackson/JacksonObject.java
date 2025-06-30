package com.hancomins.jsn4j.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hancomins.jsn4j.*;

import java.util.*;

public class JacksonObject implements ObjectContainer {
    
    private final ObjectNode node;
    private final ObjectMapper mapper;
    private JacksonWriter writer;
    
    public JacksonObject(ObjectMapper mapper) {
        this.mapper = mapper;
        this.node = mapper.createObjectNode();
    }
    
    public JacksonObject(ObjectNode node, ObjectMapper mapper) {
        this.node = node;
        this.mapper = mapper;
    }
    
    /**
     * 정적 팩토리 메서드 - ObjectNode를 JacksonObject로 래핑
     */
    public static JacksonObject wrap(ObjectNode node) {
        return new JacksonObject(node, JacksonContainerFactory.getInstance().getObjectMapper());
    }
    
    public static JacksonObject wrap(ObjectNode node, ObjectMapper mapper) {
        return new JacksonObject(node, mapper);
    }
    
    /**
     * Jackson의 ObjectNode를 직접 반환
     */
    public ObjectNode getObjectNode() {
        return node;
    }
    
    @Override
    public int size() {
        return node.size();
    }
    
    @Override
    public ObjectContainer put(String key, Object value) {
        if (value == null) {
            node.putNull(key);
        } else if (value instanceof ContainerValue) {
            node.set(key, toJsonNode((ContainerValue) value));
        } else if (value instanceof Collection) {
            JacksonArray array = new JacksonArray(mapper);
            for (Object item : (Collection<?>) value) {
                array.put(item);
            }
            node.set(key, array.getArrayNode());
        } else if (value instanceof Map) {
            JacksonObject object = new JacksonObject(mapper);
            ((Map<?, ?>) value).forEach((k, v) -> object.put(String.valueOf(k), v));
            node.set(key, object.getObjectNode());
        } else {
            node.set(key, mapper.valueToTree(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer put(String key, ContainerValue value) {
        if (value == null || value.isNull()) {
            node.putNull(key);
        } else {
            node.set(key, toJsonNode(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject(String key) {
        ObjectNode newNode = mapper.createObjectNode();
        node.set(key, newNode);
        return new JacksonObject(newNode, mapper);
    }
    
    @Override
    public ArrayContainer newAndPutArray(String key) {
        ArrayNode newNode = mapper.createArrayNode();
        node.set(key, newNode);
        return new JacksonArray(newNode, mapper);
    }
    
    @Override
    public ContainerValue remove(String key) {
        JsonNode removed = node.remove(key);
        if (removed == null) {
            return null;
        }
        return JacksonContainerFactory.wrap(removed, mapper);
    }
    
    @Override
    public boolean containsKey(String key) {
        return node.has(key);
    }
    
    @Override
    public void putAll(Map<String, ?> map) {
        map.forEach(this::put);
    }
    
    @Override
    public Set<Map.Entry<String, ContainerValue>> entrySet() {
        Set<Map.Entry<String, ContainerValue>> result = new LinkedHashSet<>();
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            result.add(new AbstractMap.SimpleEntry<>(
                entry.getKey(), 
                JacksonContainerFactory.wrap(entry.getValue(), mapper)
            ));
        }
        return result;
    }
    
    @Override
    public Set<String> keySet() {
        Set<String> keys = new LinkedHashSet<>();
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        return keys;
    }
    
    @Override
    public ContainerValue get(String key) {
        JsonNode value = node.get(key);
        if (value == null) {
            return null;
        }
        return JacksonContainerFactory.wrap(value, mapper);
    }
    
    @Override
    public boolean has(String key) {
        return node.has(key);
    }
    
    @Override
    public void clear() {
        node.removeAll();
    }
    
    @Override
    public ContainerFactory getContainerFactory() {
        return JacksonContainerFactory.getInstance();
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.OBJECT;
    }
    
    @Override
    public Object raw() {
        return this;
    }
    
    @Override
    public ContainerWriter<? extends Enum<?>> getWriter() {
        if (writer == null) {
            writer = new JacksonWriter(this, mapper);
        }
        return writer;
    }
    
    @Override
    public Iterator<Map.Entry<String, ContainerValue>> iterator() {
        return entrySet().iterator();
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
        return node.hashCode();
    }
    
    /**
     * ContainerValue를 JsonNode로 변환
     */
    private JsonNode toJsonNode(ContainerValue value) {
        if (value == null || value.isNull()) {
            return node.nullNode();
        } else if (value.isPrimitive()) {
            return primitiveToJsonNode((PrimitiveValue) value);
        } else if (value instanceof JacksonObject) {
            return ((JacksonObject) value).node;
        } else if (value instanceof JacksonArray) {
            return ((JacksonArray) value).getArrayNode();
        } else if (value.isObject()) {
            // 다른 구현체의 ObjectContainer 변환
            ObjectNode objectNode = mapper.createObjectNode();
            ObjectContainer obj = value.asObject();
            for (Map.Entry<String, ContainerValue> entry : obj) {
                objectNode.set(entry.getKey(), toJsonNode(entry.getValue()));
            }
            return objectNode;
        } else if (value.isArray()) {
            // 다른 구현체의 ArrayContainer 변환
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
     * PrimitiveValue를 JsonNode로 변환
     */
    private JsonNode primitiveToJsonNode(PrimitiveValue value) {
        Object raw = value.raw();
        if (raw == null) {
            return node.nullNode();
        } else if (raw instanceof String) {
            return node.textNode((String) raw);
        } else if (raw instanceof Integer) {
            return node.numberNode((Integer) raw);
        } else if (raw instanceof Long) {
            return node.numberNode((Long) raw);
        } else if (raw instanceof Float) {
            return node.numberNode((Float) raw);
        } else if (raw instanceof Double) {
            return node.numberNode((Double) raw);
        } else if (raw instanceof Boolean) {
            return node.booleanNode((Boolean) raw);
        } else if (raw instanceof byte[]) {
            return node.binaryNode((byte[]) raw);
        } else if (raw instanceof Number) {
            return mapper.valueToTree(raw);
        }
        return node.textNode(String.valueOf(raw));
    }
}