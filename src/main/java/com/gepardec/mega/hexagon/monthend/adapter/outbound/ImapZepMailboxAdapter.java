package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.ZepRawMail;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.ZepMailboxPort;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.SearchTerm;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

@ApplicationScoped
public class ImapZepMailboxAdapter implements ZepMailboxPort {

    private static final String HTML_CONTENT_TYPE = "text/html";

    private final String protocol;
    private final String host;
    private final Integer port;
    private final String username;
    private final String password;
    private final String sender;

    @Inject
    public ImapZepMailboxAdapter(
            @ConfigProperty(name = "mega.mail.receiver.protocol") String protocol,
            @ConfigProperty(name = "mega.mail.receiver.host") String host,
            @ConfigProperty(name = "mega.mail.receiver.port") Integer port,
            @ConfigProperty(name = "mega.mail.receiver.username") String username,
            @ConfigProperty(name = "mega.mail.receiver.password") String password,
            @ConfigProperty(name = "mega.mail.receiver.sender") String sender
    ) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sender = sender;
    }

    @Override
    public List<ZepRawMail> fetchUnreadMessages() {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", protocol);
        properties.put("mail.imaps.host", host);
        properties.put("mail.imaps.port", port);

        try {
            Session session = Session.getDefaultInstance(properties);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);

            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            try {
                Message[] unreadMessages = inbox.search(bySenderAndUnseen());
                Log.infof("Found %d unread ZEP mail(s) in inbox", unreadMessages.length);
                return Stream.of(unreadMessages)
                        .map(message -> toZepRawMail(cloneMessage(session, message)))
                        .toList();
            } finally {
                inbox.close(false);
                store.close();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to fetch unread ZEP mail messages", exception);
        }
    }

    private ZepRawMail toZepRawMail(Message message) {
        try {
            String subject = Objects.toString(message.getSubject(), "");
            String htmlBody = extractHtmlBody(message.getContent());
            return new ZepRawMail(subject, htmlBody);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to extract content from ZEP mail message", exception);
        }
    }

    private String extractHtmlBody(Object content) throws MessagingException, IOException {
        if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (part.getContentType().toLowerCase().contains(HTML_CONTENT_TYPE)) {
                    return Objects.toString(part.getContent(), "");
                }
            }
            return Objects.toString(multipart.getBodyPart(0).getContent(), "");
        }
        return Objects.toString(content, "");
    }

    private Message cloneMessage(Session session, Message message) {
        try {
            if (message instanceof MimeMessage mimeMessage) {
                return new MimeMessage(mimeMessage);
            }
            return new MimeMessage(session, message.getInputStream());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to copy unread ZEP mail message", exception);
        }
    }

    private SearchTerm bySenderAndUnseen() {
        Flags seen = new Flags(Flags.Flag.SEEN);
        return new AndTerm(new FlagTerm(seen, false), new FromStringTerm(sender));
    }
}
