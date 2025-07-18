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
            for (int i = 0; i < split.length; i++) {
                System.out.println(split[i]);
            }
        }

        return Response.ok().build();
    }
}
