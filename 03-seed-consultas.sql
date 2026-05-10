-- ============================================================
-- RedNorte — Datos de prueba para db_consultas
-- ============================================================

USE db_consultas;

INSERT IGNORE INTO consultas
  (usuario_id, nombre_paciente, sintomas, especialidad, estado, fecha_creacion, fecha_cita, bloques_agenda_id, notas_admin)
VALUES
  -- Consultas pendientes (sin cita asignada aún)
  ('USR002', 'Juan Pérez Castro',
   'Dolor en el pecho al hacer ejercicio, dificultad para respirar desde hace 2 semanas.',
   'cardiologia', 'PENDIENTE', NOW() - INTERVAL 5 DAY, NULL, NULL, NULL),

  ('USR003', 'María López Vega',
   'Manchas rojizas en los brazos con picazón intensa, empeoran con el calor.',
   'dermatologia', 'PENDIENTE', NOW() - INTERVAL 3 DAY, NULL, NULL, NULL),

  ('USR004', 'Carlos Soto Ramos',
   'Dolor de cabeza frecuente, mareos ocasionales y visión borrosa.',
   'neurologia', 'PENDIENTE', NOW() - INTERVAL 1 DAY, NULL, NULL, NULL),

  -- Consulta agendada (con bloque asignado)
  ('USR002', 'Juan Pérez Castro',
   'Control por dolor de rodilla derecha, dificultad al bajar escaleras.',
   'traumatologia', 'AGENDADA',
   NOW() - INTERVAL 10 DAY,
   NOW() + INTERVAL 7 DAY,
   1, 'Cita confirmada con Dr. Fernández'),

  -- Consulta ya atendida
  ('USR003', 'María López Vega',
   'Resfriado con fiebre alta y dolor de garganta por 4 días.',
   'medicina general', 'ATENDIDA',
   NOW() - INTERVAL 20 DAY,
   NOW() - INTERVAL 14 DAY,
   NULL, 'Paciente atendida sin complicaciones'),

  -- Consulta cancelada
  ('USR004', 'Carlos Soto Ramos',
   'Dolor abdominal intermitente después de las comidas.',
   'gastroenterologia', 'CANCELADA',
   NOW() - INTERVAL 8 DAY, NULL, NULL, 'Cancelada por el paciente');

SELECT 'db_consultas cargada correctamente' AS estado;

-- ============================================================
-- RedNorte — Datos de prueba para db_reasignacion
-- ============================================================

USE db_reasignacion;

INSERT IGNORE INTO bloques_agenda (profesional_id, especialidad_id, fecha_hora) VALUES
  ('MED001', 'cardiologia',      NOW() + INTERVAL 7 DAY),
  ('MED002', 'traumatologia',    NOW() + INTERVAL 7 DAY),
  ('MED003', 'dermatologia',     NOW() + INTERVAL 10 DAY),
  ('MED004', 'neurologia',       NOW() + INTERVAL 12 DAY),
  ('MED005', 'gastroenterologia',NOW() + INTERVAL 14 DAY),
  ('MED001', 'cardiologia',      NOW() + INTERVAL 21 DAY);

SELECT 'db_reasignacion cargada correctamente' AS estado;
