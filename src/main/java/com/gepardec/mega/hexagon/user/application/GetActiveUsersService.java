package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.application.port.inbound.GetActiveUsersUseCase;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
@Transactional(Transactional.TxType.SUPPORTS)
public class GetActiveUsersService implements GetActiveUsersUseCase {

    private final UserRepository userRepository;
    private final Clock clock;

    @Inject
    public GetActiveUsersService(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    public List<User> getActiveUsers() {
        YearMonth payrollMonth = YearMonth.now(clock).minusMonths(1);
        return userRepository.findAll().stream()
                .filter(user -> user.isActiveIn(payrollMonth))
                .sorted(Comparator.comparing((User user) -> user.name().displayName()).thenComparing(user -> user.id().value()))
                .toList();
    }
}
