package com.gepardec.mega.personio.employees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.personio.commons.model.Attribute;

import java.util.List;

public class PersonioEmployee {

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

    public Attribute<Integer> getId() {
        return id;
    }

    public void setId(Attribute<Integer> id) {
        this.id = id;
    }

    public Attribute<String> getFirstName() {
        return firstName;
    }

    public void setFirstName(Attribute<String> firstName) {
        this.firstName = firstName;
    }

    public Attribute<String> getLastName() {
        return lastName;
    }

    public void setLastName(Attribute<String> lastName) {
        this.lastName = lastName;
    }

    public Attribute<String> getEmail() {
        return email;
    }

    public void setEmail(Attribute<String> email) {
        this.email = email;
    }

    public Attribute<List<TimeOffType>> getAbsenceEntitlement() {
        return absenceEntitlement;
    }

    public void setAbsenceEntitlement(Attribute<List<TimeOffType>> absenceEntitlement) {
        this.absenceEntitlement = absenceEntitlement;
    }

    public Attribute<Double> getVacationDayBalance() {
        return vacationDayBalance;
    }

    public void setVacationDayBalance(Attribute<Double> vacationDayBalance) {
        this.vacationDayBalance = vacationDayBalance;
    }

    public Attribute<String> getPersonalnummer() {
        return personalnummer;
    }

    public void setPersonalnummer(Attribute<String> personalnummer) {
        this.personalnummer = personalnummer;
    }
}
