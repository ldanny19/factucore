package ec.dalara.factucore.application.contract.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoResponse {

	private List<MensajeResponse> errores;

	private List<MensajeResponse> advertencias;

	private List<MensajeResponse> mensajes;
}