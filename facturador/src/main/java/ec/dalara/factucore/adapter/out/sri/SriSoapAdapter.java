package ec.dalara.factucore.adapter.out.sri;

import ec.dalara.factucore.application.port.out.SriPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.shared.MessageResolver;
import org.springframework.stereotype.Component;

@Component
public class SriSoapAdapter implements SriPort {

    private final SriProperties properties;
    private final MessageResolver messageResolver;

    public SriSoapAdapter(
            SriProperties properties,
            MessageResolver messageResolver) {
        this.properties = properties;
        this.messageResolver = messageResolver;
    }

    @Override
    public SriResponse recibir(String xml) {
        throw new UnsupportedOperationException(
                messageResolver.resolve(MessageCodes.SRI_OPERACION_NO_IMPLEMENTADA)
        );
    }

    @Override
    public SriResponse autorizar(String claveAcceso) {
        throw new UnsupportedOperationException(
                messageResolver.resolve(MessageCodes.SRI_OPERACION_NO_IMPLEMENTADA)
        );
    }
}