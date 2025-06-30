package com.hancomins.jsn4j.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hancomins.jsn4j.ContainerParser;
import com.hancomins.jsn4j.ContainerValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class JacksonParser implements ContainerParser {
    
    private final ObjectMapper mapper;
    
    public JacksonParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public ContainerValue parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        
        try {
            JsonNode node = mapper.readTree(value);
            return JacksonContainerFactory.wrap(node, mapper);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ContainerValue parse(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        
        try {
            JsonNode node = mapper.readTree(reader);
            return JacksonContainerFactory.wrap(node, mapper);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JSON from reader: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ContainerValue parse(InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        
        try {
            JsonNode node = mapper.readTree(input);
            return JacksonContainerFactory.wrap(node, mapper);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JSON from input stream: " + e.getMessage(), e);
        }
    }
}