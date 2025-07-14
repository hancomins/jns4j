package com.hancomins.jsn4j;

public interface KeyValueWriter {
    <T extends KeyValueWriter> T put(String key, Object value);
    <T extends KeyValueWriter> T put(String key, char value);
    <T extends KeyValueWriter> T put(String key, byte value);
    <T extends KeyValueWriter> T put(String key, short value);
    <T extends KeyValueWriter> T put(String key, int value);
    <T extends KeyValueWriter> T put(String key, long value);
    <T extends KeyValueWriter> T put(String key, float value);
    <T extends KeyValueWriter> T put(String key, double value);
    <T extends KeyValueWriter> T put(String key, boolean value);
    <T extends KeyValueWriter> T putNull(String key);

}
