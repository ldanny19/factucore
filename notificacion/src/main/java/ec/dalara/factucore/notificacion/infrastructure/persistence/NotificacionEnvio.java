package ec.dalara.factucore.notificacion.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "NOTIFICACION_ENVIO")
@Getter
@Setter
@NoArgsConstructor
public class NotificacionEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_EVENTO", nullable = false, unique = true, length = 100)
    private String idEvento;

    @Column(name = "ID_TRANSACCION", length = 100)
    private String idTransaccion;

    @Column(name = "EVENTO_JSON", nullable = false, columnDefinition = "text")
    private String eventoJson;

    @Column(name = "DESTINATARIO", nullable = false, length = 320)
    private String destinatario;

    @Column(name = "ASUNTO", nullable = false, length = 500)
    private String asunto;

    @Column(name = "CONTENIDO_HTML", columnDefinition = "text")
    private String contenidoHtml;

    @Column(name = "RUTA_XML_AUTORIZADO", nullable = false, length = 1000)
    private String rutaXmlAutorizado;

    @Column(name = "RUTA_RIDE", nullable = false, length = 1000)
    private String rutaRide;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_ENVIO", nullable = false, length = 20)
    private EstadoEnvio estadoEnvio;

    @Column(name = "NUMERO_INTENTOS", nullable = false)
    private int numeroIntentos;

    @Column(name = "FECHA_RECEPCION", nullable = false)
    private Instant fechaRecepcion;

    @Column(name = "FECHA_ENVIO")
    private Instant fechaEnvio;

    @Column(name = "RESPUESTA_ENVIO", columnDefinition = "text")
    private String respuestaEnvio;

    @Column(name = "FECHA_CREACION", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "FECHA_MODIFICACION", nullable = false)
    private Instant fechaModificacion;

    public enum EstadoEnvio {
        PENDIENTE,
        EN_PROCESO,
        ENVIADA,
        FALLIDA
    }
}
