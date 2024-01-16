package com.gepardec.mega.domain.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceTime {

    private Integer id;
    @NonNull private String userId;
    @NonNull private LocalDate fromDate;
    private LocalDate toDate;
    private String fromTime;
    private String toTime;
    @NonNull private String reason;
    private Boolean isHalfADay;
    private Boolean accepted;
    private String comment;
    private String timezone;
    private Boolean suppressMails;
    private String created;
    private String modified;
    private Map<String, String> attributes;
}
