package com.hancomins.jsn4j.fastjson2;

/**
 * Fastjson2 Writer 옵션
 */
public enum Fastjson2WriteOption {
    /**
     * Pretty print 활성화
     */
    PRETTY_PRINT,
    
    /**
     * null 값 출력
     */
    WRITE_NULL_VALUES,
    
    /**
     * 맵의 null 값 출력
     */
    WRITE_MAP_NULL_VALUE,
    
    /**
     * 빈 컬렉션을 null로 출력
     */
    WRITE_NULL_LIST_AS_EMPTY,
    
    /**
     * 날짜를 타임스탬프로 출력
     */
    WRITE_DATE_USE_DATE_FORMAT,
    
    /**
     * 클래스 정보 출력
     */
    WRITE_CLASS_NAME,
    
    /**
     * 들여쓰기 설정
     */
    INDENT_OUTPUT
}