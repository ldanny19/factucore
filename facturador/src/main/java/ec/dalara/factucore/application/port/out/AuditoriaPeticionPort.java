package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.messaging.api.auditoria.PeticionAuditada;

public interface AuditoriaPeticionPort {

    void publicar(PeticionAuditada peticion);
}
