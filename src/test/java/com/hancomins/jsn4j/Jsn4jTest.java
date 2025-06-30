package com.hancomins.jsn4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Jsn4jTest {
    @Test
    public void testJsn4j() {
        // Create a new instance of Jsn4j

        ObjectContainer container = Jsn4j.newObject();
        assertNotNull(container);

        container.put("foo", "bar");
        container.newAndPutObject("obj").put("foo_in", "bar_in\r\n_");

        container.getWriter().enable("pretty_print");
        System.out.println(container);

        String prettyJson = container.toString();
        System.out.println(prettyJson);

    }

}