package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;

@ApplicationScoped
public class ProjectEmployeesMapper implements Mapper<MultivaluedMap<String, String>, List<ZepProjectEmployee>> {

    public static final String USER = "user";
    public static final String LEAD = "lead";

    @Override
    public MultivaluedMap<String, String> map(List<ZepProjectEmployee> zepProjectEmployees) {

        try {
            MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
            zepProjectEmployees.forEach(zepProjectEmployee -> {
                map.add(USER, zepProjectEmployee.username());

                if (isLeadEmployee(zepProjectEmployee)) {
                    map.add(LEAD, String.valueOf(zepProjectEmployee.username()));
                }
            });
            return map;
        } catch (Exception e) {
            throw new ZepServiceException("While trying to map ZepProjectEmployee to MultivaluedMap of \"user\" and \"lead\" collections, an error occurred", e);
        }
    }

    private boolean isLeadEmployee(ZepProjectEmployee employee) {
        return employee.type() != null && employee.type().id() != 0;
    }
}
