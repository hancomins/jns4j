package com.hancomins.jsn4j.orgjson;

import org.json.JSONArray;
import org.json.JSONObject;
import com.hancomins.jsn4j.*;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract base class for org.json container implementations.
 * Provides common functionality for OrgJsonObject and OrgJsonArray.
 */
abstract class AbstractOrgJsonContainer implements ContainerValue, ContainerFactoryProvidable {
    protected OrgJsonWriter writer;
    
    public ContainerFactory getContainerFactory() {
        return OrgJsonContainerFactory.getInstance();
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
     * Converts a ContainerValue to org.json value (Object).
     * This method is shared between OrgJsonObject and OrgJsonArray.
     */
    protected Object toOrgJsonValue(ContainerValue value) {
        if (value == null || value.isNull()) {
            return JSONObject.NULL;
        } else if (value.isPrimitive()) {
            Object raw = value.raw();
            if (raw == null) {
                return JSONObject.NULL;
            }
            return raw;
        } else if (value instanceof OrgJsonObject) {
            return ((OrgJsonObject) value).getJSONObject();
        } else if (value instanceof OrgJsonArray) {
            return ((OrgJsonArray) value).getJSONArray();
        } else if (value.isObject()) {
            // Convert non-OrgJson ObjectContainer
            JSONObject jsonObject = new JSONObject();
            ObjectContainer obj = value.asObject();
            for (Map.Entry<String, ContainerValue> entry : obj) {
                jsonObject.put(entry.getKey(), toOrgJsonValue(entry.getValue()));
            }
            return jsonObject;
        } else if (value.isArray()) {
            // Convert non-OrgJson ArrayContainer
            JSONArray jsonArray = new JSONArray();
            ArrayContainer arr = value.asArray();
            for (ContainerValue item : arr) {
                jsonArray.put(toOrgJsonValue(item));
            }
            return jsonArray;
        }
        return value.raw() == null ? JSONObject.NULL : value.raw();
    }
}