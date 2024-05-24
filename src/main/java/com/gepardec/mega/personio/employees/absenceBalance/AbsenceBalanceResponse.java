package com.gepardec.mega.personio.employees.absenceBalance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbsenceBalanceResponse {

    private Integer id;
    private String name;
    private List<String> category;
    private Integer balance;
    private Integer availableBalance;

    @JsonCreator
    public AbsenceBalanceResponse(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("category") List<String> category,
            @JsonProperty("balance") Integer balance,
            @JsonProperty("available_balance") Integer availableBalance) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.balance = balance;
        this.availableBalance = availableBalance;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Integer availableBalance) {
        this.availableBalance = availableBalance;
    }
}

