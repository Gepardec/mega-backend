package com.gepardec.mega.rest.impl;

import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.rest.api.BulkUpdateResource;
import com.gepardec.mega.rest.model.HourlyRateFileDto;
import com.gepardec.mega.zep.ZepService;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BulkUpdateResourceImpl implements BulkUpdateResource {

    @Inject
    ZepService zepService;

    @Inject
    UserRepository userRepo;

    @Override
    public Response uploadHourlyRate(HourlyRateFileDto input) {

        Scanner sc = new Scanner(input.file);
        List<String> lines = new ArrayList<>();

        while(sc.hasNextLine()){
            lines.add(sc.nextLine());
        }

        if(!verifyUploadHourlyRate(lines)) return Response.status(Response.Status.BAD_REQUEST).build();

        if(!verifyEmployeeExistance(lines)) return  Response.status(Response.Status.BAD_REQUEST).build();



        //TODO: get a list of the requested empl and check whether they exist and everything is correct. if not return.

        return Response.ok().build();
    }

    private boolean verifyUploadHourlyRate(List<String> lines) {

        for(String l : lines){
            String[] split = l.split(",");

            for (String s : split) {
                if(s.isEmpty()) return false;
            }
        }
        return true;
    }

    private boolean verifyEmployeeExistance(List<String> lines) {
        try{
            for(String l : lines){
                String zId = l.split(",")[0];
                Optional<User> user = userRepo.findByZepId(zId);
                if(user.isEmpty()) return false;
            }
        }catch(ForbiddenException fe){
            return false;
        }
        return true;
    }
}
