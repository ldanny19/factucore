package ec.dalara.factucore.application.contract.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComprobanteReporteFiltro {

	@NotNull
	private Long idEmpresa;

	private LocalDate fechaDesde;

	private LocalDate fechaHasta;

	private Long idEstablecimiento;

	private Long idPuntoEmision;

	@Size(max = 2)
	private String codigoDocumento;

	@Size(max = 50)
	private String estadoProceso;

	@Size(max = 50)
	private String estadoSri;

	@Size(max = 200)
	private String busqueda;

	@Size(max = 20)
	@Pattern(regexp = "(?i)^(PENDIENTES|FALLIDOS)?$")
	private String grupoEstado;
}
