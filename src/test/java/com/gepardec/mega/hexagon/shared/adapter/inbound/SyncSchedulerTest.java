package com.gepardec.mega.hexagon.shared.adapter.inbound;

import com.gepardec.mega.hexagon.project.application.port.inbound.ProjectLeadSyncResult;
import com.gepardec.mega.hexagon.project.application.port.inbound.ProjectSyncResult;
import com.gepardec.mega.hexagon.project.application.port.inbound.SyncProjectLeadsUseCase;
import com.gepardec.mega.hexagon.project.application.port.inbound.SyncProjectsUseCase;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.application.port.inbound.SyncProjectLeadRolesResult;
import com.gepardec.mega.hexagon.user.application.port.inbound.SyncProjectLeadRolesUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.SyncUsersUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.UserSyncResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SyncSchedulerTest {

    private SyncUsersUseCase syncUsersUseCase;
    private SyncProjectsUseCase syncProjectsUseCase;
    private SyncProjectLeadsUseCase syncProjectLeadsUseCase;
    private SyncProjectLeadRolesUseCase syncProjectLeadRolesUseCase;
    private SyncScheduler scheduler;

    @BeforeEach
    void setUp() {
        syncUsersUseCase = mock(SyncUsersUseCase.class);
        syncProjectsUseCase = mock(SyncProjectsUseCase.class);
        syncProjectLeadsUseCase = mock(SyncProjectLeadsUseCase.class);
        syncProjectLeadRolesUseCase = mock(SyncProjectLeadRolesUseCase.class);
        scheduler = new SyncScheduler(
                syncUsersUseCase,
                syncProjectsUseCase,
                syncProjectLeadsUseCase,
                syncProjectLeadRolesUseCase
        );
    }

    @Test
    void sync_shouldRunProjectLeadRoleAssignmentAfterProjectLeadSync() {
        Set<UserId> leadUserIds = Set.of(UserId.generate(), UserId.generate());
        when(syncUsersUseCase.sync()).thenReturn(new UserSyncResult(1, 2, 3, 4, 5));
        when(syncProjectsUseCase.sync()).thenReturn(new ProjectSyncResult(1, 2, 3));
        when(syncProjectLeadsUseCase.sync()).thenReturn(new ProjectLeadSyncResult(6, 7, leadUserIds));
        when(syncProjectLeadRolesUseCase.sync(leadUserIds)).thenReturn(new SyncProjectLeadRolesResult(8, 9));

        scheduler.sync();

        var inOrder = inOrder(syncUsersUseCase, syncProjectsUseCase, syncProjectLeadsUseCase, syncProjectLeadRolesUseCase);
        inOrder.verify(syncUsersUseCase).sync();
        inOrder.verify(syncProjectsUseCase).sync();
        inOrder.verify(syncProjectLeadsUseCase).sync();
        inOrder.verify(syncProjectLeadRolesUseCase).sync(leadUserIds);
    }
}
