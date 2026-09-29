INSERT INTO catalogo (
    id,
    codigo,
    nombre,
    descripcion,
    estado_registro,
    usuario_creacion,
    fecha_creacion
) VALUES
    (1, 'AMBIENTE', 'Ambiente', 'Ambientes definidos por el SRI para comprobantes electrónicos.', 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (2, 'TIPO_EMISION', 'Tipo de emisión', 'Tipos de emisión definidos por el SRI.', 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (3, 'TIPO_DOCUMENTO', 'Tipo de documento', 'Tipos de comprobantes electrónicos soportados por FactuCore.', 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (4, 'TIPO_IDENTIFICACION', 'Tipo de identificación', 'Tipos de identificación del receptor definidos por el SRI.', 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (5, 'FORMA_PAGO', 'Forma de pago', 'Formas de pago definidas por el SRI.', 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (6, 'CODIGO_IMPUESTO', 'Código de impuesto', 'Códigos de impuesto utilizados en comprobantes electrónicos.', 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO catalogo_item (
    id,
    catalogo_id,
    codigo,
    nombre,
    descripcion,
    orden,
    estado_registro,
    usuario_creacion,
    fecha_creacion
) VALUES
    (101, 1, '1', 'Pruebas', 'Ambiente de pruebas del SRI.', 1, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (102, 1, '2', 'Producción', 'Ambiente de producción del SRI.', 2, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),

    (201, 2, '1', 'Emisión normal', 'Tipo de emisión normal.', 1, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),

    (301, 3, '01', 'Factura', 'Factura electrónica.', 1, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (302, 3, '03', 'Liquidación de compra de bienes y prestación de servicios', 'Liquidación de compra de bienes y prestación de servicios.', 2, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (303, 3, '04', 'Nota de crédito', 'Nota de crédito electrónica.', 3, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (304, 3, '05', 'Nota de débito', 'Nota de débito electrónica.', 4, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (305, 3, '06', 'Guía de remisión', 'Guía de remisión electrónica.', 5, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (306, 3, '07', 'Comprobante de retención', 'Comprobante de retención electrónico.', 6, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),

    (401, 4, '04', 'RUC', 'Registro Único de Contribuyentes.', 1, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (402, 4, '05', 'Cédula', 'Cédula de identidad.', 2, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (403, 4, '06', 'Pasaporte', 'Pasaporte.', 3, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (404, 4, '07', 'Consumidor final', 'Consumidor final.', 4, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (405, 4, '08', 'Identificación del exterior', 'Identificación emitida en el exterior.', 5, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (406, 4, '09', 'Placa', 'Placa del vehículo.', 6, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),

    (501, 5, '01', 'Sin utilización del sistema financiero', NULL, 1, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (502, 5, '02', 'Cheque propio', NULL, 2, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (503, 5, '03', 'Cheque certificado', NULL, 3, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (504, 5, '04', 'Cheque de gerencia', NULL, 4, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (505, 5, '05', 'Cheque del exterior', NULL, 5, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (506, 5, '06', 'Débito de cuenta', NULL, 6, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (507, 5, '07', 'Transferencia propio banco', NULL, 7, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (508, 5, '08', 'Transferencia otro banco nacional', NULL, 8, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (509, 5, '09', 'Transferencia banco exterior', NULL, 9, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (510, 5, '10', 'Tarjeta de crédito nacional', NULL, 10, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (511, 5, '11', 'Tarjeta de crédito internacional', NULL, 11, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (512, 5, '12', 'Giro', NULL, 12, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (513, 5, '13', 'Depósito en cuenta (corriente/ahorros)', NULL, 13, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (514, 5, '14', 'Endoso de inversión', NULL, 14, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (515, 5, '15', 'Compensación de deudas', NULL, 15, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),

    (601, 6, '2', 'Impuesto al Valor Agregado (IVA)', 'Código SRI 2 para IVA.', 1, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (602, 6, '3', 'Impuesto a los Consumos Especiales (ICE)', 'Código SRI 3 para ICE.', 2, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP),
    (603, 6, '5', 'Impuesto Redimible a las Botellas Plásticas no Retornables (IRBPNR)', 'Código SRI 5 para IRBPNR.', 3, 'ACTIVO', 'FLYWAY', CURRENT_TIMESTAMP)
ON CONFLICT (catalogo_id, codigo) DO NOTHING;