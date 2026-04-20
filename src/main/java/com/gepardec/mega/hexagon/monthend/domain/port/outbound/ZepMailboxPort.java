package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.ZepRawMail;

import java.util.List;

public interface ZepMailboxPort {

    List<ZepRawMail> fetchUnreadMessages();
}
