package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Applies the real changelog to a fresh in-memory database. The shared test database cannot be used here,
 * because Hibernate's drop-and-create wipes the seeded rows after Liquibase has run.
 */
class WarningRuleSettingsSeedTest {

    private static final String URL =
            "jdbc:h2:mem:warning-rule-seed;MODE=PostgreSQL;INIT=create schema if not exists mega\\;SET SCHEMA mega";

    @Test
    void changelog_shouldSeedOneSettingPerRuleWithInitialValues() throws Exception {
        Map<WarningRule, WarningRuleSetting> byRule = new EnumMap<>(WarningRule.class);
        try (Connection connection = DriverManager.getConnection(URL, "mega", "mega")) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            // Not closed on purpose: closing Liquibase would close the connection used for the query below.
            new Liquibase("db/changelog-master.xml", new ClassLoaderResourceAccessor(), database)
                    .update(new Contexts(), new LabelExpression());
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery(
                         "select rule, enabled, active_from from worktime_warning_rule_setting")) {
                while (rows.next()) {
                    WarningRule rule = WarningRule.valueOf(rows.getString("rule"));
                    Date activeFrom = rows.getDate("active_from");
                    byRule.put(rule, new WarningRuleSetting(rule, rows.getBoolean("enabled"),
                            activeFrom == null ? null : activeFrom.toLocalDate()));
                }
            }
        }

        assertThat(byRule).containsOnlyKeys(WarningRule.values());
        LocalDate start = LocalDate.of(2026, 10, 1);
        for (WarningRule rule : WarningRule.values()) {
            boolean dated = rule == WarningRule.BILLABLE_PROJECT || rule == WarningRule.INTERNAL_NOTE
                    || rule == WarningRule.MEAL_ENTRY || rule == WarningRule.TRAVEL_ALLOWANCE;
            assertThat(byRule).as(rule.name()).containsEntry(rule, new WarningRuleSetting(rule, true, dated ? start : null));
        }
    }
}
