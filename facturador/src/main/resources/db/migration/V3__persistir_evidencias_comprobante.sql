ALTER TABLE comprobante
    ADD COLUMN xml_generado TEXT,
    ADD COLUMN xml_firmado TEXT,
    ADD COLUMN respuesta_sri_recepcion JSONB,
    ADD COLUMN respuesta_sri_autorizacion JSONB,
    ADD COLUMN fecha_generacion_xml TIMESTAMP,
    ADD COLUMN fecha_firma TIMESTAMP,
    ADD COLUMN fecha_respuesta_sri_recepcion TIMESTAMP,
    ADD COLUMN fecha_respuesta_sri_autorizacion TIMESTAMP;
