ALTER TABLE comprobante_auditoria
    ADD COLUMN etapa VARCHAR(50),
    ADD COLUMN resultado VARCHAR(20),
    ADD COLUMN fecha_inicio TIMESTAMP,
    ADD COLUMN fecha_fin TIMESTAMP,
    ADD COLUMN intento INTEGER;

UPDATE comprobante_auditoria
SET etapa = COALESCE(etapa, 'AUDITORIA_HISTORICA'),
    resultado = COALESCE(resultado, CASE
        WHEN codigo_error IS NULL THEN 'EXITOSO'
        ELSE 'ERROR'
    END),
    fecha_inicio = COALESCE(fecha_inicio, fecha_creacion),
    fecha_fin = COALESCE(fecha_fin, fecha_creacion),
    intento = COALESCE(intento, 1);

ALTER TABLE comprobante_auditoria
    ALTER COLUMN etapa SET NOT NULL,
    ALTER COLUMN resultado SET NOT NULL,
    ALTER COLUMN fecha_inicio SET NOT NULL,
    ALTER COLUMN intento SET NOT NULL;

ALTER TABLE comprobante_auditoria
    ADD CONSTRAINT ck_comprobante_auditoria_intento
        CHECK (intento > 0);

CREATE INDEX idx_comprobante_auditoria_comprobante_etapa
    ON comprobante_auditoria (comprobante_id, etapa, intento DESC);
