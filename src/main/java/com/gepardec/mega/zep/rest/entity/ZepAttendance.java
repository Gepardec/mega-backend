package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZepAttendance {
    private int id;
    private LocalDate date;
    private LocalTime from;
    private LocalTime to;
    private String employee_id;
//    private "project_id":116,
//    private "project_task_id":511,
//    private "duration":"2.5000000000",
//    private "billable":2,
//    private "work_location":null,
//    private "work_location_is_project_relevant":null,
//    private "note":"Auswertung der Lasttestläufe, neu generieren und exportieren Statistiken von lava und nebu",
//    private "activity":"bearbeiten",
//    private "start":null,
//    private "destination":null,
//    private "vehicle":null,
//    private "private":null,
//    private "passengers":null,
//    private "km":null,
//    private "direction_of_travel":null,
//    private "ticket_id":null,
//    private "subtask_id":null,
//    private "invoice_item_id":null,
//    private "created":"2019-01-09T14:46:45.000000Z",
//    private "modified":"2019-04-15T10:06:39.000000Z"
}
