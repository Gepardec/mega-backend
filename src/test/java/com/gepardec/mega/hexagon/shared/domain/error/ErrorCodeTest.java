package com.gepardec.mega.hexagon.shared.domain.error;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the API's error codes. Codes are derived from enum constant names, so renaming a constant changes
 * the code clients switch on. {@link #codes_shouldMatchPublishedCatalogue()} pins every code: update its list
 * only when a code is intentionally added, renamed or removed.
 */
class ErrorCodeTest {

    private static List<ErrorCode> allErrorCodes;

    @BeforeAll
    static void collectErrorCodes() {
        allErrorCodes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages("com.gepardec.mega.hexagon")
                .stream()
                .filter(JavaClass::isEnum)
                .filter(javaClass -> javaClass.isAssignableTo(ErrorCode.class))
                .map(JavaClass::reflect)
                .flatMap(enumClass -> Arrays.stream(enumClass.getEnumConstants()))
                .map(ErrorCode.class::cast)
                .toList();
    }

    @Test
    void codes_shouldBeUnique() {
        List<String> codes = allErrorCodes.stream().map(ErrorCode::code).toList();

        assertThat(codes).doesNotHaveDuplicates();
    }

    @Test
    void codes_shouldBeUpperSnakeCase() {
        assertThat(allErrorCodes)
                .extracting(ErrorCode::code)
                .allMatch(code -> code.matches("[A-Z][A-Z0-9]*(_[A-Z0-9]+)*"));
    }

    @Test
    void codes_shouldMatchPublishedCatalogue() {
        assertThat(allErrorCodes)
                .extracting(ErrorCode::code)
                .containsExactlyInAnyOrder(
                        "FORBIDDEN",
                        "MONTHEND_TASK_NOT_FOUND",
                        "MONTHEND_CLARIFICATION_NOT_FOUND",
                        "MONTHEND_ACTOR_NOT_AUTHORIZED",
                        "MONTHEND_CLARIFICATION_CLOSED",
                        "MONTHEND_EMPLOYEE_CONTEXT_NOT_FOUND",
                        "MONTHEND_EMPLOYEE_NOT_ASSIGNED_TO_PROJECT",
                        "MONTHEND_PROJECT_CONTEXT_NOT_FOUND",
                        "MONTHEND_VALIDATION_FAILED",
                        "PROJECT_NOT_FOUND",
                        "PROJECT_LEISTUNGSNACHWEIS_NOT_APPLICABLE",
                        "PROJECT_ACTOR_NOT_LEAD",
                        "WORKTIME_USER_NOT_FOUND",
                        "WORKTIME_VALIDATION_FAILED",
                        "USER_UNKNOWN_USERS"
                );
    }
}
