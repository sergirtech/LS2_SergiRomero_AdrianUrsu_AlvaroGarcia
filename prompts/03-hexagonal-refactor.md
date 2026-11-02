  # Backend refactor to hexagonal architecture

**Approved on:** _pending team approval_
**Approved by:** _pending — approve in the PR of `feature/hexagonal-refactor`_
**Used for:** evaluating and applying a refactor of the Spring Boot backend from layered (controller → service →
repository) to hexagonal architecture (ports and adapters), without changing the API behaviour.

## Prompt

First prompt (analysis and plan, no code changes):

```
Voy a pasar el backend Spring Boot de este proyecto a arquitectura hexagonal (puertos y adaptadores).

Antes de hacer cambios:
- No modifiques ningún archivo.
- Analiza la estructura actual del backend (paquetes, clases, dependencias entre capas y tests).
- Identifica qué partes dependen de frameworks (JPA, Spring Security, HTTP) y cuáles son lógica de negocio.
- Propón la estructura de paquetes destino (domain, application con puertos de entrada y salida, infrastructure con
  los adaptadores) y qué habría que mover, crear o adaptar.
- Estima el esfuerzo y señala los riesgos (tests, cobertura de JaCoCo, ramas abiertas del equipo, documentación).

Devuélveme el análisis y el plan. No implementes nada todavía.
```

Second prompt (apply the refactor):

```
Aplica el refactor a arquitectura hexagonal que has propuesto.

- Trabaja en una rama nueva creada desde main (feature/hexagonal-refactor). No hagas commit ni push.
- No cambies el comportamiento de la API: mismos endpoints, códigos de estado y JSON.
- El dominio debe ser Java puro, sin imports de Spring, JPA ni Jakarta.
- Los controladores dependen de puertos de entrada y los servicios de puertos de salida.
- Mueve los archivos con git mv para conservar el historial.
- Adapta los tests existentes y añade los necesarios para las clases nuevas.
- Respeta las convenciones del proyecto (AGENTS.md, backend/AGENTS.md).
- Comprueba que .\mvnw.cmd clean verify pasa, incluidos los umbrales de JaCoCo.
- Actualiza AGENTS.md, backend/AGENTS.md, docs/architecture.md, docs/decisions.md y docs/project-status.md.
```

## Notes

* Resulting layout and rules: `backend/AGENTS.md` ("Package structure"), `docs/architecture.md`, decision D20 in
  `docs/decisions.md`.
* Result: `.\mvnw.cmd clean verify` passes (47 tests, JaCoCo thresholds met).
* Review in particular: `UserNotFoundException` → 401 mapping in `ApiExceptionHandler`, and that the domain package
  has no framework imports.
* Uncommitted changes from other branches are carried over when creating the new branch; keep them out of the
  refactor commit.
