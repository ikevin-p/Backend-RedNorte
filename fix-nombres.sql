UPDATE db_consultas.consultas SET nombre_paciente = 'Juan Pérez Castro' WHERE id IN (7,10,13);
UPDATE db_consultas.consultas SET nombre_paciente = 'María López Vega' WHERE id IN (8,11,14);
SELECT id, nombre_paciente FROM db_consultas.consultas WHERE id IN (7,8,10,11);
