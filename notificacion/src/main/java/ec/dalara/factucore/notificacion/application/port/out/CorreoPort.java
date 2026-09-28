package ec.dalara.factucore.notificacion.application.port.out;
public interface CorreoPort { void enviar(String destinatario, String asunto, String contenidoHtml, AdjuntoCorreo... adjuntos);
record AdjuntoCorreo(String nombre, String tipoContenido, byte[] contenido) {} }