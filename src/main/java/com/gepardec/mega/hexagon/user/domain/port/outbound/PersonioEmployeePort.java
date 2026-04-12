package com.gepardec.mega.hexagon.user.domain.port.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.PersonioId;

import java.util.Optional;

public interface PersonioEmployeePort {

    Optional<PersonioId> findPersonioIdByEmail(Email email);
}
