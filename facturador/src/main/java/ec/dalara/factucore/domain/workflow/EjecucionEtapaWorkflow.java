package ec.dalara.factucore.domain.workflow;

import java.time.LocalDateTime;

public final class EjecucionEtapaWorkflow {
	private final EtapaWorkflow etapa;
	private final Object entrada;
	private final LocalDateTime fechaInicio;
	private Object salida;
	private ResultadoEtapa resultado;
	private LocalDateTime fechaFin;

	public EjecucionEtapaWorkflow(EtapaWorkflow etapa, Object entrada, LocalDateTime fechaInicio) {
		this.etapa = etapa;
		this.entrada = entrada;
		this.fechaInicio = fechaInicio;
	}

	public void completar(Object salida, ResultadoEtapa resultado, LocalDateTime fechaFin) {
		this.salida = salida;
		this.resultado = resultado;
		this.fechaFin = fechaFin;
	}

	public EtapaWorkflow getEtapa() { return etapa; }
	public Object getEntrada() { return entrada; }
	public Object getSalida() { return salida; }
	public ResultadoEtapa getResultado() { return resultado; }
	public LocalDateTime getFechaInicio() { return fechaInicio; }
	public LocalDateTime getFechaFin() { return fechaFin; }
}
