package com.hancomins.jsn4j.json5;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.jsn4j.*;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract base class for JSON5 container implementations.
 * Provides common functionality for Json5Object and Json5Array.
 */
abstract class AbstractJson5Container implements ContainerValue, ContainerFactoryProvidable {
    protected Json5Writer writer;
    
    public ContainerFactory getContainerFactory() {
        return Json5ContainerFactory.getInstance();
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
     * Converts a ContainerValue to JSON5 value (Object).
     * This method is shared between Json5Object and Json5Array.
     */
    protected Object toJson5Value(ContainerValue value) {
        if (value == null || value.isNull()) {
            return null;
        } else if (value.isPrimitive()) {
            return value.raw();
        } else if (value instanceof Json5Object) {
            return ((Json5Object) value).getJSON5Object();
        } else if (value instanceof Json5Array) {
            return ((Json5Array) value).getJSON5Array();
        } else if (value.isObject()) {
            // Convert non-JSON5 ObjectContainer
            JSON5Object json5Object = new JSON5Object();
            ObjectContainer obj = value.asObject();
            for (Map.Entry<String, ContainerValue> entry : obj) {
                json5Object.put(entry.getKey(), toJson5Value(entry.getValue()));
            }
            return json5Object;
        } else if (value.isArray()) {
            // Convert non-JSON5 ArrayContainer
            JSON5Array json5Array = new JSON5Array();
            ArrayContainer arr = value.asArray();
            for (ContainerValue item : arr) {
                json5Array.add(toJson5Value(item));
            }
            return json5Array;
        }
        return value.raw();
    }
}