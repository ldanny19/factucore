package ec.dalara.factucore.adapter.in.web.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import ec.dalara.factucore.adapter.in.web.filter.TransactionIdFilter;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class TransactionIdResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		if (body == null) {
			return null;
		}

		if (request instanceof org.springframework.http.server.ServletServerHttpRequest servletRequest) {
			HttpServletRequest httpRequest = servletRequest.getServletRequest();
			Object idTransaccion = httpRequest.getAttribute(TransactionIdFilter.ATTRIBUTE_ID_TRANSACCION);
			if (idTransaccion instanceof String id && !id.isBlank()) {
				if (body instanceof AdministracionResponse<?> administracionResponse) {
					administracionResponse.setIdTransaccion(id);
				} else if (body instanceof ComprobanteGeneracionResponse comprobanteResponse) {
					comprobanteResponse.setIdTransaccion(id);
				}
			}
		}

		return body;
	}
}
