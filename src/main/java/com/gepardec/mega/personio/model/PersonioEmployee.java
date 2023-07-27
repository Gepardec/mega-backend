package com.gepardec.mega.personio.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
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
}
