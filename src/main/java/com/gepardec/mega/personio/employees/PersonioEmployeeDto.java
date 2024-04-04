package com.gepardec.mega.personio.employees;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.personio.commons.model.Attribute;

import java.util.List;

public record PersonioEmployeeDto(
        Attribute<Integer> id,
        Attribute<String> firstName,
        Attribute<String> lastName,
        Attribute<String> email,
        Attribute<List<TimeOffType>> absenceEntitlement,
        Attribute<Double> vacationDayBalance,
        Attribute<String> personalnummer,
        Attribute<String> guildLead,
        Attribute<String> internalProjectLead
) {

    @JsonCreator
    private PersonioEmployeeDto(Builder builder) {
        this(
                builder.id,
                builder.firstName,
                builder.lastName,
                builder.email,
                builder.absenceEntitlement,
                builder.vacationDayBalance,
                builder.personalnummer,
                builder.guildLead,
                builder.internalProjectLead
        );
    }

    public static Builder builder() {
        return Builder.aPersonioEmployeeDto();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private Attribute<Integer> id;

        @JsonProperty("first_name")
        private Attribute<String> firstName;

        @JsonProperty("last_name")
        private Attribute<String> lastName;

        private Attribute<String> email;

        @JsonProperty("absence_entitlement")
        private Attribute<List<TimeOffType>> absenceEntitlement;

        @JsonProperty("vacation_day_balance")
        private Attribute<Double> vacationDayBalance;

        @JsonProperty("dynamic_393528")
        private Attribute<String> personalnummer;

        @JsonProperty("dynamic_10177615")
        private Attribute<String> guildLead;

        @JsonProperty("dynamic_10177735")
        private Attribute<String> internalProjectLead;

        public Builder id(Attribute<Integer> id) {
            this.id = id;
            return this;
        }

        public Builder firstName(Attribute<String> firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(Attribute<String> lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(Attribute<String> email) {
            this.email = email;
            return this;
        }

        public Builder absenceEntitlement(Attribute<List<TimeOffType>> absenceEntitlement) {
            this.absenceEntitlement = absenceEntitlement;
            return this;
        }

        public Builder vacationDayBalance(Attribute<Double> vacationDayBalance) {
            this.vacationDayBalance = vacationDayBalance;
            return this;
        }

        public Builder personalnummer(Attribute<String> personalnummer) {
            this.personalnummer = personalnummer;
            return this;
        }

        public Builder guildLead(Attribute<String> guildLead) {
            this.guildLead = guildLead;
            return this;
        }

        public Builder internalProjectLead(Attribute<String> internalProjectLead) {
            this.internalProjectLead = internalProjectLead;
            return this;
        }

        public PersonioEmployeeDto build() {
            return new PersonioEmployeeDto(this);
        }

        public static Builder aPersonioEmployeeDto() {
            return new Builder();
        }
    }
}
