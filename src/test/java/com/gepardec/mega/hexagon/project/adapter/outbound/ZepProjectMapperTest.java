package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.zep.rest.dto.ZepBillingType;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ZepProjectMapperTest {

    private final ZepProjectMapper mapper = Mappers.getMapper(ZepProjectMapper.class);

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void toProfile_billableTrue_forBillingTypeId1And2(int billingTypeId) {
        ZepProject project = ZepProject.builder()
                .id(10)
                .name("Billable Project")
                .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .billingType(ZepBillingType.builder().id(billingTypeId).build())
                .build();

        var profile = mapper.toProfile(project);

        assertThat(profile.billable()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4})
    void toProfile_billableFalse_forBillingTypeId3And4(int billingTypeId) {
        ZepProject project = ZepProject.builder()
                .id(11)
                .name("Internal Project")
                .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .billingType(ZepBillingType.builder().id(billingTypeId).build())
                .build();

        var profile = mapper.toProfile(project);

        assertThat(profile.billable()).isFalse();
    }

    @Test
    void toProfile_billableFalse_whenBillingTypeIsNull() {
        ZepProject project = ZepProject.builder()
                .id(12)
                .name("No Billing Type")
                .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();

        var profile = mapper.toProfile(project);

        assertThat(profile.billable()).isFalse();
    }
}
