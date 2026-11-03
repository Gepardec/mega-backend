package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSnapshotAdapterTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSnapshotAdapter adapter;

    @Test
    void findActiveCoordinatorIdsIn_shouldReturnOnlyActiveCoordinators() {
        User activeCoordinator = user("active-coordinator", LocalDate.of(2025, 1, 1), null, Role.PROJECT_COORDINATOR);
        User inactiveCoordinator = user("inactive-coordinator", LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31), Role.PROJECT_COORDINATOR);
        User activeNonCoordinator = user("lead", LocalDate.of(2025, 1, 1), null, Role.PROJECT_LEAD);
        when(userRepository.findAll()).thenReturn(List.of(activeCoordinator, inactiveCoordinator, activeNonCoordinator));

        Set<UserId> result = adapter.findActiveCoordinatorIdsIn(MONTH);

        assertThat(result).containsExactly(activeCoordinator.id());
    }

    private User user(String username, LocalDate start, LocalDate end, Role extraRole) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(start, end)),
                Set.of(Role.EMPLOYEE, extraRole)
        );
    }
}
