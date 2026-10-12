ALTER TABLE comprobante
    ADD COLUMN correo_comprador VARCHAR(320),
    ADD COLUMN subtotal NUMERIC(18, 2),
    ADD COLUMN total_descuento NUMERIC(18, 2),
    ADD COLUMN total_sin_impuestos NUMERIC(18, 2),
    ADD COLUMN total_impuestos NUMERIC(18, 2),
    ADD COLUMN total_propina NUMERIC(18, 2),
    ADD COLUMN total_a_pagar NUMERIC(18, 2);
