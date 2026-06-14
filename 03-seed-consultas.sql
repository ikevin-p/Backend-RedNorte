-- ============================================================
-- RedNorte — Seed COMPLETO de db_consultas
-- 1 consulta por cada estado para demo del admin
-- ============================================================
USE db_consultas;

DELETE FROM consultas;

INSERT INTO consultas
  (usuario_id, nombre_paciente, sintomas, especialidad, estado, fecha_creacion, fecha_cita, bloques_agenda_id, notas_admin)
VALUES
  -- ─── PENDIENTES (2) ───────────────────────────────────────
  ('USR005', 'Juan Pérez Castro',
   'Dolor en el pecho al hacer ejercicio, dificultad para respirar desde hace 2 semanas.',
   'cardiologia', 'PENDIENTE',
   NOW() - INTERVAL 5 DAY, NULL, NULL, NULL),

  ('USR006', 'María López Vega',
   'Manchas rojizas en los brazos con picazón intensa, empeoran con el calor.',
   'dermatologia', 'PENDIENTE',
   NOW() - INTERVAL 3 DAY, NULL, NULL, NULL),

  -- ─── AGENDADAS (2) ────────────────────────────────────────
  ('USR007', 'Carlos Soto Ramos',
   'Control de hipertensión arterial, requiere evaluación.',
   'cardiologia', 'AGENDADA',
   NOW() - INTERVAL 10 DAY,
   NOW() + INTERVAL 5 DAY,
   1, 'Cita confirmada con Dr. Vega'),

  ('USR008', 'Ana Silva Núñez',
   'Revisión por dolor de cabeza recurrente y mareos.',
   'neurologia', 'AGENDADA',
   NOW() - INTERVAL 7 DAY,
   NOW() + INTERVAL 3 DAY,
   2, 'Cita confirmada con Dr. Morales'),

  -- ─── REASIGNADA (1) ───────────────────────────────────────
  ('USR009', 'Pedro Gómez Aravena',
   'Dolor lumbar crónico, requiere evaluación traumatológica.',
   'traumatologia', 'REASIGNADA',
   NOW() - INTERVAL 15 DAY,
   NOW() + INTERVAL 10 DAY,
   3, 'Reasignada por ausencia del Dr. original. Nuevo doctor: Dr. Morales'),

  -- ─── CANCELADA (1) ────────────────────────────────────────
  ('USR010', 'Lucía Torres Hernández',
   'Dolor abdominal intermitente después de las comidas.',
   'gastroenterologia', 'CANCELADA',
   NOW() - INTERVAL 8 DAY, NULL, NULL,
   'Cancelada por la paciente. Motivo: viaje fuera de la ciudad'),

  -- ─── ATENDIDAS (2) ────────────────────────────────────────
  ('USR011', 'Diego Castro Espinoza',
   'Resfriado común con fiebre alta y dolor de garganta por 4 días.',
   'medicina general', 'ATENDIDA',
   NOW() - INTERVAL 20 DAY,
   NOW() - INTERVAL 14 DAY,
   NULL, 'Paciente atendido. Indicaciones: reposo y antibiótico recetado.'),

  ('USR012', 'Sofía Miranda Olivares',
   'Control dermatológico de lunares con cambios visibles.',
   'dermatologia', 'ATENDIDA',
   NOW() - INTERVAL 25 DAY,
   NOW() - INTERVAL 18 DAY,
   NULL, 'Atendida por Dra. Rojas. Lunares benignos, sin requerimiento de biopsia.');

SELECT 'db_consultas cargada correctamente (8 consultas)' AS estado;

-- ============================================================
-- RedNorte — Datos de prueba para db_reasignacion
-- ============================================================
USE db_reasignacion;

DELETE FROM bloques_agenda;

INSERT INTO bloques_agenda (profesional_id, especialidad_id, fecha_hora) VALUES
  ('USR002', 'cardiologia',       NOW() + INTERVAL 5 DAY),
  ('USR004', 'neurologia',        NOW() + INTERVAL 3 DAY),
  ('USR004', 'traumatologia',     NOW() + INTERVAL 10 DAY),
  ('USR003', 'dermatologia',      NOW() + INTERVAL 7 DAY),
  ('USR002', 'cardiologia',       NOW() + INTERVAL 14 DAY),
  ('USR004', 'medicina general',  NOW() + INTERVAL 21 DAY);

SELECT 'db_reasignacion cargada correctamente (6 bloques)' AS estado;
