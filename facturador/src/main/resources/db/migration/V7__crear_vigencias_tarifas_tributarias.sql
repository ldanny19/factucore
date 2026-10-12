CREATE TABLE tarifa_tributaria_vigencia (
    id BIGINT PRIMARY KEY,
    catalogo_item_id BIGINT NOT NULL,
    codigo_impuesto VARCHAR(2) NOT NULL,
    codigo_porcentaje VARCHAR(4) NOT NULL,
    tarifa NUMERIC(8, 4) NOT NULL,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    estado_registro VARCHAR(20) NOT NULL,
    usuario_creacion VARCHAR(100) NOT NULL,
    usuario_modificacion VARCHAR(100),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    observacion VARCHAR(500),
    CONSTRAINT fk_tarifa_tributaria_vigencia_catalogo_item
        FOREIGN KEY (catalogo_item_id) REFERENCES catalogo_item(id),
    CONSTRAINT ck_tarifa_tributaria_vigencia_tarifa CHECK (tarifa >= 0),
    CONSTRAINT ck_tarifa_tributaria_vigencia_fechas
        CHECK (fecha_vigencia_hasta IS NULL OR fecha_vigencia_hasta >= fecha_vigencia_desde),
    CONSTRAINT uk_tarifa_tributaria_vigencia_inicio
        UNIQUE (codigo_impuesto, codigo_porcentaje, fecha_vigencia_desde)
);

CREATE INDEX ix_tarifa_tributaria_vigencia_busqueda
    ON tarifa_tributaria_vigencia (codigo_impuesto, codigo_porcentaje, fecha_vigencia_desde, fecha_vigencia_hasta);

CREATE OR REPLACE FUNCTION validar_no_solapamiento_tarifa_tributaria()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM tarifa_tributaria_vigencia t
        WHERE t.codigo_impuesto = NEW.codigo_impuesto
          AND t.codigo_porcentaje = NEW.codigo_porcentaje
          AND t.id <> COALESCE(NEW.id, -1)
          AND NEW.fecha_vigencia_desde <= COALESCE(t.fecha_vigencia_hasta, DATE '9999-12-31')
          AND t.fecha_vigencia_desde <= COALESCE(NEW.fecha_vigencia_hasta, DATE '9999-12-31')
          AND t.estado_registro <> 'E'
    ) THEN
        RAISE EXCEPTION 'Solapamiento de vigencias para impuesto % y porcentaje %',
            NEW.codigo_impuesto, NEW.codigo_porcentaje
            USING ERRCODE = '23P01';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_no_solapamiento_tarifa_tributaria
    BEFORE INSERT OR UPDATE OF codigo_impuesto, codigo_porcentaje, fecha_vigencia_desde, fecha_vigencia_hasta, estado_registro
    ON tarifa_tributaria_vigencia
    FOR EACH ROW
    EXECUTE FUNCTION validar_no_solapamiento_tarifa_tributaria();
