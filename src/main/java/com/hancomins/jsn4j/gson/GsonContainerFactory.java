package com.hancomins.jsn4j.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hancomins.jsn4j.*;

public class GsonContainerFactory implements ContainerFactory {
    
    private static final String MODULE_NAME = "gson";
    private static GsonContainerFactory instance;
    
    private final Gson gson;
    private final GsonParser parser;
    
    private GsonContainerFactory() {
        this.gson = new GsonBuilder().create();
        this.parser = new GsonParser(gson);
    }
    
    /**
     * 싱글톤 인스턴스 반환
     */
    public static synchronized GsonContainerFactory getInstance() {
        if (instance == null) {
            instance = new GsonContainerFactory();
        }
        return instance;
    }
    
    @Override
    public String getJsn4jModuleName() {
        return MODULE_NAME;
    }
    
    @Override
    public ObjectContainer newObject() {
        return new GsonObject();
    }
    
    @Override
    public ArrayContainer newArray() {
        return new GsonArray();
    }
    
    @Override
    public ContainerParser getParser() {
        return parser;
    }
    
    /**
     * Gson 인스턴스 반환
     */
    public Gson getGson() {
        return gson;
    }
    
    /**
     * JsonElement를 적절한 ContainerValue로 변환
     */
    public static ContainerValue wrap(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return new PrimitiveValue(null);
        } else if (element.isJsonObject()) {
            return new GsonObject(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            return new GsonArray(element.getAsJsonArray());
        } else if (element.isJsonPrimitive()) {
            return wrapPrimitive(element.getAsJsonPrimitive());
        }
        return new PrimitiveValue(null);
    }
    
    /**
     * JsonPrimitive를 PrimitiveValue로 변환
     */
    private static PrimitiveValue wrapPrimitive(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return new PrimitiveValue(primitive.getAsBoolean());
        } else if (primitive.isNumber()) {
            Number number = primitive.getAsNumber();
            // Try to determine the most appropriate number type
            if (number instanceof Integer || number instanceof Short || number instanceof Byte) {
                return new PrimitiveValue(number.intValue());
            } else if (number instanceof Long) {
                return new PrimitiveValue(number.longValue());
            } else if (number instanceof Float) {
                return new PrimitiveValue(number.floatValue());
            } else if (number instanceof Double) {
                return new PrimitiveValue(number.doubleValue());
            } else {
                // Check if it's actually an integer
                double doubleValue = number.doubleValue();
                if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
                    if (doubleValue >= Integer.MIN_VALUE && doubleValue <= Integer.MAX_VALUE) {
                        return new PrimitiveValue((int) doubleValue);
                    } else if (doubleValue >= Long.MIN_VALUE && doubleValue <= Long.MAX_VALUE) {
                        return new PrimitiveValue((long) doubleValue);
                    }
                }
                return new PrimitiveValue(doubleValue);
            }
        } else if (primitive.isString()) {
            return new PrimitiveValue(primitive.getAsString());
        }
        // Fallback
        return new PrimitiveValue(primitive.getAsString());
    }
}