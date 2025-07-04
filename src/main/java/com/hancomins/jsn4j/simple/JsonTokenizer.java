package com.hancomins.jsn4j.simple;


import java.io.IOException;
import java.io.Reader;

public class JsonTokenizer {
    private final Reader reader;
    private int current = -2; // -2 = not read, -1 = EOF
    private int position = 0;
    private int line = 1;
    private int column = 0;

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
            throw new IllegalStateException("Unexpected EOF " + positionInfo());
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
            throw new IllegalStateException("Expected '" + expected + "', got '" + c + "' " + positionInfo());
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
            if (isEOF()) {
                throw new IllegalStateException("Unexpected EOF while reading string " + positionInfo());
            }
            char c = next();
            if (c == '\"') break;
            if (c == '\\') {
                if (isEOF()) {
                    throw new IllegalStateException("Unexpected EOF while reading escape sequence " + positionInfo());
                }
                char esc = next();
                switch (esc) {
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case '\"': sb.append('\"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'u': 
                        // Unicode escape sequence
                        StringBuilder unicode = new StringBuilder();
                        for (int i = 0; i < 4; i++) {
                            if (isEOF()) {
                                throw new IllegalStateException("Unexpected EOF in unicode escape sequence " + positionInfo());
                            }
                            char hex = next();
                            if (!isHexDigit(hex)) {
                                throw new IllegalStateException("Invalid unicode escape sequence: \\\\u" + unicode + hex + " " + positionInfo());
                            }
                            unicode.append(hex);
                        }
                        int codePoint = Integer.parseInt(unicode.toString(), 16);
                        sb.append((char) codePoint);
                        break;
                    default: throw new IllegalStateException("Invalid escape: \\\\" + esc + " " + positionInfo());
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public Number readNumber() {
        StringBuilder sb = new StringBuilder();
        boolean isDouble = false;
        
        // Sign
        if (peek() == '-') sb.append(next());
        
        // Integer part
        if (!isEOF() && Character.isDigit(peek())) {
            while (!isEOF() && Character.isDigit(peek())) {
                sb.append(next());
            }
        } else {
            throw new IllegalStateException("Expected digit " + positionInfo());
        }
        
        // Decimal part
        if (!isEOF() && peek() == '.') {
            isDouble = true;
            sb.append(next());
            if (!isEOF() && Character.isDigit(peek())) {
                while (!isEOF() && Character.isDigit(peek())) {
                    sb.append(next());
                }
            } else {
                throw new IllegalStateException("Expected digit after decimal point " + positionInfo());
            }
        }
        
        // Exponent part
        if (!isEOF() && (peek() == 'e' || peek() == 'E')) {
            isDouble = true;
            sb.append(next());
            if (!isEOF() && (peek() == '+' || peek() == '-')) {
                sb.append(next());
            }
            if (!isEOF() && Character.isDigit(peek())) {
                while (!isEOF() && Character.isDigit(peek())) {
                    sb.append(next());
                }
            } else {
                throw new IllegalStateException("Expected digit in exponent " + positionInfo());
            }
        }
        
        return isDouble ? Double.parseDouble(sb.toString()) : Long.parseLong(sb.toString());
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
        return "at line " + line + ", column " + column + " (position " + position + ")";
    }

    private int read() {
        try {
            int c = reader.read();
            position++;
            if (c == '\n') {
                line++;
                column = 0;
            } else if (c != -1) {
                column++;
            }
            return c;
        } catch (IOException e) {
            throw new RuntimeException("IO error at " + positionInfo(), e);
        }
    }
    
    private boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
