package com.gepardec.mega.hexagon;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * ArchUnit tests to enforce structural rules for the hexagonal backend.
 */
class HexagonalArchitectureTest {

    /*
     * adapter.inbound --> application --> domain
     * adapter.outbound -------------> domain
     *
     * Dependencies point inward only:
     * - domain must not depend on application or adapters
     * - application must not depend on adapters
     * - inbound and outbound adapters must not depend on each other
     */
    private static final DescribedPredicate<JavaClass> IMPLEMENTS_USE_CASE_INTERFACE =
            new DescribedPredicate<>("implement a *UseCase interface") {
                @Override
                public boolean test(JavaClass input) {
                    return !input.isInterface() && input.getAllRawInterfaces().stream()
                            .anyMatch(javaInterface -> javaInterface.getSimpleName().endsWith("UseCase"));
                }
            };
    private static final DescribedPredicate<JavaAnnotation<?>> JAKARTA_PERSISTENCE_ANNOTATION =
            new DescribedPredicate<>("a jakarta.persistence annotation") {
                @Override
                public boolean test(JavaAnnotation<?> input) {
                    return input.getRawType().getPackageName().startsWith("jakarta.persistence");
                }
            };

    private static JavaClasses allClasses;

    @BeforeAll
    static void setup() {
        allClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.gepardec.mega.hexagon");
    }

    @Test
    void domainMustNotDependOnApplicationLayer() {
        noClasses()
                .that().resideInAPackage("..hexagon..domain..")
                .should().dependOnClassesThat().resideInAPackage("..hexagon..application..")
                .because("the domain layer is the innermost ring and must not depend on the application layer")
                .check(allClasses);
    }

    @Test
    void domainMustNotDependOnAdapterLayer() {
        noClasses()
                .that().resideInAPackage("..hexagon..domain..")
                .should().dependOnClassesThat().resideInAPackage("..hexagon..adapter..")
                .because("the domain layer must stay free of adapter concerns")
                .check(allClasses);
    }

    @Test
    void applicationMustNotDependOnAdapterLayer() {
        noClasses()
                .that().resideInAPackage("..hexagon..application..")
                .should().dependOnClassesThat().resideInAPackage("..hexagon..adapter..")
                .because("application services should orchestrate ports instead of depending on adapters directly")
                .check(allClasses);
    }

    @Test
    void inboundAdaptersMustNotDependOnOutboundAdapters() {
        noClasses()
                .that().resideInAPackage("..hexagon..adapter..inbound..")
                .should().dependOnClassesThat().resideInAPackage("..hexagon..adapter..outbound..")
                .because("inbound adapters must communicate through ports instead of reaching into outbound adapters")
                .check(allClasses);
    }

    @Test
    void outboundAdaptersMustNotDependOnInboundAdapters() {
        noClasses()
                .that().resideInAPackage("..hexagon..adapter..outbound..")
                .should().dependOnClassesThat().resideInAPackage("..hexagon..adapter..inbound..")
                .because("outbound adapters must not depend on inbound adapter implementation details")
                .check(allClasses);
    }

    @Test
    void domainModelsMustBeRecordsOrEnums() {
        classes()
                .that().resideInAPackage("..hexagon..domain..model..")
                .and().areNotEnums()
                .and().areNotAnonymousClasses()
                .should().beAssignableTo(Record.class)
                .because("hexagon domain models should be immutable records unless they are enums")
                .check(allClasses);
    }

    @Test
    void domainModelsMustNotHaveJpaAnnotations() {
        classes()
                .that().resideInAPackage("..hexagon..domain..model..")
                .should().notBeAnnotatedWith(JAKARTA_PERSISTENCE_ANNOTATION)
                .because("jakarta.persistence annotations are adapter concerns and must not leak into hexagon domain models")
                .check(allClasses);
    }

    @Test
    void inboundPortInterfacesMustEndWithUseCase() {
        classes()
                .that().resideInAPackage("..hexagon..application..port..inbound..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("UseCase")
                .because("inbound port interfaces represent use case contracts and should end with UseCase")
                .check(allClasses);
    }

    @Test
    void inboundPortNonInterfacesMustBeRecords() {
        classes()
                .that().resideInAPackage("..hexagon..application..port..inbound..")
                .and().areNotInterfaces()
                .should().beAssignableTo(Record.class)
                .because("supporting types in inbound port packages should be immutable records")
                .check(allClasses);
    }

    @Test
    void outboundPortsMustBeInterfaces() {
        classes()
                .that().resideInAPackage("..hexagon..domain..port..outbound..")
                .should().beInterfaces()
                .because("outbound ports define contracts that adapters implement")
                .check(allClasses);
    }

    @Test
    void applicationServicesMustEndWithService() {
        classes()
                .that().resideInAPackage("..hexagon..application..")
                .and(IMPLEMENTS_USE_CASE_INTERFACE)
                .should().haveSimpleNameEndingWith("Service")
                .because("hexagon use case implementations should follow the Service naming convention")
                .check(allClasses);
    }

    @Test
    void applicationServicesMustBeApplicationScoped() {
        classes()
                .that().resideInAPackage("..hexagon..application..")
                .and(IMPLEMENTS_USE_CASE_INTERFACE)
                .should().beAnnotatedWith(ApplicationScoped.class)
                .because("hexagon use case implementations should be exposed as application-scoped beans")
                .check(allClasses);
    }

    @Test
    void jpaEntitiesInHexagonMustResideInOutboundAdapter() {
        classes()
                .that().resideInAPackage("..hexagon..")
                .and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..hexagon..adapter..outbound..")
                .because("hexagon JPA entities must be confined to outbound adapters")
                .check(allClasses);
    }

    @Test
    void jpaEntitiesInHexagonMustEndWithEntity() {
        classes()
                .that().resideInAPackage("..hexagon..adapter..outbound..")
                .and().areAnnotatedWith(Entity.class)
                .should().haveSimpleNameEndingWith("Entity")
                .because("hexagon JPA entities should follow the Entity suffix naming convention")
                .check(allClasses);
    }

    @Test
    void panacheRepositoriesMustResideInOutboundAdapter() {
        classes()
                .that().areAssignableTo(PanacheRepository.class)
                .should().resideInAPackage("..hexagon..adapter..outbound..")
                .because("Panache repositories are outbound persistence adapters and should stay in that layer")
                .check(allClasses);
    }

    @Test
    void hexagonClassesOutsideApplicationMustNotBeTransactional() {
        noClasses()
                .that().resideInAPackage("..hexagon..")
                .and().resideOutsideOfPackage("..application..")
                .should().beAnnotatedWith(Transactional.class)
                .because("hexagon transaction boundaries belong to application services, not adapters or domain code")
                .check(allClasses);

        noMethods()
                .that().areDeclaredInClassesThat().resideInAPackage("..hexagon..")
                .and().areDeclaredInClassesThat().resideOutsideOfPackage("..application..")
                .should().beAnnotatedWith(Transactional.class)
                .because("hexagon transaction boundaries should not be declared on non-application methods")
                .check(allClasses);
    }

    @Test
    void hexagonUseCaseImplementationsMustBeTransactional() {
        classes()
                .that().resideInAPackage("..hexagon..application..")
                .and(IMPLEMENTS_USE_CASE_INTERFACE)
                .should().beAnnotatedWith(Transactional.class)
                .because("use case implementations define the unit of work in the hexagonal application layer")
                .check(allClasses);
    }
}
