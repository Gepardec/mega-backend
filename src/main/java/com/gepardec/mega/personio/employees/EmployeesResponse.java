package com.gepardec.mega.personio.employees;

import java.util.List;

public class EmployeesResponse {

    private boolean success;

    private List<EmployeesResponseData> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<EmployeesResponseData> getData() {
        return data;
    }

    public void setData(List<EmployeesResponseData> data) {
        this.data = data;
    }
}
