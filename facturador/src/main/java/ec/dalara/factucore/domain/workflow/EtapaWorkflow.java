package ec.dalara.factucore.domain.workflow;

/**
 * Identifica las etapas de negocio y de finalización que participan en el
 * workflow de facturación.
 *
 * <p>Este enum no define las transiciones. Las transiciones pertenecen al XML
 * de Camel. Una etapa solo identifica quién produjo el resultado.</p>
 */
public enum EtapaWorkflow {

	RECEPCION,
	VALIDACION,
	ASIGNACION_SECUENCIAL,
	GENERACION_CLAVE_ACCESO,
	PERSISTENCIA_COMPROBANTE,
	GENERACION_XML,
	VALIDACION_XSD,
	FIRMA_ELECTRONICA,
	ENVIO_SRI,
	AUTORIZACION_SRI,
	GENERACION_RIDE,
	NOTIFICACION,
	GENERAR_RESPUESTA,
	RESPUESTA_ERROR_NOTIFICACION,
	FIN
}
