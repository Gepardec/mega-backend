package com.gepardec.mega.personio.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attribute<T> {

    private String label;

    private T value;

    private String type;

    @JsonProperty("universal_id")
    private String universalId;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUniversalId() {
        return universalId;
    }

    public void setUniversalId(String universalId) {
        this.universalId = universalId;
    }

    public static <T> Attribute<T> ofValue(T value) {
        Attribute<T> attr = new Attribute<>();
        attr.setValue(value);
        return attr;
    }
}
