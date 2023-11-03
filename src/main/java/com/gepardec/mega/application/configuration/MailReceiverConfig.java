package com.gepardec.mega.application.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MailReceiverConfig {

    @Inject
    @ConfigProperty(name = "mega.mail.receiver.protocol")
    String protocol;

    @Inject
    @ConfigProperty(name = "mega.mail.receiver.host")
    String host;

    @Inject
    @ConfigProperty(name = "mega.mail.receiver.port")
    Integer port;

    @Inject
    @ConfigProperty(name = "mega.mail.receiver.username")
    String username;

    @Inject
    @ConfigProperty(name = "mega.mail.receiver.password")
    String password;

    @Inject
    @ConfigProperty(name = "mega.mail.receiver.sender")
    String sender;

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSender() {
        return sender;
    }
}
