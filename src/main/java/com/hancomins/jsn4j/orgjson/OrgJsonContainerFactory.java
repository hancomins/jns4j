package com.hancomins.jsn4j.orgjson;

import org.json.JSONArray;
import org.json.JSONObject;
import com.hancomins.jsn4j.*;

public class OrgJsonContainerFactory implements ContainerFactory {
    
    private static final OrgJsonContainerFactory INSTANCE = new OrgJsonContainerFactory();
    private static final String MODULE_NAME = "orgjson";
    private final OrgJsonParser parser;
    
    private OrgJsonContainerFactory() {
        this.parser = new OrgJsonParser();
    }
    
    public static OrgJsonContainerFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getJsn4jModuleName() {
        return MODULE_NAME;
    }
    
    @Override
    public ObjectContainer newObject() {
        return new OrgJsonObject();
    }
    
    @Override
    public ArrayContainer newArray() {
        return new OrgJsonArray();
    }
    
    @Override
    public ObjectContainer newObject(ContainerValue rootContainer) {
        return new OrgJsonObject();
    }
    
    @Override
    public ArrayContainer newArray(ContainerValue rootContainer) {
        return new OrgJsonArray();
    }
    
    @Override
    public ContainerParser getParser() {
        return parser;
    }
    
    /**
     * org.json의 Object를 JSN4J ContainerValue로 래핑합니다.
     */
    public static ContainerValue wrap(Object value) {
        if (value == null || value == JSONObject.NULL) {
            return new PrimitiveValue(null);
        } else if (value instanceof JSONObject) {
            return OrgJsonObject.wrap((JSONObject) value);
        } else if (value instanceof JSONArray) {
            return OrgJsonArray.wrap((JSONArray) value);
        } else if (value instanceof String || value instanceof Number || 
                   value instanceof Boolean || value instanceof byte[]) {
            return new PrimitiveValue(value);
        }
        return new PrimitiveValue(value);
    }
}