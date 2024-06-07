package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepAttendanceDirectionOfTravel(
        String id,
        String name
) {

    @JsonCreator
    public ZepAttendanceDirectionOfTravel(Builder builder) {
        this(builder.id,
             builder.name
        );
    }

    public static Builder builder() {return Builder.aZepAttendanceDirectionOfTravel();}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty("id")
        String id;

        @JsonProperty("name")
        String name;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ZepAttendanceDirectionOfTravel build() {return new ZepAttendanceDirectionOfTravel(this);}

        public static Builder aZepAttendanceDirectionOfTravel() {return new Builder();}
    }
}
