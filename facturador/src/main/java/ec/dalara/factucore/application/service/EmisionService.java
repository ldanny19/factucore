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

	public ResultadoEmision resolver(Long empresaId, String codigoEstablecimiento, String codigoPuntoEmision) {
		Establecimiento establecimiento = establecimientoService
				.obtenerPorEmpresaYCodigo(empresaId, codigoEstablecimiento).orElseThrow();

		PuntoEmision puntoEmision = puntoEmisionService
				.obtenerPorEstablecimientoYCodigo(establecimiento.getId(), codigoPuntoEmision).orElseThrow();

		return new ResultadoEmision(establecimiento, puntoEmision);
	}

	public record ResultadoEmision(Establecimiento establecimiento, PuntoEmision puntoEmision) {
	}
}