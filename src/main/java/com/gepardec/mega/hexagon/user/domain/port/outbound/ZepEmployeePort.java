package com.gepardec.mega.hexagon.user.domain.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.HourlyRate;
import com.gepardec.mega.hexagon.user.domain.model.ZepEmployeeSyncData;
import io.smallrye.mutiny.Uni;

import java.time.LocalDate;
import java.util.List;

public interface ZepEmployeePort {

    List<ZepEmployeeSyncData> fetchAll();

    Uni<Void> updateReleaseDate(ZepUsername username, LocalDate releaseDate);

    Uni<Void> updateHourlyRate(ZepUsername username, HourlyRate hourlyRate, LocalDate effectiveFrom);
}
