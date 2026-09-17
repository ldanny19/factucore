package ec.dalara.factucore.application.port.out;

public interface XmlGeneratorPort {

    String generar(
            String codigoDocumento,
            String versionXsd,
            Object datos
    );
}