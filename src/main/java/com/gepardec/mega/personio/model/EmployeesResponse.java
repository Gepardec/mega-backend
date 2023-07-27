package com.gepardec.mega.personio.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class EmployeesResponse {

    private boolean success;
    private List<EmployeesResponseData> data;
}
