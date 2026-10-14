package ec.dalara.factucore.notificacion.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.notificacion.ComprobanteAutorizado;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvio;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvio.EstadoEnvio;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacionPersistenciaService {

    private final NotificacionEnvioRepository repository;

    @Transactional
    public NotificacionEnvio registrar(EventoMensaje evento, ComprobanteAutorizado comprobante, String eventoJson,
            String asunto) {
        Optional<NotificacionEnvio> existente = repository.findByIdEvento(evento.id());
        if (existente.isPresent()) {
            return existente.get();
        }

        Instant ahora = Instant.now();
        NotificacionEnvio envio = new NotificacionEnvio();
        envio.setIdEvento(evento.id());
        envio.setIdTransaccion(comprobante.idTransaccion());
        envio.setEventoJson(eventoJson);
        envio.setDestinatario(comprobante.correo());
        envio.setAsunto(asunto);
        envio.setRutaXmlAutorizado(comprobante.rutaXmlAutorizado());
        envio.setRutaRide(comprobante.rutaRide());
        envio.setEstadoEnvio(EstadoEnvio.PENDIENTE);
        envio.setNumeroIntentos(0);
        envio.setFechaRecepcion(ahora);
        envio.setFechaCreacion(ahora);
        envio.setFechaModificacion(ahora);

        return repository.saveAndFlush(envio);
    }

    @Transactional
    public void guardarContenidoHtml(Long id, String contenidoHtml) {
        NotificacionEnvio envio = obtener(id);
        if (envio.getContenidoHtml() == null) {
            envio.setContenidoHtml(contenidoHtml);
        }
        envio.setFechaModificacion(Instant.now());
    }

    @Transactional
    public boolean marcarEnProceso(Long id) {
        return repository.actualizarEstadoSiCoincide(id, EstadoEnvio.PENDIENTE, EstadoEnvio.EN_PROCESO,
                Instant.now()) == 1;
    }

    @Transactional
    public void registrarIntento(Long id, String respuesta, boolean enviado, boolean definitivo) {
        NotificacionEnvio envio = obtener(id);
        envio.setNumeroIntentos(envio.getNumeroIntentos() + 1);
        envio.setRespuestaEnvio(respuesta);
        envio.setFechaModificacion(Instant.now());

        if (enviado) {
            envio.setEstadoEnvio(EstadoEnvio.ENVIADA);
            envio.setFechaEnvio(Instant.now());
        } else if (definitivo) {
            envio.setEstadoEnvio(EstadoEnvio.FALLIDA);
        } else {
            envio.setEstadoEnvio(EstadoEnvio.PENDIENTE);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificacionEnvio> pendientes() {
        return repository.findTop50ByEstadoEnvioOrderByFechaRecepcionAsc(EstadoEnvio.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public NotificacionEnvio obtener(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new IllegalStateException("No existe el registro de notificación " + id));
    }
}
