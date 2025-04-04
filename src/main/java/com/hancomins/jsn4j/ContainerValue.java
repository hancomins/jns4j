
package com.hancomins.jsn4j;


/**
 * JSN4j의 모든 값이 구현해야 하는 최상위 인터페이스
 * (int, string, boolean, object, array 등)
 */
public interface ContainerValue {

    ValueType getValueType();
    /**
     * 원시 Object 값 반환 (JSN4jPrimitive일 경우만 의미 있음)
     * - int, long, boolean, String, byte[], null 등
     * - ObjectContainer나 ArrayContainer는 자기 자신 반환 가능
     */
    Object raw();

    default ContainerWriter<? extends Enum<?>> getWriter() {
        return null;
    }

    /**
     * 이 값이 null 타입인지 확인
     */
    default boolean isNull() {
        return raw() == null;
    }

    /**
     * 이 값이 오브젝트인지 확인
     */
    default boolean isObject() {
        return getValueType() == ValueType.OBJECT;
    }

    default boolean isPrimitive() {
        return getValueType() == ValueType.PRIMITIVE;
    }

    /**
     * 이 값이 배열인지 확인
     */
    default boolean isArray() {
        return getValueType() == ValueType.ARRAY;
    }




    /**
     * 오브젝트 캐스팅 시도 (아닐 경우 예외)
     */
    default ObjectContainer asObject() {
        if(isObject()) {
            return (ObjectContainer) this;
        }
        throw new ClassCastException("Not a ObjectContainer: " + getValueType());
    }

    /**
     * 배열 캐스팅 시도 (아닐 경우 예외)
     */
    default ArrayContainer asArray() {
        if(isArray()) {
            return (ArrayContainer) this;
        }
        throw  new ClassCastException("Not a ArrayContainer: " + getValueType());
    }








}

