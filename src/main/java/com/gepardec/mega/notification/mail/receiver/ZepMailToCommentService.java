package com.gepardec.mega.notification.mail.receiver;

import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.SourceSystem;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.notification.mail.Mail;
import com.gepardec.mega.notification.mail.MailParameter;
import com.gepardec.mega.notification.mail.MailSender;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@RequestScoped
public class ZepMailToCommentService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String UNKNOWN = "unknown";

    @Inject
    Logger logger;

    @Inject
    ZepProjektzeitDetailsMailMapper zepProjektzeitDetailsMailMapper;

    @Inject
    UserService userService;

    @Inject
    ProjectService projectService;

    @Inject
    CommentService commentService;

    @Inject
    MailSender mailSender;

    private MailSenderMetadata mailSenderMetadata = new MailSenderMetadata();

    public void saveAsComment(Message message) {
        try {
            zepProjektzeitDetailsMailMapper.convert(message).ifPresent(
                    zepProjektzeitDetailsMail -> {
                        var ersteller = userService.findByZepId(zepProjektzeitDetailsMail.getZepIdErsteller());
                        mailSenderMetadata = new MailSenderMetadata();
                        mailSenderMetadata.setRecipientFirstname(ersteller.getFirstname());
                        mailSenderMetadata.setRecipientEmail(ersteller.getEmail());
                        mailSenderMetadata.setRawContent(zepProjektzeitDetailsMail.getRawContent());

                        var empfaenger = userService.findByName(
                                zepProjektzeitDetailsMail.getMitarbeiterVorname(),
                                zepProjektzeitDetailsMail.getMitarbeiterNachname()
                        );
                        mailSenderMetadata.setOriginalRecipient(zepProjektzeitDetailsMail.getMitarbeiterName());

                        var project = projectService.getProjectByName(
                                zepProjektzeitDetailsMail.getProjekt(),
                                YearMonth.from(zepProjektzeitDetailsMail.getTag())
                        );

                        var projectBillable = project.map(Project::isBillable)
                                .orElseThrow(projectNotFoundInZep(zepProjektzeitDetailsMail.getProjekt()));

                        commentService.create(
                                projectBillable
                                        ? StepName.CONTROL_TIME_EVIDENCES.getId()
                                        : StepName.CONTROL_INTERNAL_TIMES.getId(),
                                SourceSystem.ZEP,
                                empfaenger.getEmail(),
                                buildComment(zepProjektzeitDetailsMail),
                                ersteller.getEmail(),
                                projectBillable
                                        ? zepProjektzeitDetailsMail.getProjekt()
                                        : null,
                                YearMonth.from(zepProjektzeitDetailsMail.getTag())
                        );
                        logger.info("E-Mail saved as comment.");
                    }
            );
        } catch (Exception e) {
            reportExceptionToErsteller(e);
        }
    }

    private static Supplier<IllegalArgumentException> projectNotFoundInZep(String projectName) {
        return () -> new IllegalArgumentException(String.format("Project '%s' could not be found in ZEP!", projectName));
    }

    private static String buildComment(ZepProjektzeitDetailsMail mail) {
        return String.format(
                "Buchung vom %s (%s - %s) im Projekt '%s' - '%s' mit dem Text '%s' ist anzupassen.\n %s",
                DATE_FORMATTER.format(mail.getTag()),
                mail.getUhrzeitVon(),
                mail.getUhrzeitBis(),
                mail.getProjekt(),
                mail.getVorgang(),
                mail.getBemerkung(),
                mail.getNachricht()
        );
    }

    private void reportExceptionToErsteller(Exception e) {
        if (mailSenderMetadata.getRecipientEmail() != null) {
            logger.error("Error processing E-Mail: {}.", e.getMessage());

            Map<String, String> mailParameter = new HashMap<>() {{
                put(MailParameter.RECIPIENT, mailSenderMetadata.getRecipientFirstname()); // employee who sent the comment
                put(MailParameter.COMMENT, e.getMessage()); // error message
                put(MailParameter.ORIGINAL_MAIL, mailSenderMetadata.getRawContent()); // original E-Mail
            }};

            mailSender.send(
                    Mail.ZEP_COMMENT_PROCESSING_ERROR,
                    mailSenderMetadata.getRecipientEmail(), // recipient of this email
                    mailSenderMetadata.getOriginalRecipient().orElse(UNKNOWN), // employee that the comment was sent to
                    Locale.GERMAN,
                    mailParameter,
                    List.of(mailSenderMetadata.getOriginalRecipient().orElse(UNKNOWN)) // employee name of original email for subject
            );
        } else {
            logger.error("Recipient unknown, error cannot be reported.");
        }
    }
}
