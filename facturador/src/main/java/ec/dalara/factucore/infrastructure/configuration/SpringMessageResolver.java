package ec.dalara.factucore.infrastructure.configuration;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.MessageResolver;

@Component
public class SpringMessageResolver implements MessageResolver {

	private final MessageSource messageSource;

	public SpringMessageResolver(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String resolver(String codigo, Object... parametros) {
		return resolverObligatorio(codigo, parametros, Locale.getDefault());
	}

	@Override
	public String resolver(String codigo, Locale locale, Object... parametros) {
		return resolverObligatorio(codigo, parametros, locale);
	}

	private String resolverObligatorio(String codigo, Object[] parametros, Locale locale) {
		String mensaje = messageSource.getMessage(codigo, parametros, null, locale);
		if (mensaje == null) {
			throw new IllegalStateException("No existe mensaje configurado para el código: " + codigo);
		}
		return mensaje;
	}
}