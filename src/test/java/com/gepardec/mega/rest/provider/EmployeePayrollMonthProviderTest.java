package com.gepardec.mega.rest.provider;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.service.api.StepEntryService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static com.gepardec.mega.rest.provider.PayrollContext.PayrollContextType.EMPLOYEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
class EmployeePayrollMonthProviderTest {

    @InjectSpy
    @PayrollContext(EMPLOYEE)
    EmployeePayrollMonthProvider payrollMonthProvider;

    @InjectMock
    UserContext userContext;

    @InjectMock
    StepEntryService stepEntryService;

    private static final String TEST_EMAIL = "test@gepardec.com";
    private final LocalDate now = LocalDate.now();

    @BeforeEach
    void setUp() {
        User user = Mockito.mock(User.class);
        when(user.getEmail()).thenReturn(TEST_EMAIL);
        when(userContext.getUser()).thenReturn(user);
    }

    @Nested
    class GetPayrollMonth {

        @Nested
        class WhenAfterMidOfMonth {

            @Test
            void getPayrollMonth_WhenSysdateIsAfterMidOfMonthButMonthNotConfirmed_ShouldReturnPreviousMonth() {
                // Arrange
                LocalDate afterMidOfMonth = now.withDayOfMonth(15);
                LocalDate firstOfPreviousMonth = now.minusMonths(1).withDayOfMonth(1);

                // Mock step entry with IN_PROGRESS state
                StepEntry stepEntry = Mockito.mock(StepEntry.class);
                when(stepEntry.getState()).thenReturn(EmployeeState.IN_PROGRESS);
                when(stepEntryService.findControlTimesStepEntry(TEST_EMAIL, firstOfPreviousMonth))
                        .thenReturn(Optional.of(stepEntry));

                when(payrollMonthProvider.getSysdate()).thenReturn(afterMidOfMonth);

                // Act
                YearMonth result = payrollMonthProvider.getPayrollMonth();

                // Assert
                assertThat(result).isEqualTo(YearMonth.now().minusMonths(1));
            }

            @Test
            void getPayrollMonth_WhenSysdateIsAfterMidOfMonthAndMonthConfirmed_ShouldReturnCurrentMonth() {
                // Arrange
                LocalDate afterMidOfMonth = now.withDayOfMonth(15);
                LocalDate firstOfPreviousMonth = now.minusMonths(1).withDayOfMonth(1);

                // Mock step entry with DONE state
                StepEntry stepEntry = Mockito.mock(StepEntry.class);
                when(stepEntry.getState()).thenReturn(EmployeeState.DONE);
                when(stepEntryService.findControlTimesStepEntry(TEST_EMAIL, firstOfPreviousMonth))
                        .thenReturn(Optional.of(stepEntry));

                when(payrollMonthProvider.getSysdate()).thenReturn(afterMidOfMonth);

                // Act
                YearMonth result = payrollMonthProvider.getPayrollMonth();

                // Assert
                assertThat(result).isEqualTo(YearMonth.now());
            }
        }

        @Nested
        class WhenBeforeMidOfMonth {

            @Test
            void getPayrollMonth_WhenSysdateIsBeforeMidOfMonth_ShouldReturnPreviousMonth() {
                // Arrange
                LocalDate firstOfPreviousMonth = now.minusMonths(1).withDayOfMonth(1);
                LocalDate firstOfCurrentMonth = now.withDayOfMonth(1);

                // Mock step entry with DONE state
                StepEntry stepEntry = Mockito.mock(StepEntry.class);
                when(stepEntry.getState()).thenReturn(EmployeeState.DONE);
                when(stepEntryService.findControlTimesStepEntry(TEST_EMAIL, firstOfPreviousMonth))
                        .thenReturn(Optional.of(stepEntry));

                when(payrollMonthProvider.getSysdate()).thenReturn(firstOfCurrentMonth);

                // Act
                YearMonth result = payrollMonthProvider.getPayrollMonth();

                // Assert
                assertThat(result).isEqualTo(YearMonth.now().minusMonths(1));
            }
        }
    }
}
