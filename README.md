# RedNorte — Sistema de Gestión de Clínicas y Consultas Médicas

Sistema de gestión de consultas médicas, fichas clínicas, reasignación de citas y
administración de establecimientos de salud, construido con una arquitectura de
microservicios en Java Spring Boot, frontend en React y orquestación completa
mediante Docker Compose.

Proyecto desarrollado como caso semestral para la asignatura **Desarrollo Fullstack III**.

## Integrantes

- **Kevin Joan Michel Pinochet Requena** — Jefe de proyecto
- **Taylor Andrey Medolphe Forero** — Desarrollo

## Arquitectura

El sistema está compuesto por **10 microservicios independientes**, un servidor de
descubrimiento (Eureka) y un API Gateway que centraliza el enrutamiento, la
seguridad y el patrón Circuit Breaker.

```
backend-RedNorte/
├── eureka-server/           Service Discovery                    → :8761
├── api-gateway/              Gateway + Circuit Breaker + Eureka UI → :8090
├── ms-bff/                   Backend For Frontend (dashboard admin) → :8091
├── Backend-usuarios/         Usuarios, roles, login, JWT, BCrypt    → :8080
├── ms-consultas/             CRUD de consultas médicas              → :8083
├── Backend_reasignacion/     Cancelación y reasignación automática  → :8082
├── ms-ficha-medica/          Ficha clínica digital                  → :8084
├── ms-notificaciones/        Notificaciones a pacientes              → :8092
├── ms-establecimientos/      CRUD de establecimientos de salud       → :8093
├── ms-agenda-medica/         Bloques de agenda médica                → :8094
├── ms-estadisticas/          Métricas y reportes agregados            → :8095
├── ms-auditoria/             Registro de auditoría del sistema       → :8096
├── init-db/                  Script SQL de inicialización (01-init.sql)
├── 02 a 07-seed-*.sql        Seeds de datos de prueba (carga manual)
└── docker-compose.yml        Orquestación de los 12 contenedores + MySQL
```

## Tecnologías

| Capa            | Tecnología                                                          |
|-----------------|----------------------------------------------------------------------|
| Backend         | Java 17 · Spring Boot 3.5.13 (Spring Cloud 2025.0.0) · Lombok        |
| Seguridad       | Spring Security · JWT (HS256, librería `jjwt`) · BCrypt              |
| Documentación   | springdoc-openapi / Swagger UI                                       |
| Service Mesh    | Netflix Eureka (Service Discovery) · Spring Cloud Gateway             |
| Resiliencia     | Resilience4j (Circuit Breaker)                                       |
| Testing         | JUnit 5 · Mockito · JaCoCo (reporte de cobertura)                    |
| CI/CD           | GitHub Actions (test → build → imagen Docker)                        |
| Base de datos   | MySQL 8.4 (una base de datos por microservicio, utf8mb4)             |
| Frontend        | React 18 · React Router 6                                            |
| Infraestructura | Docker · Docker Compose                                              |

> `Backend-usuarios` es la excepción: corre en **Spring Boot 4.0.6** y no está
> registrado en Eureka (se enruta por hostname fijo del contenedor en el Gateway).
> El resto de los microservicios sí usa Service Discovery real (`lb://` en las
> rutas del Gateway).

## Seguridad

- **Login** (`POST /usuarios/login`) valida credenciales contra `db_usuarios`
  (contraseñas hasheadas con BCrypt) y devuelve un **JWT firmado con HS256**,
  válido por 8 horas, con los claims `sub` (mail), `rol` y `id`.
- **Todos los microservicios** (salvo Swagger, Actuator y el propio login) exigen
  el header `Authorization: Bearer <token>` para responder.
- Los microservicios "satélite" (`ms-consultas`, `ms-ficha-medica`,
  `ms-reasignacion`, etc.) **no generan tokens ni consultan la base de datos de
  usuarios**: solo validan la firma y expiración del JWT emitido por
  `ms-usuarios`, usando la misma clave secreta compartida.
- Las llamadas servidor-a-servidor (`ms-bff` → `ms-consultas`/`ms-usuarios`,
  `ms-estadisticas` → 4 microservicios, `ms-reasignacion` → `ms-consultas`)
  reenvían el token JWT del usuario original; sin esto, esas llamadas internas
  recibirían 401 ahora que la seguridad está activa en todos los servicios.

## Service Discovery (Eureka)

`eureka-server` (puerto 8761) actúa como registro central. El dashboard también
es accesible a través del Gateway en:

```
http://localhost:8090/eureka-ui/
```

Microservicios registrados en Eureka: `ms-bff`, `ms-notificaciones`,
`ms-establecimientos`, `ms-agenda-medica`, `ms-auditoria`, `ms-estadisticas`,
`ms-consultas`, `ms-ficha-medica`, `ms-reasignacion`, `api-gateway`.
`ms-usuarios` queda fuera por la incompatibilidad de versión mencionada arriba.

## Credenciales de prueba

| Rol      | Correo                  | Contraseña    | Dashboard            |
|----------|--------------------------|---------------|------------------------|
| Admin    | admin@rednorte.cl        | admin123      | /admin/dashboard       |
| Doctor   | dr.vega@rednorte.cl       | doctor123     | /doctor/dashboard      |
| Doctor   | dra.rojas@rednorte.cl     | doctor123     | /doctor/dashboard      |
| Doctor   | dr.morales@rednorte.cl    | doctor123     | /doctor/dashboard      |
| Paciente | juan.perez@correo.cl      | paciente123   | /mis-consultas         |
| Paciente | maria.lopez@correo.cl     | paciente123   | /mis-consultas         |
| Paciente | carlos.soto@correo.cl     | paciente123   | /mis-consultas         |

## Levantar el proyecto completo

### Prerequisitos
- Docker Desktop instalado y corriendo
- Node.js (para el frontend)
- PowerShell (Windows)

### 1 — Levantar el backend

```powershell
cd C:\Users\katty\Documents\backend-RedNorte
docker compose down
docker compose up --build
```

Esperar a que los 12 contenedores arranquen (~90 segundos: MySQL y Eureka
primero, luego el resto de microservicios que dependen de ellos). Verificar que
todos terminen con `Started ... in X seconds` en los logs.

### 2 — Cargar datos de prueba (nueva terminal)

Los seeds **no** están en `init-db/` (eso causaba errores de tabla inexistente
al ejecutarse antes de que Hibernate creara las tablas). Se cargan manualmente
después de que el backend esté arriba:

```powershell
cd C:\Users\katty\Documents\backend-RedNorte
Get-Content 02-seed-usuarios.sql | docker exec -i cnt-db-rednorte mysql -uroot -p11223344
Get-Content 03-seed-consultas.sql | docker exec -i cnt-db-rednorte mysql -uroot -p11223344
Get-Content 04-seed-fichas.sql | docker exec -i cnt-db-rednorte mysql -uroot -p11223344
Get-Content 05-seed-establecimientos.sql | docker exec -i cnt-db-rednorte mysql -uroot -p11223344
Get-Content 06-seed-agenda.sql | docker exec -i cnt-db-rednorte mysql -uroot -p11223344
Get-Content 07-seed-demo.sql | docker exec -i cnt-db-rednorte mysql -uroot -p11223344
```

> **Nota sobre acentos y ñ:** si algún seed tiene tildes y `Get-Content | docker
> exec -i` corrompe el texto, usar en su lugar:
> ```powershell
> docker cp 02-seed-usuarios.sql cnt-db-rednorte:/tmp/
> docker exec -it cnt-db-rednorte mysql -uroot -p11223344 --default-character-set=utf8mb4 -e "source /tmp/02-seed-usuarios.sql"
> ```

### 3 — Levantar el frontend (otra terminal)

```powershell
cd C:\Users\katty\Documents\RedNorte-frontend
npm install
npm start
```

## URLs del sistema

| Servicio                    | URL                                                  |
|------------------------------|-------------------------------------------------------|
| Frontend                    | http://localhost:3000                                |
| API Gateway                 | http://localhost:8090                                |
| Eureka Dashboard (directo)  | http://localhost:8761                                |
| Eureka Dashboard (vía Gateway) | http://localhost:8090/eureka-ui/                  |
| Swagger ms-usuarios          | http://localhost:8080/swagger-ui/index.html          |
| BFF Dashboard                | http://localhost:8090/bff/dashboard *(requiere token)* |

## Testing y cobertura

Cada uno de los 11 módulos backend tiene **JaCoCo** configurado (sin regla de
mínimo activada todavía, solo generación de reporte) y al menos una clase de
test con JUnit 5 + Mockito.

Para correr los tests de un microservicio (usando el contenedor de Maven, ya
que Maven no está instalado localmente):

```powershell
docker run --rm -v "${PWD}\ms-consultas:/app" -w /app maven:3.9-eclipse-temurin-17 mvn test --no-transfer-progress
```

El reporte de cobertura queda en `<microservicio>/target/site/jacoco/index.html`.

## CI/CD

`.github/workflows/ci-cd.yml` ejecuta automáticamente en cada push/PR a `main`:

1. **Tests de `ms-usuarios`** (job independiente, publica resultados y cobertura)
2. **Tests de los 9 microservicios restantes** (matrix de GitHub Actions, en
   paralelo, publica resultados y cobertura de cada uno)
3. **Build** del JAR de `ms-usuarios`
4. **Build** de la imagen Docker de `ms-usuarios`

## Patrones de arquitectura implementados

- **Microservicios independientes**, cada uno con su propia base de datos
  (`db_usuarios`, `db_consultas`, `db_reasignacion`, `db_ficha`,
  `db_notificaciones`, `db_establecimientos`, `db_agenda`, `db_auditoria`).
- **API Gateway** (Spring Cloud Gateway) como punto único de entrada para el
  frontend, con CORS centralizado.
- **Service Discovery** (Netflix Eureka) para que el Gateway enrute por nombre
  lógico de servicio (`lb://ms-consultas`) en vez de hostname fijo, permitiendo
  escalar instancias sin reconfigurar rutas.
- **Circuit Breaker** (Resilience4j) en cada ruta del Gateway, con un
  `FallbackController` dedicado que responde de forma controlada si un
  microservicio cae.
- **Backend For Frontend (BFF)** (`ms-bff`): agrega y simplifica datos de
  varios microservicios (consultas + usuarios) en una sola respuesta para el
  dashboard del administrador.
- **Repository** (Spring Data JPA) en todos los microservicios con
  persistencia.

## Flujos principales

### Paciente
1. Inicia sesión en `/login` y recibe un JWT.
2. Crea una consulta con especialidad y síntomas.
3. Ve el estado de sus consultas en "Mis consultas".
4. Puede editar nombre o síntomas mientras la consulta está PENDIENTE o AGENDADA.

### Administrador
1. Inicia sesión y ve el **Panel de control** con estadísticas agregadas
   (vía `ms-bff` y `ms-estadisticas`).
2. En **Todas las consultas** puede cambiar el estado, asignar fecha de cita y
   agregar notas.
3. En **Gestión de usuarios** puede ver y crear usuarios.
4. En **Reasignación** puede cancelar una cita agendada; el sistema busca
   automáticamente al siguiente paciente pendiente de esa especialidad y
   reasigna el bloque.
5. En **Establecimientos** administra el catálogo de clínicas, hospitales,
   CESFAM, consultorios y postas rurales.

## Estados de una consulta

```
PENDIENTE → AGENDADA → ATENDIDA
         ↘ CANCELADA → (reasignación) → REASIGNADA
```

## Endpoints principales

| Microservicio        | Ruta base                  | Descripción                          |
|------------------------|-----------------------------|----------------------------------------|
| ms-usuarios            | `/usuarios`, `/roles`       | Login, CRUD de usuarios y roles        |
| ms-consultas            | `/consultas`                | CRUD de consultas médicas              |
| ms-reasignacion          | `/api/reasignacion`         | Cancelar y reasignar citas             |
| ms-ficha-medica          | `/ficha`                    | Ficha clínica por usuario              |
| ms-notificaciones        | `/notificaciones`           | Notificaciones por usuario             |
| ms-establecimientos      | `/establecimientos`         | CRUD de clínicas y centros de salud    |
| ms-agenda-medica         | `/agenda`                   | Bloques de agenda médica               |
| ms-estadisticas          | `/estadisticas`             | Métricas agregadas del sistema         |
| ms-auditoria             | `/auditoria`                | Registro de eventos de auditoría       |
| ms-bff                  | `/bff`                      | Dashboard consolidado para el admin    |

