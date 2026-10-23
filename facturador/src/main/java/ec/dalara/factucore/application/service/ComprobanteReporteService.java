package ec.dalara.factucore.application.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;
import ec.dalara.factucore.application.contract.request.ComprobanteReporteFiltro;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteAuditoriaResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteDetalleResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteDiaResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteEmpresaResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReportePaginaResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteResumenResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteResponse;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteAuditoriaRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteReporteService {

	private static final int MAXIMO_TAMANO_PAGINA = 100;
	private static final int MAXIMO_FILAS_EXPORTACION = 10_000;
	private static final List<String> ESTADOS_PENDIENTES = List.of("RECIBIDO", "VALIDADO", "SECUENCIAL_ASIGNADO",
			"CLAVE_ACCESO_GENERADA", "XML_GENERADO", "XSD_VALIDADO", "FIRMADO", "ENVIADO_SRI",
			"AUTORIZACION_PENDIENTE", "AUTORIZACION_SRI", "RIDE_GENERADO");
	private static final List<String> ESTADOS_NO_AUTORIZADOS = List.of("NO_AUTORIZADO", "NO AUTORIZADO",
			"NOAUTORIZADO", "RECHAZADO");
	private static final Sort ORDEN_REPORTE = Sort.by(Sort.Direction.DESC, "fechaEmision")
			.and(Sort.by(Sort.Direction.DESC, "id"));
	private final ComprobanteRepository comprobanteRepository;
	private final ComprobanteAuditoriaRepository auditoriaRepository;
	private final EmpresaRepository empresaRepository;

	@Transactional(readOnly = true)
	public List<ComprobanteReporteEmpresaResponse> listarEmpresas() {
		return empresaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
				.map(empresa -> ComprobanteReporteEmpresaResponse.builder().id(empresa.getId()).ruc(empresa.getRuc())
						.razonSocial(empresa.getRazonSocial()).build())
				.toList();
	}

	@Transactional(readOnly = true)
	public ComprobanteReportePaginaResponse buscar(ComprobanteReporteFiltro filtro, int pagina, int tamano) {
		validarFiltro(filtro);
		if (pagina < 0 || tamano < 1 || tamano > MAXIMO_TAMANO_PAGINA) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		Page<Comprobante> resultados = comprobanteRepository.findAll(criterio(filtro),
				PageRequest.of(pagina, tamano, ORDEN_REPORTE));
		return ComprobanteReportePaginaResponse.builder().resultados(resultados.getContent().stream()
				.map(this::mapear).toList()).pagina(resultados.getNumber()).tamano(resultados.getSize())
				.totalElementos(resultados.getTotalElements()).totalPaginas(resultados.getTotalPages()).build();
	}

	@Transactional(readOnly = true)
	public ComprobanteReporteResumenResponse resumir(ComprobanteReporteFiltro filtro) {
		validarFiltro(filtro);
		List<ComprobanteReporteDiaResponse> porDia = comprobanteRepository.agruparPorDia(criterio(filtro)).stream()
				.map(dia -> {
					long otros = dia.total() - dia.autorizados() - dia.pendientes() - dia.noAutorizados()
							- dia.conError();
					return ComprobanteReporteDiaResponse.builder().fecha(dia.fecha()).total(dia.total())
							.autorizados(dia.autorizados()).pendientes(dia.pendientes())
							.noAutorizados(dia.noAutorizados()).conError(dia.conError()).otros(otros).build();
				}).toList();
		long total = porDia.stream().mapToLong(ComprobanteReporteDiaResponse::getTotal).sum();
		long autorizados = porDia.stream().mapToLong(ComprobanteReporteDiaResponse::getAutorizados).sum();
		long pendientes = porDia.stream().mapToLong(ComprobanteReporteDiaResponse::getPendientes).sum();
		long noAutorizados = porDia.stream().mapToLong(ComprobanteReporteDiaResponse::getNoAutorizados).sum();
		long conError = porDia.stream().mapToLong(ComprobanteReporteDiaResponse::getConError).sum();
		long otros = porDia.stream().mapToLong(ComprobanteReporteDiaResponse::getOtros).sum();

		return ComprobanteReporteResumenResponse.builder().total(total).autorizados(autorizados)
				.pendientes(pendientes).noAutorizados(noAutorizados).conError(conError).otros(otros)
				.porDia(porDia).build();
	}

	@Transactional(readOnly = true)
	public ComprobanteReporteDetalleResponse obtenerDetalle(Long idEmpresa, Long id) {
		Comprobante comprobante = comprobanteRepository.findByIdAndEmpresaIdAndEstadoRegistro(id, idEmpresa,
				EstadoRegistro.ACTIVO).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		List<ComprobanteReporteAuditoriaResponse> auditoria = auditoriaRepository
				.findByComprobanteIdAndEstadoRegistroOrderByFechaInicioDesc(id, EstadoRegistro.ACTIVO).stream()
				.map(evento -> ComprobanteReporteAuditoriaResponse.builder().etapa(evento.getEtapa())
						.resultado(evento.getResultado()).estadoAnterior(evento.getEstadoAnterior())
						.estadoNuevo(evento.getEstadoNuevo()).codigoError(evento.getCodigoError())
						.mensajeError(evento.getMensajeError()).fechaInicio(evento.getFechaInicio())
						.fechaFin(evento.getFechaFin()).intento(evento.getIntento()).build())
				.toList();
		return ComprobanteReporteDetalleResponse.builder().comprobante(mapear(comprobante)).auditoria(auditoria)
				.build();
	}

	@Transactional(readOnly = true)
	public byte[] exportarCsv(ComprobanteReporteFiltro filtro) {
		StringBuilder csv = new StringBuilder("\uFEFF");
		csv.append("Numero,Clave de acceso,Fecha de emision,Empresa,RUC emisor,Identificacion receptor,")
				.append("Razon social receptor,Tipo de documento,Estado del proceso,Estado SRI,")
				.append("Numero de autorizacion,Codigo de error,Mensaje de error\r\n");
		for (ComprobanteReporteResponse fila : buscarParaExportacion(filtro)) {
			csv.append(celda(fila.getNumeroComprobante())).append(',')
					.append(celda(fila.getClaveAcceso())).append(',')
					.append(celda(fila.getFechaEmision())).append(',')
					.append(celda(fila.getRazonSocialEmisor())).append(',')
					.append(celda(fila.getRucEmisor())).append(',')
					.append(celda(fila.getIdentificacionReceptor())).append(',')
					.append(celda(fila.getRazonSocialReceptor())).append(',')
					.append(celda(fila.getTipoDocumento())).append(',')
					.append(celda(fila.getEstadoProceso())).append(',')
					.append(celda(fila.getEstadoSri())).append(',')
					.append(celda(fila.getNumeroAutorizacion())).append(',')
					.append(celda(fila.getCodigoError())).append(',')
					.append(celda(fila.getMensajeError())).append("\r\n");
		}
		return csv.toString().getBytes(StandardCharsets.UTF_8);
	}

	@Transactional(readOnly = true)
	public List<ComprobanteReporteResponse> buscarParaExportacion(ComprobanteReporteFiltro filtro) {
		validarFiltro(filtro);
		Specification<Comprobante> criterio = criterio(filtro);
		long total = comprobanteRepository.count(criterio);
		if (total > MAXIMO_FILAS_EXPORTACION) {
			throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
		}
		return comprobanteRepository.findAll(criterio,
				PageRequest.of(0, (int) Math.max(total, 1), ORDEN_REPORTE)).getContent().stream()
				.map(this::mapear).toList();
	}

	private Specification<Comprobante> criterio(ComprobanteReporteFiltro filtro) {
		return (root, query, builder) -> {
			List<Predicate> condiciones = new ArrayList<>();
			condiciones.add(builder.equal(root.get("empresa").get("id"), filtro.getIdEmpresa()));
			condiciones.add(builder.equal(root.get("estadoRegistro"), EstadoRegistro.ACTIVO));
			if (filtro.getFechaDesde() != null) {
				condiciones.add(builder.greaterThanOrEqualTo(root.get("fechaEmision"), filtro.getFechaDesde()));
			}
			if (filtro.getFechaHasta() != null) {
				condiciones.add(builder.lessThanOrEqualTo(root.get("fechaEmision"), filtro.getFechaHasta()));
			}
			if (filtro.getIdEstablecimiento() != null) {
				condiciones.add(builder.equal(root.get("establecimiento").get("id"), filtro.getIdEstablecimiento()));
			}
			if (filtro.getIdPuntoEmision() != null) {
				condiciones.add(builder.equal(root.get("puntoEmision").get("id"), filtro.getIdPuntoEmision()));
			}
			if (noVacio(filtro.getCodigoDocumento())) {
				condiciones.add(builder.equal(root.get("codigoDocumento"), filtro.getCodigoDocumento().trim()));
			}
			if (noVacio(filtro.getEstadoProceso())) {
				condiciones.add(builder.equal(builder.upper(root.get("estadoProceso")),
						filtro.getEstadoProceso().trim().toUpperCase(Locale.ROOT)));
			}
			if (noVacio(filtro.getEstadoSri())) {
				String estadoSri = filtro.getEstadoSri().trim().toUpperCase(Locale.ROOT);
				if ("NO_AUTORIZADO".equals(estadoSri)) {
					condiciones.add(builder.upper(root.get("estadoSri"))
							.in("NO_AUTORIZADO", "NO AUTORIZADO", "NOAUTORIZADO"));
				} else if ("EN_PROCESO".equals(estadoSri)) {
					condiciones.add(builder.upper(root.get("estadoSri")).in("EN_PROCESO", "EN PROCESO"));
				} else {
					condiciones.add(builder.equal(builder.upper(root.get("estadoSri")), estadoSri));
				}
			}
			if ("PENDIENTES".equalsIgnoreCase(filtro.getGrupoEstado())) {
				condiciones.add(builder.and(builder.isNull(root.get("codigoError")),
						builder.upper(root.get("estadoProceso")).in(ESTADOS_PENDIENTES)));
			} else if ("FALLIDOS".equalsIgnoreCase(filtro.getGrupoEstado())) {
				condiciones.add(builder.or(
						builder.and(builder.isNotNull(root.get("codigoError")),
								builder.notEqual(root.get("codigoError"), "")),
						builder.upper(root.get("estadoSri")).in(ESTADOS_NO_AUTORIZADOS)));
			}
			if (noVacio(filtro.getBusqueda())) {
				String texto = "%" + filtro.getBusqueda().trim().toLowerCase(Locale.ROOT) + "%";
				condiciones.add(builder.or(
						builder.like(builder.lower(root.get("idTransaccion")), texto),
						builder.like(builder.lower(root.get("claveAcceso")), texto),
						builder.like(builder.lower(root.get("numComprobante")), texto),
						builder.like(builder.lower(root.get("identificacionReceptor")), texto),
						builder.like(builder.lower(root.get("razonSocialReceptor")), texto)));
			}
			return builder.and(condiciones.toArray(Predicate[]::new));
		};
	}

	private void validarFiltro(ComprobanteReporteFiltro filtro) {
		if (filtro == null || filtro.getIdEmpresa() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		if (filtro.getFechaDesde() != null && filtro.getFechaHasta() != null
				&& filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}

	private ComprobanteReporteResponse mapear(Comprobante comprobante) {
		String numero = comprobante.getNumComprobante();
		if (!noVacio(numero)) {
			numero = comprobante.getEstablecimiento().getCodigo() + "-"
					+ comprobante.getPuntoEmision().getCodigo() + "-" + comprobante.getSecuencial();
		}
		return ComprobanteReporteResponse.builder().id(comprobante.getId())
				.idEmpresa(comprobante.getEmpresa().getId()).razonSocialEmisor(comprobante.getRazonSocialEmisor())
				.rucEmisor(comprobante.getRucEmisor()).idEstablecimiento(comprobante.getEstablecimiento().getId())
				.idPuntoEmision(comprobante.getPuntoEmision().getId())
				.codigoDocumento(comprobante.getCodigoDocumento())
				.tipoDocumento(comprobante.getDocumentoXsd().getNombre()).numeroComprobante(numero)
				.claveAcceso(comprobante.getClaveAcceso()).fechaEmision(comprobante.getFechaEmision())
				.identificacionReceptor(comprobante.getIdentificacionReceptor())
				.razonSocialReceptor(comprobante.getRazonSocialReceptor())
				.estadoProceso(comprobante.getEstadoProceso()).estadoSri(comprobante.getEstadoSri())
				.numeroAutorizacion(comprobante.getNumeroAutorizacion())
				.fechaAutorizacion(comprobante.getFechaAutorizacion()).codigoError(comprobante.getCodigoError())
				.mensajeError(comprobante.getMensajeError()).build();
	}

	private String celda(Object valor) {
		String texto = Objects.toString(valor, "");
		if (texto.matches("^[=+\\-@\\t\\r].*")) {
			texto = "'" + texto;
		}
		return "\"" + texto.replace("\"", "\"\"") + "\"";
	}

	private boolean noVacio(String valor) {
		return valor != null && !valor.isBlank();
	}
}
