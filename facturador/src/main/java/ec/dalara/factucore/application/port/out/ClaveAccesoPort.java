package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.domain.claveacceso.ClaveAccesoModel;

public interface ClaveAccesoPort {

    ClaveAccesoModel generar(
            ClaveAccesoDatos datos
    );
}