# Guía de contribución — RedNorte

Convenciones de trabajo en equipo para el desarrollo de RedNorte
(DSY1106 — Desarrollo Fullstack III).

## Estrategia de branching: GitHub Flow

Se usa **GitHub Flow**, una variante simplificada de Git Flow adecuada
para un equipo de 2 personas y un ciclo de releases continuo (no hay
versiones paralelas que mantener):

```
main ──●──●──●──────●──●──●──→  (siempre desplegable)
            \              /
             ●──●──●──●───   feature/nombre-de-la-tarea
```

- **`main`** es la única rama de larga duración. Siempre debe estar en un
  estado funcional: si algo se rompe en `main`, es prioridad arreglarlo
  antes de seguir agregando funcionalidad nueva.
- **Ramas de feature** (`feature/`, `fix/`, `docs/`) se crean desde `main`
  para cada tarea concreta, y se integran de vuelta a `main` mediante
  Pull Request una vez que la tarea está completa y probada localmente.
- No se trabaja directamente sobre `main` para cambios que tomen más de
  una sesión o que toquen varios microservicios a la vez — eso va en una
  rama de feature, justamente para poder revisar el diff completo antes
  de integrarlo.

### Convención de nombres de rama

| Prefijo | Uso |
|---|---|
| `feature/` | Funcionalidad nueva (ej. `feature/notificaciones-push`) |
| `fix/` | Corrección de un bug (ej. `fix/cors-duplicado`) |
| `docs/` | Solo documentación (ej. `docs/arquitectura`) |
| `test/` | Solo tests, sin cambiar lógica de negocio |
| `chore/` | Mantenimiento (dependencias, configuración, limpieza) |

### Mensajes de commit

Se sigue una convención inspirada en
[Conventional Commits](https://www.conventionalcommits.org/), usada de
forma consistente en este repositorio:

```
<tipo>(<alcance>): <resumen corto en presente>

<cuerpo explicando el por qué, no solo el qué — opcional pero recomendado>
```

Tipos usados en este proyecto: `feat`, `fix`, `docs`, `test`, `ci`,
`build`, `chore`. El alcance suele ser el nombre del microservicio o el
área tocada (`eureka`, `security`, `cors`, `jacoco`, `patrones`).

Ejemplo real de este repositorio:

```
fix(cors): eliminar @CrossOrigin duplicado en 11 controllers y
CorsConfig.java de ms-consultas; el navegador rechazaba las respuestas
por header Access-Control-Allow-Origin con valores multiples
(Gateway + microservicio)
```

## Antes de abrir un Pull Request

1. **Correr los tests** del microservicio que se tocó:
   ```powershell
   docker run --rm -v "${PWD}\<microservicio>:/app" -w /app maven:3.9-eclipse-temurin-17 mvn test
   ```
2. **Levantar el stack completo** (`docker compose up --build`) y probar el
   flujo afectado de extremo a extremo, no solo el endpoint aislado.
3. Si el cambio toca seguridad o CORS, probar explícitamente **sin token**
   (debe rechazar) y **con token válido** (debe aceptar) — son los dos
   casos que más bugs reales generaron en este proyecto.

## Revisión de Pull Requests

Con un equipo de 2 personas, ambos integrantes revisan los cambios del
otro antes de fusionar a `main`, prestando especial atención a:
- Si el cambio afecta un microservicio que el otro integrante mantiene.
- Si se agregan o quitan dependencias en algún `pom.xml`.
- Si se modifican rutas del Gateway o configuración de Eureka.

## Resolución de conflictos

Cuando dos integrantes modifican el mismo archivo en paralelo (situación
real que ocurrió en este proyecto en `AdminDashboard.jsx`,
`DoctorDashboard.jsx` y `NuevaConsultaPage.jsx`), el criterio para
resolver el conflicto es:

1. **Nunca perder funcionalidad de ningún lado** — si un lado agregó
   una validación y el otro un campo nuevo, el resultado final debe
   tener ambos, no elegir uno y descartar el otro.
2. Preferir el estilo/estructura más reciente cuando el conflicto es
   puramente cosmético (clases CSS, formato de JSX).
3. Si hay duda sobre si una función fue movida a otro archivo o
   eliminada por error, **preguntar antes de resolver** — no asumir.
