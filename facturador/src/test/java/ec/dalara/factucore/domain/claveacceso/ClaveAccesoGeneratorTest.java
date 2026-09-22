package ec.dalara.factucore.domain.claveacceso;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ClaveAccesoGeneratorTest {
    @Test
    void generaClaveDe49DigitosYValidaChecksum() {
        ClaveAccesoDatos datos = new ClaveAccesoDatos(
                LocalDate.of(2026, 9, 22), "01", "0999999999001", "1",
                "001", "001", "000000001", "12345678", "1");
        ClaveAccesoModel resultado = ClaveAccesoGenerator.generar(datos);
        assertEquals(49, resultado.getClave().length());
        assertTrue(resultado.getClave().matches("\\d{49}"));
        assertTrue(ClaveAccesoValidator.esValida(resultado.getClave()));
    }

    @Test
    void rechazaDatosConLongitudesInvalidas() {
        ClaveAccesoDatos datos = new ClaveAccesoDatos(
                LocalDate.of(2026, 9, 22), "1", "0999999999001", "1",
                "001", "001", "000000001", "12345678", "1");
        assertThrows(RuntimeException.class, () -> ClaveAccesoGenerator.generar(datos));
    }

    @Test
    void rechazaClaveAlterada() {
        ClaveAccesoDatos datos = new ClaveAccesoDatos(
                LocalDate.of(2026, 9, 22), "01", "0999999999001", "1",
                "001", "001", "000000001", "12345678", "1");
        String clave = ClaveAccesoGenerator.generar(datos).getClave();
        char ultimo = clave.charAt(48);
        String alterada = clave.substring(0, 48) + (ultimo == '0' ? '1' : '0');
        assertFalse(ClaveAccesoValidator.esValida(alterada));
    }
}