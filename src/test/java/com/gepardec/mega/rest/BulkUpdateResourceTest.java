package com.gepardec.mega.rest;

import com.gepardec.mega.application.producer.ResourceBundleProducer;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.rest.api.BulkUpdateResource;
import com.gepardec.mega.rest.model.BulkUpdateDto;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.ZepServiceImpl;
import de.provantis.zep.InternersatzListeType;
import de.provantis.zep.InternersatzType;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BulkUpdateResourceTest {

    @Inject
    BulkUpdateResource bulkUpdateResource;

    @InjectMock
    ZepServiceImpl zepService;

    @InjectMock
    UserRepository userRepo;

    @InjectMock
    ResourceBundleProducer resourceBundleProducer;

    @BeforeEach
    void setUp() {
        when(userRepo.findByZepId(any(String.class)))
                .thenReturn(Optional.of(User.of("test@mail.com")));
    }

    @Test
    void uploadInternalRate() throws IOException {
        File correctFile = createCorrectTestFile(); //with this input
        Map<String, InternersatzListeType> expectedData = getExpectedInternersatzListeToCorrectTestFile(); //this should be the outcome

        doNothing()
                .when(zepService)
                .updateEmployeeHourlyRate(
                        any(String.class),
                        any(InternersatzListeType.class)
                );

        given()
                .multiPart("file", correctFile, "text/plain")
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(200);

        verify(zepService, Mockito
                .times(2))
                .updateEmployeeHourlyRate(
                        any(String.class),
                        any(InternersatzListeType.class)
                );


        verify(zepService).updateEmployeeHourlyRate(eq("005-wbruckmueller"), any(InternersatzListeType.class));

        expectedData.forEach((zepId, internnalRate) -> {
            verify(zepService)
                    .updateEmployeeHourlyRate(eq(zepId), any(InternersatzListeType.class));
        });
    }

    private File createCorrectTestFile() throws IOException {
        final File tempFile = Files.createTempFile("test", ".csv").toFile();
        tempFile.deleteOnExit();
        String fileData =
                "#ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD\n" +
                        "005-wbruckmueller,72.00,2025-01-01\n" +
                        "102-funger,20.00,2025-12-31";
        Files.write(tempFile.toPath(), fileData.getBytes());

        return tempFile;
    }

    private Map<String, InternersatzListeType> getExpectedInternersatzListeToCorrectTestFile() {
        final Map<String, InternersatzListeType> expectedData = new HashMap<>();

        expectedData.put(
                "005-wbruckmueller",
                createInternersatzListe("005-wbruckmueller", 72.00D, "2025-01-01"));
        expectedData.put(
                "102-funger",
                createInternersatzListe("102-funger", 20.00D, "2025-12-31")
        );

        return expectedData;
    }

    private InternersatzListeType createInternersatzListe(String uId, double newRate, String date) {
        final InternersatzType internerSatz = new InternersatzType();
        internerSatz.setUserId(uId);
        internerSatz.setSatz(newRate);
        internerSatz.setStartdatum(date);
        internerSatz.setSatztype(1);

        final List<InternersatzType> internersatzList = new ArrayList<>();
        internersatzList.add(internerSatz);

        final InternersatzListeType internersatzListe = new InternersatzListeType();
        internersatzListe.setInternersatz(internersatzList);

        return internersatzListe;
    }

    private BulkUpdateDto createHourlyRateFileDto(File file) throws FileNotFoundException {
        final InputStream inputStream = new FileInputStream(file);
        final BulkUpdateDto ret = new BulkUpdateDto();
        ret.setFile(inputStream);
        return ret;
    }
}