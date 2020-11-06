package com.gepardec.mega.domain.model.monthlyreport;

import com.google.auto.value.AutoValue;

import java.time.LocalDateTime;

@AutoValue
public abstract class ProjectTimeEntry implements ProjectEntry {

    public static ProjectTimeEntry of(LocalDateTime fromTime, LocalDateTime toTime, Task task) {
        return new com.gepardec.mega.domain.model.monthlyreport.AutoValue_ProjectTimeEntry(fromTime, toTime, task);
    }

    @Override
    public abstract LocalDateTime getFromTime();

    @Override
    public abstract LocalDateTime getToTime();

    @Override
    public abstract Task getTask();
}
