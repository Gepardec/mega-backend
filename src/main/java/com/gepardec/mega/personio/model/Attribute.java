package com.gepardec.mega.personio.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class Attribute<T> {

    private String label;
    private T value;
    private String type;
    @JsonProperty("universal_id")
    private String universalId;
}
