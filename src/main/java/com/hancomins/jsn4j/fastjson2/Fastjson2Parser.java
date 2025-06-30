package com.hancomins.jsn4j.fastjson2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.hancomins.jsn4j.ContainerParser;
import com.hancomins.jsn4j.ContainerValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class Fastjson2Parser implements ContainerParser {
    
    @Override
    public ContainerValue parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        
        try {
            Object parsed = JSON.parse(value);
            return Fastjson2ContainerFactory.wrap(parsed);
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
            // Reader를 String으로 읽기
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
            return parse(sb.toString());
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
            // InputStream을 byte[]로 읽기
            byte[] bytes = new byte[input.available()];
            int totalRead = 0;
            int read;
            while ((read = input.read(bytes, totalRead, bytes.length - totalRead)) != -1) {
                totalRead += read;
                if (totalRead == bytes.length) {
                    // 버퍼가 가득 찬 경우 확장
                    byte[] newBytes = new byte[bytes.length * 2];
                    System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                    bytes = newBytes;
                }
            }
            
            // 실제 읽은 크기만큼의 배열로 조정
            if (totalRead < bytes.length) {
                byte[] actualBytes = new byte[totalRead];
                System.arraycopy(bytes, 0, actualBytes, 0, totalRead);
                bytes = actualBytes;
            }
            
            Object parsed = JSON.parse(bytes);
            return Fastjson2ContainerFactory.wrap(parsed);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read from input stream: " + e.getMessage(), e);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Failed to parse JSON from input stream: " + e.getMessage(), e);
        }
    }
}