package ec.dalara.factucore.application.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoResponse {

    private List<MensajeResponse> errores;

    private List<MensajeResponse> advertencias;

    private List<MensajeResponse> mensajes;
}