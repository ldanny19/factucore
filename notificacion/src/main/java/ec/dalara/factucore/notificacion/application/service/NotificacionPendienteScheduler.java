package ec.dalara.factucore.notificacion.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionPendienteScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacionPendienteScheduler.class);

    private final NotificacionPersistenciaService persistenciaService;
    private final NotificacionService notificacionService;

    @Scheduled(fixedDelayString = "${notificacion.recuperacion.intervalo-ms:30000}")
    public void recuperarPendientes() {
        var pendientes = persistenciaService.pendientes();
        if (!pendientes.isEmpty()) {
            LOGGER.info("Se encontraron {} notificaciones pendientes para recuperación.", pendientes.size());
        }
        pendientes.forEach(notificacionService::procesarPendiente);
    }
}
