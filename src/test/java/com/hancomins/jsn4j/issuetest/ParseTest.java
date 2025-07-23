package com.hancomins.jsn4j.issuetest;

import com.hancomins.jsn4j.Jsn4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;


public class ParseTest {

    @Test
    public void ParseErrorTest() {
        String value = "{\"name\":\"한글이름\",\"age:10}";
        try {
            // Assuming parseJson is a method that parses the JSON string
            Jsn4j.parse(value);
            fail();
        } catch (Exception e) {
            // Handle the exception, e.g., log it or assert it
            System.out.println("Parse error: " + e.getMessage());
        }
    }



}