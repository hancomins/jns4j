package com.hancomins.jsn4j.fastjson2;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hancomins.jsn4j.*;

public class Fastjson2ContainerFactory implements ContainerFactory {
    
    private static final Fastjson2ContainerFactory INSTANCE = new Fastjson2ContainerFactory();
    private static final String MODULE_NAME = "fastjson2";
    private final Fastjson2Parser parser;
    
    private Fastjson2ContainerFactory() {
        this.parser = new Fastjson2Parser();
    }
    
    public static Fastjson2ContainerFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getJsn4jModuleName() {
        return MODULE_NAME;
    }
    
    @Override
    public ObjectContainer newObject() {
        return new Fastjson2Object();
    }
    
    @Override
    public ArrayContainer newArray() {
        return new Fastjson2Array();
    }
    
    @Override
    public ObjectContainer newObject(ContainerValue rootContainer) {
        return new Fastjson2Object();
    }
    
    @Override
    public ArrayContainer newArray(ContainerValue rootContainer) {
        return new Fastjson2Array();
    }
    
    @Override
    public ContainerParser getParser() {
        return parser;
    }
    
    /**
     * Fastjson2의 Object를 JSN4J ContainerValue로 래핑합니다.
     */
    public static ContainerValue wrap(Object value) {
        if (value == null) {
            return new PrimitiveValue(null);
        } else if (value instanceof JSONObject) {
            return Fastjson2Object.wrap((JSONObject) value);
        } else if (value instanceof JSONArray) {
            return Fastjson2Array.wrap((JSONArray) value);
        } else if (value instanceof String || value instanceof Number || 
                   value instanceof Boolean || value instanceof byte[]) {
            return new PrimitiveValue(value);
        }
        return new PrimitiveValue(value);
    }
}