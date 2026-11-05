package com.gepardec.mega.hexagon.shared.domain.error;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Guards the API's error codes. Codes are derived from enum constant names, so renaming a constant changes
 * the code clients switch on. {@link #codes_shouldMatchPublishedCatalogue()} pins every code with its category,
 * which decides the HTTP status: update it only when a code or status is intentionally changed.
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
        // The category decides the HTTP status, so it is pinned together with the code.
        Map<String, ErrorCategory> catalogue = allErrorCodes.stream()
                .collect(Collectors.toMap(ErrorCode::code, ErrorCode::category));

        assertThat(catalogue)
                .containsOnly(
                        entry("FORBIDDEN", ErrorCategory.FORBIDDEN),
                        entry("MONTHEND_TASK_NOT_FOUND", ErrorCategory.NOT_FOUND),
                        entry("MONTHEND_CLARIFICATION_NOT_FOUND", ErrorCategory.NOT_FOUND),
                        entry("MONTHEND_ACTOR_NOT_AUTHORIZED", ErrorCategory.FORBIDDEN),
                        entry("MONTHEND_CLARIFICATION_CLOSED", ErrorCategory.CONFLICT),
                        entry("MONTHEND_EMPLOYEE_CONTEXT_NOT_FOUND", ErrorCategory.UNPROCESSABLE),
                        entry("MONTHEND_EMPLOYEE_NOT_ASSIGNED_TO_PROJECT", ErrorCategory.UNPROCESSABLE),
                        entry("MONTHEND_PROJECT_CONTEXT_NOT_FOUND", ErrorCategory.UNPROCESSABLE),
                        entry("MONTHEND_NO_ELIGIBLE_PROJECT_LEAD", ErrorCategory.UNPROCESSABLE),
                        entry("MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE", ErrorCategory.UNPROCESSABLE),
                        entry("PROJECT_NOT_FOUND", ErrorCategory.NOT_FOUND),
                        entry("PROJECT_LEISTUNGSNACHWEIS_NOT_APPLICABLE", ErrorCategory.CONFLICT),
                        entry("WORKTIME_USER_NOT_FOUND", ErrorCategory.UNPROCESSABLE),
                        entry("USER_INTERNAL_RATES_EMPTY_FILE", ErrorCategory.INVALID),
                        entry("USER_INTERNAL_RATES_BAD_FORMAT", ErrorCategory.INVALID),
                        entry("USER_INTERNAL_RATES_UNKNOWN_USERS", ErrorCategory.UNPROCESSABLE)
                );
    }
}
