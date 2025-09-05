package com.gepardec.mega.rest;

import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.zep.impl.ZepServiceImpl;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.specification.MultiPartSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BulkUpdateResourceTest {

    private static final String VALID_HOURLY_RATES_CSV = """
            #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
            005-wbruckmueller,72.00,2025-01-01
            102-funger,20.00,2025-12-31
            """;

    private static final String INVALID_HOURLY_RATES_CSV = """
            #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
            ,,
            102-funger,20.00,2025-12-31
            """;

    private static final String NON_EXISTING_USER_HOURLY_RATES_CSV = """
            #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
            xxxxx,15.00,2025-01-01
            """;

    @InjectMock
    ZepServiceImpl zepService;

    @InjectMock
    UserRepository userRepo;

    @BeforeEach
    void setUp() {
        doNothing()
                .when(zepService)
                .updateEmployeeHourlyRate(
                        any(String.class),
                        any(Double.class),
                        any(String.class)
                );
    }

    @Test
    void uploadCorrectInternalRate() throws IOException {
        when(userRepo.findByZepId(any(String.class)))
                .thenReturn(Optional.of(User.of("test@mail.com")));

        given()
                .multiPart(buildMultipartSpec(VALID_HOURLY_RATES_CSV))
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(200);

        verify(zepService).updateEmployeeHourlyRate(eq("005-wbruckmueller"), eq(72.00D), eq("2025-01-01"));
        verify(zepService).updateEmployeeHourlyRate(eq("102-funger"), eq(20.00D), eq("2025-12-31"));
    }

    @Test
    void uploadIncorrectInternalRate() throws IOException {
        given()
                .multiPart(buildMultipartSpec(INVALID_HOURLY_RATES_CSV))
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", anyOf(
                        equalTo("Error: The uploaded file is not formatted correctly"),
                        equalTo("Fehler: Die hochgeladene Datei ist nicht richtig formatiert")))
                .body("location", equalTo(List.of(2)));
    }

    @Test
    void uploadInternalrateWithNonExistingEmployee() throws IOException {
        when(userRepo.findByZepId(any(String.class)))
                .thenReturn(Optional.empty());

        given()
                .multiPart(buildMultipartSpec(NON_EXISTING_USER_HOURLY_RATES_CSV))
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", anyOf(
                        equalTo("Error: The specified employee does not exist"),
                        equalTo("Fehler: Der angegebene Mitarbeiter existiert nicht")))
                .body("location", equalTo(List.of(2)));
    }

    private MultiPartSpecification buildMultipartSpec(String fileContent) {
        return new MultiPartSpecBuilder(fileContent)
                .fileName("hourlyRates.csv")
                .controlName("file")
                .mimeType("text/plain")
                .build();
    }
}
