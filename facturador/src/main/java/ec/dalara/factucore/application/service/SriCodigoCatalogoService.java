package ec.dalara.factucore.application.service;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SriCodigoCatalogoService {

	private static final String CATALOGO_CODIGO = "COD_ERROR_SRI";
	private final CatalogoService catalogoService;
	private final CatalogoItemService catalogoItemService;

	public String resolverDescripcion(String codigo, String mensajeAlternativo) {
		if (codigo != null && !codigo.isBlank()) {
			var item = catalogoService.obtenerPorCodigo(CATALOGO_CODIGO)
					.filter(catalogo -> EstadoRegistro.ACTIVO.equals(catalogo.getEstadoRegistro()))
					.flatMap(catalogo -> catalogoItemService.obtenerPorCatalogoYCodigo(catalogo.getId(), codigo));
			if (item.isPresent()) {
				String descripcion = item.get().getDescripcion();
				if (descripcion != null && !descripcion.isBlank()) {
					return descripcion;
				}
				String nombre = item.get().getNombre();
				if (nombre != null && !nombre.isBlank()) {
					return nombre;
				}
			}
		}
		return mensajeAlternativo;
	}

	public boolean esCodigoConocido(String codigo) {
		if (codigo == null || codigo.isBlank()) {
			return false;
		}

		return catalogoService.obtenerPorCodigo(CATALOGO_CODIGO)
				.filter(catalogo -> EstadoRegistro.ACTIVO.equals(catalogo.getEstadoRegistro()))
				.flatMap(catalogo -> catalogoItemService.obtenerPorCatalogoYCodigo(catalogo.getId(), codigo))
				.isPresent();
	}
}
