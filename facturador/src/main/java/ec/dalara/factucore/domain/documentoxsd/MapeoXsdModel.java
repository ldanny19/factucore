package ec.dalara.factucore.domain.documentoxsd;

import ec.dalara.factucore.domain.shared.DomainException;

public final class MapeoXsdModel {

	private final Long id;
	private final Long versionDocumentoXsdId;
	private final String rutaOrigen;
	private final Long elementoXsdId;
	private final Long atributoXsdId;
	private final String tipoMapeo;

	public MapeoXsdModel(Long id, Long versionDocumentoXsdId, String rutaOrigen, Long elementoXsdId, Long atributoXsdId,
			String tipoMapeo) {
		if (versionDocumentoXsdId == null) {
			throw new DomainException("FACTUCORE.MAPEO_XSD.VERSION.REQUERIDA");
		}

		if (rutaOrigen == null || rutaOrigen.isBlank()) {
			throw new DomainException("FACTUCORE.MAPEO_XSD.RUTA_ORIGEN.REQUERIDA");
		}

		boolean elemento = elementoXsdId != null;
		boolean atributo = atributoXsdId != null;

		if (elemento == atributo) {
			throw new DomainException("FACTUCORE.MAPEO_XSD.DESTINO.INVALIDO");
		}

		if (tipoMapeo == null || tipoMapeo.isBlank()) {
			throw new DomainException("FACTUCORE.MAPEO_XSD.TIPO.REQUERIDO");
		}

		this.id = id;
		this.versionDocumentoXsdId = versionDocumentoXsdId;
		this.rutaOrigen = rutaOrigen;
		this.elementoXsdId = elementoXsdId;
		this.atributoXsdId = atributoXsdId;
		this.tipoMapeo = tipoMapeo;
	}

	public Long getId() {
		return id;
	}

	public Long getVersionDocumentoXsdId() {
		return versionDocumentoXsdId;
	}

	public String getRutaOrigen() {
		return rutaOrigen;
	}

	public Long getElementoXsdId() {
		return elementoXsdId;
	}

	public Long getAtributoXsdId() {
		return atributoXsdId;
	}

	public String getTipoMapeo() {
		return tipoMapeo;
	}

	public boolean esElemento() {
		return elementoXsdId != null;
	}

	public boolean esAtributo() {
		return atributoXsdId != null;
	}
}