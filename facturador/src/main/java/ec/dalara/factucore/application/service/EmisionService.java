package ec.dalara.factucore.application.service;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmisionService {

	private final EstablecimientoService establecimientoService;
	private final PuntoEmisionService puntoEmisionService;

	public ResultadoEmision resolver(Long empresaId, Long establecimientoId, Long puntoEmisionId) {
		Establecimiento establecimiento = establecimientoService
				.obtenerPorId(establecimientoId).filter(item -> empresaId != null && empresaId.equals(item.getEmpresa().getId())).orElseThrow();

		PuntoEmision puntoEmision = puntoEmisionService
				.obtenerPorId(puntoEmisionId).filter(item -> item.getEstablecimiento() != null && establecimiento.getId().equals(item.getEstablecimiento().getId())).orElseThrow();

		return new ResultadoEmision(establecimiento, puntoEmision);
	}

	public record ResultadoEmision(Establecimiento establecimiento, PuntoEmision puntoEmision) {
	}
}