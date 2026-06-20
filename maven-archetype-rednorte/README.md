# rednorte-microservicio-archetype

Arquetipo Maven personalizado del equipo RedNorte. Genera la estructura base
de un nuevo microservicio Spring Boot ya configurado con todo lo que el resto
del proyecto ya tiene resuelto: Eureka client, Spring Security + JWT
(validación liviana, sin generar tokens), Swagger/OpenAPI, JaCoCo, y la
separación de capas (`controller`/`service`/`repository`/`model`/`security`)
usada en los otros 10 microservicios.

Basado en la estructura real de `ms-agenda-medica`, generado con
`mvn archetype:create-from-project` y luego limpiado/parametrizado a mano.

## Qué incluye un microservicio generado con este arquetipo

- `pom.xml` con Spring Boot 3.5.13, Spring Cloud 2025.0.0, y las dependencias
  que ya usan los demás microservicios: Eureka client, Spring Security,
  `jjwt` (validación de JWT), MySQL connector, springdoc-openapi, JaCoCo.
- `security/JwtUtilCompartido.java` y `security/JwtAuthFilter.java`: validan
  el token emitido por `ms-usuarios` sin consultar ninguna base de datos.
- `security/SecurityConfig.java`: exige JWT en todos los endpoints salvo
  Swagger, actuator y el preflight CORS (el CORS real lo maneja el Gateway).
- `application.properties` con Eureka, Swagger y JPA ya configurados —
  solo falta ajustar `server.port` y el nombre de la base de datos
  (quedan marcados con `8XXX` y `db_NOMBRE` como placeholders explícitos).
- `Dockerfile` multi-stage (build con Maven, runtime con JRE) — el puerto
  expuesto también queda como placeholder (`8XXX`).

## Cómo instalar el arquetipo (una sola vez)

```powershell
cd C:\Users\katty\Documents\backend-RedNorte\maven-archetype-rednorte
docker run --rm -v "${PWD}:/app" -v rednorte-m2:/root/.m2 -w /app `
  maven:3.9-eclipse-temurin-17 mvn -B install
```

Esto compila e instala el arquetipo en el repositorio Maven local
(volumen Docker `rednorte-m2`, persistente entre ejecuciones).

## Cómo generar un microservicio nuevo

**Importante:** por un bug conocido de Maven al combinarse con el bind-mount
de Docker Desktop en Windows (`archetype:generate` no reconoce un directorio
montado como válido para crear el POM implícito — falla con
`MissingProjectException` incluso con el arquetipo oficial de Apache), el
proyecto se debe generar **dentro** del contenedor y copiarlo afuera después,
en el mismo comando:

```powershell
cd C:\Users\katty\Documents\backend-RedNorte
docker run --rm -v "${PWD}:/out" -v rednorte-m2:/root/.m2 `
  maven:3.9-eclipse-temurin-17 bash -c "
    mkdir -p /tmp/gen && cd /tmp/gen &&
    mvn -B archetype:generate \
      -DarchetypeGroupId=com.rednorte \
      -DarchetypeArtifactId=rednorte-microservicio-archetype \
      -DarchetypeVersion=1.0.0 \
      -DgroupId=com.rednorte \
      -DartifactId=ms-NUEVO-SERVICIO \
      -Dversion=0.0.1-SNAPSHOT \
      -Dpackage=com.rednorte.msNuevoServicio &&
    cp -r /tmp/gen/ms-NUEVO-SERVICIO /out/
  "
```

Reemplaza `ms-NUEVO-SERVICIO` y `com.rednorte.msNuevoServicio` por el nombre
real del microservicio. El proyecto generado aparece en
`backend-RedNorte/ms-NUEVO-SERVICIO/`, listo para:

1. Asignarle un puerto libre (ver `contexto-clave.md`) en `application.properties`
2. Nombrar su base de datos (reemplazar `db_NOMBRE`)
3. Agregarlo a `docker-compose.yml`
4. Agregar su ruta en `api-gateway/application.yml`

## Verificación realizada

Se generó un microservicio de prueba (`ms-prueba-generada`) y se confirmó que:
- El `pom.xml` queda con `groupId`/`artifactId`/`version`/`name` correctos
  y todas las dependencias intactas.
- Las clases Java (`controller`, `model`, `repository`, `security`) quedan
  con el `package` correcto.
- `application.properties` sustituye `spring.application.name` automáticamente
  y deja explícitos los placeholders que sí requieren decisión manual.
