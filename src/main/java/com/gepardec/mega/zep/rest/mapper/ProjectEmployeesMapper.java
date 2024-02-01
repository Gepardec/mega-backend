package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployee;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectEmployeesMapper implements Mapper<MultivaluedMap<String, String>, List<ZepProjectEmployee>>{

    public static final String USER = "user";
    public static final String LEAD = "lead";

    @Override
    public MultivaluedMap<String, String> map(List<ZepProjectEmployee> zepProjectEmployees) {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        zepProjectEmployees.forEach(zepProjectEmployee -> {
            map.add(USER, zepProjectEmployee.getUsername());

            if (zepProjectEmployee.getType() != null) {
                if (zepProjectEmployee.getType().getId() != 0) {
                    map.add(LEAD, String.valueOf(zepProjectEmployee.getUsername()));
                }
            }
        });
        return map;
    }
}
