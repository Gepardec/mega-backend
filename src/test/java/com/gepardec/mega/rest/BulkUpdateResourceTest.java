package com.gepardec.mega.rest;

import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;

import com.gepardec.mega.zep.impl.ZepServiceImpl;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BulkUpdateResourceTest {

    @InjectMock
    ZepServiceImpl zepService;

    @InjectMock
    UserRepository userRepo;

    @BeforeEach
    void setUp() {
        when(userRepo.findByZepId(any(String.class)))
                .thenReturn(Optional.of(User.of("test@mail.com")));

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

        given()
                .multiPart("file", createCorrectTestFile(), "text/plain")
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(200);

        verify(zepService, Mockito
                .times(2))
                .updateEmployeeHourlyRate(
                        any(String.class),
                        any(Double.class),
                        any(String.class)
                );


        verify(zepService).updateEmployeeHourlyRate(eq("005-wbruckmueller"), eq(72.00D), eq("2025-01-01"));
        verify(zepService).updateEmployeeHourlyRate(eq("102-funger"), eq(20.00D), eq("2025-12-31"));

    }

    private File createCorrectTestFile() throws IOException {
        final File tempFile = Files.createTempFile("test", ".csv").toFile();
        tempFile.deleteOnExit();
        String fileData = """
                #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
                005-wbruckmueller,72.00,2025-01-01
                102-funger,20.00,2025-12-31""";
        Files.write(tempFile.toPath(), fileData.getBytes());

        return tempFile;
    }

    @Test
    void uploadIncorrectInternalRate() throws IOException {

        given()
                .multiPart("file", createIncorrectTestFile(), "text/plain")
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(400);
    }

    private File createIncorrectTestFile() throws IOException {
        final File tempFile = Files.createTempFile("test", ".csv").toFile();
        tempFile.deleteOnExit();
        String fileData = """
                #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
                ,,
                102-funger,20.00,2025-12-31""";
        Files.write(tempFile.toPath(), fileData.getBytes());

        return tempFile;
    }
}