package ec.dalara.factucore.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteAuditoriaRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteAuditoriaService extends BaseService<ComprobanteAuditoria> {

	private static final String RESULTADO_EXITOSO = "EXITOSO";
	private static final String RESULTADO_ERROR = "ERROR";

	private final ComprobanteAuditoriaRepository comprobanteAuditoriaRepository;
	private final ComprobanteRepository comprobanteRepository;
	private final Clock factuCoreClock;

	@Override
	protected BaseRepository<ComprobanteAuditoria, Long> getRepository() {
		return comprobanteAuditoriaRepository;
	}

	@Transactional
	public void registrarResultado(Long comprobanteId, String etapa, String estadoAnterior, ResultadoEtapa resultado,
			LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		if (comprobanteId == null || resultado == null || etapa == null || etapa.isBlank()) {
			return;
		}

		var comprobante = comprobanteRepository.findById(comprobanteId).orElse(null);
		if (comprobante == null) {
			return;
		}

		int intento = siguienteIntento(comprobanteId, etapa);
		var ahora = LocalDateTime.now(factuCoreClock);

		var auditoria = ComprobanteAuditoria.builder().comprobante(comprobante).etapa(etapa)
				.resultado(resultado.isExitosa() ? RESULTADO_EXITOSO : RESULTADO_ERROR).estadoAnterior(estadoAnterior)
				.estadoNuevo(resultado.getEstado()).codigoError(resultado.getCodigoError())
				.mensajeError(resultado.getMensaje()).fechaInicio(fechaInicio == null ? ahora : fechaInicio)
				.fechaFin(fechaFin == null ? ahora : fechaFin).intento(intento).estadoRegistro(EstadoRegistro.ACTIVO)
				.observacion(null).build();

		comprobanteAuditoriaRepository.save(auditoria);
	}

	@Transactional
	public void registrarError(Long comprobanteId, String etapa, String estadoAnterior, String codigoError,
			String mensajeError, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		if (comprobanteId == null || etapa == null || etapa.isBlank()) {
			return;
		}

		var comprobante = comprobanteRepository.findById(comprobanteId).orElse(null);
		if (comprobante == null) {
			return;
		}

		int intento = siguienteIntento(comprobanteId, etapa);
		var ahora = LocalDateTime.now(factuCoreClock);

		var auditoria = ComprobanteAuditoria.builder().comprobante(comprobante).etapa(etapa).resultado(RESULTADO_ERROR)
				.estadoAnterior(estadoAnterior).estadoNuevo(comprobante.getEstadoProceso()).codigoError(codigoError)
				.mensajeError(mensajeError).fechaInicio(fechaInicio == null ? ahora : fechaInicio)
				.fechaFin(fechaFin == null ? ahora : fechaFin).intento(intento).estadoRegistro(EstadoRegistro.ACTIVO)
				.observacion(null).build();

		comprobanteAuditoriaRepository.save(auditoria);
	}

	private int siguienteIntento(Long comprobanteId, String etapa) {
		return comprobanteAuditoriaRepository.findByComprobanteIdAndEtapaOrderByIntentoDesc(comprobanteId, etapa)
				.stream().findFirst().map(ComprobanteAuditoria::getIntento).map(actual -> actual + 1).orElse(1);
	}

	public List<ComprobanteAuditoria> listarPorComprobante(Long comprobanteId) {
		return comprobanteAuditoriaRepository.findByComprobanteIdOrderByFechaCreacionDesc(comprobanteId).stream()
				.filter(auditoria -> !EstadoRegistro.ELIMINADO.equals(auditoria.getEstadoRegistro())).toList();
	}
}
