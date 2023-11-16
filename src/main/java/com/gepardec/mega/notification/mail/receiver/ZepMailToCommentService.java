package com.gepardec.mega.notification.mail.receiver;

import com.gepardec.mega.domain.model.SourceSystem;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.notification.mail.Mail;
import com.gepardec.mega.notification.mail.MailParameter;
import com.gepardec.mega.notification.mail.MailSender;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.UserService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Dependent
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

                        var empfaenger = userService.findByName(
                                zepProjektzeitDetailsMail.getMitarbeiterVorname(),
                                zepProjektzeitDetailsMail.getMitarbeiterNachname()
                        );

                        mailSenderMetadata.setOriginalRecipient(zepProjektzeitDetailsMail.getMitarbeiterName());

                        commentService.create(
                                StepName.CONTROL_TIME_EVIDENCES.getId(),
                                SourceSystem.ZEP,
                                empfaenger.getEmail(),
                                buildComment(zepProjektzeitDetailsMail),
                                ersteller.getEmail(),
                                zepProjektzeitDetailsMail.getProjekt(),
                                zepProjektzeitDetailsMail.getTag().toString()
                        );
                    }
            );
        } catch (Exception e) {
            reportExceptionToErsteller(e);
        }
    }

    private static String buildComment(ZepProjektzeitDetailsMail mail) {
        return String.format(
                "Deine Buchung vom %s im Projekt '%s' weist beim Vorgang '%s' %s " +
                        "mit der Bemerkung '%s' einen Fehler auf. %s",
                DATE_FORMATTER.format(mail.getTag()),
                mail.getProjekt(),
                mail.getVorgang(),
                mail.getBuchungInfo(),
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
