package ec.dalara.factucore.messaging.infrastructure;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

@RequiredArgsConstructor
public class GenericConsumidorRegistrar implements InitializingBean {

    private final List<BrokerMensajeria> brokers;
    private final List<ConsumidorMensajes> consumidores;

    @Override
    public void afterPropertiesSet() {
        consumidores.forEach(consumidor -> brokers.stream()
                .filter(broker -> broker.soporta(consumidor.destino()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe un broker configurado para el destino: " + consumidor.destino()))
                .registrar(consumidor));
    }
}
