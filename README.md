# mega-backend


## Building an Image

Use the GitHub Action "Release Containerimage" to build an image of a selected branch and release it as a package in this repository.
The naming convention for the image is [branch]-[commit-hash] 

The action is also triggered automatically when a commit to main occurs.

## Code hygiene (OpenRewrite)

[OpenRewrite](https://docs.openrewrite.org/) keeps the code from drifting away from agreed conventions: imports,
Java 21 idioms, SonarQube-style fixes, and JUnit 5 / AssertJ / Mockito test style.
The enforced recipe is `com.gepardec.mega.CodeHygiene`, defined in [`rewrite.yml`](rewrite.yml) together with the
import layout and the reasons why some recipes are left out.

| Command | What it does |
|---|---|
| `mvn rewrite:dryRun` | Reports drift as a patch in `target/rewrite/rewrite.patch`; never fails |
| `mvn rewrite:run` | Fixes drift in place |
| `mvn package -Prewrite-check` | Fails the build on drift (runs before the tests); also checked in CI |
| `mvn rewrite:dryRun -Drewrite.activeRecipes=<recipe>` | Tries any other recipe without changing code |
| `mvn rewrite:discover` | Lists all available recipes |

A normal `mvn package` does not run the check.
After upgrading the recipe versions in `pom.xml`, new rules may flag existing code: run `mvn rewrite:run` and commit the result.
