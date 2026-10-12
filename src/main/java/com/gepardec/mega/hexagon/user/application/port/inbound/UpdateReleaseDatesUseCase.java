package com.gepardec.mega.hexagon.user.application.port.inbound;

import java.util.List;

public interface UpdateReleaseDatesUseCase {

    UpdateReleaseDatesResult update(List<UpdateReleaseDateCommand> commands);
}
