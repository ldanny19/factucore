package ec.dalara.factucore.infrastructure.adapter.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "factucore.auditoria.peticion")
public class AuditoriaPeticionMessagingProperties {

	private boolean habilitada = false;
	private String tipoEvento = "REGISTRO_AUDITORIA_HTTP";
	private String versionEvento = "1.0";
}
