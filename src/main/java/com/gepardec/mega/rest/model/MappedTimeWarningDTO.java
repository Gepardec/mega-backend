package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MappedTimeWarningDTO {

    @JsonProperty
    public final String description;
    @JsonProperty
    private final LocalDate date;

    @JsonIgnore
    public LocalDate getDate() {
        return date;
    }


    @JsonCreator
    private MappedTimeWarningDTO(Builder builder) {
        this.date = builder.date;
        this.description = builder.description;
    }

    public static Builder builder() {
        return Builder.aMappedTimeWarningDTO();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MappedTimeWarningDTO that = (MappedTimeWarningDTO) o;
        return Objects.equals(getDate(), that.getDate()) && Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getDescription());
    }

    public String getDescription() {
        return description;
    }

    public static final class Builder {
        @JsonProperty
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate date;
        @JsonProperty
        private String description;

        private Builder() {
        }

        public static Builder aMappedTimeWarningDTO() {
            return new Builder();
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public MappedTimeWarningDTO build() {
            return new MappedTimeWarningDTO(this);
        }
    }
}
