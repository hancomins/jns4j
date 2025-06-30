package com.hancomins.jsn4j.jackson;

/**
 * Jackson Writer 옵션
 */
public enum JacksonWriteOption {
    /**
     * Pretty print 활성화
     */
    PRETTY_PRINT,
    
    /**
     * null 값 제외
     */
    EXCLUDE_NULL,
    
    /**
     * 빈 객체/배열 제외
     */
    EXCLUDE_EMPTY,
    
    /**
     * 날짜를 타임스탬프로 출력
     */
    WRITE_DATES_AS_TIMESTAMPS,
    
    /**
     * 들여쓰기 설정
     */
    INDENT_OUTPUT
}