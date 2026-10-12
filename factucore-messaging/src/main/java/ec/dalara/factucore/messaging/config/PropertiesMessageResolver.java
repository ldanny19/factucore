package ec.dalara.factucore.messaging.config;

import java.util.Locale;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class PropertiesMessageResolver implements MessageResolver {

	private final ReloadableResourceBundleMessageSource messageSource;

	public PropertiesMessageResolver() {
		messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("file:/app/messaging-config/message", "classpath:factucore-messaging-messages");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setCacheSeconds(60);
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
