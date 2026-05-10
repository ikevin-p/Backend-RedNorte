# RedNorte — Plataforma de Gestión de Listas de Espera

Sistema de gestión de consultas médicas y reasignación de citas para el sistema público de salud.

## Arquitectura

```
backend-RedNorte/
├── Backend-usuarios/       ms-usuarios    → :8080  (compañero)
├── Backend_reasignacion/   ms-reasignacion → :8082  (compañero)
├── ms-consultas/           ms-consultas   → :8083  (nuevo)
├── frontend-rednorte/      React          → :3000
├── init-db/                Scripts SQL de inicialización
└── docker-compose.yml
```

## Tecnologías

| Capa       | Tecnología                          |
|------------|-------------------------------------|
| Backend    | Java 17 · Spring Boot 3.x · Lombok  |
| Base datos | MySQL 8.4                           |
| Frontend   | React 18 · React Router 6           |
| Infra      | Docker · Docker Compose             |

## Credenciales de prueba

| Rol           | Correo                  | Contraseña   |
|---------------|-------------------------|--------------|
| Administrador | admin@rednorte.cl       | admin123     |
| Paciente      | juan.perez@correo.cl    | paciente123  |
| Paciente      | maria.lopez@correo.cl   | paciente123  |

## Levantar el proyecto completo

### Prerequisitos
- Docker Desktop instalado y corriendo
- Git

### Pasos

```bash
# 1. Clonar el repositorio
git clone <URL_DEL_REPO>
cd backend-RedNorte

# 2. Construir y levantar todos los servicios
docker-compose up --build

# Esperar ~60 segundos a que MySQL esté listo y los 3 servicios arranquen.
# Verás en los logs: "Started MsConsultasApplication in X seconds"
```

### Cargar datos de prueba

Los scripts en `init-db/` crean las bases de datos automáticamente.
Para cargar roles, usuarios y consultas de ejemplo:

```bash
# Esperar que MySQL esté corriendo (~20 seg después del docker-compose up)
docker exec -i cnt-db-rednorte mysql -uroot -p11223344 < init-db/02-seed-usuarios.sql
docker exec -i cnt-db-rednorte mysql -uroot -p11223344 < init-db/03-seed-consultas.sql
```

### Levantar el frontend (desarrollo)

```bash
cd frontend-rednorte
npm install
npm start
# Abre http://localhost:3000
```

## Endpoints disponibles

### ms-usuarios (puerto 8080)
| Método | Ruta             | Descripción          |
|--------|------------------|----------------------|
| POST   | /usuarios/login  | Iniciar sesión       |
| GET    | /usuarios        | Listar usuarios      |
| POST   | /usuarios        | Crear usuario        |
| GET    | /roles           | Listar roles         |
| POST   | /roles           | Crear rol            |

### ms-consultas (puerto 8083)
| Método | Ruta                              | Descripción                        |
|--------|-----------------------------------|------------------------------------|
| POST   | /consultas                        | Crear consulta (paciente)          |
| GET    | /consultas                        | Listar todas (admin)               |
| GET    | /consultas/{id}                   | Detalle de una consulta            |
| GET    | /consultas/usuario/{uid}          | Consultas de un paciente           |
| PUT    | /consultas/{id}/paciente          | Editar nombre y síntomas           |
| PUT    | /consultas/{id}/admin             | Cambiar estado, fecha, notas       |
| GET    | /consultas/prioritario/{esp}      | Consulta prioritaria (reasignación)|
| PUT    | /consultas/{id}/reasignar         | Marcar como reasignada             |
| DELETE | /consultas/{id}                   | Eliminar consulta (admin)          |

### ms-reasignacion (puerto 8082)
| Método | Ruta                                     | Descripción                   |
|--------|------------------------------------------|-------------------------------|
| POST   | /api/reasignacion/solo-cancelar/{id}     | Cancelar cita                 |
| POST   | /api/reasignacion/cancelar-y-reasignar/{id} | Cancelar y reasignar       |

## Flujos principales

### Paciente
1. Inicia sesión en `/login`
2. Crea una consulta con especialidad y síntomas
3. Ve el estado de sus consultas en "Mis consultas"
4. Edita su nombre o síntomas mientras está PENDIENTE o AGENDADA

### Administrador
1. Inicia sesión y ve el **Panel de control** con estadísticas
2. En **Todas las consultas** puede cambiar el estado, asignar fecha de cita y agregar notas
3. En **Gestión de usuarios** puede ver y crear usuarios
4. En **Reasignación** puede cancelar una cita agendada y el sistema asigna automáticamente el bloque al siguiente paciente pendiente de esa especialidad

## Estados de una consulta

```
PENDIENTE → AGENDADA → ATENDIDA
         ↘ CANCELADA → (reasignación) → REASIGNADA
```

## Integrantes

- [Tu nombre] — ms-consultas + Frontend React
- [Nombre compañero] — ms-usuarios + ms-reasignacion
