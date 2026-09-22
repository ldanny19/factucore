CREATE TABLE mapeo_xsd (
    id BIGINT PRIMARY KEY,
    version_documento_xsd_id BIGINT NOT NULL,
    ruta_origen VARCHAR(1000) NOT NULL,
    elemento_xsd_id BIGINT,
    atributo_xsd_id BIGINT,
    tipo_mapeo VARCHAR(20) NOT NULL,
    estado_registro VARCHAR(20) NOT NULL,
    usuario_creacion VARCHAR(100) NOT NULL,
    usuario_modificacion VARCHAR(100),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    observacion VARCHAR(500),
    CONSTRAINT fk_mapeo_xsd_version
        FOREIGN KEY (version_documento_xsd_id)
        REFERENCES version_documento_xsd(id),
    CONSTRAINT fk_mapeo_xsd_elemento
        FOREIGN KEY (elemento_xsd_id)
        REFERENCES elemento_xsd(id),
    CONSTRAINT fk_mapeo_xsd_atributo
        FOREIGN KEY (atributo_xsd_id)
        REFERENCES atributo_xsd(id),
    CONSTRAINT ck_mapeo_xsd_destino
        CHECK (
            (elemento_xsd_id IS NOT NULL AND atributo_xsd_id IS NULL)
            OR
            (elemento_xsd_id IS NULL AND atributo_xsd_id IS NOT NULL)
        ),
    CONSTRAINT ck_mapeo_xsd_tipo
        CHECK (tipo_mapeo IN ('ELEMENTO', 'ATRIBUTO')),
    CONSTRAINT uk_mapeo_xsd_origen
        UNIQUE (version_documento_xsd_id, ruta_origen)
);

CREATE INDEX idx_mapeo_xsd_version
    ON mapeo_xsd (version_documento_xsd_id);

CREATE INDEX idx_mapeo_xsd_elemento
    ON mapeo_xsd (elemento_xsd_id);

CREATE INDEX idx_mapeo_xsd_atributo
    ON mapeo_xsd (atributo_xsd_id);