package com.hancomins.jsn4j.gson;

/**
 * Gson Writer 옵션 열거형
 */
public enum GsonWriteOption {
    
    /**
     * Pretty print - 들여쓰기와 줄바꿈을 포함한 포맷팅
     */
    PRETTY_PRINT,
    
    /**
     * HTML 이스케이프 비활성화
     * 기본적으로 <, >, &, =, ' 등의 문자를 이스케이프하는데, 이를 비활성화
     */
    DISABLE_HTML_ESCAPING,
    
    /**
     * null 값을 직렬화
     * 기본적으로 null 값은 생략되는데, 이 옵션을 사용하면 포함됨
     */
    SERIALIZE_NULLS,
    
    /**
     * 특별한 부동소수점 값(NaN, Infinity, -Infinity) 허용
     * 기본적으로는 예외가 발생하는데, 이 옵션을 사용하면 문자열로 직렬화됨
     */
    SERIALIZE_SPECIAL_FLOATING_POINT_VALUES,
    
    /**
     * 복잡한 맵 키 직렬화 활성화
     * 기본적으로 맵의 키는 문자열이어야 하는데, 이 옵션을 사용하면 복잡한 객체도 키로 사용 가능
     */
    ENABLE_COMPLEX_MAP_KEY_SERIALIZATION,
    
    /**
     * 필드명을 따옴표 없이 출력 (JSON5 스타일)
     * 주의: 표준 JSON이 아니므로 호환성 문제가 있을 수 있음
     */
    LENIENT,
    
    /**
     * 들여쓰기 설정 (PRETTY_PRINT와 함께 사용)
     * 값으로 들여쓰기 문자열을 설정 (예: "  " 또는 "\t")
     */
    INDENT
}