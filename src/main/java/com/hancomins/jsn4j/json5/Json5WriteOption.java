package com.hancomins.jsn4j.json5;

/**
 * JSON5 Writer 옵션
 */
public enum Json5WriteOption {
    /**
     * Pretty print 활성화
     */
    PRETTY_PRINT,
    
    /**
     * 들여쓰기 설정
     */
    INDENT_OUTPUT,
    
    /**
     * 주석 포함 여부
     */
    INCLUDE_COMMENTS,
    
    /**
     * 작은따옴표 사용
     */
    USE_SINGLE_QUOTES,
    
    /**
     * 키에 따옴표 생략
     */
    UNQUOTED_KEYS,
    
    /**
     * 후행 쉼표 허용
     */
    TRAILING_COMMA
}