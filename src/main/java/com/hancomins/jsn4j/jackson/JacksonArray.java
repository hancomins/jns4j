package com.hancomins.jsn4j.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hancomins.jsn4j.*;

import java.util.*;

public class JacksonArray extends AbstractJacksonContainer implements ArrayContainer {
    
    private final ArrayNode node;
    
    public JacksonArray(ObjectMapper mapper) {
        super(mapper);
        this.node = mapper.createArrayNode();
    }
    
    public JacksonArray(ArrayNode node, ObjectMapper mapper) {
        super(mapper);
        this.node = node;
    }
    
    /**
     * 정적 팩토리 메서드 - ArrayNode를 JacksonArray로 래핑
     */
    public static JacksonArray wrap(ArrayNode node) {
        return new JacksonArray(node, JacksonContainerFactory.getInstance().getObjectMapper());
    }
    
    public static JacksonArray wrap(ArrayNode node, ObjectMapper mapper) {
        return new JacksonArray(node, mapper);
    }
    
    /**
     * Jackson의 ArrayNode를 직접 반환
     */
    public ArrayNode getArrayNode() {
        return node;
    }
    
    @Override
    public ArrayContainer put(int index, Object value) {
        ensureCapacity(index + 1);
        if (value == null) {
            node.setNull(index);
        } else if (value instanceof ContainerValue) {
            node.set(index, toJsonNode((ContainerValue) value));
        } else if (value instanceof Collection) {
            ArrayContainer array = new JacksonArray(mapper);
            for (Object item : (Collection<?>) value) {
                array.put(item);
            }
            node.set(index, ((JacksonArray) array).getArrayNode());
        } else if (value instanceof Map) {
            ObjectContainer object = new JacksonObject(mapper);
            ((Map<?, ?>) value).forEach((k, v) -> object.put(String.valueOf(k), v));
            node.set(index, ((JacksonObject) object).getObjectNode());
        } else {
            node.set(index, mapper.valueToTree(value));
        }
        return this;
    }
    
    @Override
    public ArrayContainer put(Object value) {
        if (value == null) {
            node.addNull();
        } else if (value instanceof ContainerValue) {
            node.add(toJsonNode((ContainerValue) value));
        } else if (value instanceof Collection) {
            ArrayContainer array = new JacksonArray(mapper);
            for (Object item : (Collection<?>) value) {
                array.put(item);
            }
            node.add(((JacksonArray) array).getArrayNode());
        } else if (value instanceof Map) {
            ObjectContainer object = new JacksonObject(mapper);
            ((Map<?, ?>) value).forEach((k, v) -> object.put(String.valueOf(k), v));
            node.add(((JacksonObject) object).getObjectNode());
        } else {
            node.add(mapper.valueToTree(value));
        }
        return this;
    }
    
    @Override
    public ObjectContainer newAndPutObject() {
        ObjectNode newNode = mapper.createObjectNode();
        node.add(newNode);
        return new JacksonObject(newNode, mapper);
    }
    
    @Override
    public ArrayContainer newAndPutArray() {
        ArrayNode newNode = mapper.createArrayNode();
        node.add(newNode);
        return new JacksonArray(newNode, mapper);
    }
    
    @Override
    public int size() {
        return node.size();
    }
    
    @Override
    public ContainerValue remove(int index) {
        if (index < 0 || index >= node.size()) {
            return null;
        }
        JsonNode removed = node.remove(index);
        if (removed == null) {
            return null;
        }
        return JacksonContainerFactory.wrap(removed, mapper);
    }
    
    @Override
    public ContainerValue get(int index) {
        if (index < 0 || index >= node.size()) {
            return null;
        }
        JsonNode value = node.get(index);
        if (value == null) {
            return null;
        }
        return JacksonContainerFactory.wrap(value, mapper);
    }
    
    @Override
    public void clear() {
        node.removeAll();
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }
    
    @Override
    public ContainerWriter<? extends Enum<?>> getWriter() {
        if (writer == null) {
            writer = new JacksonWriter(this, mapper);
        }
        return writer;
    }
    
    @Override
    public Iterator<ContainerValue> iterator() {
        return new Iterator<ContainerValue>() {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < node.size();
            }
            
            @Override
            public ContainerValue next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                JsonNode value = node.get(index++);
                return JacksonContainerFactory.wrap(value, mapper);
            }
        };
    }
    
    
    @Override
    public int hashCode() {
        return node.hashCode();
    }
    
    /**
     * 배열 크기를 지정된 용량만큼 확장
     */
    private void ensureCapacity(int minCapacity) {
        while (node.size() < minCapacity) {
            node.addNull();
        }
    }
    
}