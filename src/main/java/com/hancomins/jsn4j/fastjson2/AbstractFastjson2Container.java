package com.hancomins.jsn4j.fastjson2;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hancomins.jsn4j.*;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract base class for Fastjson2 container implementations.
 * Provides common functionality for Fastjson2Object and Fastjson2Array.
 */
abstract class AbstractFastjson2Container implements ContainerValue, ContainerFactoryProvidable {
    protected Fastjson2Writer writer;
    
    public ContainerFactory getContainerFactory() {
        return Fastjson2ContainerFactory.getInstance();
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
     * Converts a ContainerValue to Fastjson2 value (Object).
     * This method is shared between Fastjson2Object and Fastjson2Array.
     */
    protected Object toFastjson2Value(ContainerValue value) {
        if (value == null || value.isNull()) {
            return null;
        } else if (value.isPrimitive()) {
            return value.raw();
        } else if (value instanceof Fastjson2Object) {
            return ((Fastjson2Object) value).getJSONObject();
        } else if (value instanceof Fastjson2Array) {
            return ((Fastjson2Array) value).getJSONArray();
        } else if (value.isObject()) {
            // Convert non-Fastjson2 ObjectContainer
            JSONObject jsonObject = new JSONObject();
            ObjectContainer obj = value.asObject();
            for (Map.Entry<String, ContainerValue> entry : obj) {
                jsonObject.put(entry.getKey(), toFastjson2Value(entry.getValue()));
            }
            return jsonObject;
        } else if (value.isArray()) {
            // Convert non-Fastjson2 ArrayContainer
            JSONArray jsonArray = new JSONArray();
            ArrayContainer arr = value.asArray();
            for (ContainerValue item : arr) {
                jsonArray.add(toFastjson2Value(item));
            }
            return jsonArray;
        }
        return value.raw();
    }
}