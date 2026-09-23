package ec.dalara.factucore.adapter.out.ride;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.RidePort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PdfRideAdapter implements RidePort {

    private static final float MARGEN_X = 40;
    private static final float POSICION_Y_INICIAL = 800;
    private static final float INTERLINEADO = 13;
    private static final float POSICION_Y_MINIMA = 50;

    private final ObjectMapper objectMapper;

    @Override
    public byte[] generar(Comprobante comprobante) {
        if (comprobante == null || comprobante.getDatosComprobante() == null) {
            throw new ApplicationException(MessageCodes.RIDE_COMPROBANTE_REQUERIDO);
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            List<String> lineas = construirLineas(comprobante);

            PDPageContentStream contenido = null;
            float y = POSICION_Y_INICIAL;

            try {
                for (String linea : lineas) {
                    if (contenido == null || y < POSICION_Y_MINIMA) {
                        if (contenido != null) {
                            contenido.close();
                        }
                        PDPage pagina = new PDPage(PDRectangle.A4);
                        document.addPage(pagina);
                        contenido = new PDPageContentStream(document, pagina);
                        y = POSICION_Y_INICIAL;
                    }

                    contenido.beginText();
                    contenido.setFont(linea.startsWith("FACTUCORE") ? negrita : normal, 9);
                    contenido.newLineAtOffset(MARGEN_X, y);
                    contenido.showText(sanitizar(linea));
                    contenido.endText();
                    y -= INTERLINEADO;
                }
            } finally {
                if (contenido != null) {
                    contenido.close();
                }
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ApplicationException applicationException) {
                throw applicationException;
            }
            throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR, exception.getMessage());
        }
    }

    private List<String> construirLineas(Comprobante comprobante) {
        List<String> lineas = new ArrayList<>();
        lineas.add("FACTUCORE - REPRESENTACION IMPRESA DEL DOCUMENTO ELECTRONICO");
        lineas.add("Clave de acceso: " + valor(comprobante.getClaveAcceso()));
        lineas.add("Numero de autorizacion: " + valor(comprobante.getNumeroAutorizacion()));
        lineas.add("Fecha de autorizacion: " + valor(comprobante.getFechaAutorizacion()));
        lineas.add("RUC emisor: " + valor(comprobante.getRucEmisor()));
        lineas.add("Razon social emisor: " + valor(comprobante.getRazonSocialEmisor()));
        lineas.add("Nombre comercial: " + valor(comprobante.getNombreComercialEmisor()));
        lineas.add("Direccion matriz: " + valor(comprobante.getDireccionMatrizEmisor()));
        lineas.add("Direccion establecimiento: " + valor(comprobante.getDireccionEstablecimientoEmisor()));
        lineas.add("Receptor: " + valor(comprobante.getRazonSocialReceptor()));
        lineas.add("Identificacion receptor: " + valor(comprobante.getIdentificacionReceptor()));
        lineas.add("Documento: " + valor(comprobante.getCodigoDocumento()) + "-" + valor(comprobante.getSecuencial()));

        try {
            Object datos = objectMapper.readValue(comprobante.getDatosComprobante(), Object.class);
            if (datos instanceof Map<?, ?> mapa) {
                mapa.forEach((clave, valor) -> agregarLinea(lineas, String.valueOf(clave), valor));
            } else {
                lineas.add("Datos: " + String.valueOf(datos));
            }
        } catch (Exception exception) {
            throw new ApplicationException(MessageCodes.RIDE_DATOS_INVALIDOS, exception.getMessage());
        }
        return lineas;
    }

    private void agregarLinea(List<String> lineas, String clave, Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            mapa.forEach((hijo, dato) -> agregarLinea(lineas, clave + "." + hijo, dato));
            return;
        }
        lineas.add(clave + ": " + String.valueOf(valor));
    }

    private String sanitizar(String valor) {
        return valor == null ? "" : valor.replaceAll("[^\\x20-\\x7E]", " ");
    }

    private String valor(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }
}
