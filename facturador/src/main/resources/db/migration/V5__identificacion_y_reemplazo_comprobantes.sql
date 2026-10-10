ALTER TABLE comprobante
    DROP CONSTRAINT uk_comprobante_clave_acceso;

ALTER TABLE comprobante
    ADD COLUMN id_documento_origen BIGINT,
    ADD COLUMN hash_identidad VARCHAR(64),
    ADD COLUMN hash_contenido VARCHAR(64),
    ADD COLUMN comprobante_reemplazado_id BIGINT;

ALTER TABLE comprobante
    ADD CONSTRAINT fk_comprobante_reemplazado
        FOREIGN KEY (comprobante_reemplazado_id) REFERENCES comprobante(id);

CREATE INDEX idx_comprobante_empresa_documento_origen
    ON comprobante (empresa_id, id_documento_origen, estado_registro);

CREATE TABLE comprobante_solicitud_proceso (
    empresa_id BIGINT NOT NULL,
    id_documento_origen BIGINT NOT NULL,
    id_transaccion VARCHAR(100) NOT NULL,
    en_proceso BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_comprobante_solicitud_proceso
        PRIMARY KEY (empresa_id, id_documento_origen),
    CONSTRAINT fk_comprobante_solicitud_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);
