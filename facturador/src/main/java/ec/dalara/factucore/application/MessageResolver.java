package ec.dalara.factucore.application;

import java.util.Locale;

public interface MessageResolver {

    String resolver(String codigo, Object... parametros);

    String resolver(String codigo, Locale locale, Object... parametros);
}