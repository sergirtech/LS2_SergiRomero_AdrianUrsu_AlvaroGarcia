# Authentication epic: registration and login (incremental, guided)

**Approved on:** _pending team approval_
**Approved by:** _pending — approve in the PR of `feature/auth`_
**Used for:** analysing and implementing Epic 1 (US-01 User registration, US-02 User login) step by step, with the
AI explaining each step and the developer writing the code.

## Prompt

First prompt (analysis and plan, no code changes):

```
Quiero implementar la épica de Authentication (US-01 User registration y US-02 User login) en este proyecto.

Antes de hacer cambios, analiza la estructura existente del backend Spring Boot y del frontend React + TypeScript.

Quiero trabajar de forma incremental y entender lo que implemento.

Por ahora:
- No modifiques ningún archivo.
- No escribas toda la implementación.
- Identifica cómo están organizados el backend, el frontend, la base de datos y la configuración actual de seguridad.
- Comprueba si ya existen entidades de usuario, repositorios, servicios, controladores o formularios relacionados con autenticación.
- Propón un plan de implementación dividido en tareas pequeñas, relacionando cada tarea con los criterios de aceptación de US-01 y US-02.
- Aprovecha las tecnologías y convenciones que ya existen en el proyecto.
- Señala las decisiones que debo entender y poder justificar ante el profesor.

Devuélveme el análisis y el plan, con los archivos que previsiblemente habría que crear o modificar. No implementes nada todavía.
```

Follow-up prompt, one per task of the plan (replace `<task id>`, e.g. `B4`):

```
Continuemos con la tarea <task id> del plan de la épica de Authentication.

Antes de darme el código, revisa el estado actual del proyecto (docs/project-status.md y los archivos implicados),
por si he cambiado algo respecto a lo que propusiste.

Quiero seguir aprendiendo mientras implemento, así que:
- No modifiques ningún archivo: yo escribiré el código.
- Divide la tarea en pasos pequeños y explícame el objetivo de cada uno antes del código.
- Indica qué criterios de aceptación de US-01 / US-02 cubre cada paso.
- Para cada archivo, dime la ruta exacta y si hay que crearlo o modificarlo.
- Respeta las convenciones del proyecto (AGENTS.md, backend/AGENTS.md, frontend/AGENTS.md).
- Incluye los tests necesarios y cómo comprobar que todo funciona (.\mvnw.cmd clean verify en backend/,
  npm run build / test / lint en frontend/) y alguna prueba manual si tiene sentido.
- Termina con el mensaje de commit siguiendo Conventional Commits.

Si encuentras algo en el plan que convenga cambiar, avísame y explícame por qué antes de seguir.
```

## Notes

* The plan and its task ids (B1–B8 backend, F1–F6 frontend, C1–C2 closing) are tracked in
  `docs/project-status.md`; the design decisions are in `docs/decisions.md`.
* The AI's code is a starting point: in step B3 the tests caught a missing `trim()` in the email normalization of a
  hand-written variant, so always run the tests the step provides.
* Tasks were reordered so the app keeps working after every commit: `spring-boot-starter-security` is added in the
  same step as the `SecurityFilterChain` (B5), not at the start.
* The AI's test data can be wrong too: in B6 `" Ana@Mail.com "` failed with 400 because `@Email` rejects surrounding
  spaces. The fix was in the test, not in the controller; the frontend trims the email before sending (F3).
