package com.gepardec.mega.notification.mail.receiver;

public class MailSenderMetadata {

    private String originalRecipient;
    private String recipientEmail;
    private String recipientFirstname;

    public String getOriginalRecipient() {
        return originalRecipient;
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
}
