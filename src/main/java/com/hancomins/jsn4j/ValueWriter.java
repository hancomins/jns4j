package com.hancomins.jsn4j;

public interface ValueWriter {
    <T extends ValueWriter> T put(Object value);
    <T extends ValueWriter> T put(char value);
    <T extends ValueWriter> T put(byte value);
    <T extends ValueWriter> T put(short value);
    <T extends ValueWriter> T put(int value);
    <T extends ValueWriter> T put(long value);
    <T extends ValueWriter> T put(float value);
    <T extends ValueWriter> T put(double value);
    <T extends ValueWriter> T put(boolean value);

    <T extends ValueWriter> T put(byte[] value);
    <T extends ValueWriter> T putNull();
}
