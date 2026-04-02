package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.GenerateMonthEndTasksRequest;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndTaskGenerationResponse;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GenerateMonthEndTasksUseCase;
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
@TestSecurity(user = "cron", roles = "mega-cron:sync")
class MonthEndOpsResourceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);

    @InjectMock
    GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;

    @Test
    void generateMonthEndTasks_shouldReturnGenerationResultForCronRole() {
        MonthEndTaskGenerationResult result = new MonthEndTaskGenerationResult(MONTH, 4, 2);
        when(generateMonthEndTasksUseCase.generate(MONTH)).thenReturn(result);

        MonthEndTaskGenerationResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new GenerateMonthEndTasksRequest().month(MONTH.toString()))
                .post("/monthend/ops/generation")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndTaskGenerationResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getCreated()).isEqualTo(4);
        assertThat(response.getSkipped()).isEqualTo(2);
        verify(generateMonthEndTasksUseCase).generate(MONTH);
    }

    @Test
    @TestSecurity(user = "cron")
    void generateMonthEndTasks_shouldRejectMissingCronRole() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new GenerateMonthEndTasksRequest().month(MONTH.toString()))
                .post("/monthend/ops/generation")
                .then()
                .statusCode(403);

        verifyNoInteractions(generateMonthEndTasksUseCase);
    }
}
