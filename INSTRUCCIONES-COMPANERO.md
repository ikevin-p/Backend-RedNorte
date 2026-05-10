# Instrucciones para el compañero — archivos a integrar

## Archivos que debes copiar a tu parte del repositorio

### 1. Reemplaza tu clase principal de Backend_reasignacion

Archivo: `Backend_reasignacion/src/main/java/com/rednorte/Backend_reasignacion/Backend_reasignacion.java`

Se agregó el bean de `RestTemplate` que faltaba. Sin esto el microservicio falla al arrancar.

---

### 2. Reemplaza tu ReasignacionService

Archivo: `Backend_reasignacion/src/main/java/com/rednorte/Backend_reasignacion/service/ReasignacionService.java`

Se corrigió la URL para que apunte a `ms-consultas` (nuestro nuevo microservicio) en vez del `ms-lista-espera` que no existía:

```
// Antes (roto):
http://ms-lista-espera:8081/api/lista-espera/obtener-prioritario/...

// Ahora (correcto):
http://cnt-ms-consultas:8083/consultas/prioritario/...
```

---

### 3. Agrega el Dockerfile a Backend-usuarios

Archivo: `Backend-usuarios/Dockerfile`

Tu carpeta `Backend-usuarios` no tenía Dockerfile, por eso el `docker-compose up` fallaba al intentar construir esa imagen.

---

## Cómo levantar todo

```bash
# Desde la raíz del repositorio
docker-compose up --build
```

Esperar ~60 segundos. Luego cargar los datos de prueba:

```bash
docker exec -i cnt-db-rednorte mysql -uroot -p11223344 < init-db/02-seed-usuarios.sql
docker exec -i cnt-db-rednorte mysql -uroot -p11223344 < init-db/03-seed-consultas.sql
```

## Credenciales de prueba

| Rol           | Correo                  | Contraseña   |
|---------------|-------------------------|--------------|
| Administrador | admin@rednorte.cl       | admin123     |
| Paciente      | juan.perez@correo.cl    | paciente123  |
| Paciente      | maria.lopez@correo.cl   | paciente123  |
