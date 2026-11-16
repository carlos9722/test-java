INSERT INTO poliza (numero_poliza, tipo, estado, canon, prima) VALUES ('POL-001', 'INDIVIDUAL', 'ACTIVA',    1000000.00, 50000.00);
INSERT INTO poliza (numero_poliza, tipo, estado, canon, prima) VALUES ('POL-002', 'COLECTIVA',  'ACTIVA',    5000000.00, 250000.00);
INSERT INTO poliza (numero_poliza, tipo, estado, canon, prima) VALUES ('POL-003', 'INDIVIDUAL', 'CANCELADA', 800000.00, 40000.00);
INSERT INTO poliza (numero_poliza, tipo, estado, canon, prima) VALUES ('POL-004', 'COLECTIVA',  'CANCELADA', 3000000.00, 150000.00);
INSERT INTO poliza (numero_poliza, tipo, estado, canon, prima) VALUES ('POL-005', 'COLECTIVA',  'RENOVADA',  4200000.00, 210000.00);
INSERT INTO poliza (numero_poliza, tipo, estado, canon, prima) VALUES ('POL-006', 'INDIVIDUAL', 'ACTIVA',    900000.00, 45000.00);

INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Incendio local comercial', 'ACTIVA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-001'));

INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Incendio bodega norte',  'ACTIVA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-002'));
INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Robo bodega sur',        'ACTIVA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-002'));
INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Inundación oficina',     'ACTIVA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-002'));

INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Terremoto vivienda',     'CANCELADA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-003'));

INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Robo sede principal',    'CANCELADA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-004'));
INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Incendio sede norte',    'CANCELADA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-004'));

INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Responsabilidad civil',  'ACTIVA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-005'));
INSERT INTO riesgo (descripcion, estado, poliza_id) VALUES ('Daños eléctricos',       'ACTIVA', (SELECT id FROM poliza WHERE numero_poliza = 'POL-005'));