package com.hancomins.jsn4j;

public interface TypeReferenceProvider<T> {
    Class<T> getRawType(); // 기본 타입
    Object getNativeType(); // Jackson/Gson 등의 원형 객체
}
