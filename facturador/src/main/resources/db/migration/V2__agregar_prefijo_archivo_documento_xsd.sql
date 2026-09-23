ALTER TABLE documento_xsd
    ADD COLUMN prefijo_archivo VARCHAR(20);

UPDATE documento_xsd
SET prefijo_archivo = codigo
WHERE prefijo_archivo IS NULL;

ALTER TABLE documento_xsd
    ALTER COLUMN prefijo_archivo SET NOT NULL;
