-- ============================================================
-- RedNorte — Datos de prueba para db_usuarios
-- Ejecutar DESPUÉS de que Spring Boot haya creado las tablas
-- ============================================================
USE db_usuarios;

-- ─── ROLES ───────────────────────────────────────────────────
INSERT IGNORE INTO rol (r_id, r_tag, r_nombre, r_descripcion, r_estado) VALUES
  ('ROL001', 'ADMIN',    'Administrador', 'Acceso completo al sistema',      'ACTIVO'),
  ('ROL002', 'PACIENTE', 'Paciente',      'Acceso al portal del paciente',   'ACTIVO'),
  ('ROL003', 'DOCTOR',   'Doctor',        'Acceso al portal medico',         'ACTIVO');

-- ─── USUARIOS ────────────────────────────────────────────────
INSERT IGNORE INTO usuario (u_id, u_mail, u_pass, u_estado, u_fecha_registro, u_rol_id) VALUES
  ('USR001', 'admin@rednorte.cl',      'admin123',    'ACTIVO', NOW(), 'ROL001'),
  ('USR002', 'juan.perez@correo.cl',   'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR003', 'maria.lopez@correo.cl',  'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR004', 'carlos.soto@correo.cl',  'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR005', 'dr.vega@rednorte.cl',    'doctor123',   'ACTIVO', NOW(), 'ROL003'),
  ('USR006', 'dra.rojas@rednorte.cl',  'doctor123',   'ACTIVO', NOW(), 'ROL003'),
  ('USR007', 'dr.morales@rednorte.cl', 'doctor123',   'ACTIVO', NOW(), 'ROL003');

-- ─── PERSONAS ────────────────────────────────────────────────
INSERT IGNORE INTO persona (p_id, p_rut, p_apellido_1, p_apellido_2, p_fecha_nacimiento, p_sexo, p_usuario_id) VALUES
  ('PER001', '12345678-9', 'González',  'Muñoz',   '1985-03-15', 'M', 'USR001'),
  ('PER002', '15678901-2', 'Pérez',     'Castro',  '1990-07-22', 'M', 'USR002'),
  ('PER003', '16789012-3', 'López',     'Vega',    '1988-11-05', 'F', 'USR003'),
  ('PER004', '17890123-4', 'Soto',      'Ramos',   '1995-02-18', 'M', 'USR004'),
  ('PER005', '11111111-1', 'Vega',      'Soto',    '1975-06-10', 'M', 'USR005'),
  ('PER006', '22222222-2', 'Rojas',     'Fuentes', '1980-03-22', 'F', 'USR006'),
  ('PER007', '33333333-3', 'Morales',   'Ibáñez',  '1978-09-15', 'M', 'USR007');

SELECT 'db_usuarios cargada correctamente' AS estado;
