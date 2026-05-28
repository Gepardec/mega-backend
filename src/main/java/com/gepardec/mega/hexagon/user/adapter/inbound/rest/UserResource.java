package com.gepardec.mega.hexagon.user.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.UserApi;
import com.gepardec.mega.hexagon.generated.model.ActiveUserDto;
import com.gepardec.mega.hexagon.generated.model.InternalRateUploadErrorDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDateEntryDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDatesRequestDto;
import com.gepardec.mega.hexagon.generated.model.UserDto;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.GetActiveUsersUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.InternalRateUpdateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateInternalRatesUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesUseCase;
import com.gepardec.mega.hexagon.user.domain.error.UnknownUsersException;
import com.gepardec.mega.hexagon.user.domain.model.HourlyRate;
import com.gepardec.mega.hexagon.user.domain.model.User;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequestScoped
@Authenticated
public class UserResource implements UserApi {

    private static final int EXPECTED_CSV_COLUMNS = 3;
    private static final String CSV_DELIMITER_REGEX = "[,;]";
    private static final String CSV_COMMENT_PREFIX = "#";
    private static final String TEMPLATE_HEADER = "#ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD";
    private static final String TEMPLATE_FILENAME = "hourly_rates_template.csv";
    private static final String ERROR_CODE_EMPTY_FILE = "EMPTY_FILE";
    private static final String ERROR_CODE_BAD_FORMAT = "BAD_FORMAT";
    private static final String ERROR_CODE_UNKNOWN_USERS = "UNKNOWN_USERS";

    private final GetActiveUsersUseCase getActiveUsersUseCase;
    private final UpdateInternalRatesUseCase updateInternalRatesUseCase;
    private final UpdateReleaseDatesUseCase updateReleaseDatesUseCase;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final UserRestMapper userRestMapper;

    @Inject
    public UserResource(
            GetActiveUsersUseCase getActiveUsersUseCase,
            UpdateInternalRatesUseCase updateInternalRatesUseCase,
            UpdateReleaseDatesUseCase updateReleaseDatesUseCase,
            AuthenticatedActorContext authenticatedActorContext,
            UserRestMapper userRestMapper
    ) {
        this.getActiveUsersUseCase = getActiveUsersUseCase;
        this.updateInternalRatesUseCase = updateInternalRatesUseCase;
        this.updateReleaseDatesUseCase = updateReleaseDatesUseCase;
        this.authenticatedActorContext = authenticatedActorContext;
        this.userRestMapper = userRestMapper;
    }

    @Override
    @MegaRolesAllowed(Role.OFFICE_MANAGEMENT)
    public Response getActiveUsers() {
        List<ActiveUserDto> activeUsers = getActiveUsersUseCase.getActiveUsers().stream()
                .map(userRestMapper::toDto)
                .toList();
        return Response.ok(activeUsers).build();
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response getCurrentUser() {
        UserDto currentUser = userRestMapper.toUserDto(authenticatedActorContext.user());
        return Response.ok(currentUser).build();
    }

    @Override
    @MegaRolesAllowed(Role.OFFICE_MANAGEMENT)
    public Response getInternalRatesCsvTemplate() {
        List<User> activeUsers = getActiveUsersUseCase.getActiveUsers().stream()
                .filter(user -> user.zepUsername() != null)
                .sorted(Comparator.comparing(user -> user.zepUsername().value()))
                .toList();

        String today = LocalDate.now().toString();
        String csv = activeUsers.stream()
                .map(employee -> String.format("%s,,%s", employee.zepUsername().value(), today))
                .collect(Collectors.joining("\n", TEMPLATE_HEADER + "\n", "\n"));

        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"" + TEMPLATE_FILENAME + "\"")
                .build();
    }

    @Override
    @MegaRolesAllowed(Role.OFFICE_MANAGEMENT)
    public Response updateReleaseDates(UpdateReleaseDatesRequestDto updateReleaseDatesRequestDto) {
        List<UpdateReleaseDateCommand> commands = requestEntries(updateReleaseDatesRequestDto).stream()
                .filter(Objects::nonNull)
                .map(userRestMapper::toCommand)
                .filter(Objects::nonNull)
                .toList();

        return Response.ok(
                userRestMapper.toDto(updateReleaseDatesUseCase.update(commands))
        ).build();
    }

    @Override
    @MegaRolesAllowed(Role.OFFICE_MANAGEMENT)
    public Response uploadInternalRates(InputStream fileInputStream) {
        if (fileInputStream == null) {
            return badRequest(ERROR_CODE_EMPTY_FILE, List.of());
        }

        final List<CsvLine> dataLines;
        try {
            dataLines = readAndFilterCsvLines(fileInputStream);
        } catch (IOException exception) {
            return Response.serverError().build();
        }

        if (dataLines.isEmpty()) {
            return badRequest(ERROR_CODE_EMPTY_FILE, List.of());
        }

        CsvValidationResult validationResult = validateAndMap(dataLines);
        if (!validationResult.errorLines().isEmpty()) {
            return badRequest(ERROR_CODE_BAD_FORMAT, validationResult.errorLines());
        }

        try {
            updateInternalRatesUseCase.update(
                    validationResult.parsedRows().stream()
                            .map(ParsedRateRow::command)
                            .toList()
            );
        } catch (UnknownUsersException exception) {
            Set<ZepUsername> unknownUsers = exception.unknownUsers();
            List<Integer> lines = validationResult.parsedRows().stream()
                    .filter(parsedLine -> unknownUsers.contains(parsedLine.command().zepUsername()))
                    .map(parsedLine -> parsedLine.csvLine().originalLineNumber())
                    .toList();
            return badRequest(ERROR_CODE_UNKNOWN_USERS, lines);
        }

        return Response.ok().build();
    }

    private List<UpdateReleaseDateEntryDto> requestEntries(UpdateReleaseDatesRequestDto request) {
        if (request == null) {
            return List.of();
        }
        return request.getEntries();
    }

    private List<CsvLine> readAndFilterCsvLines(InputStream fileInputStream) throws IOException {
        AtomicInteger lineNumber = new AtomicInteger(0);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(line -> new CsvLine(lineNumber.incrementAndGet(), line))
                    .filter(csvLine -> csvLine.content() != null)
                    .filter(csvLine -> !csvLine.content().isBlank())
                    .filter(csvLine -> !csvLine.content().startsWith(CSV_COMMENT_PREFIX))
                    .toList();
        }
    }

    private CsvValidationResult validateAndMap(List<CsvLine> dataLines) {
        List<Integer> errors = new ArrayList<>();
        List<ParsedRateRow> parsedRows = new ArrayList<>();

        for (CsvLine csvLine : dataLines) {
            parseRateRow(csvLine)
                    .ifPresentOrElse(
                            parsedRows::add,
                            () -> errors.add(csvLine.originalLineNumber())
                    );
        }

        return new CsvValidationResult(errors, parsedRows);
    }

    private Optional<ParsedRateRow> parseRateRow(CsvLine csvLine) {
        String[] parts = csvLine.content().split(CSV_DELIMITER_REGEX, -1);
        if (parts.length != EXPECTED_CSV_COLUMNS) {
            return Optional.empty();
        }

        String rawUsername = parts[0].trim();
        String rawHourlyRate = parts[1].trim();
        String rawEffectiveFrom = parts[2].trim();
        if (rawUsername.isBlank() || rawHourlyRate.isBlank() || rawEffectiveFrom.isBlank()) {
            return Optional.empty();
        }

        try {
            double hourlyRateValue = Double.parseDouble(rawHourlyRate);
            LocalDate effectiveFrom = LocalDate.parse(rawEffectiveFrom);
            InternalRateUpdateCommand command = new InternalRateUpdateCommand(
                    ZepUsername.of(rawUsername),
                    HourlyRate.of(hourlyRateValue),
                    effectiveFrom
            );
            return Optional.of(new ParsedRateRow(csvLine, command));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private Response badRequest(String errorCode, List<Integer> lines) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new InternalRateUploadErrorDto()
                        .errorCode(errorCode)
                        .lines(lines))
                .build();
    }

    private record CsvLine(int originalLineNumber, String content) {
    }

    private record ParsedRateRow(CsvLine csvLine, InternalRateUpdateCommand command) {
    }

    private record CsvValidationResult(List<Integer> errorLines, List<ParsedRateRow> parsedRows) {
    }
}
