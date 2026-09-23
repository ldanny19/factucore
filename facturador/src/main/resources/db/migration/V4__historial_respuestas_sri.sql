CREATE TABLE comprobante_respuesta_sri (
    id BIGINT PRIMARY KEY,
    comprobante_id BIGINT NOT NULL,
    tipo_respuesta VARCHAR(20) NOT NULL,
    estado VARCHAR(50),
    identificador VARCHAR(100),
    respuesta JSONB NOT NULL,
    fecha_respuesta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_registro VARCHAR(20) NOT NULL,
    usuario_creacion VARCHAR(100) NOT NULL,
    usuario_modificacion VARCHAR(100),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    observacion VARCHAR(500),
    CONSTRAINT fk_comprobante_respuesta_sri_comprobante
        FOREIGN KEY (comprobante_id) REFERENCES comprobante(id),
    CONSTRAINT ck_comprobante_respuesta_sri_tipo
        CHECK (tipo_respuesta IN ('RECEPCION', 'AUTORIZACION'))
);

CREATE INDEX idx_comprobante_respuesta_sri_comprobante
    ON comprobante_respuesta_sri (comprobante_id, fecha_respuesta);

CREATE INDEX idx_comprobante_respuesta_sri_tipo
    ON comprobante_respuesta_sri (tipo_respuesta, fecha_respuesta);
