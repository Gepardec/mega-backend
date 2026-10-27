package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.worktime.application.port.outbound.WarningRuleSettingsPort;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningRuleSettingsServicesTest {

    @Mock
    WarningRuleSettingsPort port;

    @Test
    void list_shouldReturnAllSettingsOrderedByRule() {
        var weekend = new WarningRuleSetting(WarningRule.WEEKEND, true, null);
        var overlap = new WarningRuleSetting(WarningRule.TIME_OVERLAP, false, null);
        when(port.findAll()).thenReturn(List.of(weekend, overlap));

        assertThat(new ListWarningRuleSettingsService(port).list()).containsExactly(overlap, weekend);
    }

    @Test
    void update_shouldSaveAndReturnTheSavedSetting() {
        var setting = new WarningRuleSetting(WarningRule.MEAL_ENTRY, true, LocalDate.of(2026, 11, 1));
        when(port.save(setting)).thenReturn(setting);

        assertThat(new UpdateWarningRuleSettingService(port).update(setting)).isEqualTo(setting);
        verify(port).save(setting);
    }
}
