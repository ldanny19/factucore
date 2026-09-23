ALTER TABLE documento_xsd
    ADD COLUMN prefijo_archivo VARCHAR(20);

UPDATE documento_xsd
SET prefijo_archivo = CASE codigo
    WHEN '01' THEN 'FAC'
    WHEN '04' THEN 'NC'
    WHEN '05' THEN 'ND'
    WHEN '06' THEN 'GR'
    WHEN '07' THEN 'RET'
    WHEN '08' THEN 'LC'
    ELSE NULL
END;

ALTER TABLE documento_xsd
    ALTER COLUMN prefijo_archivo SET NOT NULL;
