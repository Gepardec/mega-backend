# ArchUnit Quick Reference

## Quick Start

```bash
# Run architecture tests
mvn test -Dtest=ArchitectureTest

# Run all tests
mvn test
```

## Current Rule: Test Package Alignment

**What it checks:** Test classes must be in the same package as the classes they test.

**Example:**

```
✅ CORRECT:
   Main class:  com.gepardec.mega.service.EmployeeService
   Test class:  com.gepardec.mega.service.EmployeeServiceTest

❌ WRONG:
   Main class:  com.gepardec.mega.service.api.EmployeeService
   Test class:  com.gepardec.mega.service.EmployeeServiceTest
```

## Files Changed

1. **`pom.xml`** - Added ArchUnit dependency
2. **`src/test/java/com/gepardec/mega/architecture/ArchitectureTest.java`** - New test class
3. **`ARCHUNIT.md`** - Detailed documentation
4. **`ARCHUNIT_SETUP_SUMMARY.md`** - Implementation summary

## Common Commands

```bash
# Navigate to project
cd /Users/olivertod/dev/repos/mega-backend

# Run only architecture tests
mvn test -Dtest=ArchitectureTest

# Run tests with verbose output
mvn test -Dtest=ArchitectureTest -X

# Clean and test
mvn clean test
```

## When Tests Fail

ArchUnit tests fail with detailed messages showing:

- Which test class violated the rule
- Current package location
- Expected package location
- Suggestion to fix

## Adding More Rules

Edit `ArchitectureTest.java` and add new `@Test` methods. See `ARCHUNIT.md` for examples.

## Further Reading

- Full documentation: `ARCHUNIT.md`
- Implementation details: `ARCHUNIT_SETUP_SUMMARY.md`
- Official docs: https://www.archunit.org/
