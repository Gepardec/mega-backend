package com.gepardec.mega.personio.employees.absenceBalance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbsenceBalanceResponse {
    private PersonioAbsenceBalanceDto absenceBalanceDto;

    public PersonioAbsenceBalanceDto getAbsenceBalanceDto() {
        return absenceBalanceDto;
    }

    public void setAbsenceBalanceDto(PersonioAbsenceBalanceDto absenceBalanceDto) {
        this.absenceBalanceDto = absenceBalanceDto;
    }
}
