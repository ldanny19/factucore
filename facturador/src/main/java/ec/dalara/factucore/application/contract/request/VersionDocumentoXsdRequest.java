package ec.dalara.factucore.application.contract.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDocumentoXsdRequest {

    @NotNull
    private Long idDocumentoXsd;

    @NotBlank
    @Size(max = 20)
    private String version;

    @NotBlank
    @Size(max = 20)
    private String versionXsd;

    @Size(max = 300)
    private String nombreArchivo;

    @Size(max = 1000)
    private String namespaceXml;

    @Size(max = 300)
    private String elementoRaiz;

    private String plantillaJson;

    private String esquemaJson;

    @NotNull
    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;
}