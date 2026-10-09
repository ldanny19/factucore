-- Actualización manual para bases existentes que ya ejecutaron V1.
-- Primero agregar columnas opcionales para permitir completar la información histórica.
ALTER TABLE version_documento_xsd
    ADD COLUMN IF NOT EXISTS ruta_xsd VARCHAR(2000),
    ADD COLUMN IF NOT EXISTS hash_xsd VARCHAR(64);

-- Completar cada registro con la ruta persistente real del XSD original y su SHA-256 hexadecimal.
-- El hash debe calcularse sobre los bytes exactos del archivo, no sobre el contenido reconstruido.
SELECT id, documento_xsd_id, version, nombre_archivo
FROM version_documento_xsd
WHERE ruta_xsd IS NULL OR hash_xsd IS NULL;

-- Ejemplo (reemplazar valores con la ruta real y el hash SHA-256 calculado):
-- UPDATE version_documento_xsd
-- SET ruta_xsd = '/app/xsd/FACTURA_1.0.0.xsd',
--     hash_xsd = '<SHA256_HEXADECIMAL_DE_64_CARACTERES>'
-- WHERE id = <ID_VERSION>;

-- Ejecutar únicamente después de completar y verificar todos los registros:
-- ALTER TABLE version_documento_xsd ALTER COLUMN ruta_xsd SET NOT NULL;
-- ALTER TABLE version_documento_xsd ALTER COLUMN hash_xsd SET NOT NULL;
