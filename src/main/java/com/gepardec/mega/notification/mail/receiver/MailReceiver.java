package com.gepardec.mega.notification.mail.receiver;

import com.gepardec.mega.application.configuration.MailReceiverConfig;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Session;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.SearchTerm;
import org.slf4j.Logger;

import java.util.List;
import java.util.Properties;

@RequestScoped
public class MailReceiver {

    @Inject
    Logger logger;

    @Inject
    MailReceiverConfig mailReceiverConfig;

    @Inject
    ZepMailToCommentService zepMailToCommentService;

    public void retrieveZepEmailsFromInbox() {
        if (mailReceiverConfig == null) {
            logger.warn("No config property for enabling E-Mail receiver is set.");
        }

        if (!Boolean.TRUE.equals(mailReceiverConfig.isEnabled())) {
            logger.info("E-Mail receiver is disabled.");
            return;
        }

        var properties = createMailProperties();

        try {
            var session = Session.getDefaultInstance(properties);
            var store = session.getStore(mailReceiverConfig.getProtocol());
            store.connect(
                    mailReceiverConfig.getHost(),
                    mailReceiverConfig.getUsername(),
                    mailReceiverConfig.getPassword()
            );

            var inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            logger.info("Inbox opened.");

            var messages = List.of(inbox.search(bySenderAndUnseen()));
            logger.info("Found {} relevant message(s).", messages.size());

            messages.forEach(zepMailToCommentService::saveAsComment);

            inbox.close(false);
            store.close();
            logger.info("Inbox closed.");
        } catch (Exception e) {
            logger.error("Error retrieving E-Mails from Mailbox: ", e);
        }
    }

    private Properties createMailProperties() {
        var properties = new Properties();
        properties.put("mail.store.protocol", mailReceiverConfig.getProtocol());
        properties.put("mail.imaps.host", mailReceiverConfig.getHost());
        properties.put("mail.imaps.port", mailReceiverConfig.getPort());

        return properties;
    }

    private SearchTerm bySenderAndUnseen() {
        var senderTerm = new FromStringTerm(mailReceiverConfig.getSender());

        var seen = new Flags(Flags.Flag.SEEN);
        var unseenFlagTerm = new FlagTerm(seen, false);

        return new AndTerm(unseenFlagTerm, senderTerm);
    }
}

