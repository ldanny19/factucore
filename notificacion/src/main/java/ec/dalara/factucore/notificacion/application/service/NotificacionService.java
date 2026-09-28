package ec.dalara.factucore.notificacion.application.service;
import java.nio.file.Files; import java.nio.file.Path; import java.util.Map; import java.util.Objects; import java.util.zip.ZipEntry; import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service; import org.springframework.util.StringUtils;
import ec.dalara.factucore.messaging.api.notificacion.ComprobanteAutorizado; import ec.dalara.factucore.notificacion.application.port.out.CorreoPort; import ec.dalara.factucore.notificacion.infrastructure.config.NotificacionProperties; import ec.dalara.factucore.notificacion.infrastructure.template.PlantillaCorreoService; import lombok.RequiredArgsConstructor;
@Service @RequiredArgsConstructor
public class NotificacionService {
 private final CorreoPort correoPort; private final PlantillaCorreoService plantillaCorreoService; private final NotificacionProperties properties;
 public void notificar(ComprobanteAutorizado c){ Objects.requireNonNull(c,"comprobante"); validar(c); Path xml=Path.of(c.rutaXmlAutorizado()); Path ride=Path.of(c.rutaRide());
  String contenido=plantillaCorreoService.renderizar(Map.of("nombreCliente",valor(c.nombreCliente()),"nombreEmpresa",valor(c.nombreEmpresa()),"fechaComprobante",c.fechaComprobante()==null?"":c.fechaComprobante().toString()));
  correoPort.enviar(c.correo(),properties.getCorreo().getAsunto(),contenido,new CorreoPort.AdjuntoCorreo(nombreZip(xml),"application/zip",comprimirXml(xml)),new CorreoPort.AdjuntoCorreo(ride.getFileName().toString(),"application/pdf",leer(ride))); }
 private void validar(ComprobanteAutorizado c){if(!StringUtils.hasText(c.correo())||!StringUtils.hasText(c.rutaXmlAutorizado())||!StringUtils.hasText(c.rutaRide())) throw new IllegalArgumentException("Datos requeridos para notificación incompletos");}
 private byte[] comprimirXml(Path p){try(var out=new java.io.ByteArrayOutputStream();var zip=new ZipOutputStream(out)){zip.putNextEntry(new ZipEntry(p.getFileName().toString()));zip.write(leer(p));zip.closeEntry();return out.toByteArray();}catch(Exception e){throw new IllegalStateException("No fue posible comprimir el XML autorizado",e);}}
 private byte[] leer(Path p){try{return Files.readAllBytes(p);}catch(Exception e){throw new IllegalStateException("No fue posible leer el archivo: "+p,e);}}
 private String nombreZip(Path p){String n=p.getFileName().toString();return n.endsWith(".xml")?n.substring(0,n.length()-4)+".zip":n+".zip";} private String valor(String v){return v==null?"":v;}
}