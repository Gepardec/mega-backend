package com.gepardec.mega.personio.employees.absenceBalance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PersonioAbsenceBalanceDto(
        Integer id,
        String name,
        String category,
        Integer balance,
        Integer availableBalance
) {

    @JsonCreator
    private PersonioAbsenceBalanceDto(Builder builder){
        this(builder.id,
            builder.name,
            builder.category,
            builder.balance,
            builder.availableBalance
        );
    }

    public static Builder builder() {return Builder.aPersonioAbsenceBalanceDto(); }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty("id")
        private Integer id;
        @JsonProperty("name")
        private String name;

        private String category;

        private Integer balance;

        @JsonProperty("available_balance")
        private Integer availableBalance;

        public Builder id(Integer id){
            this.id = id;
            return this;
        }

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder category(String category){
            this.category = category;
            return this;
        }

        public Builder balance(Integer balance){
            this.balance = balance;
            return this;
        }

        public Builder availableBalance(Integer availableBalance){
            this.availableBalance = availableBalance;
            return this;
        }

        public PersonioAbsenceBalanceDto build() {return new PersonioAbsenceBalanceDto(this);}

        public static Builder aPersonioAbsenceBalanceDto(){return new Builder();}


    }
}
