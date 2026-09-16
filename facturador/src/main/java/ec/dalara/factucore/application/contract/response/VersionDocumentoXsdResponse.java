package ec.dalara.factucore.application.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDocumentoXsdResponse {

    private Long id;

    private Long idDocumentoXsd;

    private String version;

    private String nombreArchivo;

    private String rutaXsd;

    private String namespaceXml;

    private String elementoRaiz;

    private String plantillaJson;

    private String esquemaJson;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private String estadoRegistro;

    private String usuarioCreacion;

    private String usuarioModificacion;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;

    private String observacion;
}