package com.gepardec.mega.db.entity.common;

public enum ProjectTaskType {
    PROJECT_INTERNAL(3, "Intern"),
    TASK_DOCTOR_VISIT(233, "Arztbesuch");

    final int id;
    final String name;

    ProjectTaskType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
