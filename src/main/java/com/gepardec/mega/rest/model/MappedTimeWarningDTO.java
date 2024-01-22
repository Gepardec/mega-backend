package com.gepardec.mega.rest.model;

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

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty
    private LocalDate date;

    @JsonProperty
    public String description;

    @JsonIgnore
    public LocalDate getDate() {
        return date;
    }

    public MappedTimeWarningDTO() {
    }

    public MappedTimeWarningDTO(LocalDate date, String description) {
        this.date = date;
        this.description = description;
    }

    public static MappedTimeWarningDTOBuilder builder() {
        return MappedTimeWarningDTOBuilder.aMappedTimeWarningDTO();
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

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static final class MappedTimeWarningDTOBuilder {
        private LocalDate date;
        private String description;

        private MappedTimeWarningDTOBuilder() {
        }

        public static MappedTimeWarningDTOBuilder aMappedTimeWarningDTO() {
            return new MappedTimeWarningDTOBuilder();
        }

        public MappedTimeWarningDTOBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public MappedTimeWarningDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public MappedTimeWarningDTO build() {
            MappedTimeWarningDTO mappedTimeWarningDTO = new MappedTimeWarningDTO();
            mappedTimeWarningDTO.setDate(date);
            mappedTimeWarningDTO.setDescription(description);
            return mappedTimeWarningDTO;
        }
    }
}
