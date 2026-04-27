# Microservicio Lista de Espera

* Registrar pacientes en lista de espera
* Consultar solicitudes existentes
* Gestionar estado y prioridad de atención
* Mantener información básica de solicitudes

# Endpoints

* GET /lista-espera -> Obtiene todas las solicitudes
* POST /lista-espera -> Registra una nueva solicitud

# Estructura

* controller -> Manejo de endpoints
* service -> Lógica de negocio
* repository -> Acceso a datos (Repository Pattern)
* model -> Entidades del sistema