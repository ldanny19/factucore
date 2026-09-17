package ec.dalara.factucore.application.port.out;

public interface SriPort {

    String recibir(String xml);

    String autorizar(String claveAcceso);
}