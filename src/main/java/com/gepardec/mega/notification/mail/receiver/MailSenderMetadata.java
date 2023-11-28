package com.gepardec.mega.notification.mail.receiver;

import java.util.Optional;

public class MailSenderMetadata {

    private String originalRecipient;
    private String recipientEmail;
    private String recipientFirstname;
    private String rawContent;

    public Optional<String> getOriginalRecipient() {
        return Optional.ofNullable(originalRecipient);
    }

    public void setOriginalRecipient(String originalRecipient) {
        this.originalRecipient = originalRecipient;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientFirstname() {
        return recipientFirstname;
    }

    public void setRecipientFirstname(String recipientFirstname) {
        this.recipientFirstname = recipientFirstname;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }
}
