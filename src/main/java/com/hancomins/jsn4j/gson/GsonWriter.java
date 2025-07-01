package com.hancomins.jsn4j.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.hancomins.jsn4j.ContainerValue;
import com.hancomins.jsn4j.ContainerWriter;
import com.hancomins.jsn4j.ValueType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class GsonWriter implements ContainerWriter<GsonWriteOption> {
    
    private final ContainerValue containerValue;
    private final EnumSet<GsonWriteOption> options = EnumSet.noneOf(GsonWriteOption.class);
    private final Map<GsonWriteOption, Object> optionValues = new HashMap<>();
    
    public GsonWriter(ContainerValue containerValue) {
        this.containerValue = containerValue;
    }
    
    @Override
    public void putOption(GsonWriteOption option, Object value) {
        options.add(option);
        if (option == GsonWriteOption.INDENT && value != null) {
            optionValues.put(option, value);
        }
    }
    
    @Override
    public boolean putOption(String optionName, Object value) {
        try {
            GsonWriteOption option = GsonWriteOption.valueOf(optionName.toUpperCase());
            putOption(option, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public boolean removeOption(String optionName) {
        try {
            GsonWriteOption option = GsonWriteOption.valueOf(optionName.toUpperCase());
            removeOption(option);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public void removeOption(GsonWriteOption option) {
        options.remove(option);
        optionValues.remove(option);
    }
    
    @Override
    public String write() {
        Gson gson = buildGson();
        JsonElement element = containerValueToJsonElement(containerValue);
        return gson.toJson(element);
    }
    
    @Override
    public void write(OutputStream outputStream) throws IOException {
        Gson gson = buildGson();
        JsonElement element = containerValueToJsonElement(containerValue);
        
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            gson.toJson(element, writer);
        }
    }
    
    /**
     * 옵션에 따라 Gson 인스턴스를 구성
     */
    @SuppressWarnings("StatementWithEmptyBody")
    private Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        
        if (options.contains(GsonWriteOption.PRETTY_PRINT)) {
            builder.setPrettyPrinting();
        }
        
        if (options.contains(GsonWriteOption.DISABLE_HTML_ESCAPING)) {
            builder.disableHtmlEscaping();
        }
        
        if (options.contains(GsonWriteOption.SERIALIZE_NULLS)) {
            builder.serializeNulls();
        }
        
        if (options.contains(GsonWriteOption.SERIALIZE_SPECIAL_FLOATING_POINT_VALUES)) {
            builder.serializeSpecialFloatingPointValues();
        }
        
        if (options.contains(GsonWriteOption.ENABLE_COMPLEX_MAP_KEY_SERIALIZATION)) {
            builder.enableComplexMapKeySerialization();
        }
        
        if (options.contains(GsonWriteOption.LENIENT)) {
            builder.setLenient();
        }
        
        // INDENT 옵션 처리
        if (options.contains(GsonWriteOption.INDENT) && optionValues.containsKey(GsonWriteOption.INDENT)) {
            // Gson doesn't directly support custom indentation, but we can use this for future enhancement
            // For now, PRETTY_PRINT will use default indentation
        }
        
        return builder.create();
    }
    
    /**
     * ContainerValue를 JsonElement로 변환
     */
    private JsonElement containerValueToJsonElement(ContainerValue value) {
        if (value == null || value.isNull()) {
            return com.google.gson.JsonNull.INSTANCE;
        } else if (value instanceof GsonObject) {
            return ((GsonObject) value).getJsonObject();
        } else if (value instanceof GsonArray) {
            return ((GsonArray) value).getJsonArray();
        } else if (value instanceof AbstractGsonContainer) {
            return ((AbstractGsonContainer) value).toJsonElement(value);
        } else if (value.isObject() || value.isArray()) {
            // Non-Gson container - convert using the abstract class method
            AbstractGsonContainer temp = new AbstractGsonContainer() {
                @Override
                public ValueType getValueType() {
                    return value.getValueType();
                }
                
                @Override
                public ContainerWriter<? extends Enum<?>> getWriter() {
                    return GsonWriter.this;
                }
            };
            return temp.toJsonElement(value);
        } else if (value.isPrimitive()) {
            // For primitive values, use the raw value
            Object raw = value.raw();
            if (raw == null) {
                return com.google.gson.JsonNull.INSTANCE;
            } else if (raw instanceof Number) {
                return new com.google.gson.JsonPrimitive((Number) raw);
            } else if (raw instanceof Boolean) {
                return new com.google.gson.JsonPrimitive((Boolean) raw);
            } else if (raw instanceof Character) {
                return new com.google.gson.JsonPrimitive((Character) raw);
            } else if (raw instanceof String) {
                return new com.google.gson.JsonPrimitive((String) raw);
            } else if (raw instanceof byte[]) {
                return new com.google.gson.JsonPrimitive(
                    java.util.Base64.getEncoder().encodeToString((byte[]) raw)
                );
            }
            return new com.google.gson.JsonPrimitive(String.valueOf(raw));
        }
        return com.google.gson.JsonNull.INSTANCE;
    }
    
    /**
     * enable 헬퍼 메서드 - 편의를 위해 제공
     */
    @SuppressWarnings("unused")
    public GsonWriter enable(GsonWriteOption... options) {
        for (GsonWriteOption option : options) {
            putOption(option, true);
        }
        return this;
    }
    
    /**
     * disable 헬퍼 메서드 - 편의를 위해 제공
     */
    @SuppressWarnings("unused")
    public GsonWriter disable(GsonWriteOption... options) {
        for (GsonWriteOption option : options) {
            removeOption(option);
        }
        return this;
    }
}