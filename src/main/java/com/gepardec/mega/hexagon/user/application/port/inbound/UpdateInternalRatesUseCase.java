package com.gepardec.mega.hexagon.user.application.port.inbound;

import java.util.List;

public interface UpdateInternalRatesUseCase {

    void update(List<InternalRateUpdateCommand> commands);
}
