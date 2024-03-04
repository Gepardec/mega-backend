package com.gepardec.mega.domain.model;

public class PersonioEmployee {
    private final Integer id;

    private final String firstName;

    private final String lastName;

    private final String email;

    private final Double vacationDayBalance;

    private final String personalNumber;

    private final String guildLead;

    private final String internalProjectLead;

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Double getVacationDayBalance() {
        return vacationDayBalance;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public String getGuildLead() {
        return guildLead;
    }

    public String getInternalProjectLead() {
        return internalProjectLead;
    }

    public PersonioEmployee(Builder builder) {
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.vacationDayBalance = builder.vacationDayBalance;
        this.personalNumber = builder.personalNumber;
        this.guildLead = builder.guildLead;
        this.internalProjectLead = builder.internalProjectLead;
    }

    public static Builder builder() {
        return Builder.aPersonioEmployee();
    }

    public static class Builder {
        private Integer id;
        private String firstName;
        private String lastName;
        private String email;
        private Double vacationDayBalance;
        private String personalNumber;
        private String guildLead;
        private String internalProjectLead;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder vacationDayBalance(Double vacationDayBalance) {
            this.vacationDayBalance = vacationDayBalance;
            return this;
        }

        public Builder personalNumber(String personalNumber) {
            this.personalNumber = personalNumber;
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
