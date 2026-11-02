# backend/AGENTS.md

Rules for `backend/` only. General rules are in the root [`AGENTS.md`](../AGENTS.md).

## Stack

* Java **21** (`java.version` in `pom.xml`; CI uses Corretto 21). Build with a JDK 21 (`JAVA_HOME`): on JDK 25 the
  Mockito tests and JaCoCo 0.8.12 instrumentation fail.
* Spring Boot **3.3.3**: web, data-jpa, thymeleaf, Lombok, H2, PostgreSQL driver.
* `spring-boot-starter-security` with the filter chain in `SecurityConfig`: public `POST /api/auth/register|login`
  and `GET /api/videos/**`, `/api/hello`; the rest of `/api/**` needs a session (401, no redirect); logout at
  `POST /api/auth/logout` (204). Use `antMatcher(...)`, not string `requestMatchers`: the H2 console servlet makes
  string patterns ambiguous and the app fails to start. New public endpoints must be added to the permit list.
* The OAuth2 Google properties in `application.properties` are unused leftovers: there is no OAuth2 client
  dependency. Login is email + password (see `docs/decisions.md`).
* Tests: JUnit 5 + Mockito + MockMvc via `spring-boot-starter-test`. JUnit 4 and Cucumber 6 are also on the
  classpath — write new tests with JUnit 5 (`org.junit.jupiter.api.*`), not `org.junit.Test`.

## Commands (run from `backend/`)

* Build + all tests + coverage check: `.\mvnw.cmd clean verify`
* Single test class / method: `.\mvnw.cmd test "-Dtest=ClassName"` / `.\mvnw.cmd test "-Dtest=ClassName#method"`
* Run: `.\mvnw.cmd spring-boot:run` (defaults to `../tooling/videos/` relative to `backend/`; set
  `ENV_PROTUBE_STORE_DIR` to override the store folder)
* Coverage report: `target/site/jacoco/index.html`

Use the Maven wrapper (`mvnw.cmd`), not a global `mvn`. It is not committed yet; if it is missing, generate it with
`mvn -N io.takari:maven:wrapper`.

**Before finishing any backend change, run `.\mvnw.cmd clean verify` and make sure it passes.**

## Package structure

Base package `com.tecnocampus.LS2.protube_back`:

Hexagonal architecture (ports and adapters, `docs/decisions.md` D20). Dependencies only point inwards:
`infrastructure` → `application` → `domain`.

* `domain/` — the core, **plain Java: no Spring, JPA or Jakarta imports.**
  * `model/` — domain objects (e.g. `User`, immutable, `User.normalizeEmail`).
  * `exception/` — domain exceptions that know nothing about HTTP (`UserAlreadyExistsException`,
    `UserNotFoundException`).
* `application/` — use cases.
  * `port/in/` — input ports, one interface per use case (`RegisterUserUseCase`, `FindUserUseCase`,
    `ListVideosUseCase`). Adapters call these, never the services directly.
  * `port/out/` — output ports the application needs (`UserRepository`, `PasswordHasher`).
  * `service/` — `@Service` classes implementing the input ports and using only output ports
    (`AuthService`, `VideoService`). `@Service` is the only Spring import allowed here.
* `infrastructure/` — adapters and Spring wiring.
  * `web/` — input adapter: `@RestController`s under `/api/...`, thin, depend on input ports;
    `ApiExceptionHandler`; request/response records in `web/dto/`. Never return domain objects or JPA entities.
  * `persistence/` — output adapter: `UserJpaEntity` (table `app_user`, `user` is reserved), the package-private
    Spring Data `UserJpaRepository`, and `UserPersistenceAdapter` implementing the `UserRepository` port and
    mapping entity ↔ domain.
  * `security/` — `BCryptPasswordHasher` (implements `PasswordHasher`) and `AppUserDetailsService` (Spring Security
    loads users through `FindUserUseCase`).
  * `config/` — `MvcConfig` (`/media/**` and CORS with credentials for `/api/**` from `http://localhost:[*]`) and
    `SecurityConfig` (filter chain, `PasswordEncoder`, `AuthenticationManager`).

Adding a feature: domain model/exception → port(s) → service → adapter(s). A new external dependency (DB, file
store, mail…) gets an output port in `application/port/out/` and its implementation in `infrastructure/`.

Auth-specific rules: passwords are only stored through the `PasswordHasher` port (BCrypt); emails are normalized
with `User.normalizeEmail` (trim + lowercase) everywhere they are stored or looked up.

Tests mirror the main package under `src/test/java/...` with the `*Test` suffix (e.g.
`infrastructure/web/VideosControllerTest`).

## Testing

* Spring Boot 3.3 → mock beans with `@MockBean` (`org.springframework.boot.test.mock.mockito.MockBean`).
  `@MockitoBean` only exists from 3.4 and will not compile here.
* Domain tests: plain JUnit, no mocks (e.g. `domain/model/UserTest`).
* Controller tests: `@WebMvcTest(XController.class)` + `MockMvc` + `@MockBean` for the input ports, or plain
  Mockito unit tests.
* Service tests: plain unit tests with `@ExtendWith(MockitoExtension.class)`, `@Mock` for the output ports,
  `@InjectMocks` for the service.
* Persistence adapter tests: `@DataJpaTest` + `@Import(XPersistenceAdapter.class)` (embedded H2, rollback per
  test), e.g. `infrastructure/persistence/UserPersistenceAdapterTest`. Use `saveAndFlush` on the Spring Data
  repository when asserting a DB constraint; the `ERROR ... Unique index or primary key violation` log line in that
  test is expected.
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
