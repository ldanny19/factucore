package ec.dalara.factucore.application.port.out;

public interface SecuencialPort {

    String obtenerSiguiente(
            Long puntoEmisionId,
            String codigoDocumento
    );
}