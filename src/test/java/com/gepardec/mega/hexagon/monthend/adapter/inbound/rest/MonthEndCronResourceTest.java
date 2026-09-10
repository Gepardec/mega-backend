package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.MonthEndTaskGenerationDto;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class MonthEndCronResourceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);

    @InjectMock
    GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;

    @Test
    @TestSecurity(user = "cron", roles = "mega-cron:sync")
    void generateMonthEndTasks_shouldReturnGenerationResultForCronRole() {
        MonthEndTaskGenerationResult result = new MonthEndTaskGenerationResult(MONTH, 4, 2);
        when(generateMonthEndTasksUseCase.generate(MONTH)).thenReturn(result);

        MonthEndTaskGenerationDto response = given()
                .accept(ContentType.JSON)
                .post("/monthend/{month}/generate", MONTH.toString())
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndTaskGenerationDto.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getCreated()).isEqualTo(4);
        assertThat(response.getSkipped()).isEqualTo(2);
        verify(generateMonthEndTasksUseCase).generate(MONTH);
    }

    @Test
    @TestSecurity(user = "cron")
    void generateMonthEndTasks_shouldRejectMissingCronRole() {
        given()
                .accept(ContentType.JSON)
                .post("/monthend/{month}/generate", MONTH.toString())
                .then()
                .statusCode(403);

        verifyNoInteractions(generateMonthEndTasksUseCase);
    }
}
