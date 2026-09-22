package ec.dalara.factucore.application.service;

import ec.dalara.factucore.application.port.out.ClaveAccesoDatos;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoGenerator;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoModel;
import org.springframework.stereotype.Service;

@Service
public class ClaveAccesoService {

    public ClaveAccesoModel generar(
            ClaveAccesoDatos datos
    ) {
        return ClaveAccesoGenerator.generar(datos);
    }
}