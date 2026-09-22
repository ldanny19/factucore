package ec.dalara.factucore.domain.documentoxsd.importacion;

public record XsdImportResult(
        Long documentoXsdId,
        Long versionDocumentoXsdId,
        int elementosImportados,
        int atributosImportados,
        int enumeracionesImportadas) {
}
