package com.gepardec.mega.hexagon.user.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ActiveUserDto;
import com.gepardec.mega.hexagon.generated.model.InternalRateUploadErrorDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDateEntryDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDatesRequestDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDatesResponseDto;
import com.gepardec.mega.hexagon.generated.model.UserDto;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.GetActiveUsersUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.InternalRateUpdateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateInternalRatesUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesResult;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesUseCase;
import com.gepardec.mega.hexagon.user.domain.error.UnknownUsersException;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.HourlyRate;
import com.gepardec.mega.hexagon.user.domain.model.PersonioId;
import com.gepardec.mega.hexagon.user.domain.model.User;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.MultiPartSpecification;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class UserResourceTest {

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;

    @InjectMock
    GetActiveUsersUseCase getActiveUsersUseCase;

    @InjectMock
    UpdateReleaseDatesUseCase updateReleaseDatesUseCase;

    @InjectMock
    UpdateInternalRatesUseCase updateInternalRatesUseCase;

    @Test
    void getCurrentUser_shouldReturnCurrentUserForEmployee() {
        allowRoles(Role.EMPLOYEE);
        User user = currentUser("worker", LocalDate.of(2026, 4, 30), 4242);
        when(authenticatedActorContext.user()).thenReturn(user);

        UserDto response = given()
                .accept(ContentType.JSON)
                .get("/users/me")
                .then()
                .statusCode(200)
                .extract()
                .as(UserDto.class);

        assertThat(response.getId()).isEqualTo(user.id().value());
        assertThat(response.getEmail()).isEqualTo("worker@example.com");
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getZepUsername()).isEqualTo("worker");
        assertThat(response.getReleaseDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(response.getRoles()).containsExactlyInAnyOrder("EMPLOYEE");
        assertThat(response.getPersonioId()).isEqualTo(4242);
        assertThat(response.getIsExternal()).isFalse();
    }

    @Test
    void getCurrentUser_shouldReturnExternalFlagForExternalEmployee() {
        allowRoles(Role.EMPLOYEE);
        User user = currentUser("eworker", LocalDate.of(2026, 4, 30), 4242);
        when(authenticatedActorContext.user()).thenReturn(user);

        UserDto response = given()
                .accept(ContentType.JSON)
                .get("/users/me")
                .then()
                .statusCode(200)
                .extract()
                .as(UserDto.class);

        assertThat(response.getZepUsername()).isEqualTo("eworker");
        assertThat(response.getIsExternal()).isTrue();
    }

    @Test
    void getCurrentUser_shouldReturnNullReleaseDateAndPersonioIdWhenUnavailable() {
        allowRoles(Role.EMPLOYEE);
        User user = currentUser("worker", null, null);
        when(authenticatedActorContext.user()).thenReturn(user);

        UserDto response = given()
                .accept(ContentType.JSON)
                .get("/users/me")
                .then()
                .statusCode(200)
                .extract()
                .as(UserDto.class);

        assertThat(response.getReleaseDate()).isNull();
        assertThat(response.getPersonioId()).isNull();
    }

    @Test
    void getCurrentUser_shouldReturnForbiddenForNonEmployeeRole() {
        allowRoles(Role.OFFICE_MANAGEMENT);

        given()
                .accept(ContentType.JSON)
                .get("/users/me")
                .then()
                .statusCode(403);

        verifyNoInteractions(updateReleaseDatesUseCase, getActiveUsersUseCase);
    }

    @Test
    void getActiveUsers_shouldReturnReleaseDateInPayloadForOfficeManagement() {
        allowRoles(Role.OFFICE_MANAGEMENT);
        User user = user("office", LocalDate.of(2026, 4, 30));
        when(getActiveUsersUseCase.getActiveUsers()).thenReturn(List.of(user));

        ActiveUserDto[] response = given()
                .accept(ContentType.JSON)
                .get("/users/active")
                .then()
                .statusCode(200)
                .extract()
                .as(ActiveUserDto[].class);

        assertThat(response).hasSize(1);
        assertThat(response[0].getUserId()).isEqualTo(user.id().value());
        assertThat(response[0].getFullName()).isEqualTo("Test User");
        assertThat(response[0].getEmail()).isEqualTo("office@example.com");
        assertThat(response[0].getReleaseDate()).isEqualTo(LocalDate.of(2026, 4, 30));
    }

    @Test
    void getActiveUsers_shouldReturnForbiddenForNonOfficeManagementRole() {
        allowRoles(Role.EMPLOYEE);

        given()
                .accept(ContentType.JSON)
                .get("/users/active")
                .then()
                .statusCode(403);

        verifyNoInteractions(getActiveUsersUseCase);
    }

    @Test
    void updateReleaseDates_shouldReturnFailedUserIdsAndMapRequestCommands() {
        allowRoles(Role.OFFICE_MANAGEMENT);
        UserId firstUserId = UserId.generate();
        UserId secondUserId = UserId.generate();
        LocalDate releaseDate = LocalDate.of(2026, 4, 30);

        UpdateReleaseDatesRequestDto request = new UpdateReleaseDatesRequestDto()
                .entries(List.of(
                        new UpdateReleaseDateEntryDto().userId(firstUserId.value()).releaseDate(releaseDate),
                        new UpdateReleaseDateEntryDto().userId(secondUserId.value()).releaseDate(releaseDate)
                ));

        when(updateReleaseDatesUseCase.update(any()))
                .thenReturn(new UpdateReleaseDatesResult(List.of(secondUserId)));

        UpdateReleaseDatesResponseDto response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .put("/users/release-dates")
                .then()
                .statusCode(200)
                .extract()
                .as(UpdateReleaseDatesResponseDto.class);

        assertThat(response.getFailedUserIds()).containsExactly(secondUserId.value());
        verify(updateReleaseDatesUseCase).update(argThat(commands ->
                commands.equals(List.of(
                        new UpdateReleaseDateCommand(firstUserId, releaseDate),
                        new UpdateReleaseDateCommand(secondUserId, releaseDate)
                ))
        ));
    }

    @Test
    void updateReleaseDates_shouldReturnForbiddenForNonOfficeManagementRole() {
        allowRoles(Role.EMPLOYEE);
        UpdateReleaseDatesRequestDto request = new UpdateReleaseDatesRequestDto().entries(List.of());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .put("/users/release-dates")
                .then()
                .statusCode(403);

        verifyNoInteractions(updateReleaseDatesUseCase);
    }

    @Test
    void uploadInternalRates_shouldReturn200AndMapCsvToCommands() {
        allowRoles(Role.OFFICE_MANAGEMENT);
        String csv = """
                #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
                beta,72.5,2026-05-01
                alpha;80;2026-05-02
                """;

        given()
                .multiPart(buildCsvPart(csv))
                .post("/users/internal-rates")
                .then()
                .statusCode(200);

        verify(updateInternalRatesUseCase).update(argThat(commands ->
                commands.equals(List.of(
                        new InternalRateUpdateCommand(
                                ZepUsername.of("beta"),
                                HourlyRate.of(72.5),
                                LocalDate.of(2026, 5, 1)
                        ),
                        new InternalRateUpdateCommand(
                                ZepUsername.of("alpha"),
                                HourlyRate.of(80),
                                LocalDate.of(2026, 5, 2)
                        )
                ))
        ));
    }

    @Test
    void uploadInternalRates_shouldReturnEmptyFileError() {
        allowRoles(Role.OFFICE_MANAGEMENT);
        String csv = """
                #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
                
                #comment
                """;

        InternalRateUploadErrorDto response = given()
                .multiPart(buildCsvPart(csv))
                .post("/users/internal-rates")
                .then()
                .statusCode(400)
                .extract()
                .as(InternalRateUploadErrorDto.class);

        assertThat(response.getErrorCode()).isEqualTo("EMPTY_FILE");
        assertThat(response.getLines()).isEmpty();
        verifyNoInteractions(updateInternalRatesUseCase);
    }

    @Test
    void uploadInternalRates_shouldReturnBadFormatErrorWithOriginalLineNumbers() {
        allowRoles(Role.OFFICE_MANAGEMENT);
        String csv = """
                #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
                alpha,72.5,2026-05-01
                
                beta,not-a-number,2026-05-02
                gamma,12
                """;

        InternalRateUploadErrorDto response = given()
                .multiPart(buildCsvPart(csv))
                .post("/users/internal-rates")
                .then()
                .statusCode(400)
                .extract()
                .as(InternalRateUploadErrorDto.class);

        assertThat(response.getErrorCode()).isEqualTo("BAD_FORMAT");
        assertThat(response.getLines()).containsExactly(4, 5);
        verifyNoInteractions(updateInternalRatesUseCase);
    }

    @Test
    void uploadInternalRates_shouldReturnUnknownUsersErrorWithCorrelatedLines() {
        allowRoles(Role.OFFICE_MANAGEMENT);
        String csv = """
                #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
                alpha,72.5,2026-05-01
                missing,70,2026-05-02
                missing,71,2026-05-03
                """;

        doThrow(new UnknownUsersException(Set.of(ZepUsername.of("missing"))))
                .when(updateInternalRatesUseCase).update(anyList());

        InternalRateUploadErrorDto response = given()
                .multiPart(buildCsvPart(csv))
                .post("/users/internal-rates")
                .then()
                .statusCode(400)
                .extract()
                .as(InternalRateUploadErrorDto.class);

        assertThat(response.getErrorCode()).isEqualTo("UNKNOWN_USERS");
        assertThat(response.getLines()).containsExactly(3, 4);
    }

    @Test
    void uploadInternalRates_shouldReturnForbiddenForNonOfficeManagementRole() {
        allowRoles(Role.EMPLOYEE);

        given()
                .multiPart(buildCsvPart("alpha,72.5,2026-05-01\n"))
                .post("/users/internal-rates")
                .then()
                .statusCode(403);

        verifyNoInteractions(updateInternalRatesUseCase);
    }

    @Test
    void getInternalRatesCsvTemplate_shouldReturnTemplateWithSortedRowsAndContentDisposition() {
        allowRoles(Role.OFFICE_MANAGEMENT);

        User zeta = user("zeta", LocalDate.of(2026, 4, 30));
        User alpha = user("alpha", LocalDate.of(2026, 4, 30));
        when(getActiveUsersUseCase.getActiveUsers()).thenReturn(List.of(zeta, alpha));

        String csv = given()
                .accept("text/csv")
                .get("/users/internal-rates/csv-template")
                .then()
                .statusCode(200)
                .header("Content-Disposition", "attachment; filename=\"hourly_rates_template.csv\"")
                .extract()
                .asString();

        String[] lines = csv.split("\n");
        String today = LocalDate.now().toString();

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("#ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD");
        assertThat(lines[1]).isEqualTo("alpha,," + today);
        assertThat(lines[2]).isEqualTo("zeta,," + today);
    }

    @Test
    void getInternalRatesCsvTemplate_shouldReturnForbiddenForNonOfficeManagementRole() {
        allowRoles(Role.EMPLOYEE);

        given()
                .accept("text/csv")
                .get("/users/internal-rates/csv-template")
                .then()
                .statusCode(403);

        verifyNoInteractions(getActiveUsersUseCase);
    }

    private void allowRoles(Role... roles) {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(roles));
    }

    private MultiPartSpecification buildCsvPart(String csvContent) {
        return new MultiPartSpecBuilder(csvContent)
                .controlName("file")
                .fileName("hourly-rates.csv")
                .mimeType("text/csv")
                .build();
    }

    private User user(String username, LocalDate releaseDate) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2025, 1, 1), null)),
                Set.of(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT),
                releaseDate
        );
    }

    private User currentUser(String username, LocalDate releaseDate, Integer personioId) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                personioId == null ? null : PersonioId.of(personioId),
                EmploymentPeriods.empty(),
                Set.of(Role.EMPLOYEE),
                releaseDate
        );
    }
}
