package ec.dalara.factucore.messaging.config;

import java.util.Locale;

public interface MessageResolver {

	String resolver(String codigo, Object... parametros);

	String resolver(String codigo, Locale locale, Object... parametros);
}
