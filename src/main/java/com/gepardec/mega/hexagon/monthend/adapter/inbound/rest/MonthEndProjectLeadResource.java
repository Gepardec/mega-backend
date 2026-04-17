package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndProjectLeadApi;
import com.gepardec.mega.hexagon.generated.model.CreateProjectLeadClarificationRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.PROJECT_LEAD)
public class MonthEndProjectLeadResource implements MonthEndProjectLeadApi {

    private final GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase;
    private final CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;
    private final MonthEndUserSnapshotPort userSnapshotPort;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndProjectLeadResource(
            GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase,
            CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase,
            MonthEndUserSnapshotPort userSnapshotPort,
            AuthenticatedActorContext authenticatedActorContext,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getProjectLeadMonthEndStatusOverviewUseCase = getProjectLeadMonthEndStatusOverviewUseCase;
        this.createMonthEndClarificationUseCase = createMonthEndClarificationUseCase;
        this.userSnapshotPort = userSnapshotPort;
        this.authenticatedActorContext = authenticatedActorContext;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response getProjectLeadMonthEndStatusOverview(String month) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndStatusOverview overview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(
                actorId,
                transportHelper.parseMonth(month)
        );
        Map<UserId, UserRef> userRefs = resolveUserRefs(clarificationUserIds(overview.clarifications()), overview.month());
        return Response.ok(monthEndRestMapper.toResponse(overview, userRefs, actorId)).build();
    }

    @Override
    public Response createProjectLeadMonthEndClarification(CreateProjectLeadClarificationRequest request) {
        UserId actorId = authenticatedActorContext.userId();
        UserId subjectEmployeeId = request.getSubjectEmployeeId() != null
                ? transportHelper.toUserId(request.getSubjectEmployeeId())
                : null;
        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                transportHelper.parseMonth(request.getMonth()),
                transportHelper.toProjectId(request.getProjectId()),
                subjectEmployeeId,
                actorId,
                request.getText()
        );
        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.status(Response.Status.CREATED)
                .entity(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId))
                .build();
    }

    private Map<UserId, UserRef> resolveUserRefs(Set<UserId> ids, YearMonth month) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userSnapshotPort.findByIds(ids, month).stream()
                .collect(Collectors.toMap(UserRef::id, Function.identity()));
    }

    private static Set<UserId> clarificationUserIds(List<MonthEndClarification> clarifications) {
        return clarifications.stream()
                .flatMap(c -> c.referencedUserIds().stream())
                .collect(Collectors.toSet());
    }
}
