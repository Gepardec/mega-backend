package com.gepardec.mega;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit tests to enforce architectural rules and conventions in the codebase.
 */
class ArchitectureTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setup() {
        // Import only production classes (excludes test classes)
        allClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.gepardec.mega");
    }

    @Test
    void entitiesInDbPackageShouldHaveEntitySuffix() {
        classes()
                .that().resideInAPackage("com.gepardec.mega.db..")
                .and().areAnnotatedWith(Entity.class)
                .should().haveSimpleNameEndingWith("Entity")
                .because("All JPA entities under com.gepardec.mega.db should follow the naming convention of ending with 'Entity'")
                .check(allClasses);
    }
}
