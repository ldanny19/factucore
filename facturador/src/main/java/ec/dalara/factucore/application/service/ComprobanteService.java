package ec.dalara.factucore.application.service;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteService extends BaseService<Comprobante> {

	private final ComprobanteRepository comprobanteRepository;

	@Override
	protected BaseRepository<Comprobante, Long> getRepository() {
		return comprobanteRepository;
	}

	@Override
	@Transactional
	public Comprobante guardar(Comprobante e) {
		validar(e);
		boolean dup = e.getId() == null ? comprobanteRepository.existsByClaveAcceso(e.getClaveAcceso()) : comprobanteRepository.existsByClaveAccesoAndIdNot(e.getClaveAcceso(), e.getId());
		if (dup) throw new ApplicationException(MessageCodes.COMPROBANTE_CLAVE_ACCESO_DUPLICADA, e.getClaveAcceso());
		return super.guardar(e);
	}

	private void validar(Comprobante e) {
		if (e == null) throw new ApplicationException(MessageCodes.COMPROBANTE_REQUERIDO);
		if (e.getEmpresa() == null || e.getEmpresa().getId() == null) throw new ApplicationException(MessageCodes.COMPROBANTE_EMPRESA_REQUERIDA);
		if (e.getEstablecimiento() == null || e.getEstablecimiento().getId() == null) throw new ApplicationException(MessageCodes.COMPROBANTE_ESTABLECIMIENTO_REQUERIDO);
		if (e.getPuntoEmision() == null || e.getPuntoEmision().getId() == null) throw new ApplicationException(MessageCodes.COMPROBANTE_PUNTO_EMISION_REQUERIDO);
		if (e.getDocumentoXsd() == null || e.getDocumentoXsd().getId() == null) throw new ApplicationException(MessageCodes.COMPROBANTE_DOCUMENTO_XSD_REQUERIDO);
		if (e.getVersionDocumentoXsd() == null || e.getVersionDocumentoXsd().getId() == null) throw new ApplicationException(MessageCodes.COMPROBANTE_VERSION_DOCUMENTO_XSD_REQUERIDA);
		if (e.getAmbiente() == null || e.getAmbiente().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_AMBIENTE_REQUERIDO);
		if (e.getTipoEmision() == null || e.getTipoEmision().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_TIPO_EMISION_REQUERIDO);
		if (e.getCodigoDocumento() == null || e.getCodigoDocumento().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_CODIGO_DOCUMENTO_REQUERIDO);
		if (e.getSecuencial() == null || e.getSecuencial().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_NUMERO_REQUERIDO);
		if (e.getClaveAcceso() == null || e.getClaveAcceso().isBlank()) throw new ApplicationException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
		if (e.getFechaEmision() == null) throw new ApplicationException(MessageCodes.COMPROBANTE_FECHA_EMISION_REQUERIDA);
		if (e.getEstadoProceso() == null || e.getEstadoProceso().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_ESTADO_PROCESO_REQUERIDO);
		if (e.getDatosComprobante() == null || e.getDatosComprobante().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_DATOS_REQUERIDOS);
	}

	public Optional<Comprobante> obtenerPorClaveAcceso(String claveAcceso) {
		return comprobanteRepository.findByClaveAcceso(claveAcceso)
				.filter(comprobante -> EstadoRegistro.ACTIVO.equals(comprobante.getEstadoRegistro()));
	}

	public boolean existePorClaveAcceso(String claveAcceso) {
		return comprobanteRepository.findByClaveAcceso(claveAcceso)
				.filter(comprobante -> !EstadoRegistro.ELIMINADO.equals(comprobante.getEstadoRegistro())).isPresent();
	}

	public Optional<Comprobante> obtenerPorEmpresaEstablecimientoPuntoEmisionDocumentoSecuencial(Long empresaId,
			Long establecimientoId, Long puntoEmisionId, String codigoDocumento, String secuencial) {
		return comprobanteRepository
				.findByEmpresaIdAndEstablecimientoIdAndPuntoEmisionIdAndCodigoDocumentoAndSecuencial(empresaId,
						establecimientoId, puntoEmisionId, codigoDocumento, secuencial)
				.filter(comprobante -> EstadoRegistro.ACTIVO.equals(comprobante.getEstadoRegistro()));
	}
}