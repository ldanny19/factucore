package ec.dalara.factucore.adapter.in.web.controller;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final MessageResolver messageResolver;

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorResponse> manejarDomainException(DomainException exception) {
		return construirRespuesta(exception.getCodigo(), exception.getParametros(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ErrorResponse> manejarApplicationException(ApplicationException exception) {
		return construirRespuesta(exception.getCodigo(), exception.getParametros(), HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<ErrorResponse> construirRespuesta(String codigo, Object[] parametros, HttpStatus estado) {
		String mensaje = messageResolver.resolver(codigo, Locale.getDefault(), parametros);

		return ResponseEntity.status(estado).body(new ErrorResponse(codigo, mensaje));
	}

	public record ErrorResponse(String codigo, String mensaje) {
	}
}