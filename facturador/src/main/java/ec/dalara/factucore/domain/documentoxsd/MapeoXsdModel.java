package ec.dalara.factucore.domain.documentoxsd;

import ec.dalara.factucore.domain.shared.DomainException;
import ec.dalara.factucore.domain.shared.MessageCodes;

public final class MapeoXsdModel {

	private final Long id;
	private final Long versionDocumentoXsdId;
	private final String tipoOrigen;
	private final String origen;
	private final Long elementoXsdId;
	private final Long atributoXsdId;
	private final String tipoMapeo;

	public MapeoXsdModel(Long id, Long versionDocumentoXsdId, String tipoOrigen, String origen, Long elementoXsdId,
			Long atributoXsdId, String tipoMapeo) {
		if (versionDocumentoXsdId == null) {
			throw new DomainException(MessageCodes.MAPEO_XSD_VERSION_REQUERIDA);
		}
		if (tipoOrigen == null || tipoOrigen.isBlank()) {
			throw new DomainException(MessageCodes.MAPEO_XSD_TIPO_ORIGEN_REQUERIDO);
		}
		if (!"FACTUCORE".equals(tipoOrigen) && !"JSON".equals(tipoOrigen) && !"GENERADO".equals(tipoOrigen)) {
			throw new DomainException(MessageCodes.MAPEO_XSD_TIPO_ORIGEN_INVALIDO);
		}
		if (origen == null || origen.isBlank()) {
			throw new DomainException("FACTUCORE.MAPEO_XSD.ORIGEN.REQUERIDO");
		}

		boolean elemento = elementoXsdId != null;
		boolean atributo = atributoXsdId != null;
		if (elemento == atributo) {
			throw new DomainException("FACTUCORE.MAPEO_XSD.DESTINO.INVALIDO");
		}
		if (tipoMapeo == null || tipoMapeo.isBlank()) {
			throw new DomainException(MessageCodes.MAPEO_XSD_TIPO_REQUERIDO);
		}
		if ("ELEMENTO".equals(tipoMapeo) && !elemento) {
			throw new DomainException("FACTUCORE.MAPEO_XSD.TIPO.DESTINO_INVALIDO");
		}
		if ("ATRIBUTO".equals(tipoMapeo) && !atributo) {
			throw new DomainException("FACTUCORE.MAPEO_XSD.TIPO.DESTINO_INVALIDO");
		}

		this.id = id;
		this.versionDocumentoXsdId = versionDocumentoXsdId;
		this.tipoOrigen = tipoOrigen;
		this.origen = origen;
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

	public String getTipoOrigen() {
		return tipoOrigen;
	}

	public String getOrigen() {
		return origen;
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