package ec.dalara.factucore.notificacion.infrastructure.message;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageResolver {
    private final MessageSource messageSource;

    public String resolver(String codigo, Object... parametros) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(codigo, parametros, codigo, locale);
    }
}
