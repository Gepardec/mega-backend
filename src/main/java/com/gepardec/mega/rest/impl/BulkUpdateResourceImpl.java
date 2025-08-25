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

    @Inject
    ResourceBundleProducer resourceBundleProducer;

    @Context
    HttpHeaders headers;

    @Override
    public Response uploadInternalRate(HourlyRateFileDto input) {

        List<String> lines = new BufferedReader(new InputStreamReader(input.file))
                .lines()
                .toList();

        if(!verifyUpload(lines).isEmpty()) { //checks if the file is formatted correctly
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            createErrorMapFromBundleKey(
                                    "error.bad-file",
                                    verifyUpload(lines)))
                    .build();
        }

        if(!verifyEmployeeExistance(lines).isEmpty()) { //checks whether employees in the file exist locally
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            createErrorMapFromBundleKey(
                                    "error.employee-does-not-exist",
                                    verifyEmployeeExistance(lines)))
                    .build();
        }

        for(String l : lines){
            if(l.startsWith("#")) continue;
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

    /**
     * Creates a Map to use for a JSON error response.
     * the message language is decided by getLocaleFromHeader().
     * @param bundleKey for of the wanted error-message
     * @return the map that represents the errormessage the bundleKey specifies.
     */
    private Map<String, Object> createErrorMapFromBundleKey(String bundleKey, List<Integer> errorLocation){
        return Map.of(
                "message", resourceBundleProducer.getResourceBundle(getLocaleFromHeader()).getString(bundleKey),
                "errorLocation", errorLocation
        );
    }

    /**
     * Verifies if the lines in the File have enough chars to be formatted correctly
     * @param lines of the file
     * @return the line numbers where the format error is
     */
    private List<Integer> verifyUpload(List<String> lines) {
        List<Integer> retList = new  ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            if(lines.get(i).startsWith("#")) continue;
            if(lines.get(i).length() <= 5) retList.add(i+1);
        }
        return retList;
    }

    /**
     * Verifies the existence of an employee according to their ZEP id in the LOCAL mega-db
     * @param lines of the file
     * @return the line numbers where the format error is
     */
    private List<Integer> verifyEmployeeExistance(List<String> lines) {
        List<Integer> retList = new  ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if(lines.get(i).startsWith("#")) continue;

            String zId = lines.get(i).split(",")[0];
            Optional<User> user = userRepo.findByZepId(zId);

            if(user.isEmpty()) retList.add(i+1);
        }
        return retList;
    }

    private Locale getLocaleFromHeader(){
        List<Locale> langs = headers.getAcceptableLanguages();
        return langs.isEmpty() ? Locale.getDefault() : langs.get(0);
    }
}
