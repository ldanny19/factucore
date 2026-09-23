ALTER TABLE comprobante
    ADD COLUMN archivo_pdf BYTEA,
    ADD COLUMN fecha_proximo_reproceso TIMESTAMP,
    ADD COLUMN numero_consultas_autorizacion INTEGER NOT NULL DEFAULT 0;

ALTER TABLE comprobante
    ADD CONSTRAINT ck_comprobante_consultas_autorizacion
        CHECK (numero_consultas_autorizacion >= 0);

CREATE INDEX idx_comprobante_reproceso_autorizacion
    ON comprobante (estado_proceso, fecha_proximo_reproceso);
