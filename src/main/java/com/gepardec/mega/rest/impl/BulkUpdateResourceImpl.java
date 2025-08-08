package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.producer.ResourceBundleProducer;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.rest.api.BulkUpdateResource;
import com.gepardec.mega.rest.model.HourlyRateFileDto;
import com.gepardec.mega.zep.ZepService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import de.provantis.zep.InternersatzListeType;
import de.provantis.zep.InternersatzType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class BulkUpdateResourceImpl implements BulkUpdateResource {

    @Inject
    ZepService zepService;

    @Inject
    UserRepository userRepo;

    @Context
    HttpHeaders headers;

    @Inject
    ResourceBundleProducer bundleProducer;

    @Override
    public Response uploadHourlyRate(HourlyRateFileDto input) {

        List<String> lines = new BufferedReader(new InputStreamReader(input.file))
                .lines().
                dropWhile(line -> line.startsWith("#"))
                .toList();

        //TODO: write more explaining Response messages

        Locale locale = getLocaleFromHeader();
        if(!verifyUpload(lines)) { //checks the file format
            String msg = ResourceBundleProducer.getMessage("error.bad-file", locale);
            Map<String, Object> error = Map.of("message", msg);

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }

        if(!verifyEmployeeExistance(lines)) { //checks whether employees in the file exist locally
            String msg = ResourceBundleProducer.getMessage("error.employee-does-not-exist", locale);

            Map<String, Object> error = Map.of("message", msg);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }

        for(String l : lines){
            zepService.updateEmployeeHourlyRate(
                    l.split(",")[0],
                    createNewInternalRate(l));
        }
        return Response.ok().build();
    }

    private InternersatzListeType createNewInternalRate(String l){
        List<InternersatzType> internalRates = new ArrayList<>();
        InternersatzType newInternalRate = new InternersatzType();
        InternersatzListeType internalRatesList = new InternersatzListeType();

        newInternalRate.setUserId(l.split(",")[0]);
        newInternalRate.setSatz(Double.parseDouble(l.split(",")[1]));
        newInternalRate.setStartdatum(l.split(",")[2]);
        newInternalRate.setSatztype(1); //we only use hourlyRates --> https://developer.zep.de/en/soap-documentation for more info
        internalRates.add(newInternalRate);

        internalRatesList.setInternersatz(internalRates);

        return internalRatesList;
    }

    private boolean verifyUpload(List<String> lines) {
        for(String l : lines){//there must be at least 5 chars in a line otherwise it isn't formatted correctly
            if(l.length() <= 5) return false;
        }
        return true;
    }

    private boolean verifyEmployeeExistance(List<String> lines) {

        for(String l : lines){
            String zId = l.split(",")[0];
            Optional<User> user = userRepo.findByZepId(zId);

            if(user.isEmpty()) return false;
        }
        return true;
    }

    private Locale getLocaleFromHeader(){
        List<Locale> langs = headers.getAcceptableLanguages();
        return langs.isEmpty() ? Locale.getDefault() : langs.get(0);
    }
}
