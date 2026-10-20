---
name: create-rest-endpoint
description: Create a new REST endpoint in the ProTube Spring Boot backend (controller + service call + @WebMvcTest test). Use when asked to add an API route under /api/... in backend/.
---

# Create a REST endpoint

1. Check `REQUIREMENT.md` for the user story and acceptance criteria of the endpoint.
2. Decide the resource name (e.g. `Comment`) and the route (`/api/comments`). Routes are plural, lowercase, under
   `/api`.
3. If the business logic doesn't exist, add or extend a `@Service` in
   `backend/src/main/java/com/tecnocampus/LS2/protube_back/services/`. Controllers stay thin.
4. Create the controller from [`templates/Controller.java`](templates/Controller.java) in
   `backend/src/main/java/com/tecnocampus/LS2/protube_back/controller/<Resource>Controller.java`.
5. Create the test from [`templates/ControllerTest.java`](templates/ControllerTest.java) in
   `backend/src/test/java/com/tecnocampus/LS2/protube_back/controller/<Resource>ControllerTest.java`.
6. In both templates replace `__Resource__` (PascalCase, e.g. `Comment`), `__resource__` (camelCase, e.g. `comment`)
   and `__resources__` (URL path, e.g. `comments`). Replace `String` with the real DTO type when there is one.
7. Add a test for every status code the endpoint can return (200/201, 400, 404...).
8. Run `.\mvnw.cmd clean verify` from `backend/` and make sure tests and JaCoCo thresholds pass.

Rules:

* Spring Boot is 3.3.3 → `@MockBean`, not `@MockitoBean`.
* Return `ResponseEntity<...>`; never expose JPA entities from `model/` — use records in `controller/dto/`
  (e.g. `UserResponse.from(User)`), with Bean Validation annotations on request records
  (`spring-boot-starter-validation` must be in `pom.xml`).
* Services call repositories in `repository/`; the controller never injects a repository.
* Don't touch `MvcConfig` CORS unless the route is outside `/api/**`.
