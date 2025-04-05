package com.hancomins.jsn4j;

public interface ContainerFactory extends ContainerFactoryProvidable  {
    String getJsn4jModuleName();

    ObjectContainer newObject();
    ArrayContainer newArray();


    default ObjectContainer newObject(ContainerValue rootContainer) {
        return newObject();
    }
    default ArrayContainer newArray(ContainerValue rootContainer) {
        return newArray();
    }

    default PrimitiveValue newPrimitive(Object value) {
        return new PrimitiveValue(value);
    }
    default PrimitiveValue newPrimitive(int value) {
        return new PrimitiveValue(value);
    }
    default PrimitiveValue newPrimitive(long value) {
        return new PrimitiveValue(value);
    }
    default PrimitiveValue newPrimitive(float value) {
        return new PrimitiveValue(value);
    }
    default PrimitiveValue newPrimitive(double value) {
        return new PrimitiveValue(value);
    }
    default PrimitiveValue newPrimitive(boolean value) {
        return new PrimitiveValue(value);
    }

    default PrimitiveValue newPrimitive(char value) {
        return new PrimitiveValue(value);
    }
    default PrimitiveValue newPrimitive(byte value) {
        return new PrimitiveValue((byte)value);
    }
    default PrimitiveValue newPrimitive(short value) {
        return new PrimitiveValue((int)value);
    }



    ContainerParser getParser();

    @Override
    default ContainerFactory getContainerFactory() {
        return this;
    }

}
