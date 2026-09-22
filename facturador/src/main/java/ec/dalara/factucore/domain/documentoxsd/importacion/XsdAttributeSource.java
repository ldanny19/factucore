package ec.dalara.factucore.domain.documentoxsd.importacion;

public record XsdAttributeSource(
        String rutaElemento, String nombre, String tipoDato,
        boolean obligatorio, String valorPredeterminado, String patron) {
}
