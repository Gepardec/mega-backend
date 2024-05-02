package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = MappedTimeWarningDTO.Builder.class)
public class MappedTimeWarningDTO {

    private final String description;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate date;

    private MappedTimeWarningDTO(Builder builder) {
        this.date = builder.date;
        this.description = builder.description;
    }

    public static Builder builder() {
        return Builder.aMappedTimeWarningDTO();
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
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

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        private LocalDate date;
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
