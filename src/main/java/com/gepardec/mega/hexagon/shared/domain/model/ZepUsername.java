package com.gepardec.mega.hexagon.shared.domain.model;

import java.util.Objects;

public record ZepUsername(String value) {

    public ZepUsername {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ZepUsername of(String value) {
        return new ZepUsername(value);
    }
}
