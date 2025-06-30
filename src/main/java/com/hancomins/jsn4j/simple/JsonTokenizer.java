package com.hancomins.jsn4j.simple;


import java.io.IOException;
import java.io.Reader;

public class JsonTokenizer {
    private final Reader reader;
    private int current = -2; // -2 = not read, -1 = EOF
    private int position = 0;

    public JsonTokenizer(Reader reader) {
        this.reader = reader;
    }

    public void skipWhitespace() {
        while (!isEOF()) {
            char c = peek();
            if (!Character.isWhitespace(c)) break;
            next();
        }
    }

    public char peek() {
        if (current == -2) {
            current = read();
        }
        if (current == -1) {
            throw new IllegalStateException("Unexpected EOF");
        }
        return (char) current;
    }

    public char next() {
        if (current == -2) {
            current = read();
        }
        char c = (char) current;
        current = read();
        return c;
    }

    public void expect(char expected) {
        char c = next();
        if (c != expected) {
            throw new IllegalStateException("Expected '" + expected + "', got '" + c + "'");
        }
    }

    public boolean isEOF() {
        if (current == -2) {
            current = read();
        }
        return current == -1;
    }

    public String readString() {
        expect('\"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '\"') break;
            if (c == '\\') {
                char esc = next();
                switch (esc) {
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case '\"': sb.append('\"'); break;
                    case '\\': sb.append('\\'); break;
                    default: throw new IllegalStateException("Invalid escape: \\\\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public Number readNumber() {
        StringBuilder sb = new StringBuilder();
        if (peek() == '-') sb.append(next());
        while (!isEOF() && Character.isDigit(peek())) {
            sb.append(next());
        }
        if (!isEOF() && peek() == '.') {
            sb.append(next());
            while (!isEOF() && Character.isDigit(peek())) {
                sb.append(next());
            }
            return Double.parseDouble(sb.toString());
        }
        return Long.parseLong(sb.toString());
    }

    public boolean matchLiteral(String literal) {
        skipWhitespace();
        for (int i = 0; i < literal.length(); i++) {
            if (peek() != literal.charAt(i)) {
                return false;
            }
            next();
        }
        return true;
    }

    public String positionInfo() {
        return "at position " + position;
    }

    private int read() {
        try {
            int c = reader.read();
            position++;
            return c;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
