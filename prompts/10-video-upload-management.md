# Video upload and management (US-06, US-07)

**Approved on:** _pending team approval_
**Approved by:** _pending — approve in the PR of `feature/video-upload`_
**Used for:** implementing US-06 (upload a video) and US-07 (edit video metadata) end to end, porting the useful
parts of the `US_division` branch onto the video catalog model already in `main` (D21).

## Prompt

```
Implementa la US-06 (Upload a video) y la US-07 (Edit video metadata) de REQUIREMENT.md, backend y frontend,
en una rama feature/video-upload creada desde main.

Contexto:
- Lee AGENTS.md, backend/AGENTS.md, frontend/AGENTS.md y docs/project-status.md antes de empezar.
- El catálogo (US-04, US-05) ya está en main: tabla video con id y fileKey, importada de los .json del
  videoGrabber, y los ficheros se sirven por /media/{fileKey}.mp4. Ese modelo es el que manda.
- La rama US_division tiene una versión hecha con otro modelo (bytes del mp4 en la base de datos). Úsala solo como
  referencia: no copies su listado, su reproducción ni VideoContentJpaEntity.

Backend (hexagonal, como el resto):
- El vídeo subido se guarda en la carpeta del store (pro_tube.store.dir) con un fileKey nuevo que no choque con los
  del grabber, y su fila en la misma tabla video. En la base de datos solo metadatos.
- Añade al vídeo propietario (null para los importados), tags y categoría.
- POST /api/videos multipart (file, title, description, tags, category) con sesión. Solo .mp4, máximo 100 MB y que
  el contenido sea MP4 de verdad. Si algo falla no puede quedar nada publicado a medias.
- PUT /api/videos/{id} para título, descripción, tags y categoría, y DELETE /api/videos/{id} que borra también el
  fichero. Solo el dueño: a cualquier otro, 403. Los vídeos importados no los puede tocar nadie.
- GET /api/videos/mine con los vídeos del usuario, el más nuevo primero.
- Reglas de validación en el dominio, errores en JSON {message} (400, 403, 413, 500 sin detalles internos).

Frontend:
- /upload con el formulario (fichero, título, descripción, tags separados por comas, categoría). Un visitante va a
  /login. Al terminar, abre el vídeo nuevo.
- /my-videos con mis vídeos: editar los metadatos y borrar con confirmación (sin window.confirm).
- WatchPage muestra descripción, categoría y tags (el criterio de US-07 pide verlos en el detalle) y un enlace
  "Edit video" solo para el dueño.
- Validación en cliente con los mismos límites que el backend, pero el backend manda.

Calidad y proceso:
- No hagas commit ni push.
- Tests de dominio, servicio, adaptador JPA, almacén de ficheros (carpeta temporal), controller y un test HTTP con
  la cadena de seguridad real. En el frontend, tests de las páginas nuevas con testUtils/mockApi.
- Comprueba .\mvnw.cmd clean verify (JaCoCo) y npm run build, test y lint.
- Actualiza docs/architecture.md, docs/decisions.md (nueva decisión) y docs/project-status.md.
```

## Notes

* Result: decision D22 in `docs/decisions.md`; backend 115 tests with JaCoCo thresholds met; frontend 99 tests.
* Review in particular: a new upload appears at the end of the catalog (ordered by `id`), uploads have no thumbnail,
  and deleting a video must also delete comments and playlist entries once those branches are merged.
* Numbered `10` so it doesn't clash with the `04`–`09` prompts of the `US_division` branch.
