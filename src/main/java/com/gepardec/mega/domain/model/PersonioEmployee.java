package com.gepardec.mega.domain.model;

public class PersonioEmployee {

    private final String email;

    private final Double vacationDayBalance;

    private final String guildLead;

    private final String internalProjectLead;


    public String getEmail() {
        return email;
    }

    public Double getVacationDayBalance() {
        return vacationDayBalance;
    }


    public String getGuildLead() {
        return guildLead;
    }

    public String getInternalProjectLead() {
        return internalProjectLead;
    }

    public PersonioEmployee(Builder builder) {
        this.email = builder.email;
        this.vacationDayBalance = builder.vacationDayBalance;
        this.guildLead = builder.guildLead;
        this.internalProjectLead = builder.internalProjectLead;
    }

    public static Builder builder() {
        return Builder.aPersonioEmployee();
    }

    public static class Builder {
        private String email;
        private Double vacationDayBalance;
        private String guildLead;
        private String internalProjectLead;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder vacationDayBalance(Double vacationDayBalance) {
            this.vacationDayBalance = vacationDayBalance;
            return this;
        }


        public Builder guildLead(String guildLead) {
            this.guildLead = guildLead;
            return this;
        }

        public Builder internalProjectLead(String internalProjectLead) {
            this.internalProjectLead = internalProjectLead;
            return this;
        }

        public PersonioEmployee build() {
            return new PersonioEmployee(this);
        }

        public static Builder aPersonioEmployee() {
            return new Builder();
        }
    }
}
