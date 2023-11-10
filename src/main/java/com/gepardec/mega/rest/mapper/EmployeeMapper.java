package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.rest.model.EmployeeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmployeeMapper extends DtoMapper<Employee, EmployeeDto> {

    @Override
    public EmployeeDto mapToDto(Employee object) {
        return EmployeeDto.builder()
                .userId(object.getUserId())
                .email(object.getEmail())
                .title(object.getTitle())
                .firstname(object.getFirstname())
                .lastname(object.getLastname())
                .salutation(object.getSalutation())
                .releaseDate(object.getReleaseDate())
                .workDescription(object.getWorkDescription())
                .language(object.getLanguage())
                .regularWorkingHours(object.getRegularWorkingHours())
                .active(object.isActive())
                .exitDate(object.getExitDate())
                .build();
    }

    @Override
    public Employee mapToDomain(EmployeeDto object) {
        return Employee.builder()
                .userId(object.getUserId())
                .email(object.getEmail())
                .title(object.getTitle())
                .firstname(object.getFirstname())
                .lastname(object.getLastname())
                .salutation(object.getSalutation())
                .releaseDate(object.getReleaseDate())
                .workDescription(object.getWorkDescription())
                .language(object.getLanguage())
                .regularWorkingHours(object.getRegularWorkingHours())
                .active(object.isActive())
                .exitDate(object.getExitDate())
                .build();
    }
}
