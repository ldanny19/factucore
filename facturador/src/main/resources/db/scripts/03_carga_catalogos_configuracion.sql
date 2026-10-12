-- Carga inicial consolidada de catálogos y configuración de empresa.
-- Los códigos funcionales del SRI se conservan sin cambios.
-- El ID técnico de catalogo_item se asigna secuencialmente desde 1.
-- configuracion_empresa conserva los datos del CSV y requiere que exista
-- previamente la empresa referenciada (empresa_id = 4).

BEGIN;

INSERT INTO catalogo (
    id, codigo, nombre, descripcion, estado_registro, usuario_creacion, usuario_modificacion, fecha_creacion, fecha_modificacion, observacion
) VALUES
    (1, 'AMBIENTE', 'Ambiente', 'Ambientes definidos por el SRI para comprobantes electrónicos.', 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (2, 'TIPO_EMISION', 'Tipo de emisión', 'Tipos de emisión definidos por el SRI.', 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (3, 'TIPO_DOCUMENTO', 'Tipo de documento', 'Tipos de comprobantes electrónicos soportados por FactuCore.', 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (4, 'TIPO_IDENTIFICACION', 'Tipo de identificación', 'Tipos de identificación del receptor definidos por el SRI.', 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (5, 'FORMA_PAGO', 'Forma de pago', 'Formas de pago definidas por el SRI.', 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (6, 'CODIGO_IMPUESTO', 'Código de impuesto', 'Códigos de impuesto utilizados en comprobantes electrónicos.', 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (7, 'COD_ERROR_SRI', 'Códigos de errores y advertencias del SRI', 'Códigos reportados por los servicios de recepción y autorización del SRI.', 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (8, 'TARIFA_IVA', 'Tarifas del IVA', 'Códigos de porcentaje de IVA para asociar con sus vigencias tributarias.', 'A', 'MANUAL', NULL, '2026-10-12 03:45:29.093979', NULL, NULL)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO catalogo_item (
    id, catalogo_id, codigo, nombre, descripcion, orden, estado_registro, usuario_creacion, usuario_modificacion, fecha_creacion, fecha_modificacion, observacion
) VALUES
    (1, 1, '1', 'Pruebas', 'Ambiente de pruebas del SRI.', 1, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (2, 1, '2', 'Producción', 'Ambiente de producción del SRI.', 2, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (3, 2, '1', 'Emisión normal', 'Tipo de emisión normal.', 1, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (4, 3, '01', 'Factura', 'Factura electrónica.', 1, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (5, 3, '03', 'Liquidación de compra de bienes y prestación de servicios', 'Liquidación de compra de bienes y prestación de servicios.', 2, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (6, 3, '04', 'Nota de crédito', 'Nota de crédito electrónica.', 3, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (7, 3, '05', 'Nota de débito', 'Nota de débito electrónica.', 4, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (8, 3, '06', 'Guía de remisión', 'Guía de remisión electrónica.', 5, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (9, 3, '07', 'Comprobante de retención', 'Comprobante de retención electrónico.', 6, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (10, 4, '04', 'RUC', 'Registro Único de Contribuyentes.', 1, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (11, 4, '05', 'Cédula', 'Cédula de identidad.', 2, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (12, 4, '06', 'Pasaporte', 'Pasaporte.', 3, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (13, 4, '07', 'Consumidor final', 'Consumidor final.', 4, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (14, 4, '08', 'Identificación del exterior', 'Identificación emitida en el exterior.', 5, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (15, 4, '09', 'Placa', 'Placa del vehículo.', 6, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (16, 5, '01', 'Sin utilización del sistema financiero', NULL, 1, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (17, 5, '02', 'Cheque propio', NULL, 2, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (18, 5, '03', 'Cheque certificado', NULL, 3, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (19, 5, '04', 'Cheque de gerencia', NULL, 4, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (20, 5, '05', 'Cheque del exterior', NULL, 5, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (21, 5, '06', 'Débito de cuenta', NULL, 6, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (22, 5, '07', 'Transferencia propio banco', NULL, 7, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (23, 5, '08', 'Transferencia otro banco nacional', NULL, 8, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (24, 5, '09', 'Transferencia banco exterior', NULL, 9, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (25, 5, '10', 'Tarjeta de crédito nacional', NULL, 10, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (26, 5, '11', 'Tarjeta de crédito internacional', NULL, 11, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (27, 5, '12', 'Giro', NULL, 12, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (28, 5, '13', 'Depósito en cuenta (corriente/ahorros)', NULL, 13, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (29, 5, '14', 'Endoso de inversión', NULL, 14, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (30, 5, '15', 'Compensación de deudas', NULL, 15, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (31, 6, '2', 'Impuesto al Valor Agregado (IVA)', 'Código SRI 2 para IVA.', 1, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (32, 6, '3', 'Impuesto a los Consumos Especiales (ICE)', 'Código SRI 3 para ICE.', 2, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (33, 6, '5', 'Impuesto Redimible a las Botellas Plásticas no Retornables (IRBPNR)', 'Código SRI 5 para IRBPNR.', 3, 'A', 'FLYWAY', NULL, '2026-09-29 23:52:12.935712', NULL, NULL),
    (34, 7, '2', 'ERROR - AUTORIZACION - RUC del emisor no activo', 'Verificar que el RUC del emisor se encuentre en estado ACTIVO.', 1, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (35, 7, '10', 'ERROR - AUTORIZACION - Establecimiento del emisor clausurado', 'El servicio se habilitará una vez concluida la clausura.', 2, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (36, 7, '26', 'ERROR - RECEPCION - Tamaño máximo superado', 'El tamaño del archivo supera el límite establecido.', 3, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (37, 7, '27', 'ERROR - AUTORIZACION - Clase no permitida', 'La clase del contribuyente no puede emitir comprobantes electrónicos.', 4, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (38, 7, '28', 'ERROR - RECEPCION - Acuerdo de medios electrónicos no aceptado', 'El contribuyente debe aceptar el acuerdo de medios electrónicos.', 5, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (39, 7, '35', 'ERROR - RECEPCION - Documento inválido', 'El XML no supera la validación de esquema.', 6, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (40, 7, '36', 'ERROR - RECEPCION - Versión de esquema descontinuada', 'Verificar que la versión del esquema sea la vigente para el documento.', 7, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (41, 7, '37', 'ERROR - AUTORIZACION - RUC sin autorización de emisión', 'Verificar que el RUC tenga autorización para emitir comprobantes electrónicos.', 8, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (42, 7, '39', 'ERROR - AUTORIZACION - Firma inválida', 'Verificar la firma electrónica del emisor.', 9, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (43, 7, '40', 'ERROR - AUTORIZACION - Error en el certificado', 'Verificar que el certificado exista y pueda convertirse a X509.', 10, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (44, 7, '59', 'ADVERTENCIA - AUTORIZACION - Identificación no existe', 'Verificar la identificación del adquirente.', 11, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (45, 7, '60', 'ADVERTENCIA - AUTORIZACION - Ambiente de ejecución', 'Advertencia esperada en ambiente de pruebas/certificación.', 12, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (46, 7, '62', 'ADVERTENCIA - AUTORIZACION - Identificación incorrecta', 'Verificar el número de identificación del adquirente.', 13, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (47, 7, '68', 'ADVERTENCIA - AUTORIZACION - Documento sustento', 'Verificar la existencia del comprobante relacionado.', 14, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (48, 7, '70', 'ESTADO - AUTORIZACION - Clave de acceso en procesamiento', 'No reenviar ni generar otra clave/secuencial; esperar respuesta final del SRI.', 15, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (49, 7, '80', 'ERROR - AUTORIZACION - Estructura de clave de acceso', 'Verificar que la clave tenga 49 dígitos numéricos y que el parámetro no esté vacío.', 16, 'A', 'FLYWAY', NULL, '2026-10-09 21:54:32.464338', NULL, NULL),
    (50, 8, '0', 'Código IVA 0', 'Código de porcentaje IVA 0.', 1, 'A', 'MANUAL', NULL, '2026-10-12 03:45:29.093979', NULL, NULL),
    (51, 8, '2', 'Código IVA 2', 'Código de porcentaje IVA 2.', 2, 'A', 'MANUAL', NULL, '2026-10-12 03:45:29.093979', NULL, NULL),
    (52, 8, '3', 'Código IVA 3', 'Código de porcentaje IVA 3.', 3, 'A', 'MANUAL', NULL, '2026-10-12 03:45:29.093979', NULL, NULL),
    (53, 8, '4', 'Código IVA 4', 'Código de porcentaje IVA 4.', 4, 'A', 'MANUAL', NULL, '2026-10-12 03:45:29.093979', NULL, NULL),
    (54, 8, '5', 'Código IVA 5', 'Código de porcentaje IVA 5.', 5, 'A', 'MANUAL', NULL, '2026-10-12 03:45:29.093979', NULL, NULL),
    (55, 8, '6', 'IVA no objeto', 'Código de IVA no objeto.', 6, 'A', 'MANUAL', NULL, '2026-10-12 03:45:29.093979', NULL, NULL),
    (56, 8, '7', 'IVA exento', 'Código de IVA exento.', 7, 'A', 'MANUAL', NULL, '2026-10-12 03:45:29.093979', NULL, NULL)
ON CONFLICT (catalogo_id, codigo) DO NOTHING;

INSERT INTO configuracion_empresa (
    id, empresa_id, clave, valor, tipo_dato, fecha_vigencia_desde, fecha_vigencia_hasta, estado_registro, usuario_creacion, usuario_modificacion, fecha_creacion, fecha_modificacion, observacion
) VALUES
    (1, 4, 'AMBIENTE', '1', 'STRING', '2025-12-31 19:00:00', NULL, 'A', 'jucarsa', NULL, '2026-10-05 11:08:44.941533', NULL, NULL),
    (2, 4, 'LOGO', '/app/imagenes/logo.jpg', 'STRING', '2025-12-31 19:00:00', NULL, 'A', 'jucarsa', NULL, '2026-10-11 07:44:39.751036', NULL, NULL),
    (3, 4, 'TIPO_EMISION', '1', 'STRING', '2025-12-31 19:00:00', NULL, 'A', 'jucarsa', NULL, '2026-10-11 07:47:12.400131', NULL, NULL),
    (4, 4, 'TIEMPO_MAX_COD_70', '86400', 'INTEGER', '2025-12-31 19:00:00', NULL, 'A', 'jucarsa', NULL, '2026-10-12 05:49:56.818025', NULL, NULL),
    (5, 4, 'TIEMPO_INTERVALO_COD_70', '60', 'INTEGER', '2025-12-31 19:00:00', NULL, 'A', 'jucarsa', NULL, '2026-10-12 05:50:28.436526', NULL, NULL)
ON CONFLICT (empresa_id, clave, fecha_vigencia_desde) DO NOTHING;

COMMIT;
