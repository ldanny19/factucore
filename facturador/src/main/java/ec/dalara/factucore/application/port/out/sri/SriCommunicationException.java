package ec.dalara.factucore.application.port.out.sri;

public class SriCommunicationException extends RuntimeException {

    public SriCommunicationException(String message) {
        super(message);
    }

    public SriCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
