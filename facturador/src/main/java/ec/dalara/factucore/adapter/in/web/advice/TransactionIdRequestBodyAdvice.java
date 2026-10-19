package ec.dalara.factucore.adapter.in.web.advice;

import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import ec.dalara.factucore.adapter.in.web.filter.TransactionIdFilter;
import ec.dalara.factucore.application.contract.request.AdministracionRequest;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class TransactionIdRequestBodyAdvice extends RequestBodyAdviceAdapter {

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		Class<?> requestType = methodParameter.getParameterType();
		return AdministracionRequest.class.isAssignableFrom(requestType)
				|| ComprobanteGeneracionRequest.class.isAssignableFrom(requestType);
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		Object idTransaccion = request.getAttribute(TransactionIdFilter.ATTRIBUTE_ID_TRANSACCION);

		if (idTransaccion instanceof String id) {
			if (body instanceof AdministracionRequest<?> administracionRequest) {
				administracionRequest.setIdTransaccion(id);
			} else if (body instanceof ComprobanteGeneracionRequest comprobanteRequest) {
				comprobanteRequest.setIdTransaccion(id);
			}
		}

		return body;
	}
}
