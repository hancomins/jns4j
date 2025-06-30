package com.hancomins.jsn4j.json5;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.jsn4j.*;

public class Json5ContainerFactory implements ContainerFactory {
    
    private static final Json5ContainerFactory INSTANCE = new Json5ContainerFactory();
    private static final String MODULE_NAME = "json5";
    private final Json5Parser parser;
    
    private Json5ContainerFactory() {
        this.parser = new Json5Parser();
    }
    
    public static Json5ContainerFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getJsn4jModuleName() {
        return MODULE_NAME;
    }
    
    @Override
    public ObjectContainer newObject() {
        return new Json5Object();
    }
    
    @Override
    public ArrayContainer newArray() {
        return new Json5Array();
    }
    
    @Override
    public ObjectContainer newObject(ContainerValue rootContainer) {
        return new Json5Object();
    }
    
    @Override
    public ArrayContainer newArray(ContainerValue rootContainer) {
        return new Json5Array();
    }
    
    @Override
    public ContainerParser getParser() {
        return parser;
    }
    
    /**
     * JSON5 값을 JSN4J ContainerValue로 래핑합니다.
     */
    public static ContainerValue wrap(Object value) {
        if (value == null) {
            return new PrimitiveValue(null);
        } else if (value instanceof JSON5Object) {
            return Json5Object.wrap((JSON5Object) value);
        } else if (value instanceof JSON5Array) {
            return Json5Array.wrap((JSON5Array) value);
        } else if (value instanceof String || value instanceof Number || 
                   value instanceof Boolean || value instanceof byte[]) {
            return new PrimitiveValue(value);
        }
        return new PrimitiveValue(value);
    }
}