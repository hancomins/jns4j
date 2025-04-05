package com.hancomins.jsn4j.simple;

import com.hancomins.jsn4j.*;

public class SimpleJsonContainerFactory implements ContainerFactory {

    private final static SimpleJsonContainerFactory instance = new SimpleJsonContainerFactory();

    private final static String NAME = "simple";
    private final SimpleJsonParser parser = new SimpleJsonParser();

    public static SimpleJsonContainerFactory getInstance() {
        return instance;
    }

    @Override
    public String getJsn4jModuleName() {
        return NAME;
    }

    @Override
    public ObjectContainer newObject() {
        return new SimpleObject();
    }

    @Override
    public ArrayContainer newArray() {
        return new SimpleArray();
    }


    @Override
    public ContainerParser getParser() {
        return parser;
    }


}
