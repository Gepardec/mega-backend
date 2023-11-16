package com.gepardec.mega.notification.mail.receiver;

import com.gepardec.mega.application.configuration.MailReceiverConfig;
import com.sun.mail.imap.IMAPMessage;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailReceiverTest {

    @Mock
    Logger logger;

    @Mock
    MailReceiverConfig mailReceiverConfig;

    @InjectMocks
    MailReceiver testedObject;

    @BeforeEach
    void setup() {
        when(mailReceiverConfig.getProtocol()).thenReturn("IMAP");
        when(mailReceiverConfig.getHost()).thenReturn("host.gepardec.com");
        when(mailReceiverConfig.getPassword()).thenReturn("password");
    }

    @Test
    void retrieveZepEmailsFromInbox_ReceiverDisabled_EmptyList() {
        //GIVEN
        when(mailReceiverConfig.isEnabled()).thenReturn(Boolean.FALSE);

        //WHEN
        //THEN
        assertThat(testedObject.retrieveZepEmailsFromInbox()).isEmpty();
    }

    @Test
    void retrieveZepEmailsFromInbox_Successful() throws MessagingException {
        //GIVEN
        try (var mockedStatic = Mockito.mockStatic(Session.class)) {
            when(mailReceiverConfig.isEnabled()).thenReturn(true);

            var inbox = mock(Folder.class);
            when(inbox.search(any())).thenReturn(new Message[]{mock(IMAPMessage.class)});

            var store = mock(Store.class);
            when(store.getFolder(anyString())).thenReturn(inbox);

            var session = mock(Session.class);
            when(session.getStore(anyString())).thenReturn(store);

            mockedStatic.when(() -> Session.getDefaultInstance(any())).thenReturn(session);

            //WHEN
            //THEN
            assertThat(testedObject.retrieveZepEmailsFromInbox()).hasSize(1);
        }
    }

    @Test
    void retrieveZepEmailsFromInbox_Exception_EmptyList() throws MessagingException {
        //GIVEN
        try (var mockedStatic = Mockito.mockStatic(Session.class)) {
            when(mailReceiverConfig.isEnabled()).thenReturn(true);

            var store = mock(Store.class);
            doThrow(MessagingException.class).when(store).connect();

            var session = mock(Session.class);
            when(session.getStore(anyString())).thenReturn(store);

            mockedStatic.when(() -> Session.getDefaultInstance(any())).thenReturn(session);

            //WHEN
            //THEN
            assertThat(testedObject.retrieveZepEmailsFromInbox()).isEmpty();
            verify(logger).error(any(), any(Exception.class));
        }
    }
}
