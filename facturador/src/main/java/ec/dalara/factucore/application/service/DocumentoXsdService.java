package ec.dalara.factucore.application.service;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.DocumentoXsdRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentoXsdService extends BaseService<DocumentoXsd> {

	private final DocumentoXsdRepository documentoXsdRepository;

	@Override
	protected BaseRepository<DocumentoXsd, Long> getRepository() {
		return documentoXsdRepository;
	}

	@Override
	@Transactional
	public DocumentoXsd guardar(DocumentoXsd documento) {
		validar(documento);
		boolean duplicado = documento.getId() == null ? documentoXsdRepository.existsByCodigo(documento.getCodigo()) : documentoXsdRepository.existsByCodigoAndIdNot(documento.getCodigo(), documento.getId());
		if (duplicado) throw new ApplicationException(MessageCodes.DOCUMENTO_XSD_CODIGO_DUPLICADO, documento.getCodigo());
		return super.guardar(documento);
	}

	public Optional<DocumentoXsd> obtenerPorCodigo(String codigo) {
		return documentoXsdRepository.findByCodigo(codigo)
				.filter(documento -> EstadoRegistro.ACTIVO.equals(documento.getEstadoRegistro()));
	}

	private void validar(DocumentoXsd documento) {
		if (documento == null) throw new ApplicationException(MessageCodes.DOCUMENTO_XSD_REQUERIDO);
		if (documento.getCodigo() == null || documento.getCodigo().isBlank()) throw new ApplicationException(MessageCodes.DOCUMENTO_XSD_CODIGO_REQUERIDO);
		if (documento.getNombre() == null || documento.getNombre().isBlank()) throw new ApplicationException(MessageCodes.DOCUMENTO_XSD_NOMBRE_REQUERIDO);
		if (documento.getTipoDocumento() == null || documento.getTipoDocumento().isBlank()) throw new ApplicationException(MessageCodes.DOCUMENTO_XSD_TIPO_DOCUMENTO_REQUERIDO);
	}

	public boolean existePorCodigo(String codigo) {
		return documentoXsdRepository.findByCodigo(codigo)
				.filter(documento -> !EstadoRegistro.ELIMINADO.equals(documento.getEstadoRegistro())).isPresent();
	}
}