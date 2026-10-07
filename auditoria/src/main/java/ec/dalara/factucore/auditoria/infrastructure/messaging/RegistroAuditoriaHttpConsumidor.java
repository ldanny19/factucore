package ec.dalara.factucore.auditoria.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.auditoria.RegistroAuditoriaHttp;
import ec.dalara.factucore.auditoria.infrastructure.config.AuditoriaMessagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegistroAuditoriaHttpConsumidor implements ConsumidorMensajes {

    private static final String TIPO_EVENTO = "REGISTRO_AUDITORIA_HTTP";

    private final ObjectMapper objectMapper;
    private final AuditoriaMessagingProperties properties;

    @Override
    public String topico() {
        return properties.getTopico();
    }

    @Override
    public String grupo() {
        return properties.getGrupoConsumidor();
    }

    @Override
    public Set<String> tiposEvento() {
        return Set.of(TIPO_EVENTO);
    }

    @Override
    public void consumir(EventoMensaje evento) {
        var registro = objectMapper.convertValue(
                evento.payload(),
                RegistroAuditoriaHttp.class);

        log.info(
                "Registro de auditoría HTTP recibido. eventoId={}, idTransaccion={}, endpoint={}, estadoHttp={}",
                evento.id(),
                registro.idTransaccion(),
                registro.endpoint(),
                registro.estadoHttp()
        );

        // V1: recepción del evento. La persistencia y consulta se implementarán posteriormente.
    }
}
