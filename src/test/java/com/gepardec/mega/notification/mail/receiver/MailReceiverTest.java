package com.gepardec.mega.notification.mail.receiver;

import com.gepardec.mega.application.configuration.MailReceiverConfig;
import com.sun.mail.imap.IMAPMessage;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class MailReceiverTest {

    @InjectMock
    Logger logger;

    @InjectMock
    MailReceiverConfig mailReceiverConfig;

    @InjectMock
    ZepMailToCommentService zepMailToCommentService;

    @Inject
    MailReceiver testedObject;

    @BeforeEach
    void setup() {
        when(mailReceiverConfig.getProtocol()).thenReturn("IMAP");
        when(mailReceiverConfig.getHost()).thenReturn("host.gepardec.com");
        when(mailReceiverConfig.getPassword()).thenReturn("password");
    }

    @Test
    void retrieveZepEmailsFromInbox_Successful() throws MessagingException {
        //GIVEN
        try (var mockedStatic = Mockito.mockStatic(Session.class)) {
            var inbox = mock(Folder.class);
            when(inbox.search(any())).thenReturn(new Message[]{mock(IMAPMessage.class)});

            var store = mock(Store.class);
            when(store.getFolder(anyString())).thenReturn(inbox);

            var session = mock(Session.class);
            when(session.getStore(anyString())).thenReturn(store);

            mockedStatic.when(() -> Session.getDefaultInstance(any())).thenReturn(session);

            //WHEN
            testedObject.retrieveZepEmailsFromInbox();

            //THEN
            verify(zepMailToCommentService).saveAsComment(any());
        }
    }

    @Test
    void retrieveZepEmailsFromInbox_Exception_EmptyList() throws MessagingException {
        //GIVEN
        try (var mockedStatic = Mockito.mockStatic(Session.class)) {
            var store = mock(Store.class);
            doThrow(MessagingException.class).when(store).connect();

            var session = mock(Session.class);
            when(session.getStore(anyString())).thenReturn(store);

            mockedStatic.when(() -> Session.getDefaultInstance(any())).thenReturn(session);

            //WHEN
            testedObject.retrieveZepEmailsFromInbox();

            //THEN
            verify(zepMailToCommentService, times(0)).saveAsComment(any());
            verify(logger).error(any(), any(Exception.class));
        }
    }
}
