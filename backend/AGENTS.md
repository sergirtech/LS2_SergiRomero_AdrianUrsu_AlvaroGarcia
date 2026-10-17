# backend/AGENTS.md

Rules for `backend/` only. General rules are in the root [`AGENTS.md`](../AGENTS.md).

## Stack

* Java **21** (`java.version` in `pom.xml`; CI uses Corretto 21). Build with a JDK 21 (`JAVA_HOME`): on JDK 25 the
  Mockito tests and JaCoCo 0.8.12 instrumentation fail.
* Spring Boot **3.3.3**: web, data-jpa, thymeleaf, Lombok, H2, PostgreSQL driver.
* Tests: JUnit 5 + Mockito + MockMvc via `spring-boot-starter-test`. JUnit 4 and Cucumber 6 are also on the
  classpath — write new tests with JUnit 5 (`org.junit.jupiter.api.*`), not `org.junit.Test`.

## Commands (run from `backend/`)

* Build + all tests + coverage check: `.\mvnw.cmd clean verify`
* Single test class / method: `.\mvnw.cmd test "-Dtest=ClassName"` / `.\mvnw.cmd test "-Dtest=ClassName#method"`
* Run: `.\mvnw.cmd spring-boot:run` (needs `ENV_PROTUBE_STORE_DIR` set to the absolute store folder path)
* Coverage report: `target/site/jacoco/index.html`

Use the Maven wrapper (`mvnw.cmd`), not a global `mvn`. It is not committed yet; if it is missing, generate it with
`mvn -N io.takari:maven:wrapper`.

**Before finishing any backend change, run `.\mvnw.cmd clean verify` and make sure it passes.**

## Package structure

Base package `com.tecnocampus.LS2.protube_back`:

* `controller/` — `@RestController`s mapped under `/api/...`; thin, delegate to services, return `ResponseEntity`.
* `services/` — `@Service` business logic.
* `configuration/` — Spring config (`MvcConfig`: `/media/**` and CORS for `/api/**`, `/auth/**`).
* `repository/` — to be created for Spring Data JPA repositories (does not exist yet).

Tests mirror the main package under `src/test/java/...` with the `*Test` suffix (e.g. `controller/VideosControllerTest`).

## Testing

* Spring Boot 3.3 → mock beans with `@MockBean` (`org.springframework.boot.test.mock.mockito.MockBean`).
  `@MockitoBean` only exists from 3.4 and will not compile here.
* Controller tests: `@WebMvcTest(XController.class)` + `MockMvc` + `@MockBean` for the services.
* Service tests: plain unit tests with `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`.
* `@SpringBootTest` needs `pro_tube.store.dir` and `pro_tube.load_initial_data=false` passed as properties (see
  `ProtubeBackApplicationTests`).
* JaCoCo fails the build below 50% line coverage per class or 75% instruction/line/method coverage overall.

## Formatting

The code style is defined in `eclipse-formatter.xml`. No Maven plugin applies it, so format from the IDE:

* IntelliJ IDEA: install the *Adapter for Eclipse Code Formatter* plugin and point it to `backend/eclipse-formatter.xml`,
  or import the file in *Settings → Editor → Code Style → Java → Import Scheme → Eclipse XML Profile*.
* Eclipse: *Preferences → Java → Code Style → Formatter → Import*.

Format only the files you change; don't reformat whole files in unrelated PRs.

## Don'ts

* Don't put configuration values or secrets in `application.properties`; read them from env vars.
* Don't read store files with hardcoded paths; use `pro_tube.store.dir`.
