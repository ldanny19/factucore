package ec.dalara.factucore.domain.documentoxsd;

import java.util.List;

public final class DocumentDefinitionModel {

    private final DocumentoXsdModel documento;
    private final VersionDocumentoXsdModel version;
    private final List<ElementoXsdModel> elementos;
    private final List<AtributoXsdModel> atributos;
    private final List<EnumeracionXsdModel> enumeraciones;

    public DocumentDefinitionModel(
            DocumentoXsdModel documento,
            VersionDocumentoXsdModel version,
            List<ElementoXsdModel> elementos,
            List<AtributoXsdModel> atributos,
            List<EnumeracionXsdModel> enumeraciones
    ) {
        this.documento = documento;
        this.version = version;
        this.elementos = List.copyOf(elementos);
        this.atributos = List.copyOf(atributos);
        this.enumeraciones = List.copyOf(enumeraciones);
    }

    public DocumentoXsdModel getDocumento() {
        return documento;
    }

    public VersionDocumentoXsdModel getVersion() {
        return version;
    }

    public List<ElementoXsdModel> getElementos() {
        return elementos;
    }

    public List<AtributoXsdModel> getAtributos() {
        return atributos;
    }

    public List<EnumeracionXsdModel> getEnumeraciones() {
        return enumeraciones;
    }
}