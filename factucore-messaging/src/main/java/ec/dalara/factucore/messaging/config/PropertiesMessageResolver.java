package ec.dalara.factucore.messaging.config;

import java.util.Locale;

import org.springframework.context.support.ResourceBundleMessageSource;

public class PropertiesMessageResolver implements MessageResolver {

	private final ResourceBundleMessageSource messageSource;

	public PropertiesMessageResolver() {
		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("factucore-messaging-messages");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setFallbackToSystemLocale(false);
	}

	@Override
	public String resolver(String codigo, Object... parametros) {
		return resolver(codigo, Locale.getDefault(), parametros);
	}

	@Override
	public String resolver(String codigo, Locale locale, Object... parametros) {
		return messageSource.getMessage(codigo, parametros, codigo, locale);
	}
}
