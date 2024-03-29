package com.gepardec.mega.db.entity.employee;

public enum PrematureEmployeeCheckState {
    //    This state is used when the employee has created a premature employee check and cancelled it
    CANCELLED,
    //    This state is used when the employee has made a premature employee check but has still a reason to not confirm it (is a requirement)
    IN_PROGRESS,
    //    This state is used when the employee has made a premature employee check and has confirmed it
    DONE
}
