package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WarningRuleSettingsPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeAbsenceZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeBookingZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeExpectedWorkingDaysPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeMealEntryZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class WarningRuleSettingsIT {
    private static final YearMonth MONTH = YearMonth.of(2023, 10);
    private static final UserId USER_ID = UserId.of(Instancio.create(UUID.class));
    private static final ZepUsername USERNAME = ZepUsername.of("ada");

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;
    @InjectMock
    WorkTimeUserSnapshotPort userPort;
    @InjectMock
    WorkTimeBookingZepPort bookingPort;
    @InjectMock
    WorkTimeAbsenceZepPort absencePort;
    @InjectMock
    WorkTimeExpectedWorkingDaysPort expectedDaysPort;
    @InjectMock
    WorkTimeProjectSnapshotPort projectSnapshotPort;
    @InjectMock
    WorkTimeMealEntryZepPort mealEntryPort;
    @Inject
    WarningRuleSettingsPort settingsPort;

    @BeforeEach
    void setUp() {
        QuarkusTransaction.requiringNew().run(() -> Arrays.stream(WarningRule.values())
                .forEach(rule -> settingsPort.save(new WarningRuleSetting(rule, true, null))));

        LocalDate date = MONTH.atDay(6);
        when(authenticatedActorContext.userId()).thenReturn(USER_ID);
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(
                new UserRef(USER_ID, FullName.of("Ada", "Lovelace"), USERNAME)));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of(
                journey(date.atTime(8, 0), JourneyDirection.TO), journey(date.atTime(16, 0), JourneyDirection.BACK)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, MONTH)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, MONTH)).thenReturn(Set.of(date));
        when(projectSnapshotPort.findBillableZepIds(anySet())).thenReturn(Set.of());
        when(mealEntryPort.fetchMealEntryDates(any(), any())).thenReturn(Set.of());
    }

    @Test
    void disablingMealEntryRuleThroughAdminEndpointRemovesItsWarning() {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(Role.EMPLOYEE, Role.ADMIN));

        given().accept(ContentType.JSON).get("/worktime/warnings/2023-10")
                .then().statusCode(200)
                .body("type", hasItem("MEAL_ENTRY_MISSING"));

        given().contentType(ContentType.JSON).body("{\"enabled\": false, \"activeFrom\": null}")
                .put("/worktime/warning-rules/MEAL_ENTRY")
                .then().statusCode(200);

        given().accept(ContentType.JSON).get("/worktime/warnings/2023-10")
                .then().statusCode(200)
                .body("type", not(hasItem("MEAL_ENTRY_MISSING")));
    }

    private static JourneyBooking journey(LocalDateTime from, JourneyDirection direction) {
        return Instancio.of(JourneyBooking.class)
                .set(field(JourneyBooking::from), from)
                .set(field(JourneyBooking::to), from.plusHours(1))
                .set(field(JourneyBooking::direction), direction)
                .create();
    }
}
