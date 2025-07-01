package com.hancomins.jsn4j.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hancomins.jsn4j.*;

import java.util.Map;

/**
 * Abstract base class for Gson container implementations.
 * Provides common functionality for GsonObject and GsonArray.
 */
abstract class AbstractGsonContainer implements ContainerValue, ContainerFactoryProvidable {
    protected GsonWriter writer;
    
    public ContainerFactory getContainerFactory() {
        return GsonContainerFactory.getInstance();
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
     * Converts a ContainerValue to JsonElement.
     * This method is shared between GsonObject and GsonArray.
     */
    protected JsonElement toJsonElement(ContainerValue value) {
        if (value == null || value.isNull()) {
            return com.google.gson.JsonNull.INSTANCE;
        } else if (value.isPrimitive()) {
            return primitiveToJsonElement((PrimitiveValue) value);
        } else if (value instanceof GsonObject) {
            return ((GsonObject) value).getJsonObject();
        } else if (value instanceof GsonArray) {
            return ((GsonArray) value).getJsonArray();
        } else if (value.isObject()) {
            // Convert non-Gson ObjectContainer
            JsonObject jsonObject = new JsonObject();
            ObjectContainer obj = value.asObject();
            for (Map.Entry<String, ContainerValue> entry : obj) {
                jsonObject.add(entry.getKey(), toJsonElement(entry.getValue()));
            }
            return jsonObject;
        } else if (value.isArray()) {
            // Convert non-Gson ArrayContainer
            JsonArray jsonArray = new JsonArray();
            ArrayContainer arr = value.asArray();
            for (ContainerValue item : arr) {
                jsonArray.add(toJsonElement(item));
            }
            return jsonArray;
        }
        
        // Fallback for raw values
        Object raw = value.raw();
        if (raw instanceof Number) {
            return new JsonPrimitive((Number) raw);
        } else if (raw instanceof Boolean) {
            return new JsonPrimitive((Boolean) raw);
        } else if (raw instanceof Character) {
            return new JsonPrimitive((Character) raw);
        } else if (raw instanceof String) {
            return new JsonPrimitive((String) raw);
        }
        return com.google.gson.JsonNull.INSTANCE;
    }
    
    /**
     * Converts a PrimitiveValue to JsonElement.
     */
    protected JsonElement primitiveToJsonElement(PrimitiveValue value) {
        Object raw = value.raw();
        if (raw == null) {
            return com.google.gson.JsonNull.INSTANCE;
        } else if (raw instanceof Number) {
            return new JsonPrimitive((Number) raw);
        } else if (raw instanceof Boolean) {
            return new JsonPrimitive((Boolean) raw);
        } else if (raw instanceof Character) {
            return new JsonPrimitive((Character) raw);
        } else if (raw instanceof String) {
            return new JsonPrimitive((String) raw);
        } else if (raw instanceof byte[]) {
            // Convert byte array to base64 string
            return new JsonPrimitive(java.util.Base64.getEncoder().encodeToString((byte[]) raw));
        }
        // For any other type, convert to string
        return new JsonPrimitive(String.valueOf(raw));
    }
}