package com.gepardec.mega.hexagon;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean-validation problems report a body violation as {@code in: "body"} only when the resource
 * method's parameter names match the generated API interface. Otherwise the location becomes
 * {@code "?"} and the field is prefixed with the parameter name.
 */
class ResourceParameterNamesTest {

    private static final String GENERATED_API_PACKAGE = "com.gepardec.mega.hexagon.generated.api";

    @Test
    void resourcesShouldUseTheParameterNamesOfTheGeneratedApiInterfaces() throws Exception {
        List<JavaClass> resources = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.gepardec.mega.hexagon")
                .stream()
                .filter(javaClass -> !javaClass.isInterface())
                .filter(javaClass -> javaClass.getInterfaces().stream()
                        .anyMatch(api -> GENERATED_API_PACKAGE.equals(api.toErasure().getPackageName())))
                .toList();

        List<String> mismatches = new ArrayList<>();
        for (JavaClass resource : resources) {
            Class<?> resourceClass = Class.forName(resource.getName());
            for (Class<?> api : resourceClass.getInterfaces()) {
                if (!GENERATED_API_PACKAGE.equals(api.getPackageName())) {
                    continue;
                }
                for (Method apiMethod : api.getMethods()) {
                    Method implementation = resourceClass.getMethod(apiMethod.getName(), apiMethod.getParameterTypes());
                    List<String> expected = parameterNames(apiMethod);
                    List<String> actual = parameterNames(implementation);
                    if (!expected.equals(actual)) {
                        mismatches.add("%s#%s uses %s, expected %s".formatted(
                                resourceClass.getSimpleName(), apiMethod.getName(), actual, expected));
                    }
                }
            }
        }

        assertThat(resources).isNotEmpty();
        assertThat(mismatches).isEmpty();
    }

    private static List<String> parameterNames(Method method) {
        return Arrays.stream(method.getParameters()).map(Parameter::getName).toList();
    }
}
