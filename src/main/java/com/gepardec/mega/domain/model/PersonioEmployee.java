package com.gepardec.mega.domain.model;

public class PersonioEmployee {

    private final String email;

    private final Double vacationDayBalance;

    private final String guildLead;

    private final String internalProjectLead;

    private final boolean hasCreditCard;

    private final Integer personioId;


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

    public boolean getHasCreditCard() {
        return hasCreditCard;
    }

    public Integer getPersonioId() {
        return personioId;
    }

    private PersonioEmployee(Builder builder) {
        this.email = builder.email;
        this.vacationDayBalance = builder.vacationDayBalance;
        this.guildLead = builder.guildLead;
        this.internalProjectLead = builder.internalProjectLead;
        this.hasCreditCard = builder.hasCreditCard;
        this.personioId = builder.personioId;
    }

    public static Builder builder() {
        return Builder.aPersonioEmployee();
    }

    public static final class Builder {
        private String email;
        private Double vacationDayBalance;
        private String guildLead;
        private String internalProjectLead;
        private boolean hasCreditCard;
        private Integer personioId;

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

        public Builder hasCreditCard(boolean hasCreditCard) {
            this.hasCreditCard = hasCreditCard;
            return this;
        }

        public Builder personioId(Integer personioId) {
            this.personioId = personioId;
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
