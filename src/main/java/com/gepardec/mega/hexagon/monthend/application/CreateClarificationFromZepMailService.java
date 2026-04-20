package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateClarificationFromZepMailUseCase;
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
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class CreateClarificationFromZepMailService implements CreateClarificationFromZepMailUseCase {

    private static final String DEFAULT_PARSE_ERROR_MESSAGE = "Subject or body of E-Mail is not parseable.";
    private static final String UNKNOWN_RECIPIENT = "unknown";

    private final ZepMailboxPort zepMailboxPort;
    private final ZepMailMessageParser zepMailMessageParser;
    private final UserRepository userRepository;
    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndEmployeeProjectContextService employeeContextService;
    private final PersistZepClarificationService persistZepClarificationService;
    private final Event<ZepMailProcessingFailedEvent> zepMailProcessingFailedEvent;

    @Inject
    public CreateClarificationFromZepMailService(
            ZepMailboxPort zepMailboxPort,
            ZepMailMessageParser zepMailMessageParser,
            UserRepository userRepository,
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndEmployeeProjectContextService employeeContextService,
            PersistZepClarificationService persistZepClarificationService,
            Event<ZepMailProcessingFailedEvent> zepMailProcessingFailedEvent
    ) {
        this.zepMailboxPort = zepMailboxPort;
        this.zepMailMessageParser = zepMailMessageParser;
        this.userRepository = userRepository;
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.employeeContextService = employeeContextService;
        this.persistZepClarificationService = persistZepClarificationService;
        this.zepMailProcessingFailedEvent = zepMailProcessingFailedEvent;
    }

    @Override
    public void create() {
        List<ZepRawMail> unreadMessages = zepMailboxPort.fetchUnreadMessages();
        Log.infof("Processing %d unread ZEP mail(s)", unreadMessages.size());

        for (ZepRawMail rawMail : unreadMessages) {
            processSingleMessage(rawMail);
        }
    }

    private void processSingleMessage(ZepRawMail rawMail) {
        String rawMailContent = formatRawMailContent(rawMail);
        User creator = null;
        String originalRecipient = UNKNOWN_RECIPIENT;

        try {
            ZepMailParseResult parseResult = zepMailMessageParser.parse(rawMail);
            creator = resolveOptionalCreator(parseResult);

            Optional<ZepProjektzeitEntry> entryOptional = parseResult.entry();
            if (entryOptional.isEmpty()) {
                Log.error("Skipping unread ZEP mail because it is not parseable");
                fireProcessingFailedEvent(creator, originalRecipient, DEFAULT_PARSE_ERROR_MESSAGE, rawMailContent);
                return;
            }

            ZepProjektzeitEntry entry = entryOptional.orElseThrow();
            originalRecipient = FullName.of(entry.employeeFirstName(), entry.employeeLastName()).displayName();
            User creatorUser = creator != null
                    ? creator
                    : userRepository.findByZepUsername(entry.zepIdErsteller())
                      .orElseThrow(() -> new IllegalArgumentException(
                              "User with ZEP username '%s' could not be found".formatted(entry.zepIdErsteller().value())
                      ));

            createClarification(entry, creatorUser);
            Log.infof(
                    "Processed ZEP clarification mail for %s %s in project %s",
                    entry.employeeFirstName(),
                    entry.employeeLastName(),
                    entry.projectName()
            );
        } catch (Exception exception) {
            Log.error("Error processing ZEP clarification mail", exception);
            fireProcessingFailedEvent(creator, originalRecipient, errorMessage(exception), rawMailContent);
        }
    }

    private User resolveOptionalCreator(ZepMailParseResult parseResult) {
        return parseResult.creatorUsername()
                .flatMap(userRepository::findByZepUsername)
                .orElse(null);
    }

    private void createClarification(ZepProjektzeitEntry entry, User creator) {
        YearMonth month = YearMonth.from(entry.date());
        User subjectEmployee = resolveSubjectEmployee(entry, month);
        MonthEndProjectSnapshot projectSnapshot = resolveProjectSnapshot(entry, month);
        MonthEndEmployeeProjectContext context = employeeContextService.resolve(month, projectSnapshot.id(), subjectEmployee.id());

        persistZepClarificationService.persist(month, context, creator.id(), entry.message());
    }

    private User resolveSubjectEmployee(ZepProjektzeitEntry entry, YearMonth month) {
        return userRepository.findByFullName(FullName.of(entry.employeeFirstName(), entry.employeeLastName()))
                .filter(user -> user.isActiveIn(month))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unable to resolve employee %s %s from ZEP clarification mail"
                                .formatted(entry.employeeFirstName(), entry.employeeLastName())
                ));
    }

    private MonthEndProjectSnapshot resolveProjectSnapshot(ZepProjektzeitEntry entry, YearMonth month) {
        return monthEndProjectSnapshotPort.findActiveIn(month).stream()
                .filter(project -> project.name().equals(entry.projectName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unable to resolve project '%s' from ZEP clarification mail".formatted(entry.projectName())
                ));
    }

    private void fireProcessingFailedEvent(
            User creator,
            String originalRecipient,
            String errorMessage,
            String rawMailContent
    ) {
        zepMailProcessingFailedEvent.fire(new ZepMailProcessingFailedEvent(
                creator != null ? creator.id() : null,
                creator != null ? creator.email() : null,
                originalRecipient,
                errorMessage,
                rawMailContent
        ));
    }

    private String formatRawMailContent(ZepRawMail rawMail) {
        return "Subject: %s%nBody: %s".formatted(rawMail.subject(), rawMail.htmlBody());
    }

    private String errorMessage(Exception exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Unexpected processing error"
                : exception.getMessage();
    }
}
