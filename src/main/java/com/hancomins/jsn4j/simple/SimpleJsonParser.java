package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SimpleJsonParser implements ContainerParser {

    @Override
    public ContainerValue parse(String value) {
        return parse(new StringReader(value));
    }




    @Override
    public ContainerValue parse(Reader reader) {
        JsonTokenizer tokenizer = new JsonTokenizer(reader);
        tokenizer.skipWhitespace();
        ContainerValue value = parseValue(tokenizer);
        tokenizer.skipWhitespace();
        if (!tokenizer.isEOF()) {
            throw new IllegalStateException("Extra content after end of JSON");
        }
        return value;
    }

    @Override
    public ContainerValue parse(InputStream input) {
        return parse(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    private ContainerValue parseValue(JsonTokenizer tokenizer) {
        tokenizer.skipWhitespace();
        char c = tokenizer.peek();
        if (c == '{') return parseObject(tokenizer);
        if (c == '[') return parseArray(tokenizer);
        if (c == '"') return new PrimitiveValue(tokenizer.readString());
        if (Character.isDigit(c) || c == '-') return new PrimitiveValue(tokenizer.readNumber());
        if (tokenizer.matchLiteral("true")) return new PrimitiveValue(true);
        if (tokenizer.matchLiteral("false")) return new PrimitiveValue(false);
        if (tokenizer.matchLiteral("null")) return new PrimitiveValue(null);
        throw new IllegalArgumentException("Unexpected token at: " + tokenizer.positionInfo());
    }

    private ObjectContainer parseObject(JsonTokenizer tokenizer) {
        tokenizer.expect('{');
        SimpleObject obj = new SimpleObject();
        tokenizer.skipWhitespace();
        if (tokenizer.peek() == '}') {
            tokenizer.expect('}');
            return obj;
        }
        while (true) {
            tokenizer.skipWhitespace();
            String key = tokenizer.readString();
            tokenizer.skipWhitespace();
            tokenizer.expect(':');
            ContainerValue value = parseValue(tokenizer);
            obj.put(key, value);
            tokenizer.skipWhitespace();
            char next = tokenizer.peek();
            if (next == ',') {
                tokenizer.expect(',');
                continue;
            } else if (next == '}') {
                tokenizer.expect('}');
                break;
            } else {
                throw new IllegalStateException("Expected ',' or '}' in object");
            }
        }
        return obj;
    }

    private ArrayContainer parseArray(JsonTokenizer tokenizer) {
        tokenizer.expect('[');
        SimpleArray arr = new SimpleArray();
        tokenizer.skipWhitespace();
        if (tokenizer.peek() == ']') {
            tokenizer.expect(']');
            return arr;
        }
        while (true) {
            ContainerValue value = parseValue(tokenizer);
            arr.put(value);
            tokenizer.skipWhitespace();
            char next = tokenizer.peek();
            if (next == ',') {
                tokenizer.expect(',');
                continue;
            } else if (next == ']') {
                tokenizer.expect(']');
                break;
            } else {
                throw new IllegalStateException("Expected ',' or ']' in array");
            }
        }
        return arr;
    }
}
