-- Procedimientos y triggers consolidados de FactuCore.
-- Las migraciones versionadas contienen esta única función trigger.

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
    BEFORE INSERT OR UPDATE OF codigo_impuesto, codigo_porcentaje,
        fecha_vigencia_desde, fecha_vigencia_hasta, estado_registro
    ON tarifa_tributaria_vigencia
    FOR EACH ROW
    EXECUTE FUNCTION validar_no_solapamiento_tarifa_tributaria();
