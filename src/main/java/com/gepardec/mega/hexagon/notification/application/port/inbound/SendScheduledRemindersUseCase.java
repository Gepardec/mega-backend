package com.gepardec.mega.hexagon.notification.application.port.inbound;

import java.time.LocalDate;

public interface SendScheduledRemindersUseCase {

    void send(LocalDate date);
}
