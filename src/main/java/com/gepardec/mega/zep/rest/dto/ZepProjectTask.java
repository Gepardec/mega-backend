package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepProjectTask(
        Integer id,
        String name,
        Integer projectId) {

    @JsonCreator
    public ZepProjectTask(Builder builder) {
        this(builder.id, builder.name, builder.projectId);
    }

    public static Builder builder() {
        return Builder.aZepProjectTask();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {

        @JsonProperty
        private Integer id;

        @JsonProperty
        private String name;

        @JsonProperty("project_id")
        private Integer projectId;

        public static Builder aZepProjectTask() {
            return new Builder();
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder projectId(Integer projectId) {
            this.projectId = projectId;
            return this;
        }

        public ZepProjectTask build() {
            return new ZepProjectTask(this);
        }
    }
}
