package ec.dalara.factucore.notificacion.infrastructure.message;

public final class MessageCodes {
    public static final String COMPROBANTE_REQUERIDO = "FACTUCORE.NOTIFICACION.COMPROBANTE.REQUERIDO";
    public static final String DATOS_REQUERIDOS = "FACTUCORE.NOTIFICACION.DATOS.REQUERIDOS";
    public static final String XML_COMPRESION_ERROR = "FACTUCORE.NOTIFICACION.XML.COMPRESION_ERROR";
    public static final String ARCHIVO_LECTURA_ERROR = "FACTUCORE.NOTIFICACION.ARCHIVO.LECTURA_ERROR";
    public static final String PLANTILLA_LECTURA_ERROR = "FACTUCORE.NOTIFICACION.PLANTILLA.LECTURA_ERROR";
    public static final String CORREO_ENVIO_ERROR = "FACTUCORE.NOTIFICACION.CORREO.ENVIO_ERROR";
    public static final String EVENTO_PROCESAMIENTO_ERROR = "FACTUCORE.NOTIFICACION.EVENTO.PROCESAMIENTO_ERROR";

    private MessageCodes() {
    }
}
