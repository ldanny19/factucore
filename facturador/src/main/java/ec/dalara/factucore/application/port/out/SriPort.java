package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.application.port.out.sri.SriResponse;

public interface SriPort {

    SriResponse recibir(String xml);

    SriResponse autorizar(String claveAcceso);
}