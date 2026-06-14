-- ============================================================
-- RedNorte — Seed COMPLETO de db_usuarios
-- Cubre demo: admin + 3 doctores + 8 pacientes (uno por estado)
-- ============================================================
USE db_usuarios;

-- Agregar columna p_nombre si no existe (compatible con MySQL 8.x)
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'db_usuarios' AND TABLE_NAME = 'persona' AND COLUMN_NAME = 'p_nombre'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE persona ADD COLUMN p_nombre VARCHAR(255) AFTER p_id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Limpiar datos previos (orden: persona → usuario → rol por FK)
DELETE FROM persona;
DELETE FROM usuario;
DELETE FROM rol;

-- ─── ROLES ───────────────────────────────────────────────────
INSERT INTO rol (r_id, r_tag, r_nombre, r_descripcion, r_estado) VALUES
  ('ROL001', 'ADMIN',    'Administrador', 'Acceso completo al sistema',    'ACTIVO'),
  ('ROL002', 'PACIENTE', 'Paciente',      'Acceso al portal del paciente', 'ACTIVO'),
  ('ROL003', 'DOCTOR',   'Doctor',        'Acceso al portal médico',       'ACTIVO');

-- ─── USUARIOS ────────────────────────────────────────────────
-- ADMIN
INSERT INTO usuario (u_id, u_mail, u_pass, u_estado, u_fecha_registro, u_rol_id) VALUES
  ('USR001', 'admin@rednorte.cl', 'admin123', 'ACTIVO', NOW(), 'ROL001');

-- DOCTORES (3)
INSERT INTO usuario (u_id, u_mail, u_pass, u_estado, u_fecha_registro, u_rol_id) VALUES
  ('USR002', 'dr.vega@rednorte.cl',    'doctor123', 'ACTIVO', NOW(), 'ROL003'),
  ('USR003', 'dra.rojas@rednorte.cl',  'doctor123', 'ACTIVO', NOW(), 'ROL003'),
  ('USR004', 'dr.morales@rednorte.cl', 'doctor123', 'ACTIVO', NOW(), 'ROL003');

-- PACIENTES (8 — uno por estado de consulta para demo)
INSERT INTO usuario (u_id, u_mail, u_pass, u_estado, u_fecha_registro, u_rol_id) VALUES
  ('USR005', 'juan.perez@correo.cl',     'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR006', 'maria.lopez@correo.cl',    'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR007', 'carlos.soto@correo.cl',    'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR008', 'ana.silva@correo.cl',      'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR009', 'pedro.gomez@correo.cl',    'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR010', 'lucia.torres@correo.cl',   'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR011', 'diego.castro@correo.cl',   'paciente123', 'ACTIVO', NOW(), 'ROL002'),
  ('USR012', 'sofia.miranda@correo.cl',  'paciente123', 'ACTIVO', NOW(), 'ROL002');

-- ─── PERSONAS ────────────────────────────────────────────────
INSERT INTO persona (p_id, p_nombre, p_apellido_1, p_apellido_2, p_rut, p_fecha_nacimiento, p_sexo, p_usuario_id) VALUES
  -- Admin
  ('PER001', 'Roberto',  'González',  'Muñoz',    '12.345.678-9', '1985-03-15', 'M', 'USR001'),
  -- Doctores
  ('PER002', 'Andrés',   'Vega',      'Soto',     '11.111.111-1', '1975-06-10', 'M', 'USR002'),
  ('PER003', 'Carolina', 'Rojas',     'Fuentes',  '22.222.222-2', '1980-03-22', 'F', 'USR003'),
  ('PER004', 'Felipe',   'Morales',   'Ibáñez',   '33.333.333-3', '1978-09-15', 'M', 'USR004'),
  -- Pacientes
  ('PER005', 'Juan',     'Pérez',     'Castro',   '15.678.901-2', '1990-07-22', 'M', 'USR005'),
  ('PER006', 'María',    'López',     'Vega',     '16.789.012-3', '1988-11-05', 'F', 'USR006'),
  ('PER007', 'Carlos',   'Soto',      'Ramos',    '17.890.123-4', '1995-02-18', 'M', 'USR007'),
  ('PER008', 'Ana',      'Silva',     'Núñez',    '18.901.234-5', '1992-08-30', 'F', 'USR008'),
  ('PER009', 'Pedro',    'Gómez',     'Aravena',  '19.012.345-6', '1987-04-12', 'M', 'USR009'),
  ('PER010', 'Lucía',    'Torres',    'Hernández','20.123.456-7', '1993-12-03', 'F', 'USR010'),
  ('PER011', 'Diego',    'Castro',    'Espinoza', '21.234.567-8', '1991-01-25', 'M', 'USR011'),
  ('PER012', 'Sofía',    'Miranda',   'Olivares', '22.345.678-9', '1996-09-08', 'F', 'USR012');

SELECT 'db_usuarios cargada correctamente (12 usuarios)' AS estado;
