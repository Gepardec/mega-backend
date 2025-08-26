package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.producer.ResourceBundleProducer;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.rest.api.BulkUpdateResource;
import com.gepardec.mega.rest.model.BulkUpdateResponseDto;
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

    @Override
    public Response uploadInternalRate(HourlyRateFileDto input, HttpHeaders headers) {

        final List<String> lines = new BufferedReader(new InputStreamReader(input.getFile()))
                .lines()
                .toList();

        final Locale locale = getLocaleFromHeader(headers);

        List<Integer> verifyUpload = determineLinesWhereLengthInvalid(lines); //checks if the file is formatted correctly

        if (!verifyUpload.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            createErrorResponseDto(
                                    "error.bad-file",
                                    verifyUpload,
                                    locale
                            ))
                    .build();
        }

        List<Integer> verifyEmployees = determineLinesWhereEmployeeNotExists(lines);

        if (!verifyEmployees.isEmpty()) { //checks whether employees in the file exist locally
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            createErrorResponseDto(
                                    "error.employee-does-not-exist",
                                    verifyEmployees,
                                    locale
                            ))
                    .build();
        }

        for (String l : lines) {
            if (l.startsWith("#")) {
                continue;
            }
            zepService.updateEmployeeHourlyRate(
                    l.split(",")[0],
                    createNewInternalRate(l));
        }
        return Response.ok().build();
    }

    private InternersatzListeType createNewInternalRate(String line) {
        List<InternersatzType> internalRates = new ArrayList<>();
        InternersatzType newInternalRate = new InternersatzType();
        InternersatzListeType internalRatesList = new InternersatzListeType();

        newInternalRate.setUserId(extractUserId(line));
        newInternalRate.setSatz(Double.parseDouble(line.split(",")[1]));
        newInternalRate.setStartdatum(line.split(",")[2]);
        newInternalRate.setSatztype(1); //we only use hourlyRates --> https://developer.zep.de/en/soap-documentation for more info
        internalRates.add(newInternalRate);
        internalRatesList.setInternersatz(internalRates);

        return internalRatesList;
    }

    /**
     * Creates a BulkUpdateResponse to use for a JSON error response.
     * the message language is decided by getLocaleFromHeader().
     *
     * @param bundleKey for of the wanted error-message
     * @return the map that represents the errormessage the bundleKey specifies.
     */
    private BulkUpdateResponseDto createErrorResponseDto(String bundleKey, List<Integer> errorLocation, Locale locale) {
        return new BulkUpdateResponseDto(
                resourceBundleProducer
                        .getResourceBundle(locale)
                        .getString(bundleKey),
                errorLocation);
    }

    /**
     * Verifies if the lines in the File have enough chars to be formatted correctly
     *
     * @param lines of the file
     * @return the line numbers where the format error is
     */
    private List<Integer> determineLinesWhereLengthInvalid(List<String> lines) {
        final List<Integer> retList = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("#")) {
                continue;
            }
            if (lines.get(i).length() <= 5) {
                retList.add(i + 1);
            }
        }
        return retList;
    }

    /**
     * Verifies the existence of an employee according to their ZEP id in the LOCAL mega-db
     *
     * @param lines of the file
     * @return the line numbers where the format error is
     */
    private List<Integer> determineLinesWhereEmployeeNotExists(List<String> lines) {
        final List<Integer> retList = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("#")) {
                continue;
            }

            String zId = lines.get(i).split(",")[0];
            Optional<User> user = userRepo.findByZepId(zId);

            if (user.isEmpty()) {
                retList.add(i + 1);
            }
        }
        return retList;
    }

    /**
     * Extracts the first csv from one line, which is the UserId
     *
     * @param line one line of the request
     * @return userId
     */
    private String extractUserId(String line) {
        return line.split(",")[0];
    }

    private Locale getLocaleFromHeader(HttpHeaders headers) {
        final List<Locale> langs = headers.getAcceptableLanguages();
        return langs.isEmpty() ? Locale.getDefault() : langs.get(0);
    }
}
