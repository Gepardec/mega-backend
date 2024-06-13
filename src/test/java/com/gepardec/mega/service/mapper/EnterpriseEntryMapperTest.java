package com.gepardec.mega.service.mapper;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.enterprise.EnterpriseEntry;
import com.gepardec.mega.rest.model.EnterpriseEntryDto;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class EnterpriseEntryMapperTest {
    @Inject
    EnterpriseEntryMapper mapper;

    @Test
    void map_whenEntryIsEmpty_thenReturnNull() {
        EnterpriseEntryDto actual = mapper.map(Optional.empty());
        assertThat(actual).isNull();
    }

    @Test
    void map_whenEntryIsNotEmpty_thenReturnDto() {
        EnterpriseEntry entry = new EnterpriseEntry();
        entry.setId(1L);
        entry.setZepTimesReleased(State.OPEN);
        entry.setChargeabilityExternalEmployeesRecorded(State.OPEN);
        entry.setPayrollAccountingSent(State.DONE);
        entry.setDate(LocalDate.of(2024,5,3));
        entry.setCreationDate(LocalDateTime.of(2024,4,20, 13, 20));

        EnterpriseEntryDto actual = mapper.map(Optional.of(entry));
        assertThat(actual).isNotNull();
        assertThat(actual.getDate()).isEqualTo(entry.getDate());
    }
}
