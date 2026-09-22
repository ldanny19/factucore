package ec.dalara.factucore.domain.workflow;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

public final class ContextoWorkflow {

    private final Long comprobanteId;
    private final ComprobanteGeneracionRequest solicitud;
    private final LocalDateTime fechaInicio;

    private Comprobante comprobante;

    private String secuencial;
    private String claveAcceso;
    private String xml;
    private String xmlFirmado;
    private byte[] ride;

    private String numeroAutorizacion;
    private LocalDateTime fechaAutorizacion;
    private String estadoSri;

    private EtapaWorkflow etapaActual;
    private ResultadoEtapa ultimoResultado;

    private final Map<EtapaWorkflow, ResultadoEtapa> resultados =
            new EnumMap<>(EtapaWorkflow.class);

    private ContextoWorkflow(
            Long comprobanteId,
            ComprobanteGeneracionRequest solicitud,
            LocalDateTime fechaInicio
    ) {
        this.comprobanteId = comprobanteId;
        this.solicitud = solicitud;
        this.fechaInicio = fechaInicio;
    }

    public static ContextoWorkflow nuevo(
            ComprobanteGeneracionRequest solicitud
    ) {
        return new ContextoWorkflow(
                null,
                solicitud,
                LocalDateTime.now()
        );
    }

    public static ContextoWorkflow existente(
            Comprobante comprobante,
            ComprobanteGeneracionRequest solicitud
    ) {
        ContextoWorkflow contexto = new ContextoWorkflow(
                comprobante.getId(),
                solicitud,
                comprobante.getFechaCreacion()
        );

        contexto.comprobante = comprobante;
        contexto.secuencial = comprobante.getSecuencial();
        contexto.claveAcceso = comprobante.getClaveAcceso();
        contexto.numeroAutorizacion = comprobante.getNumeroAutorizacion();
        contexto.fechaAutorizacion = comprobante.getFechaAutorizacion();
        contexto.estadoSri = comprobante.getEstadoProceso();

        return contexto;
    }

    public void registrarResultado(ResultadoEtapa resultado) {
        this.etapaActual = resultado.getEtapa();
        this.ultimoResultado = resultado;
        this.resultados.put(resultado.getEtapa(), resultado);
    }

    public boolean etapaCompletada(EtapaWorkflow etapa) {
        ResultadoEtapa resultado = resultados.get(etapa);
        return resultado != null && resultado.isExitosa();
    }

    public Long getComprobanteId() {
        return comprobanteId;
    }

    public ComprobanteGeneracionRequest getSolicitud() {
        return solicitud;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public String getSecuencial() {
        return secuencial;
    }

    public void setSecuencial(String secuencial) {
        this.secuencial = secuencial;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getXmlFirmado() {
        return xmlFirmado;
    }

    public void setXmlFirmado(String xmlFirmado) {
        this.xmlFirmado = xmlFirmado;
    }

    public byte[] getRide() {
        return ride;
    }

    public void setRide(byte[] ride) {
        this.ride = ride;
    }

    public String getNumeroAutorizacion() {
        return numeroAutorizacion;
    }

    public void setNumeroAutorizacion(String numeroAutorizacion) {
        this.numeroAutorizacion = numeroAutorizacion;
    }

    public LocalDateTime getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public String getEstadoSri() {
        return estadoSri;
    }

    public void setEstadoSri(String estadoSri) {
        this.estadoSri = estadoSri;
    }

    public EtapaWorkflow getEtapaActual() {
        return etapaActual;
    }

    public ResultadoEtapa getUltimoResultado() {
        return ultimoResultado;
    }

    public Map<EtapaWorkflow, ResultadoEtapa> getResultados() {
        return Map.copyOf(resultados);
    }
}