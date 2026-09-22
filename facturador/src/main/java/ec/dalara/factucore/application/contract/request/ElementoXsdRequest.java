package ec.dalara.factucore.application.contract.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElementoXsdRequest {

	@NotNull
	private Long idVersionDocumentoXsd;

	private Long idElementoPadre;

	@NotBlank
	@Size(max = 300)
	private String nombre;

	@Size(max = 100)
	private String tipoDato;

	private Integer orden;

	@NotNull
	private Boolean obligatorio;

	@NotNull
	private Boolean repetible;

	private Integer minOcurrencias;

	private Integer maxOcurrencias;

	private Integer longitudMinima;

	private Integer longitudMaxima;

	private Integer digitosTotales;

	private Integer decimales;

	private BigDecimal valorMinimo;

	private BigDecimal valorMaximo;

	@Size(max = 2000)
	private String patron;

	private LocalDateTime fechaInicio;

	private LocalDateTime fechaFin;
}