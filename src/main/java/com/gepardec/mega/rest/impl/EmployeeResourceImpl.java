package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.application.producer.ResourceBundleProducer;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.rest.api.EmployeeResource;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.BulkUpdateDto;
import com.gepardec.mega.rest.model.BulkUpdateErrorResponseDto;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.UserService;
import com.gepardec.mega.zep.ZepService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequestScoped
@Authenticated
@MegaRolesAllowed({Role.PROJECT_LEAD, Role.OFFICE_MANAGEMENT})
public class EmployeeResourceImpl implements EmployeeResource {

    private static final int EXPECTED_CSV_COLUMNS = 3;
    private static final String CSV_DELIMITER_REGEX = "[,;]";
    private static final int USER_ID_COLUMN = 0;
    private static final int RATE_COLUMN = 1;
    private static final int DATE_COLUMN = 2;
    private static final String CSV_COMMENT_PREFIX = "#";
    private static final String CSV_HEADER = "#ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD";

    /**
     * Represents a line from the CSV file with its original line number.
     * This is necessary to report accurate line numbers in error messages,
     * even after filtering out blank lines and comments.
     */
    private record CsvLine(int originalLineNumber, String content) {
    }

    @Inject
    EmployeeMapper mapper;

    @Inject
    EmployeeService employeeService;

    @Inject
    ZepService zepService;

    @Inject
    UserService userService;

    @Inject
    ResourceBundleProducer resourceBundleProducer;

    @Override
    public Response list() {
        final List<Employee> allActiveEmployees = employeeService.getAllActiveEmployees();
        return Response.ok(mapper.mapListToDto(allActiveEmployees)).build();
    }

    @Override
    public Response update(final List<EmployeeDto> employeesDto) {
        return Response.ok(employeeService.updateEmployeesReleaseDate(mapper.mapListToDomain(employeesDto))).build();
    }

    @Override
    public Response uploadInternalRate(BulkUpdateDto input, HttpHeaders headers) {
        if (input == null || input.getFile() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final Locale locale = getLocaleFromHeader(headers);
        final List<CsvLine> dataLines;

        try {
            dataLines = readAndFilterCsvLines(input);
        } catch (IOException e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponseDto("error.reading-file", List.of(), locale))
                    .build();
        }

        if (dataLines.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponseDto("error.empty-file", List.of(), locale))
                    .build();
        }

        List<Integer> formatErrors = validateCsvFormat(dataLines);
        if (!formatErrors.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponseDto("error.bad-file", formatErrors, locale))
                    .build();
        }

        List<Integer> nonExistentEmployeeErrors = findNonExistentEmployees(dataLines);
        if (!nonExistentEmployeeErrors.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponseDto("error.employee-does-not-exist", nonExistentEmployeeErrors, locale))
                    .build();
        }

        updateEmployeeRates(dataLines);
        return Response.ok().build();
    }

    private List<CsvLine> readAndFilterCsvLines(BulkUpdateDto input) throws IOException {
        final AtomicInteger lineNumber = new AtomicInteger(0);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input.getFile(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(line -> new CsvLine(lineNumber.incrementAndGet(), line))
                    .filter(csvLine -> csvLine.content() != null)
                    .filter(csvLine -> !csvLine.content().isBlank())
                    .filter(csvLine -> !csvLine.content().startsWith(CSV_COMMENT_PREFIX))
                    .toList();
        }
    }

    private List<Integer> validateCsvFormat(List<CsvLine> lines) {
        final List<Integer> errorLines = new ArrayList<>();

        lines.forEach(csvLine -> {
            String[] parts = csvLine.content().split(CSV_DELIMITER_REGEX, -1);

            if (parts.length != EXPECTED_CSV_COLUMNS) {
                errorLines.add(csvLine.originalLineNumber());
                return;
            }

            if (parts[USER_ID_COLUMN].isBlank() || parts[RATE_COLUMN].isBlank() || parts[DATE_COLUMN].isBlank()) {
                errorLines.add(csvLine.originalLineNumber());
                return;
            }

            try {
                Double.parseDouble(parts[RATE_COLUMN]);
            } catch (NumberFormatException e) {
                errorLines.add(csvLine.originalLineNumber());
                return;
            }

            try {
                LocalDate.parse(parts[DATE_COLUMN]);
            } catch (DateTimeParseException e) {
                errorLines.add(csvLine.originalLineNumber());
            }
        });

        return errorLines;
    }

    private List<Integer> findNonExistentEmployees(List<CsvLine> lines) {
        Set<String> employeeIds = lines.stream()
                .map(csvLine -> extractUserId(csvLine.content()))
                .collect(Collectors.toSet());

        Set<String> existingIds = userService.findByZepIds(employeeIds).stream()
                .map(User::getUserId)
                .collect(Collectors.toSet());

        return lines.stream()
                .filter(csvLine -> !existingIds.contains(extractUserId(csvLine.content())))
                .map(CsvLine::originalLineNumber)
                .toList();
    }

    private void updateEmployeeRates(List<CsvLine> dataLines) {
        for (CsvLine csvLine : dataLines) {
            zepService.updateEmployeeHourlyRate(
                    extractUserId(csvLine.content()),
                    extractNewRate(csvLine.content()),
                    extractFromDate(csvLine.content())
            );
        }
    }

    @Override
    public Response downloadCsvTemplate() {
        final List<Employee> allActiveEmployees = employeeService.getAllActiveEmployees();
        final String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        final String csvContent = allActiveEmployees.stream()
                .sorted(Comparator.comparing(Employee::getUserId))
                .map(employee -> String.format("%s,,%s", employee.getUserId(), currentDate))
                .collect(Collectors.joining("\n",
                        CSV_HEADER + "\n",
                        "\n"));

        return Response.ok(csvContent)
                .header("Content-Disposition", "attachment; filename=\"hourly_rates_template.csv\"")
                .build();
    }

    private BulkUpdateErrorResponseDto createErrorResponseDto(String bundleKey, List<Integer> errorLocation, Locale locale) {
        return new BulkUpdateErrorResponseDto(
                resourceBundleProducer.getResourceBundle(locale).getString(bundleKey),
                errorLocation
        );
    }

    private String extractUserId(String line) {
        String[] parts = line.split(CSV_DELIMITER_REGEX, -1);
        if (parts.length < EXPECTED_CSV_COLUMNS) {
            throw new IllegalArgumentException("Invalid CSV line format");
        }
        return parts[USER_ID_COLUMN];
    }

    private Double extractNewRate(String line) {
        String[] parts = line.split(CSV_DELIMITER_REGEX, -1);
        if (parts.length < EXPECTED_CSV_COLUMNS) {
            throw new IllegalArgumentException("Invalid CSV line format");
        }
        try {
            return Double.parseDouble(parts[RATE_COLUMN]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid rate format in line: " + line, e);
        }
    }

    private String extractFromDate(String line) {
        String[] parts = line.split(CSV_DELIMITER_REGEX, -1);
        if (parts.length < EXPECTED_CSV_COLUMNS) {
            throw new IllegalArgumentException("Invalid CSV line format");
        }
        return parts[DATE_COLUMN];
    }

    private Locale getLocaleFromHeader(HttpHeaders headers) {
        final List<Locale> langs = headers.getAcceptableLanguages();
        return langs.isEmpty() ? Locale.ENGLISH : langs.getFirst();
    }
}
