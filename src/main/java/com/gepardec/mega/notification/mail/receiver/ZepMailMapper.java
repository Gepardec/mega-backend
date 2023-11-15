package com.gepardec.mega.notification.mail.receiver;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface ZepMailMapper<R> {

    R convert(Message message) throws MessagingException, IOException;
}
