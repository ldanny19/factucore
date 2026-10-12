-- Catálogo de códigos de errores, advertencias y estados devueltos por el SRI.
-- Los estados usan los valores definidos por EstadoRegistro (A/I/X).
INSERT INTO catalogo (
    id, codigo, nombre, descripcion, estado_registro, usuario_creacion, fecha_creacion
) VALUES (
    7, 'COD_ERROR_SRI', 'Códigos de errores y advertencias del SRI',
    'Códigos reportados por los servicios de recepción y autorización del SRI.',
    'A', 'FLYWAY', CURRENT_TIMESTAMP
)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO catalogo_item (
    id, catalogo_id, codigo, nombre, descripcion, orden,
    estado_registro, usuario_creacion, fecha_creacion
)
SELECT datos.id, catalogo.id, datos.codigo, datos.nombre, datos.descripcion, datos.orden,
       'A', 'FLYWAY', CURRENT_TIMESTAMP
FROM catalogo
CROSS JOIN (VALUES
    (701, '2',  'ERROR - AUTORIZACION - RUC del emisor no activo', 'Verificar que el RUC del emisor se encuentre en estado ACTIVO.', 1),
    (702, '10', 'ERROR - AUTORIZACION - Establecimiento del emisor clausurado', 'El servicio se habilitará una vez concluida la clausura.', 2),
    (703, '26', 'ERROR - RECEPCION - Tamaño máximo superado', 'El tamaño del archivo supera el límite establecido.', 3),
    (704, '27', 'ERROR - AUTORIZACION - Clase no permitida', 'La clase del contribuyente no puede emitir comprobantes electrónicos.', 4),
    (705, '28', 'ERROR - RECEPCION - Acuerdo de medios electrónicos no aceptado', 'El contribuyente debe aceptar el acuerdo de medios electrónicos.', 5),
    (706, '35', 'ERROR - RECEPCION - Documento inválido', 'El XML no supera la validación de esquema.', 6),
    (707, '36', 'ERROR - RECEPCION - Versión de esquema descontinuada', 'Verificar que la versión del esquema sea la vigente para el documento.', 7),
    (708, '37', 'ERROR - AUTORIZACION - RUC sin autorización de emisión', 'Verificar que el RUC tenga autorización para emitir comprobantes electrónicos.', 8),
    (709, '39', 'ERROR - AUTORIZACION - Firma inválida', 'Verificar la firma electrónica del emisor.', 9),
    (710, '40', 'ERROR - AUTORIZACION - Error en el certificado', 'Verificar que el certificado exista y pueda convertirse a X509.', 10),
    (711, '59', 'ADVERTENCIA - AUTORIZACION - Identificación no existe', 'Verificar la identificación del adquirente.', 11),
    (712, '60', 'ADVERTENCIA - AUTORIZACION - Ambiente de ejecución', 'Advertencia esperada en ambiente de pruebas/certificación.', 12),
    (713, '62', 'ADVERTENCIA - AUTORIZACION - Identificación incorrecta', 'Verificar el número de identificación del adquirente.', 13),
    (714, '68', 'ADVERTENCIA - AUTORIZACION - Documento sustento', 'Verificar la existencia del comprobante relacionado.', 14),
    (715, '70', 'ESTADO - AUTORIZACION - Clave de acceso en procesamiento', 'No reenviar ni generar otra clave/secuencial; esperar respuesta final del SRI.', 15),
    (716, '80', 'ERROR - AUTORIZACION - Estructura de clave de acceso', 'Verificar que la clave tenga 49 dígitos numéricos y que el parámetro no esté vacío.', 16)
) AS datos(id, codigo, nombre, descripcion, orden)
WHERE catalogo.codigo = 'COD_ERROR_SRI'
ON CONFLICT (catalogo_id, codigo) DO NOTHING;
