package com.hancomins.jsn4j.json5;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Exception;
import com.hancomins.jsn4j.ContainerParser;
import com.hancomins.jsn4j.ContainerValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Scanner;

public class Json5Parser implements ContainerParser {
    
    @Override
    public ContainerValue parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON5 string cannot be null or empty");
        }
        
        try {
            // JSON5 문자열이 객체인지 배열인지 확인
            String trimmed = value.trim();
            if (trimmed.startsWith("{")) {
                JSON5Object obj = new JSON5Object(value);
                return Json5ContainerFactory.wrap(obj);
            } else if (trimmed.startsWith("[")) {
                JSON5Array arr = new JSON5Array(value);
                return Json5ContainerFactory.wrap(arr);
            } else {
                // 원시 값인 경우
                JSON5Array wrapper = new JSON5Array("[" + value + "]");
                if (wrapper.size() > 0) {
                    return Json5ContainerFactory.wrap(wrapper.get(0));
                }
                throw new IllegalArgumentException("Unable to parse JSON5 value: " + value);
            }
        } catch (JSON5Exception e) {
            throw new IllegalArgumentException("Invalid JSON5: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON5: " + e.getMessage(), e);
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
            throw new IllegalArgumentException("Failed to parse JSON5 from reader: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ContainerValue parse(InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        
        try (Scanner scanner = new Scanner(input, "UTF-8")) {
            scanner.useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";
            return parse(content);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON5 from input stream: " + e.getMessage(), e);
        }
    }
}