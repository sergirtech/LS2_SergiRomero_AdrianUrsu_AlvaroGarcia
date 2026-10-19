---
applyTo: "backend/src/test/**/*.java"
---

# Backend test rules

* Use JUnit 5 only: `org.junit.jupiter.api.Test` and `org.junit.jupiter.api.Assertions` (or AssertJ). Don't import
  `org.junit.Test` or `org.junit.Assert` (JUnit 4 is on the classpath by accident).
* Spring Boot is 3.3.3: mock Spring beans with `@MockBean` from `org.springframework.boot.test.mock.mockito`.
  Don't use `@MockitoBean` (3.4+).
* Controllers: `@WebMvcTest(XController.class)` + `@Autowired MockMvc` + `@MockBean` services. Assert HTTP status
  and JSON body (`jsonPath`), not only that the call doesn't throw.
* Services: `@ExtendWith(MockitoExtension.class)` with `@Mock` / `@InjectMocks`; no Spring context. Mock
  `PasswordEncoder` instead of hashing for real.
* Repositories: `@DataJpaTest` with real queries on the embedded H2; use `saveAndFlush` to trigger DB constraints.
* DTO validation rules: plain unit test with `Validation.buildDefaultValidatorFactory().getValidator()`.
* `@SpringBootTest` only when the full context is really needed, passing
  `"pro_tube.store.dir=..."` and `"pro_tube.load_initial_data=false"` as properties.
* Mirror the main package and name the class `<ClassUnderTest>Test`; test method names describe the behaviour
  (e.g. `getVideosReturnsAllVideos`).
* Cover error paths too (404, 400): JaCoCo requires ≥50% lines per class and ≥75% overall.
* Tests must not depend on the real video store folder or on `ENV_PROTUBE_*` env vars.
