package com.gepardec.mega.zep.rest.entity;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZepAbsence {

    /**
     * {
     *   "data": {
     *     "id": 1275,
     *     "employee_id": "032-melBanna",
     *     "type": "0",
     *     "start_date": "2019-10-01",
     *     "end_date": "2019-10-01",
     *     "hours": null,
     *     "from": null,
     *     "to": null,
     *     "note": "Kopfweh, Übelkeit",
     *     "approved": true,
     *     "timezone": "Europe\/Vienna",
     *     "created": null,
     *     "modified": null,
     *     "absenceReason": null,
     *     "leaveApprovalApplication": null
     *   }
     * }
     */

    private Integer id;
    private String employeeId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double hours;
    private LocalTime from;
    private LocalTime to;
    private String note;
    private boolean approved;
    private String timezone;
    private String created;
    private String modified;
    private String absenceReason;
}
