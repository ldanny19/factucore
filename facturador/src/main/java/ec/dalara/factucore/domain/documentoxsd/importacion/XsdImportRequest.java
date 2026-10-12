package ec.dalara.factucore.domain.documentoxsd.importacion;

import java.time.LocalDateTime;
import java.util.Objects;

import ec.dalara.factucore.domain.shared.DomainException;

public record XsdImportRequest(String codigo, String nombre, String descripcion, String tipoDocumento,
		String prefijoArchivo, String version, String nombreArchivo, LocalDateTime fechaInicio, LocalDateTime fechaFin,
		String usuario, String observacion) {

	public XsdImportRequest {
		codigo = requerido(codigo, "FACTUCORE.XSD.IMPORTACION.CODIGO.REQUERIDO");
		nombre = requerido(nombre, "FACTUCORE.XSD.IMPORTACION.NOMBRE.REQUERIDO");
		tipoDocumento = requerido(tipoDocumento, "FACTUCORE.XSD.IMPORTACION.TIPO_DOCUMENTO.REQUERIDO");
		prefijoArchivo = requerido(prefijoArchivo, "FACTUCORE.XSD.IMPORTACION.PREFIJO_ARCHIVO.REQUERIDO");
		version = requerido(version, "FACTUCORE.XSD.IMPORTACION.VERSION.REQUERIDA");
		fechaInicio = Objects.requireNonNull(fechaInicio);
		usuario = requerido(usuario, "FACTUCORE.XSD.IMPORTACION.USUARIO.REQUERIDO");
	}

	private static String requerido(String valor, String codigo) {
		if (valor == null || valor.isBlank()) {
			throw new DomainException(codigo);
		}
		return valor;
	}
}
