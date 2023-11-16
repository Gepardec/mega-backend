package com.gepardec.mega.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrematureEmployeeCheck {
    private long id;

    private long stepEntryId;

    private User userId;

    private LocalDate forMonth;

    private LocalDateTime creationDate;
}
