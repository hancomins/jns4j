package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract base class for Simple implementation containers.
 * Provides common functionality for SimpleObject and SimpleArray.
 */
abstract class AbstractSimpleContainer implements ContainerValue, ContainerFactoryProvidable {
    private SimpleJsonWriter jsonWriter;
    
    @Override
    public ContainerWriter<? extends Enum<?>> getWriter() {
        if (jsonWriter == null) {
            jsonWriter = new SimpleJsonWriter(this);
        }
        return jsonWriter;
    }
    
    public ContainerFactory getContainerFactory() {
        return SimpleJsonContainerFactory.getInstance();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContainerValue)) {
            return false;
        }
        return ContainerValues.equals(this, (ContainerValue) o);
    }
    
    @Override
    public String toString() {
        return getWriter().write();
    }
    
    @Override
    public Object raw() {
        return this;
    }
    
    /**
     * Converts a value to ContainerValue.
     * Handles ContainerValue, Collection, Map, and primitive types.
     */
    protected ContainerValue convertValue(Object value) {
        if (value instanceof ContainerValue) {
            return (ContainerValue) value;
        } else if (value instanceof Collection) {
            return ContainerValues.collectionToArrayContainer(this, (Collection<?>) value);
        } else if (value instanceof Map) {
            return ContainerValues.mapToObjectContainer(this, (Map<?, ?>) value);
        } else {
            return new PrimitiveValue(value);
        }
    }
}