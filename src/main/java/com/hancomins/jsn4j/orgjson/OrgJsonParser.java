package com.hancomins.jsn4j.orgjson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.hancomins.jsn4j.ContainerParser;
import com.hancomins.jsn4j.ContainerValue;

import java.io.InputStream;
import java.io.Reader;

public class OrgJsonParser implements ContainerParser {
    
    @Override
    public ContainerValue parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        
        try {
            JSONTokener tokener = new JSONTokener(value);
            Object parsed = tokener.nextValue();
            return OrgJsonContainerFactory.wrap(parsed);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ContainerValue parse(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        
        try {
            JSONTokener tokener = new JSONTokener(reader);
            Object parsed = tokener.nextValue();
            return OrgJsonContainerFactory.wrap(parsed);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Failed to parse JSON from reader: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ContainerValue parse(InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        
        try {
            JSONTokener tokener = new JSONTokener(input);
            Object parsed = tokener.nextValue();
            return OrgJsonContainerFactory.wrap(parsed);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Failed to parse JSON from input stream: " + e.getMessage(), e);
        }
    }
}