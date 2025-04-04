package com.hancomins.jsn4j;

import java.io.InputStream;
import java.io.Reader;

public interface ContainerParser {
    ContainerValue parse(String value);
    ContainerValue parse(Reader reader);
    ContainerValue parse(InputStream input);
}
