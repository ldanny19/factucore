package ec.dalara.factucore.application.port.out;

import java.util.Map;

public interface FactuCoreSourcePort {

	ValorOrigen resolver(String origen, Map<String, Object> contexto);

	record ValorOrigen(Object resultado, String tipoDato) {
	}
}
