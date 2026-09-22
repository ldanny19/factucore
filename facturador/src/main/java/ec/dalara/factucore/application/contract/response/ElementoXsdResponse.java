package ec.dalara.factucore.application.contract.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElementoXsdResponse {

	private Long id;

	private Long idVersionDocumentoXsd;

	private Long idElementoPadre;

	private String nombre;

	private String tipoDato;

	private Integer orden;

	private Boolean obligatorio;

	private Boolean repetible;

	private Integer minOcurrencias;

	private Integer maxOcurrencias;

	private Integer longitudMinima;

	private Integer longitudMaxima;

	private Integer digitosTotales;

	private Integer decimales;

	private BigDecimal valorMinimo;

	private BigDecimal valorMaximo;

	private String patron;

	private LocalDateTime fechaInicio;

	private LocalDateTime fechaFin;

	private String estadoRegistro;

	private String usuarioCreacion;

	private String usuarioModificacion;

	private LocalDateTime fechaCreacion;

	private LocalDateTime fechaModificacion;

	private String observacion;
}