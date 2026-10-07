package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.messaging.api.auditoria.RegistroAuditoriaHttp;

public interface AuditoriaPeticionPort {

	void publicar(RegistroAuditoriaHttp peticion);
}
