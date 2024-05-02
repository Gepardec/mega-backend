package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = PrematureEmployeeCheckDto.Builder.class)
public class PrematureEmployeeCheckDto {

    private final Long id;

    private final UserDto user;

    private final LocalDate forMonth;

    private final String reason;

    private final PrematureEmployeeCheckState state;

    private PrematureEmployeeCheckDto(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.forMonth = builder.forMonth;
        this.reason = builder.reason;
        this.state = builder.state;
    }

    public static Builder builder() {
        return Builder.aPrematureEmployeeCheckDto();
    }

    public UserDto getUser() {
        return user;
    }

    public LocalDate getForMonth() {
        return forMonth;
    }

    public String getReason() {
        return reason;
    }

    public Long getId() {
        return id;
    }

    public PrematureEmployeeCheckState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrematureEmployeeCheckDto that = (PrematureEmployeeCheckDto) o;
        return Objects.equals(getUser(), that.getUser()) && Objects.equals(getForMonth(), that.getForMonth()) && Objects.equals(getReason(), that.getReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getForMonth(), getReason());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Long id;
        private UserDto user;
        private LocalDate forMonth;
        private String reason;
        private PrematureEmployeeCheckState state;

        private Builder() {
        }

        public static Builder aPrematureEmployeeCheckDto() {
            return new Builder();
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(UserDto user) {
            this.user = user;
            return this;
        }

        public Builder forMonth(LocalDate forMonth) {
            this.forMonth = forMonth;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder state(PrematureEmployeeCheckState state) {
            this.state = state;
            return this;
        }

        public PrematureEmployeeCheckDto build() {
            return new PrematureEmployeeCheckDto(this);
        }
    }
}
