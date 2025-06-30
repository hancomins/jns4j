package com.hancomins.jsn4j.orgjson;

/**
 * org.json Writer 옵션
 */
public enum OrgJsonWriteOption {
    /**
     * Pretty print 활성화
     */
    PRETTY_PRINT,
    
    /**
     * 들여쓰기 설정
     */
    INDENT_OUTPUT,
    
    /**
     * 들여쓰기 크기 (기본값: 4)
     */
    INDENT_SIZE
}