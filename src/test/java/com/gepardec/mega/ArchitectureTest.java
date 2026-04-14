package com.gepardec.mega;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * ArchUnit tests to enforce architectural rules and conventions in the codebase.
 */
class ArchitectureTest {

    private static final DescribedPredicate<JavaClass> IMPLEMENTS_USE_CASE_INTERFACE =
            new DescribedPredicate<>("implement a *UseCase interface") {
                @Override
                public boolean test(JavaClass input) {
                    return !input.isInterface() && input.getAllRawInterfaces().stream()
                            .anyMatch(javaInterface -> javaInterface.getSimpleName().endsWith("UseCase"));
                }
            };

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

    @Test
    void restClassesShouldNotUseRepositories() {
        noClasses()
                .that().resideInAPackage("com.gepardec.mega.rest..")
                .should().dependOnClassesThat().areAssignableTo(PanacheRepository.class)
                .because("REST layer classes should not directly use Repository classes. Use service layer instead.")
                .check(allClasses);
    }

    @Test
    void domainPackageShouldNotDependOnOtherPackages() {
        noClasses()
                .that().resideInAPackage("com.gepardec.mega.domain..")
                .should().dependOnClassesThat(
                        resideInAPackage("com.gepardec.mega..")
                                .and(resideOutsideOfPackages("com.gepardec.mega.domain.."))
                )
                .because("Domain package should remain pure and independent. " +
                        "It should only depend on other domain classes, not on other application packages. ")
                .check(allClasses);
    }

    @Test
    void hexagonPackageShouldNotDependOnLegacyPackages() {
        noClasses()
                .that().resideInAPackage("com.gepardec.mega.hexagon..")
                .should().dependOnClassesThat(
                        resideInAnyPackage(
                                "com.gepardec.mega.application..",
                                "com.gepardec.mega.domain..",
                                "com.gepardec.mega.service..",
                                "com.gepardec.mega.rest..",
                                "com.gepardec.mega.db.."
                        )
                )
                .because("Hexagon code should own its boundaries instead of depending on legacy layered packages.")
                .check(allClasses);
    }

    @Test
    void hexagonClassesOutsideApplicationShouldNotBeTransactional() {
        noClasses()
                .that().resideInAPackage("com.gepardec.mega.hexagon..")
                .and().resideOutsideOfPackage("..application..")
                .should().beAnnotatedWith(Transactional.class)
                .because("Hexagon transaction boundaries belong to application services, not adapters or domain code.")
                .check(allClasses);

        noMethods()
                .that().areDeclaredInClassesThat().resideInAPackage("com.gepardec.mega.hexagon..")
                .and().areDeclaredInClassesThat().resideOutsideOfPackage("..application..")
                .should().beAnnotatedWith(Transactional.class)
                .because("Hexagon transaction boundaries should not be declared on non-application methods.")
                .check(allClasses);
    }

    @Test
    void hexagonUseCaseImplementationsShouldBeTransactional() {
        classes()
                .that().resideInAPackage("com.gepardec.mega.hexagon..application..")
                .and(IMPLEMENTS_USE_CASE_INTERFACE)
                .should().beAnnotatedWith(Transactional.class)
                .because("Use case implementations define the unit of work in the hexagonal application layer.")
                .check(allClasses);
    }
}
