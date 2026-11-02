# Verification evidence

Evidence recorded on 2026-11-01. The working tree was based on `438db04` (`main`) and all upgrade changes were uncommitted when commands ran; therefore these results are local working-tree evidence, not a release/commit attestation.

Additional local Docker verification was completed later on 2026-11-01 after Docker became available. The project remains uncommitted on `feat/devops-upgrade`.

## Local checks

| Check | Command / result |
| --- | --- |
| Baseline build | `./gradlew --no-daemon clean build` → `BUILD SUCCESSFUL` on base `438db04`; original report: one test discovered, one skipped, zero executed |
| Java tests and coverage | `./gradlew --no-daemon clean build jacocoTestCoverageVerification` → `BUILD SUCCESSFUL`; builds the executable JAR, runs tests, creates the report, and passes the coverage gate |
| Framework security update | Upgraded Spring Boot from 4.0.1 to 4.0.9 after the initial critical image scan; resolved Tomcat 11.0.27, Spring Framework 7.0.10, and Thymeleaf 3.1.5.RELEASE |
| Unit test count | 16 unit tests passed (10 CartService, 1 ProductService, 4 CartController, 1 HomeController) |
| MySQL integration tests | 6 Testcontainers/MySQL tests executed and passed with Docker available, including product-name round-trip; final JUnit result was 22 discovered, 22 executed, 0 skipped, 0 failures |
| Measured application line coverage | 64 of 65 lines covered = 98.46%, excluding `model/**` and `ReShoppingCartWebAppApplication*`; gate set to 95% (floor of measured percentage minus three points) |
| Docker Compose build and startup | `docker compose up --build -d` → app and MySQL containers started; both reported healthy |
| Readiness endpoint | `curl -fsS http://localhost:8080/actuator/health/readiness` → `{"status":"UP"}` |
| Homepage smoke test | `GET http://localhost:8080/` → HTTP 200; checked `Welcome to RE Shop`, `Shopping Cart`, `Product Catalog`, `Phone case`, `Phone charger`, `Add to Cart`, `Clear Cart`, `VND`, and `Dao Nguyen` in the response |
| Runtime user and Docker health | `docker inspect --format '{{.State.Health.Status}} {{.Config.User}}' ...` → `healthy 10001` (non-root) |
| Local image size | `aws-shopping-cart-app:latest` → 593 MB after the Spring Boot security update (first measured image; no historical baseline comparison available) |
| Localization check | Source/config/documentation scan found no Vietnamese phrases or accented Vietnamese characters; checked English UI response contained no non-ASCII bytes |
| Existing database content | The local Compose volume's original sample product rows were updated to English. Other existing MySQL volumes retain their old rows because the SQL initialization script runs only when a volume is first created. |
| Service mutation check | Temporarily changed quantity condition `> 1` to `>= 1`; `decreaseProductQuantity_deletesLineWhenQuantityIsOne` failed as expected. Source was restored. |
| Controller mutation check | Temporarily changed add-cart redirect `/` to `/wrong`; `add_redirectsHomeAndDelegates` failed as expected. Source was restored. |
| Deploy rollback tests | `python3 -m unittest discover -s deploy/tests -v` → 8 tests passed: first deploy, automatic rollback, no-prior-image cleanup, unhealthy rollback, new-container start failure, rollback start failure, image pull failure preserving current container, and invalid image tag |
| Shell syntax | `bash -n deploy/deploy.sh` → passed |
| Shell lint | `shellcheck deploy/deploy.sh` → passed |
| Workflow lint | `actionlint .github/workflows/ci-cd.yaml .github/workflows/rollback.yaml` → passed |
| Terraform formatting/validation | `terraform fmt -check -recursive` and `terraform -chdir=infra validate` → passed; `terraform -chdir=infra init -backend=false` downloaded AWS provider v6.71.0 |
| Trivy filesystem scan | `trivy fs --scanners vuln,secret,misconfig --severity CRITICAL,HIGH --ignore-unfixed --ignorefile .trivyignore --exit-code 1 .` → passed with zero reported findings at the selected severity; AWS-0104 for the remaining HTTPS egress is an explicit documented exception |
| Initial Trivy image scan | `trivy image --severity CRITICAL --ignore-unfixed --exit-code 1 aws-shopping-cart-app:latest` → failed with 15 CRITICAL findings in the original Spring Boot 4.0.1 dependency set |
| Updated Trivy image scan | After upgrading Spring Boot to 4.0.9 and rebuilding: same Trivy image command → passed with zero CRITICAL findings across Ubuntu OS packages, `app.jar`, and `pebble` |
| YAML syntax | Ruby `YAML.load_file` parsed both workflows, Dependabot config, Compose config, and application config |
| Git whitespace | `git diff --check` → passed |
| Action pins | GitHub public API confirmed the configured action refs resolve to commit objects; `script_path` support was checked in the pinned SSH action metadata |

## NOT VERIFIED

- GitHub Actions, EC2 connectivity, AWS deployment, and rollback against real infrastructure: not run.
- Live Docker behavior is verified only on the current local Compose setup; cloud behavior is not verified.
- Repository/MySQL mutation check: not run.
- Repository licensing and permission to redistribute `daonguyendev`'s original application: not confirmed.
- Image size before/after, mutation test of real repository/MySQL behavior, screenshots, and a public demo URL: not available.

## Owner evidence TODOs

- `TODO(owner)`: record the first successful GitHub Actions run URL and SHA.
- `TODO(owner)`: record an EC2 health probe response and a successful `GET /` plus cart operation.
- `TODO(owner)`: demonstrate rollback on an owner-controlled environment and attach the relevant Actions log.
- `TODO(owner)`: after configuring an AWS account, review a real Terraform plan and costs before deciding whether to apply. No plan or cloud operation was run in this session.
- `TODO(owner)`: confirm publication rights and add an appropriate license before promoting the public repository.
