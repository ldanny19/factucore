package ec.dalara.factucore.domain.claveacceso;

public final class ClaveAccesoValidator {
    private ClaveAccesoValidator() {}
    public static boolean esValida(String clave) {
        if (clave == null || !clave.matches("\\d{49}")) return false;
        return ClaveAccesoGenerator.calcularModulo11(clave.substring(0, 48)).equals(clave.substring(48));
    }
}