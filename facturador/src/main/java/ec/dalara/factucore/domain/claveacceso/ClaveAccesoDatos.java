package ec.dalara.factucore.domain.claveacceso;

import java.time.LocalDate;

public record ClaveAccesoDatos(LocalDate fechaEmision, String codigoDocumento, String ruc, String ambiente,
		String establecimiento, String puntoEmision, String secuencial, String codigoNumerico, String tipoEmision) {
}
