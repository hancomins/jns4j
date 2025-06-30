package com.hancomins.jsn4j;

import java.io.IOException;
import java.io.OutputStream;

public interface ContainerWriter<E extends Enum<E>> {


    void putOption(E option, Object value);
    default void enable(E option) {
        putOption(option, Boolean.TRUE);
    }
    boolean putOption(String optionName, Object value);
    default boolean enable(String optionName) {
        return putOption(optionName, Boolean.TRUE);
    }

    boolean removeOption(String optionName);
    void removeOption(E option);
    String write();
    void write(OutputStream outputStream) throws IOException;


}
