package ec.dalara.factucore.messaging.infrastructure;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
@ConditionalOnProperty(prefix = "factucore.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessagingCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PublicadorMensajes.class)
    PublicadorMensajes publicadorMensajes(List<BrokerMensajeria> brokers, MessagingProperties properties) {
        return new GenericPublicadorMensajes(brokers, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    GenericConsumidorRegistrar consumidorRegistrar(
            List<BrokerMensajeria> brokers,
            List<ConsumidorMensajes> consumidores) {
        return new GenericConsumidorRegistrar(brokers, consumidores);
    }
}
