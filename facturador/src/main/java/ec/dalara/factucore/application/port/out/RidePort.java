package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;

public interface RidePort {

    byte[] generar(Comprobante comprobante);
}
