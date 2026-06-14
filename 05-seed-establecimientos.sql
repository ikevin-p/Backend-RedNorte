-- ============================================================
-- RedNorte — Seed establecimientos
-- Se ejecuta igual que los otros seeds
-- ============================================================
USE db_establecimientos;

DELETE FROM establecimiento;

INSERT INTO establecimiento (est_id, est_nombre, est_tipo, est_direccion, est_comuna, est_region, est_telefono, est_email, est_capacidad_diaria, est_estado, est_fecha_registro) VALUES
('EST-001', 'Hospital Regional del Norte', 'HOSPITAL',    'Av. Arturo Prat 1234',         'Iquique',       'Tarapaca',       '+56 57 2123456', 'contacto@hospitalnorte.cl',      50, 'ACTIVO', NOW()),
('EST-002', 'CESFAM Dr. Salvador Allende', 'CESFAM',      'Calle Los Alerces 567',         'Antofagasta',   'Antofagasta',    '+56 55 2456789', 'cesfam.allende@rednorte.cl',     80, 'ACTIVO', NOW()),
('EST-003', 'Clinica RedNorte Sur',        'CLINICA',     'Av. Bernardo OHiggins 890',     'Santiago',      'Metropolitana',  '+56 2 23456789', 'clinica.sur@rednorte.cl',       120, 'ACTIVO', NOW()),
('EST-004', 'Consultorio Las Americas',    'CONSULTORIO', 'Calle Las Americas 321',        'Calama',        'Antofagasta',    '+56 55 2789012', 'consultorio.americas@rednorte.cl', 40, 'ACTIVO', NOW()),
('EST-005', 'Posta Rural Colchane',        'POSTA_RURAL', 'Camino Internacional km 3',     'Colchane',      'Tarapaca',       '+56 57 2654321', 'posta.colchane@rednorte.cl',     15, 'ACTIVO', NOW());

SELECT 'db_establecimientos cargada correctamente (5 establecimientos)' AS estado;
