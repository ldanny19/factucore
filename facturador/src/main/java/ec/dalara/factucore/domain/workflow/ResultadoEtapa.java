package ec.dalara.factucore.domain.workflow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ResultadoEtapa {

    private final EtapaWorkflow etapa;
    private final boolean exitosa;
    private final String estado;
    private final String codigoError;
    private final String mensaje;
    private final Map<String, Object> datos;

    private ResultadoEtapa(
            EtapaWorkflow etapa,
            boolean exitosa,
            String estado,
            String codigoError,
            String mensaje,
            Map<String, Object> datos
    ) {
        this.etapa = etapa;
        this.exitosa = exitosa;
        this.estado = estado;
        this.codigoError = codigoError;
        this.mensaje = mensaje;
        this.datos = datos == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(datos));
    }

    public static ResultadoEtapa exitosa(
            EtapaWorkflow etapa,
            String estado
    ) {
        return new ResultadoEtapa(
                etapa,
                true,
                estado,
                null,
                null,
                Map.of()
        );
    }

    public static ResultadoEtapa exitosa(
            EtapaWorkflow etapa,
            String estado,
            Map<String, Object> datos
    ) {
        return new ResultadoEtapa(
                etapa,
                true,
                estado,
                null,
                null,
                datos
        );
    }

    public static ResultadoEtapa fallida(
            EtapaWorkflow etapa,
            String estado,
            String codigoError,
            String mensaje
    ) {
        return new ResultadoEtapa(
                etapa,
                false,
                estado,
                codigoError,
                mensaje,
                Map.of()
        );
    }

    public EtapaWorkflow getEtapa() {
        return etapa;
    }

    public boolean isExitosa() {
        return exitosa;
    }

    public String getEstado() {
        return estado;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }
}