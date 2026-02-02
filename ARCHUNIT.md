# ArchUnit Setup and Usage

This project uses [ArchUnit](https://www.archunit.org/) to enforce architectural rules and coding conventions.

## What is ArchUnit?

ArchUnit is a library for checking the architecture of Java code using JUnit tests. It allows you to define and enforce
architectural constraints and best practices in your codebase.

## Setup

ArchUnit has been added as a test dependency in `pom.xml`:

```xml

<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

## Current Architecture Rules

### Test Package Structure Rule

**Rule:** Test classes should reside in the same package as the classes they test.

**Location:** `src/test/java/com/gepardec/mega/architecture/ArchitectureTest.java`

**Description:** This rule ensures that test classes maintain the same package structure as their corresponding
production classes. In Maven projects, test classes are in `src/test/java` directory but should have the same package
name as their production counterparts in `src/main/java`. This convention:

- Makes it easier to locate tests for a given class
- Keeps the codebase organized and maintainable
- Follows Java testing best practices

**Example:**

- ✅ **Correct:** If `WorkerResource.java` is in `com.gepardec.mega.rest.api`, then `WorkerResourceTest.java` should also
  be in `com.gepardec.mega.rest.api`
- ❌ **Incorrect:** If `WorkerResource.java` is in `com.gepardec.mega.rest.api`, but `WorkerResourceTest.java` is in
  `com.gepardec.mega.rest`

## Running ArchUnit Tests

### Run all tests (including ArchUnit tests)

```bash
mvn test
```

### Run only ArchUnit tests

```bash
mvn test -Dtest=ArchitectureTest
```

## Adding New Architecture Rules

To add new architecture rules:

1. Open `src/test/java/com/gepardec/mega/architecture/ArchitectureTest.java`
2. Add a new `@Test` method
3. Use ArchUnit's fluent API to define your rule

### Example: No classes should use System.out.println

```java

@Test
void noClassesShouldUseSystemOutPrintln() {
    noClasses()
            .should().callMethod(System.class, "println", String.class)
            .because("Use proper logging instead of System.out.println")
            .check(allClasses);
}
```

### Example: Services should only be accessed by controllers

```java

@Test
void servicesShouldOnlyBeAccessedByControllersOrServices() {
    classes()
            .that().resideInAPackage("..service..")
            .should().onlyBeAccessed().byAnyPackage("..rest..", "..service..")
            .check(allClasses);
}
```

## Common ArchUnit Patterns

### Package Dependencies

```java
// No cycles in package dependencies
slices().

matching("com.gepardec.mega.(*)..").

should().

beFreeOfCycles();

// Layered architecture
layeredArchitecture()
    .

consideringAllDependencies()
    .

layer("Rest").

definedBy("..rest..")
    .

layer("Service").

definedBy("..service..")
    .

layer("Persistence").

definedBy("..db..")
    .

whereLayer("Rest").

mayNotBeAccessedByAnyLayer()
    .

whereLayer("Service").

mayOnlyBeAccessedByLayers("Rest")
    .

whereLayer("Persistence").

mayOnlyBeAccessedByLayers("Service");
```

### Naming Conventions

```java
// All classes ending with "Service" should be in the service package
classes().

that().

haveSimpleNameEndingWith("Service")
    .

should().

resideInAPackage("..service..")
    .

check(allClasses);
```

### Annotation Rules

```java
// All REST endpoints should have @Path annotation
classes().

that().

resideInAPackage("..rest..")
    .

and().

arePublic()
    .

should().

beAnnotatedWith(Path .class)
    .

check(allClasses);
```

## Resources

- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [ArchUnit Examples](https://github.com/TNG/ArchUnit-Examples)
- [ArchUnit API Documentation](https://javadoc.io/doc/com.tngtech.archunit/archunit/latest/index.html)
