package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrematureEmployeeCheckDto {

    @JsonProperty
    private UserDto user;

    @JsonProperty
    private LocalDate forMonth;

    @JsonProperty
    private String reason;

    public PrematureEmployeeCheckDto() {
    }

    public PrematureEmployeeCheckDto(UserDto user, LocalDate forMonth, String reason) {
        this.user = user;
        this.forMonth = forMonth;
        this.reason = reason;
    }

    public static PrematureEmployeeCheckDtoBuilder builder() {
        return PrematureEmployeeCheckDtoBuilder.aPrematureEmployeeCheckDto();
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public LocalDate getForMonth() {
        return forMonth;
    }

    public void setForMonth(LocalDate forMonth) {
        this.forMonth = forMonth;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public static final class PrematureEmployeeCheckDtoBuilder {
        private UserDto user;
        private LocalDate forMonth;
        private String reason;

        private PrematureEmployeeCheckDtoBuilder() {
        }

        public static PrematureEmployeeCheckDtoBuilder aPrematureEmployeeCheckDto() {
            return new PrematureEmployeeCheckDtoBuilder();
        }

        public PrematureEmployeeCheckDtoBuilder user(UserDto user) {
            this.user = user;
            return this;
        }

        public PrematureEmployeeCheckDtoBuilder forMonth(LocalDate forMonth) {
            this.forMonth = forMonth;
            return this;
        }

        public PrematureEmployeeCheckDtoBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public PrematureEmployeeCheckDto build() {
            PrematureEmployeeCheckDto prematureEmployeeCheckDto = new PrematureEmployeeCheckDto();
            prematureEmployeeCheckDto.setUser(user);
            prematureEmployeeCheckDto.setForMonth(forMonth);
            prematureEmployeeCheckDto.setReason(reason);
            return prematureEmployeeCheckDto;
        }
    }
}
