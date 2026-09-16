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
public class EnumeracionXsdResponse {

    private Long id;

    private Long idElementoXsd;

    private String valor;

    private String descripcion;

    private Integer orden;

    private String estadoRegistro;

    private String usuarioCreacion;

    private String usuarioModificacion;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;

    private String observacion;
}