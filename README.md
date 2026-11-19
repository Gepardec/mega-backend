# mega-backend


## Building an Image

Use the GitHub Action "Release Containerimage" to build an image of a selected branch and release it as a package in this repository.
The naming convention for the image is [branch]-[commit-hash] 

The action is also triggered automatically when a commit to main occurs.

### Hotfixes

When `main` has moved on since the last deployment, a hotfix must be based on the commit that is currently running in prod
(the commit hash in the image tag in the deployment repository's prod `values.yaml`):

1. Create a branch named `hotfix/<topic>` from that commit and push it. Every push to a `hotfix/**` branch builds and releases an image
   (named [branch]-[commit-hash] as above); no manual trigger is needed. If the prod commit predates this convention, add the `hotfix/**`
   trigger from `.github/workflows/main.yaml` to the hotfix branch as well, because GitHub reads the workflow from the pushed commit.
2. Deploy that image tag to prod.
3. Open a PR from the hotfix branch to `main` so the fix is part of the next regular release.

Pull request builds never release an image: they build the merge result with `main`, which would contain the newer changes.

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
