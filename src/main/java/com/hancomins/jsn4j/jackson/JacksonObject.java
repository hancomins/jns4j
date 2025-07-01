package com.hancomins.jsn4j.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hancomins.jsn4j.*;

import java.util.*;

public class JacksonObject extends AbstractJacksonContainer implements ObjectContainer {
    
    private final ObjectNode node;
    
    public JacksonObject(ObjectMapper mapper) {
        super(mapper);
        this.node = mapper.createObjectNode();
    }
    
    public JacksonObject(ObjectNode node, ObjectMapper mapper) {
        super(mapper);
        this.node = node;
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
    public ValueType getValueType() {
        return ValueType.OBJECT;
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
    public int hashCode() {
        return node.hashCode();
    }
}