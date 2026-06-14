-- ============================================================
-- RedNorte — Seed COMPLETO de db_ficha
-- Ficha médica realista para los 12 usuarios del sistema
-- ============================================================
USE db_ficha;

DELETE FROM ficha_medica;

-- ─── ADMIN ──────────────────────────────────────────────────
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales, cirugias_previas,
   antecedentes_familiares, habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR001', 1.78, 82.5, 'O+', '120/80', '70', '95',
   '+56 9 8765 4321', 'Av. Vicuña Mackenna 1234, Iquique',
   'Ninguna conocida', 'Ninguna', 'Ninguno', 'Apendicectomía (2010)',
   'Padre con hipertensión', 'No fumador', 'Ocasional',
   'Patricia Muñoz', '+56 9 1234 5678', 'Esposa');

-- ─── DOCTORES ───────────────────────────────────────────────
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales, cirugias_previas,
   antecedentes_familiares, habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR002', 1.82, 88.0, 'A+', '125/82', '68', '90',
   '+56 9 8888 1111', 'Calle Tarapacá 567, Iquique',
   'Penicilina', 'Hipertensión leve', 'Losartán 50mg', 'Ninguna',
   'Madre con diabetes tipo 2', 'No fumador', 'Social',
   'Carmen Soto', '+56 9 1111 2222', 'Esposa'),

  ('USR003', 1.65, 62.0, 'B+', '115/75', '72', '88',
   '+56 9 8888 2222', 'Av. Brasil 890, Antofagasta',
   'Ninguna conocida', 'Ninguna', 'Anticonceptivo', 'Cesárea (2019)',
   'Sin antecedentes relevantes', 'No fumador', 'Ocasional',
   'Roberto Fuentes', '+56 9 2222 3333', 'Esposo'),

  ('USR004', 1.75, 78.5, 'AB+', '130/85', '75', '102',
   '+56 9 8888 3333', 'Calle Latorre 234, Iquique',
   'Mariscos', 'Pre-diabetes', 'Metformina 500mg', 'Vesícula (2015)',
   'Padre con diabetes tipo 2', 'No fumador', 'Moderado',
   'Andrea Ibáñez', '+56 9 3333 4444', 'Esposa');

-- ─── PACIENTES — fichas con distinto grado de completitud ────────
-- USR005 Juan Pérez — PENDIENTE (perfil 80%)
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales,
   antecedentes_familiares, habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR005', 1.75, 85.0, 'O+', '140/90', '85', '110',
   '+56 9 5555 1111', 'Calle Baquedano 456, Iquique',
   'Ninguna conocida', 'Hipertensión arterial', 'Enalapril 10mg',
   'Padre falleció de infarto', 'Exfumador (5 años)', 'Ocasional',
   'Rosa Castro', '+56 9 5555 9999', 'Madre');

-- USR006 María López — PENDIENTE (perfil 60%)
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias,
   habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR006', 1.62, 58.0, 'A-', '110/70', '78', '95',
   '+56 9 5555 2222', 'Av. Aníbal Pinto 789, Iquique',
   'Polen, ácaros',
   'No fumador', 'Ocasional',
   'José López', '+56 9 5555 8888', 'Padre');

-- USR007 Carlos Soto — AGENDADA (perfil 100%)
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales, cirugias_previas,
   antecedentes_familiares, habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR007', 1.80, 90.5, 'O-', '145/95', '88', '125',
   '+56 9 5555 3333', 'Calle Sotomayor 123, Antofagasta',
   'AINEs (antiinflamatorios)', 'Hipertensión, dislipidemia',
   'Atenolol 50mg, Atorvastatina 20mg', 'Hernia inguinal (2018)',
   'Padre con infarto a los 55, madre diabética', 'Exfumador (10 años)', 'Moderado',
   'Marta Ramos', '+56 9 5555 7777', 'Esposa');

-- USR008 Ana Silva — AGENDADA (perfil 85%)
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales,
   antecedentes_familiares, habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR008', 1.68, 65.0, 'B+', '118/78', '74', '92',
   '+56 9 5555 4444', 'Av. Argentina 456, Antofagasta',
   'Sulfas', 'Migraña crónica', 'Sumatriptán (según necesidad)',
   'Madre con migraña', 'No fumador', 'Ocasional',
   'Carlos Núñez', '+56 9 5555 6666', 'Hermano');

-- USR009 Pedro Gómez — REASIGNADA (perfil 100%)
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales, cirugias_previas,
   antecedentes_familiares, habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR009', 1.72, 95.0, 'A+', '135/85', '80', '115',
   '+56 9 5555 5555', 'Pasaje Los Aromos 234, Iquique',
   'Ibuprofeno', 'Lumbalgia crónica, sobrepeso',
   'Paracetamol, relajante muscular', 'Cirugía de menisco (2020)',
   'Padre con artrosis severa, madre con osteoporosis', 'No fumador', 'Moderado',
   'Soledad Aravena', '+56 9 5555 4444', 'Esposa');

-- USR010 Lucía Torres — CANCELADA (perfil 75%)
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales,
   habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR010', 1.60, 55.0, 'O+', '105/68', '70', '88',
   '+56 9 5555 6666', 'Calle Almirante Latorre 678, Iquique',
   'Lactosa (intolerancia)', 'Síndrome de intestino irritable',
   'Probióticos, antiespasmódico',
   'No fumador', 'No bebe',
   'Camilo Hernández', '+56 9 5555 3333', 'Pareja');

-- USR011 Diego Castro — ATENDIDA (perfil 100%)
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales, cirugias_previas,
   antecedentes_familiares, habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR011', 1.78, 75.0, 'AB-', '120/80', '68', '90',
   '+56 9 5555 7777', 'Av. Salvador Allende 890, Antofagasta',
   'Ninguna conocida', 'Asma controlada',
   'Salbutamol (broncodilatador)', 'Amigdalectomía (2008)',
   'Madre con asma', 'No fumador', 'Ocasional',
   'Daniela Espinoza', '+56 9 5555 2222', 'Hermana');

-- USR012 Sofía Miranda — ATENDIDA (perfil 100%)
INSERT INTO ficha_medica
  (usuario_id, estatura, peso, grupo_sanguineo, presion_arterial, frecuencia_cardiaca, glucosa,
   telefono, direccion, alergias, condiciones_cronicas, medicamentos_actuales, cirugias_previas,
   antecedentes_familiares, habito_tabaco, habito_alcohol,
   emergencia_nombre, emergencia_telefono, emergencia_relacion)
VALUES
  ('USR012', 1.66, 60.0, 'O+', '112/72', '72', '85',
   '+56 9 5555 8888', 'Pasaje Las Camelias 345, Santiago',
   'Níquel (dermatitis de contacto)', 'Dermatitis atópica',
   'Crema hidratante, corticoide tópico', 'Cesárea (2022)',
   'Madre con dermatitis, abuelo con melanoma', 'No fumador', 'Ocasional',
   'Rodrigo Olivares', '+56 9 5555 1111', 'Esposo');

SELECT 'db_ficha cargada correctamente (12 fichas médicas)' AS estado;
