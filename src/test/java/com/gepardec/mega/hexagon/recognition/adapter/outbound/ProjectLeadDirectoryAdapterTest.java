package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ProjectLeadDirectoryAdapterTest {

    private UserRepository userRepository;
    private ProjectLeadDirectoryMapper mapper;
    private ProjectLeadDirectoryAdapter adapter;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        mapper = mock(ProjectLeadDirectoryMapper.class);
        adapter = new ProjectLeadDirectoryAdapter(userRepository, mapper);
    }

    @Test
    void findActiveInternalProjectLeads_shouldReturnOnlyActiveInternalProjectLeadsForReferenceDate() {
        LocalDate referenceDate = LocalDate.of(2026, 7, 6);
        User activeInternalLead = user("lead", Set.of(Role.PROJECT_LEAD), LocalDate.of(2024, 1, 1), null);
        User inactiveLead = user("inactive", Set.of(Role.PROJECT_LEAD), LocalDate.of(2024, 1, 1), LocalDate.of(2026, 7, 5));
        User externalLead = user("external", Set.of(Role.PROJECT_LEAD), LocalDate.of(2024, 1, 1), null);
        User nonLead = user("employee", Set.of(Role.EMPLOYEE), LocalDate.of(2024, 1, 1), null);
        RecognitionMailRecipient recipient = new RecognitionMailRecipient(Email.of("lead@example.com"), "Lead");

        when(userRepository.findByRole(Role.PROJECT_LEAD))
                .thenReturn(List.of(activeInternalLead, inactiveLead, externalLead, nonLead));
        when(mapper.toRecipient(activeInternalLead)).thenReturn(recipient);

        List<RecognitionMailRecipient> recipients = adapter.findActiveInternalProjectLeads(referenceDate);

        assertThat(recipients).containsExactly(recipient);
        verify(userRepository).findByRole(Role.PROJECT_LEAD);
        verify(mapper).toRecipient(activeInternalLead);
        verifyNoMoreInteractions(mapper);
    }

    private User user(String username, Set<Role> roles, LocalDate start, LocalDate end) {
        return new User(
                UserId.of(Instancio.create(UUID.class)),
                Email.of(username + "@example.com"),
                FullName.of(username, "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(start, end)),
                roles
        );
    }
}
