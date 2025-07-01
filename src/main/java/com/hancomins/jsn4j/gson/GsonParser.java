package com.hancomins.jsn4j.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.hancomins.jsn4j.ContainerParser;
import com.hancomins.jsn4j.ContainerValue;

import java.io.*;

public class GsonParser implements ContainerParser {
    
    private final Gson gson;
    
    public GsonParser(Gson gson) {
        this.gson = gson;
    }
    
    @Override
    public ContainerValue parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            JsonElement element = JsonParser.parseString(json);
            return GsonContainerFactory.wrap(element);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ContainerValue parse(Reader reader) {
        if (reader == null) {
            return null;
        }
        
        try {
            JsonElement element = JsonParser.parseReader(reader);
            return GsonContainerFactory.wrap(element);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ContainerValue parse(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        
        try (Reader reader = new InputStreamReader(inputStream, "UTF-8")) {
            return parse(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input stream: " + e.getMessage(), e);
        }
    }
}