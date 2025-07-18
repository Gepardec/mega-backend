package com.gepardec.mega.rest.impl;

import com.gepardec.mega.rest.api.BulkUpdateResource;
import com.gepardec.mega.rest.model.HourlyRateFileDto;
import jakarta.ws.rs.core.Response;

import java.util.Scanner;

public class BulkUpdateResourceImpl implements BulkUpdateResource {
    @Override
    public Response uploadHourlyRate(HourlyRateFileDto input) {
        Scanner sc = new Scanner(input.file).useDelimiter("\\A");
        while(sc.hasNext()){
            String line = sc.nextLine();
            String[] split = line.split(";");
            for (String s : split) {
                System.out.println(s);
            }
        }

        return Response.ok().build();
    }
}
