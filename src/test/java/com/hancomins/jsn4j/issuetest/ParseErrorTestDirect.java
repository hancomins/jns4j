package com.hancomins.jsn4j.issuetest;

import com.hancomins.jsn4j.simple.JsonTokenizer;
import java.io.StringReader;

public class ParseErrorTestDirect {
    public static void main(String[] args) {
        String value = "{\"name\":\"한글이름\",\"age:10}";
        try {
            JsonTokenizer tokenizer = new JsonTokenizer(new StringReader(value));
            tokenizer.skipWhitespace();
            tokenizer.expect('{');
            tokenizer.skipWhitespace();
            
            // Try to read the first key
            String key1 = tokenizer.readString();
            System.out.println("Key 1: " + key1);
            
            tokenizer.skipWhitespace();
            tokenizer.expect(':');
            tokenizer.skipWhitespace();
            
            // Read the first value
            String value1 = tokenizer.readString();
            System.out.println("Value 1: " + value1);
            
            tokenizer.skipWhitespace();
            tokenizer.expect(',');
            tokenizer.skipWhitespace();
            
            // Try to read the second key - this should fail
            String key2 = tokenizer.readString();
            System.out.println("Key 2: " + key2);
            
        } catch (Exception e) {
            System.out.println("Caught exception: " + e.getClass().getName() + " - " + e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}