package ec.dalara.factucore.application.port.out;

public interface XmlValidatorPort {

    void validar(
            String xml,
            String contenidoXsd
    );
}