package ec.dalara.factucore.application.service;

import java.util.List;

import ec.dalara.factucore.application.contract.response.CampoFuenteMapeoXsdResponse;
import ec.dalara.factucore.application.contract.response.FuenteMapeoXsdResponse;

public final class FactuCoreMapeoFuenteCatalogo {

	private FactuCoreMapeoFuenteCatalogo() {
	}

	public static List<FuenteMapeoXsdResponse> listar() {
		return List.of(
				fuente("empresa", "Empresa", false,
						campo("ruc", "RUC", "String", "empresa.ruc"),
						campo("razonSocial", "Razón social", "String", "empresa.razonSocial"),
						campo("nombreComercial", "Nombre comercial", "String", "empresa.nombreComercial"),
						campo("direccionMatriz", "Dirección matriz", "String", "empresa.direccionMatriz"),
						campo("obligadoContabilidad", "Obligado a llevar contabilidad", "String",
								"empresa.obligadoContabilidad"),
						campo("contribuyenteRimpe", "Contribuyente RIMPE", "String", "empresa.contribuyenteRimpe")),
				fuente("establecimiento", "Establecimiento", false,
						campo("codigo", "Código", "String", "establecimiento.codigo"),
						campo("nombre", "Nombre", "String", "establecimiento.nombre"),
						campo("direccion", "Dirección", "String", "establecimiento.direccion")),
				fuente("punto_emision", "Punto de emisión", false,
						campo("codigo", "Código", "String", "punto_emision.codigo"),
						campo("nombre", "Nombre", "String", "punto_emision.nombre")),
				fuente("version_documento_xsd", "Versión de documento XSD", false,
						campo("version", "Versión", "String", "version_documento_xsd.version"),
						campo("elementoRaiz", "Elemento raíz", "String", "version_documento_xsd.elementoRaiz")),
				fuente("catalogo_item", "Ítem de catálogo", true,
						campo("codigo", "Código", "String", "catalogo_item.{codigoCatalogo}.codigo"),
						campo("nombre", "Nombre", "String", "catalogo_item.{codigoCatalogo}.nombre"),
						campo("descripcion", "Descripción", "String", "catalogo_item.{codigoCatalogo}.descripcion")));
	}

	private static FuenteMapeoXsdResponse fuente(String codigo, String nombre, boolean requiereCatalogo,
			CampoFuenteMapeoXsdResponse... campos) {
		return FuenteMapeoXsdResponse.builder().codigo(codigo).nombre(nombre).requiereCatalogo(requiereCatalogo)
				.campos(List.of(campos)).build();
	}

	private static CampoFuenteMapeoXsdResponse campo(String codigo, String nombre, String tipoDato, String origen) {
		return CampoFuenteMapeoXsdResponse.builder().codigo(codigo).nombre(nombre).tipoDato(tipoDato).origen(origen)
				.build();
	}
}
