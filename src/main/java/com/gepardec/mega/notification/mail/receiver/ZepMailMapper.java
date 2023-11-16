package com.gepardec.mega.notification.mail.receiver;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.Optional;

public interface ZepMailMapper<R> {

    Optional<R> convert(Message message) throws MessagingException, IOException;
}
