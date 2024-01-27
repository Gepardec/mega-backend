package com.gepardec.mega.domain.model;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrematureEmployeeCheck {
    private Long id;

    private User user;

    private String reason;

    private PrematureEmployeeCheckState state;

    private LocalDate forMonth;
}
