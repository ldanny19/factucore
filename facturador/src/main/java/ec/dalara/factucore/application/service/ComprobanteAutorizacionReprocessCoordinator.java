package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import ec.dalara.factucore.application.event.ComprobanteAutorizacionPendienteEvent;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteAutorizacionReprocessCoordinator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComprobanteAutorizacionReprocessCoordinator.class);
    private static final String PREFIJO_TIEMPO_MAXIMO = "TIEMPO_MAXIMO_COD_";
    private static final String PREFIJO_TIEMPO_INTERVALO = "TIEMPO_INTERVALO_COD_";

    private final ComprobanteRepository comprobanteRepository;
    private final ConfiguracionEmpresaService configuracionEmpresaService;
    private final ComprobanteAuditoriaService comprobanteAuditoriaService;
    private final ComprobanteReprocessService comprobanteReprocessService;

    private final Set<Long> procesosActivos = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @EventListener(ApplicationReadyEvent.class)
    public void recuperarPendientesAlIniciar() {
        var pendientes = comprobanteRepository.findByEstadoProcesoAndEstadoRegistro(
                EstadoProceso.AUTORIZACION_PENDIENTE.name(), EstadoRegistro.ACTIVO);
        for (Comprobante comprobante : pendientes) {
            iniciarSiConfigurado(comprobante);
        }
        LOGGER.info("Recuperación inicial de autorización SRI finalizada. Pendientes encontrados: {}.", pendientes.size());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void procesarNuevoPendiente(ComprobanteAutorizacionPendienteEvent evento) {
        if (evento != null && evento.comprobanteId() != null) {
            iniciarSiConfigurado(evento.comprobanteId());
        }
    }

    private void iniciarSiConfigurado(Long comprobanteId) {
        if (comprobanteId == null) {
            return;
        }
        Comprobante comprobante = comprobanteRepository.findByIdAndEstadoRegistro(comprobanteId, EstadoRegistro.ACTIVO)
                .orElse(null);
        iniciarSiConfigurado(comprobante);
    }

    private void iniciarSiConfigurado(Comprobante comprobante) {
        if (comprobante == null || comprobante.getId() == null || !procesosActivos.add(comprobante.getId())) {
            return;
        }
        Long comprobanteId = comprobante.getId();
        try {
            PoliticaReintento politica = obtenerPolitica(comprobante);
            if (politica == null) {
                procesosActivos.remove(comprobanteId);
                return;
            }
            executor.submit(() -> ejecutarReproceso(comprobanteId, politica));
        } catch (RuntimeException exception) {
            procesosActivos.remove(comprobanteId);
            LOGGER.error("No fue posible iniciar el reproceso del comprobante {}.", comprobanteId, exception);
        }
    }

    private PoliticaReintento obtenerPolitica(Comprobante comprobante) {
        if (comprobante == null
                || !EstadoProceso.AUTORIZACION_PENDIENTE.name().equals(comprobante.getEstadoProceso())
                || comprobante.getCodigoError() == null || comprobante.getCodigoError().isBlank()
                || comprobante.getEmpresa() == null || comprobante.getEmpresa().getId() == null) {
            return null;
        }
        Long empresaId = comprobante.getEmpresa().getId();
        String codigo = comprobante.getCodigoError().trim();
        Long tiempoMaximo = obtenerSegundosConfigurados(empresaId, PREFIJO_TIEMPO_MAXIMO + codigo);
        Long intervalo = obtenerSegundosConfigurados(empresaId, PREFIJO_TIEMPO_INTERVALO + codigo);
        LocalDateTime primerEnvio = comprobanteAuditoriaService.obtenerFechaPrimerEnvio(comprobante.getId());
        if (tiempoMaximo == null || intervalo == null || primerEnvio == null) {
            LOGGER.debug("El comprobante {} no tiene una política completa de reproceso para el código {}.",
                    comprobante.getId(), codigo);
            return null;
        }
        LocalDateTime limite = primerEnvio.plusSeconds(tiempoMaximo);
        if (!LocalDateTime.now().isBefore(limite)) {
            LOGGER.info("El tiempo máximo de reproceso del comprobante {} ya venció.", comprobante.getId());
            return null;
        }
        return new PoliticaReintento(limite, intervalo);
    }

    private Long obtenerSegundosConfigurados(Long empresaId, String clave) {
        var configuracion = configuracionEmpresaService.obtenerVigente(empresaId, clave, LocalDateTime.now());
        if (configuracion.isEmpty() || configuracion.get().getValor() == null
                || configuracion.get().getValor().isBlank()) {
            return null;
        }
        try {
            long segundos = Long.parseLong(configuracion.get().getValor().trim());
            return segundos > 0 ? segundos : null;
        } catch (NumberFormatException exception) {
            LOGGER.warn("El parámetro {} debe contener segundos enteros positivos.", clave);
            return null;
        }
    }

    private void ejecutarReproceso(Long comprobanteId, PoliticaReintento politica) {
        try {
            while (!Thread.currentThread().isInterrupted() && LocalDateTime.now().isBefore(politica.limite())) {
                boolean pendiente;
                try {
                    comprobanteReprocessService.reprocesar(comprobanteId);
                    pendiente = comprobanteReprocessService.estaPendienteAutorizacion(comprobanteId);
                } catch (RuntimeException exception) {
                    LOGGER.warn("Falló un intento de autorización SRI del comprobante {}.", comprobanteId, exception);
                    pendiente = true;
                }
                if (!pendiente) {
                    return;
                }
                long milisegundosRestantes = java.time.Duration.between(LocalDateTime.now(), politica.limite()).toMillis();
                if (milisegundosRestantes <= 0) {
                    return;
                }
                TimeUnit.MILLISECONDS.sleep(Math.min(TimeUnit.SECONDS.toMillis(politica.intervaloSegundos()),
                        milisegundosRestantes));
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.debug("Se interrumpió el reproceso del comprobante {}.", comprobanteId);
        } finally {
            procesosActivos.remove(comprobanteId);
            LOGGER.debug("Finalizó y se liberó el hilo de reproceso del comprobante {}.", comprobanteId);
        }
    }

    @PreDestroy
    public void detenerProcesos() {
        executor.shutdownNow();
    }

    private record PoliticaReintento(LocalDateTime limite, long intervaloSegundos) {
    }
}
