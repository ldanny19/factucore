package ec.dalara.factucore.messaging.infrastructure;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

@RequiredArgsConstructor
public class GenericConsumidorRegistrar implements InitializingBean {

    private final BrokerMensajeria broker;
    private final List<ConsumidorMensajes> consumidores;

    @Override
    public void afterPropertiesSet() {
        consumidores.forEach(broker::registrar);
    }
}
