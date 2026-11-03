package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.WorkTimeWarningDto;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WorkTimeWarningRestMapperTest {

    private final WorkTimeWarningRestMapper mapper = Mappers.getMapper(WorkTimeWarningRestMapper.class);

    @ParameterizedTest
    @EnumSource(value = WorkTimeWarningType.class, names = {"INTERNAL_PREFIX_ON_BILLABLE_PROJECT", "INTERNAL_PREFIX_MISSING"})
    void toDto_shouldMapNoteConventionTypeToIdentifierWithDateAndNoHours(WorkTimeWarningType type) {
        LocalDate date = LocalDate.of(2026, 5, 4);

        WorkTimeWarningDto dto = mapper.toDto(new WorkTimeWarning(date, type, null));

        assertThat(dto.getType()).isEqualTo(type.name());
        assertThat(dto.getDate()).isEqualTo(date);
        assertThat(dto.getHours()).isNull();
    }
}
