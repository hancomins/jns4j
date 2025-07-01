package com.hancomins.jsn4j.simple;

/**
 * Interface for JSON formatting strategies.
 * Allows different formatting implementations (compact, pretty-print, etc.)
 */
interface JsonFormatter {
    void startObject(StringBuilder sb, int depth);
    void endObject(StringBuilder sb, int depth);
    void startArray(StringBuilder sb, int depth);
    void endArray(StringBuilder sb, int depth);
    void beforeKey(StringBuilder sb, int depth, boolean first);
    void afterKey(StringBuilder sb);
    void beforeValue(StringBuilder sb, int depth, boolean first);
}

/**
 * Compact JSON formatter - no extra whitespace
 */
class CompactFormatter implements JsonFormatter {
    @Override
    public void startObject(StringBuilder sb, int depth) {
        sb.append("{");
    }
    
    @Override
    public void endObject(StringBuilder sb, int depth) {
        sb.append("}");
    }
    
    @Override
    public void startArray(StringBuilder sb, int depth) {
        sb.append("[");
    }
    
    @Override
    public void endArray(StringBuilder sb, int depth) {
        sb.append("]");
    }
    
    @Override
    public void beforeKey(StringBuilder sb, int depth, boolean first) {
        if (!first) sb.append(",");
    }
    
    @Override
    public void afterKey(StringBuilder sb) {
        sb.append(":");
    }
    
    @Override
    public void beforeValue(StringBuilder sb, int depth, boolean first) {
        if (!first) sb.append(",");
    }
}

/**
 * Pretty-print JSON formatter - with indentation and newlines
 */
class PrettyFormatter implements JsonFormatter {
    private final String indentString;
    
    public PrettyFormatter() {
        this("  "); // Default 2-space indent
    }
    
    public PrettyFormatter(String indentString) {
        this.indentString = indentString;
    }
    
    @Override
    public void startObject(StringBuilder sb, int depth) {
        sb.append("{\n");
    }
    
    @Override
    public void endObject(StringBuilder sb, int depth) {
        sb.append("\n");
        indent(sb, depth);
        sb.append("}");
    }
    
    @Override
    public void startArray(StringBuilder sb, int depth) {
        sb.append("[\n");
    }
    
    @Override
    public void endArray(StringBuilder sb, int depth) {
        sb.append("\n");
        indent(sb, depth);
        sb.append("]");
    }
    
    @Override
    public void beforeKey(StringBuilder sb, int depth, boolean first) {
        if (!first) sb.append(",\n");
        indent(sb, depth + 1);
    }
    
    @Override
    public void afterKey(StringBuilder sb) {
        sb.append(": ");
    }
    
    @Override
    public void beforeValue(StringBuilder sb, int depth, boolean first) {
        if (!first) sb.append(",\n");
        indent(sb, depth + 1);
    }
    
    private void indent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append(indentString);
        }
    }
}