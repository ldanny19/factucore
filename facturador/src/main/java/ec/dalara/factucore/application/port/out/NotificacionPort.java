package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;

public interface NotificacionPort {

    void publicar(ContextoWorkflow contexto);
}
