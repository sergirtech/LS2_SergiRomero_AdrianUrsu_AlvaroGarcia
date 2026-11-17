# Video catalog: paginated list from the database (US-04)

**Approved on:** _pending team approval_
**Approved by:** _pending — approve in the PR of `feature/video-catalog`_
**Used for:** implementing US-04 (browse video list) end to end: backend catalog stored in the database and
imported from the video store, paginated REST API, and the frontend list with pagination.

## Prompt

```
Implementa la US-04 (Browse video list) de REQUIREMENT.md, backend y frontend.

Contexto:
- Lee AGENTS.md, backend/AGENTS.md, frontend/AGENTS.md y docs/project-status.md antes de empezar.
- Ahora VideoService devuelve una lista fija y el frontend usa 5 vídeos escritos a mano en App.tsx.
- Los vídeos están en tooling/videos: un .mp4, un .webp y un .json (generado por tooling/videoGrabber) por vídeo.

Cómo lo quiero:

Backend (arquitectura hexagonal, como el resto del proyecto):
- Los vídeos se guardan en la base de datos (tabla propia), no se leen de la carpeta en cada petición.
- Al arrancar, el backend importa los .json de la carpeta de vídeos (pro_tube.store.dir). Tiene que ser
  idempotente: si un vídeo ya está, no se duplica. Así puede ir activado también en prod.
- Ignora con un aviso en el log los .json inválidos, sin título o sin su .mp4. No debe romper el arranque.
- Cada vídeo tiene un id de base de datos (el que va en las URLs) y un fileKey (el nombre de sus ficheros en la
  carpeta), para que los vídeos subidos más adelante (US-06) no choquen con los del grabber.
- Guarda título, autor, descripción, duración, ancho, alto, visualizaciones, likes y fecha de publicación.
  Tags, categorías y comentarios todavía no.
- GET /api/videos?page=&size= paginado (12 por defecto, máximo 48, ordenado por id). Si page o size están
  fuera de rango, ajústalos en vez de devolver error.
- GET /api/videos/{id} con el detalle, y 404 con JSON {message} si no existe.
- Los endpoints devuelven DTOs, nunca el dominio ni entidades JPA. El dominio sin imports de Spring ni JPA.

Frontend:
- La home carga los vídeos de la API, con skeleton mientras carga, mensaje de error con botón de reintentar y
  un botón "Load more" para la página siguiente (nada de scroll infinito).
- La página /watch/:id carga el vídeo por id desde la API, con estados de no encontrado y de error.
  Las URLs de /media se construyen con el fileKey.
- Usa fetch (los tests mockean con jest-fetch-mock). La búsqueda de momento filtra solo lo ya cargado (US-08 la
  hará en el backend). Las listas del perfil pueden seguir con los 5 vídeos fijos hasta US-03.

Calidad y proceso:
- Trabaja en una rama feature/video-catalog creada desde main. No hagas commit ni push.
- Tests para todo lo nuevo: unitarios del servicio y del controlador, adaptador JPA con @DataJpaTest, lector de
  .json con ficheros temporales, y un test HTTP de los endpoints. En el frontend, adapta los tests que dependan
  del orden de los fetch para que respondan según la URL.
- Comprueba que pasan .\mvnw.cmd clean verify (con los umbrales de JaCoCo) y npm run build, test y lint.
- Actualiza docs/architecture.md, docs/decisions.md (nueva decisión) y docs/project-status.md.
```

## Notes

* Result: decision D21 in `docs/decisions.md`; backend 67 tests with JaCoCo thresholds met; frontend 81 tests.
* Review in particular: the import runs on every startup (`pro_tube.load_initial_data=true` also in `prod`), and the
  ids of the profile sample in `App.tsx` assume a fresh database (file `0` → id `1`).
* Frontend tests mock HTTP by URL with `src/testUtils/mockApi.ts`; reuse it instead of chains of
  `mockResponseOnce`, which break when a page adds a new request.
