package com.gepardec.mega.rest.impl;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.rest.api.BulkUpdateResource;
import com.gepardec.mega.rest.model.HourlyRateFileDto;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.ZepServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Scanner;

public class BulkUpdateResourceImpl implements BulkUpdateResource {

    @Inject
    ZepService zepService;

    @Override
    public Response uploadHourlyRate(HourlyRateFileDto input) {

        Scanner sc = new Scanner(input.file).useDelimiter("\\A");

        if(!verifyUploadHourlyRate(sc)) return Response.status(Response.Status.BAD_REQUEST).build(); //check this before the API call is made

        List<Employee> employees = zepService.getEmployees();

        if(!verifyEmployeeExistance(sc, employees)) return  Response.status(Response.Status.BAD_REQUEST).build();



        //TODO: get a list of the requested empl and check whether they exist and everything is correct. if not return.

        return Response.ok().build();
    }

    private boolean verifyUploadHourlyRate(Scanner sc) {

        while(sc.hasNext()){
            String line = sc.nextLine();
            String[] split = line.split(",");

            for (String s : split) {
                if(s.isEmpty()) return false;
            }
        }
        return true;
    }

    private boolean verifyEmployeeExistance(Scanner sc, List<Employee> employees) {

        while(sc.hasNextLine()){
            String line = sc.nextLine();
            String[] split = line.split(",");

            for(Employee employee : employees){
                if(!split[0].equals(employee.getUserId())) return false;
            }
        }
        return true;
    }
}
