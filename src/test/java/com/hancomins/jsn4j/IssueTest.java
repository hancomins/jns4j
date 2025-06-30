package com.hancomins.jsn4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IssueTest {

    @Test
    public void testIssueAsBooleanError() {
        String build = "{\"a\": false}";
        ObjectContainer container = Jsn4j.parse(build).asObject();

        System.out.println(container);

        boolean newValue = container.getBoolean("a", true);
        assertFalse(newValue);

        boolean noneValue = container.getBoolean("b", true);
        assertTrue(noneValue);

    }




}
