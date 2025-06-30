package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class SimpleJsonWriter implements ContainerWriter<SimpleJsonWriteOption> {


    private boolean isPrettyPrint = false;
    private int indent = 0;
    private final ContainerValue containerValue;

    SimpleJsonWriter(ContainerValue containerValue) {
        this.containerValue = containerValue;
    }

    @Override
    public void putOption(SimpleJsonWriteOption option, Object value) {
        if (option == SimpleJsonWriteOption.PRETTY_PRINT
                && (Objects.equals(value, Boolean.TRUE) ||
                (value  instanceof  Number && ((Number) value).intValue() > 0)) ||
                "true".equalsIgnoreCase(String.valueOf(value).trim()) ||
                "1".equalsIgnoreCase(String.valueOf(value).trim())) {
            isPrettyPrint = (boolean) value;
        }
        else if(option == SimpleJsonWriteOption.PRETTY_PRINT && value instanceof Number) {
            indent = ((Number) value).intValue();
        }
    }

    @Override
    public boolean putOption(String optionName, Object value) {
        try {
            SimpleJsonWriteOption option = SimpleJsonWriteOption.valueOf(optionName.toUpperCase());
            putOption(option, value);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;

    }

    @Override
    public boolean removeOption(String optionName) {
        try {
            SimpleJsonWriteOption option = SimpleJsonWriteOption.valueOf(optionName.toUpperCase());
            removeOption(option);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    @Override
    public void removeOption(SimpleJsonWriteOption option) {
        switch (option) {
            case PRETTY_PRINT:
                isPrettyPrint = false;
                break;
            case INDENT_OUTPUT:
                indent = 0;
                break;
            default:
                break;
        }

    }

    @Override
    public String write() {
        if(isPrettyPrint) {
            return prettyPrint(containerValue, indent);
        }
        StringBuilder sb = new StringBuilder();
        write(containerValue, sb);
        return sb.toString();
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        String json = write();
        outputStream.write(json.getBytes(StandardCharsets.UTF_8));

    }

    public static String prettyPrint(ContainerValue value, int indent) {
        StringBuilder sb = new StringBuilder();
        prettyPrint(value, sb, 0);
        return sb.toString();
    }

    // --- Compact writer ---
    private static void write(ContainerValue value, StringBuilder sb) {
        if (value == null || value.isNull()) {
            sb.append("null");
        } else if (value.isPrimitive()) {
            writePrimitive((PrimitiveValue) value, sb);
        } else if (value.isObject()) {
            writeObject((ObjectContainer) value, sb);
        } else if (value.isArray()) {
            writeArray((ArrayContainer) value, sb);
        } else {
            sb.append("null");
        }
    }

    private static void writePrimitive(PrimitiveValue value, StringBuilder sb) {
        Object raw = value.raw();
        if (raw == null) {
            sb.append("null");
        } else if (raw instanceof String || raw instanceof Character) {
            sb.append('"').append(escape(String.valueOf(raw))).append('"');
        } else if (raw instanceof Boolean || raw instanceof Number) {
            sb.append(raw.toString());
        } else {
            sb.append('"').append(escape(String.valueOf(raw))).append('"');
        }
    }

    private static void writeObject(ObjectContainer obj, StringBuilder sb) {
        sb.append("{");
        Iterator<Map.Entry<String, ContainerValue>> it = obj.iterator();
        boolean first = true;
        while (it.hasNext()) {
            if (!first) sb.append(",");
            Map.Entry<String, ContainerValue> entry = it.next();
            sb.append('"').append(escape(entry.getKey())).append('"');
            sb.append(":");
            write(entry.getValue(), sb);
            first = false;
        }
        sb.append("}");
    }

    private static void writeArray(ArrayContainer arr, StringBuilder sb) {
        sb.append("[");
        Iterator<ContainerValue> it = arr.iterator();
        boolean first = true;
        while (it.hasNext()) {
            if (!first) sb.append(",");
            write(it.next(), sb);
            first = false;
        }
        sb.append("]");
    }

    // --- Pretty printer ---
    private static void prettyPrint(ContainerValue value, StringBuilder sb, int indent) {
        if (value == null || value.isNull()) {
            sb.append("null");
        } else if (value.isPrimitive()) {
            writePrimitive((PrimitiveValue) value, sb);
        } else if (value.isObject()) {
            sb.append("{\n");
            Iterator<Map.Entry<String, ContainerValue>> it = ((ObjectContainer) value).iterator();
            boolean first = true;
            while (it.hasNext()) {
                if (!first) sb.append(",\n");
                indent(sb, indent + 1);
                Map.Entry<String, ContainerValue> entry = it.next();
                sb.append('"').append(escape(entry.getKey())).append('"').append(": ");
                prettyPrint(entry.getValue(), sb, indent + 1);
                first = false;
            }
            sb.append("\n");
            indent(sb, indent);
            sb.append("}");
        } else if (value.isArray()) {
            sb.append("[\n");
            Iterator<ContainerValue> it = ((ArrayContainer) value).iterator();
            boolean first = true;
            while (it.hasNext()) {
                if (!first) sb.append(",\n");
                indent(sb, indent + 1);
                prettyPrint(it.next(), sb, indent + 1);
                first = false;
            }
            sb.append("\n");
            indent(sb, indent);
            sb.append("]");
        }
    }

    private static void indent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) sb.append("  "); // 2-space indent
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String toString() {
        return write();
    }
}
