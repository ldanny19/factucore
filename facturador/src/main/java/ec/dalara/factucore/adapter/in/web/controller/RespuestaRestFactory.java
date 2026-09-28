package ec.dalara.factucore.adapter.in.web.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RespuestaRestFactory {
	private final MessageResolver messageResolver;

	public <T> AdministracionResponse<T> exito(String id, OffsetDateTime inicio, T datos) {
		return AdministracionResponse.<T>builder().idTransaccion(id).fechaInicio(inicio).fechaFin(OffsetDateTime.now())
				.estado("OK").codigo(MessageCodes.OPERACION_EXITOSA)
				.mensaje(messageResolver.resolver(MessageCodes.OPERACION_EXITOSA, null)).datos(datos).build();
	}

	public <T> AdministracionResponse<T> exitoConsulta(T datos) {
		return exito(UUID.randomUUID().toString(), OffsetDateTime.now(), datos);
	}
}
