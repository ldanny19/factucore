-- Los IDs cargados por Flyway en V3 llegan hasta catalogo.id = 8
-- y catalogo_item.id = 57. Hibernate debe iniciar en 9 y 58,
-- respectivamente. Se usa el máximo actual para no retroceder
-- generadores si ya existen registros adicionales.
INSERT INTO hibernate_sequences (sequence_name, next_val)
SELECT 'catalogo', COALESCE(MAX(id), 0) + 1
FROM catalogo
ON CONFLICT (sequence_name) DO UPDATE
SET next_val = GREATEST(
    COALESCE(hibernate_sequences.next_val, EXCLUDED.next_val),
    EXCLUDED.next_val
);

INSERT INTO hibernate_sequences (sequence_name, next_val)
SELECT 'catalogo_item', COALESCE(MAX(id), 0) + 1
FROM catalogo_item
ON CONFLICT (sequence_name) DO UPDATE
SET next_val = GREATEST(
    COALESCE(hibernate_sequences.next_val, EXCLUDED.next_val),
    EXCLUDED.next_val
);
