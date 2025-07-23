package com.hancomins.jsn4j.issuetest;

import com.hancomins.jsn4j.Jsn4j;
import com.hancomins.jsn4j.ObjectContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("기본 기능 이슈 테스트")
public class DefaultFunctionIssueTest {

    @DisplayName("null 값 처리 테스트")
    @Test
    public void testNullValueHandling() {
        String json = "{ \"name\": null, \"age\": 30, \"roles\": [\"admin\", null] }";
        ObjectContainer container = Jsn4j.parse(json).asObject();
        // null 값이 있는 필드에 접근
        String name = container.getString("name");
        String nameDefault = container.getString("name", "defaultName"); // 기본값 설정
        double valueDouble = container.getDouble("value"); // 존재하지 않는 필드
        int value = container.getInt("value"); // 존재하지 않는 필드
        int valueDefault = container.getInt("value", 0); // 기본값 설정
        Integer age = container.getInt("age");
        String firstRole = container.getArray("roles").getString(0);
        String secondRole = container.getArray("roles").getString(1);
        ObjectContainer roles = container.getObject("roles");
        // 검증
        assertNull(name);
        assertEquals("defaultName", nameDefault);
        assertEquals(Double.NaN, valueDouble);
        assertEquals(Integer.MIN_VALUE, value);
        assertEquals(0, valueDefault);
        assertEquals(30, age);
        assertEquals("admin", firstRole);
        assertNull(roles);
        assertNull(secondRole);

    }



}
