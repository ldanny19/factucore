package ec.dalara.factucore.adapter.in.web.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RespuestaRestFactory {
	private final MessageResolver messageResolver;
	private final Clock factuCoreClock;

	public <T> AdministracionResponse<T> exito(String id, LocalDateTime inicio, T datos) {
		LocalDateTime fin = ahora();

		return AdministracionResponse.<T>builder()
				.idTransaccion(id)
				.fechaInicio(inicio)
				.fechaFin(fin)
				.estado("OK")
				.codigo(MessageCodes.OPERACION_EXITOSA)
				.mensaje(messageResolver.resolver(MessageCodes.OPERACION_EXITOSA))
				.datos(datos)
				.build();
	}

	public <T> AdministracionResponse<T> exitoConsulta(T datos) {
		LocalDateTime ahora = ahora();
		return AdministracionResponse.<T>builder()
				.idTransaccion(UUID.randomUUID().toString())
				.fechaInicio(ahora)
				.fechaFin(ahora)
				.estado("OK")
				.codigo(MessageCodes.OPERACION_EXITOSA)
				.mensaje(messageResolver.resolver(MessageCodes.OPERACION_EXITOSA))
				.datos(datos)
				.build();
	}

	public void validarFechaInicio(LocalDateTime inicio) {
		if (inicio.isAfter(ahora())) {
			throw new ApplicationException(MessageCodes.OPERACION_FECHA_INICIO_FUTURA, inicio);
		}
	}

	private LocalDateTime ahora() {
		return LocalDateTime.now(factuCoreClock);
	}
}
