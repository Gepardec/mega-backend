package com.gepardec.mega.hexagon.notification.domain;

import com.gepardec.mega.hexagon.notification.domain.model.MailScheduleType;
import com.gepardec.mega.hexagon.shared.domain.model.Role;

public enum ReminderType implements MailNotificationId {
    EMPLOYEE_CHECK_PROJECTTIME(-1, MailScheduleType.WORKING_DAY_BASED, Role.EMPLOYEE),
    OM_CONTROL_EMPLOYEES_CONTENT(3, MailScheduleType.WORKING_DAY_BASED, Role.OFFICE_MANAGEMENT),
    PL_PROJECT_CONTROLLING(5, MailScheduleType.WORKING_DAY_BASED, Role.PROJECT_LEAD),
    OM_RELEASE(-5, MailScheduleType.WORKING_DAY_BASED, Role.OFFICE_MANAGEMENT),
    OM_ADMINISTRATIVE(15, MailScheduleType.DAY_OF_MONTH_BASED, Role.OFFICE_MANAGEMENT),
    OM_SALARY(-3, MailScheduleType.WORKING_DAY_BASED, Role.OFFICE_MANAGEMENT);

    private final int dayOffset;
    private final MailScheduleType scheduleType;
    private final Role targetRole;

    ReminderType(int dayOffset, MailScheduleType scheduleType, Role targetRole) {
        this.dayOffset = dayOffset;
        this.scheduleType = scheduleType;
        this.targetRole = targetRole;
    }

    public int dayOffset() {
        return dayOffset;
    }

    public MailScheduleType scheduleType() {
        return scheduleType;
    }

    public Role targetRole() {
        return targetRole;
    }
}
