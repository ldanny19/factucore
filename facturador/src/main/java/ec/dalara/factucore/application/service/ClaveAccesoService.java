package ec.dalara.factucore.application.service;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.port.out.ClaveAccesoPort;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoCodigoNumericoGenerator;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoDatos;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoGenerator;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoModel;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaveAccesoService implements ClaveAccesoPort {

	@Override
	public ClaveAccesoModel generar(ClaveAccesoDatos datos) {
		return ClaveAccesoGenerator.generar(datos);
	}

	@Override
	public boolean validar(String clave) {
		return ClaveAccesoValidator.esValida(clave);
	}

	public ClaveAccesoModel generarConCodigoNumerico(ClaveAccesoDatos datos) {
		if (datos == null) {
			return ClaveAccesoGenerator.generar(null);
		}

		ClaveAccesoDatos datosCompletos = new ClaveAccesoDatos(datos.fechaEmision(), datos.codigoDocumento(),
				datos.ruc(), datos.ambiente(), datos.establecimiento(), datos.puntoEmision(), datos.secuencial(),
				ClaveAccesoCodigoNumericoGenerator.generar(), datos.tipoEmision());

		return generar(datosCompletos);
	}
}