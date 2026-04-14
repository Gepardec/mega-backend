package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface MonthEndClarificationRepository {

    Optional<MonthEndClarification> findById(MonthEndClarificationId id);

    List<MonthEndClarification> findOpenEmployeeClarifications(UserId employeeId, YearMonth month);

    List<MonthEndClarification> findOpenProjectLeadClarifications(UserId projectLeadId, YearMonth month);

    void save(MonthEndClarification clarification);
}
