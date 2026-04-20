package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.event.ZepMailProcessingFailedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.ZepMailParseResult;
import com.gepardec.mega.hexagon.monthend.domain.model.ZepProjektzeitEntry;
import com.gepardec.mega.hexagon.monthend.domain.model.ZepRawMail;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.ZepMailboxPort;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndEmployeeProjectContextService;
import com.gepardec.mega.hexagon.monthend.domain.services.ZepMailMessageParser;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.event.Event;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreateClarificationFromZepMailServiceTest {

    private static final ZepRawMail RAW_MAIL = new ZepRawMail(
            "Projektzeit Fr, 03.11.2023 von 11:15 bis 12:00 (0,75 Stunden)",
            "<table>001-mmustermann</table>"
    );

    private final YearMonth month = YearMonth.of(2023, 11);
    private final UserId creatorId = UserId.of(Instancio.create(UUID.class));
    private final UserId subjectEmployeeId = UserId.of(Instancio.create(UUID.class));
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));

    private ZepMailboxPort zepMailboxPort;
    private ZepMailMessageParser zepMailMessageParser;
    private UserRepository userRepository;
    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private MonthEndEmployeeProjectContextService employeeContextService;
    private PersistZepClarificationService persistZepClarificationService;
    private Event<ZepMailProcessingFailedEvent> zepMailProcessingFailedEvent;
    private CreateClarificationFromZepMailService service;

    @BeforeEach
    void setUp() {
        zepMailboxPort = mock(ZepMailboxPort.class);
        zepMailMessageParser = mock(ZepMailMessageParser.class);
        userRepository = mock(UserRepository.class);
        monthEndProjectSnapshotPort = mock(MonthEndProjectSnapshotPort.class);
        employeeContextService = mock(MonthEndEmployeeProjectContextService.class);
        persistZepClarificationService = mock(PersistZepClarificationService.class);
        zepMailProcessingFailedEvent = mock(Event.class);
        service = new CreateClarificationFromZepMailService(
                zepMailboxPort,
                zepMailMessageParser,
                userRepository,
                monthEndProjectSnapshotPort,
                employeeContextService,
                persistZepClarificationService,
                zepMailProcessingFailedEvent
        );
    }

    @Test
    void create_shouldDelegateToTransactionalPersistenceForValidMail() {
        User creator = creatorUser();
        User subjectEmployee = subjectEmployee();
        ZepProjektzeitEntry entry = entry();
        MonthEndProjectSnapshot projectSnapshot = projectSnapshot();

        when(zepMailboxPort.fetchUnreadMessages()).thenReturn(List.of(RAW_MAIL));
        when(zepMailMessageParser.parse(RAW_MAIL)).thenReturn(parseResult(entry));
        when(userRepository.findByZepUsername(entry.zepIdErsteller())).thenReturn(Optional.of(creator));
        when(userRepository.findByFullName(FullName.of("Max", "Mustermann"))).thenReturn(Optional.of(subjectEmployee));
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(projectSnapshot));
        when(employeeContextService.resolve(month, projectId, subjectEmployeeId))
                .thenReturn(employeeContext(projectSnapshot, Set.of(creatorId)));

        service.create();

        verify(persistZepClarificationService).persist(
                month,
                employeeContext(projectSnapshot, Set.of(creatorId)),
                creatorId,
                entry.message()
        );
        verifyNoInteractions(zepMailProcessingFailedEvent);
    }

    @Test
    void create_shouldFireFailureEventWhenMailIsNotParseable() {
        User creator = creatorUser();
        ZepProjektzeitEntry entry = entry();

        when(zepMailboxPort.fetchUnreadMessages()).thenReturn(List.of(RAW_MAIL));
        when(zepMailMessageParser.parse(RAW_MAIL)).thenReturn(new ZepMailParseResult(Optional.empty(), Optional.of(entry.zepIdErsteller())));
        when(userRepository.findByZepUsername(entry.zepIdErsteller())).thenReturn(Optional.of(creator));

        service.create();

        verify(zepMailProcessingFailedEvent).fire(argThat(event ->
                event.creatorUserId().equals(creatorId)
                        && event.creatorEmail().equals(creator.email())
                        && event.originalRecipient().equals("unknown")
                        && event.errorMessage().equals("Subject or body of E-Mail is not parseable.")
                        && event.rawMailContent().contains(RAW_MAIL.htmlBody())
        ));
        verifyNoInteractions(persistZepClarificationService);
    }

    @Test
    void create_shouldFireFailureEventWhenTransactionalPersistenceRollsBack() {
        User creator = creatorUser();
        User subjectEmployee = subjectEmployee();
        ZepProjektzeitEntry entry = entry();
        MonthEndProjectSnapshot projectSnapshot = projectSnapshot();

        when(zepMailboxPort.fetchUnreadMessages()).thenReturn(List.of(RAW_MAIL));
        when(zepMailMessageParser.parse(RAW_MAIL)).thenReturn(parseResult(entry));
        when(userRepository.findByZepUsername(entry.zepIdErsteller())).thenReturn(Optional.of(creator));
        when(userRepository.findByFullName(FullName.of("Max", "Mustermann"))).thenReturn(Optional.of(subjectEmployee));
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(projectSnapshot));
        when(employeeContextService.resolve(month, projectId, subjectEmployeeId))
                .thenReturn(employeeContext(projectSnapshot, Set.of(creatorId)));
        org.mockito.Mockito.doThrow(new IllegalStateException("transaction rolled back"))
                .when(persistZepClarificationService)
                .persist(month, employeeContext(projectSnapshot, Set.of(creatorId)), creatorId, entry.message());

        service.create();

        verify(zepMailProcessingFailedEvent).fire(argThat(event ->
                event.creatorUserId().equals(creatorId)
                        && event.creatorEmail().equals(creator.email())
                        && event.originalRecipient().equals("Max Mustermann")
                        && event.errorMessage().contains("transaction rolled back")
        ));
    }

    @Test
    void create_shouldFireFailureEventWhenClarificationContextResolutionFails() {
        User creator = creatorUser();
        User subjectEmployee = subjectEmployee();
        ZepProjektzeitEntry entry = entry();
        MonthEndProjectSnapshot projectSnapshot = projectSnapshot();

        when(zepMailboxPort.fetchUnreadMessages()).thenReturn(List.of(RAW_MAIL));
        when(zepMailMessageParser.parse(RAW_MAIL)).thenReturn(parseResult(entry));
        when(userRepository.findByZepUsername(entry.zepIdErsteller())).thenReturn(Optional.of(creator));
        when(userRepository.findByFullName(FullName.of("Max", "Mustermann"))).thenReturn(Optional.of(subjectEmployee));
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(projectSnapshot));
        when(employeeContextService.resolve(month, projectId, subjectEmployeeId))
                .thenThrow(new IllegalArgumentException("month-end project context not found"));

        service.create();

        verify(zepMailProcessingFailedEvent).fire(argThat(event ->
                event.creatorUserId().equals(creatorId)
                        && event.creatorEmail().equals(creator.email())
                        && event.originalRecipient().equals("Max Mustermann")
                        && event.errorMessage().contains("context not found")
        ));
        verifyNoInteractions(persistZepClarificationService);
    }

    @Test
    void create_shouldContinueProcessingRemainingMailsWhenParseFails() {
        ZepRawMail failingMail = new ZepRawMail("subject", "body");
        User creator = creatorUser();
        User subjectEmployee = subjectEmployee();
        ZepProjektzeitEntry entry = entry();
        MonthEndProjectSnapshot projectSnapshot = projectSnapshot();

        when(zepMailboxPort.fetchUnreadMessages()).thenReturn(List.of(failingMail, RAW_MAIL));
        when(zepMailMessageParser.parse(failingMail)).thenThrow(new IllegalStateException("parse failed"));
        when(zepMailMessageParser.parse(RAW_MAIL)).thenReturn(parseResult(entry));
        when(userRepository.findByZepUsername(entry.zepIdErsteller())).thenReturn(Optional.of(creator));
        when(userRepository.findByFullName(FullName.of("Max", "Mustermann"))).thenReturn(Optional.of(subjectEmployee));
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(projectSnapshot));
        when(employeeContextService.resolve(month, projectId, subjectEmployeeId))
                .thenReturn(employeeContext(projectSnapshot, Set.of(creatorId)));

        service.create();

        verify(zepMailProcessingFailedEvent).fire(argThat(event ->
                event.creatorUserId() == null
                        && event.originalRecipient().equals("unknown")
                        && event.errorMessage().equals("parse failed")
        ));
        verify(persistZepClarificationService).persist(
                month,
                employeeContext(projectSnapshot, Set.of(creatorId)),
                creatorId,
                entry.message()
        );
    }

    private ZepProjektzeitEntry entry() {
        return new ZepProjektzeitEntry(
                LocalDate.of(2023, 11, 3),
                "11:15",
                "12:00",
                "<table>...</table>",
                ZepUsername.of("001-mmustermann"),
                "Max",
                "Mustermann",
                "Gepardec",
                "Learning Friday",
                "MEGA",
                "Projekt passt nicht, bitte anpassen!"
        );
    }

    private ZepMailParseResult parseResult(ZepProjektzeitEntry entry) {
        return new ZepMailParseResult(Optional.of(entry), Optional.of(entry.zepIdErsteller()));
    }

    private MonthEndProjectSnapshot projectSnapshot() {
        return new MonthEndProjectSnapshot(projectId, 77, "Gepardec", true, Set.of(creatorId));
    }

    private MonthEndEmployeeProjectContext employeeContext(
            MonthEndProjectSnapshot projectSnapshot,
            Set<UserId> eligibleLeadIds
    ) {
        return new MonthEndEmployeeProjectContext(
                month,
                projectSnapshot,
                new UserRef(subjectEmployeeId, FullName.of("Max", "Mustermann"), ZepUsername.of("max.mustermann")),
                eligibleLeadIds
        );
    }

    private User creatorUser() {
        return new User(
                creatorId,
                Email.of("creator@example.com"),
                FullName.of("Clara", "Creator"),
                ZepUsername.of("001-mmustermann"),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2023, 1, 1), null)),
                Set.of()
        );
    }

    private User subjectEmployee() {
        return new User(
                subjectEmployeeId,
                Email.of("max.mustermann@example.com"),
                FullName.of("Max", "Mustermann"),
                ZepUsername.of("max.mustermann"),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2023, 1, 1), null)),
                Set.of()
        );
    }
}
