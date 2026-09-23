CREATE TABLE comprobante_evidencia (
    id BIGINT PRIMARY KEY,
    comprobante_id BIGINT NOT NULL,
    tipo_evidencia VARCHAR(30) NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo VARCHAR(1000) NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    es_actual BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_generacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_registro VARCHAR(20) NOT NULL,
    usuario_creacion VARCHAR(100) NOT NULL,
    usuario_modificacion VARCHAR(100),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    observacion VARCHAR(500),

    CONSTRAINT fk_comprobante_evidencia_comprobante
        FOREIGN KEY (comprobante_id) REFERENCES comprobante(id),

    CONSTRAINT ck_comprobante_evidencia_tipo
        CHECK (tipo_evidencia IN (
            'XML_GENERADO',
            'XML_FIRMADO',
            'XML_AUTORIZADO',
            'XML_NO_AUTORIZADO',
            'RIDE'
        )),

    CONSTRAINT uk_comprobante_evidencia_actual
        UNIQUE (comprobante_id, tipo_evidencia, es_actual)
);

CREATE INDEX ix_comprobante_evidencia_comprobante
    ON comprobante_evidencia (comprobante_id);

CREATE INDEX ix_comprobante_evidencia_tipo
    ON comprobante_evidencia (tipo_evidencia);CREATE UNIQUE INDEX uk_comprobante_evidencia_actual
    ON comprobante_evidencia (comprobante_id, tipo_evidencia)
    WHERE es_actual = TRUE;

