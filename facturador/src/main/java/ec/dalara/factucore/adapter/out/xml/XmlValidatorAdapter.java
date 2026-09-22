package ec.dalara.factucore.adapter.out.xml;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
public class XmlValidatorAdapter implements XmlValidatorPort {

    @Override
    public void validar(
            String xml,
            DocumentDefinitionModel definition
    ) {
        if (xml == null || xml.isBlank()) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.XML.REQUERIDO"
            );
        }

        if (definition == null) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.DEFINITION.REQUERIDA"
            );
        }

        try {
            Document document =
                    parsearXml(xml);

            validarEstructuraBasica(
                    document,
                    definition
            );

            validarElementos(
                    document.getDocumentElement(),
                    null,
                    definition
            );

            validarAtributos(
                    document.getDocumentElement(),
                    definition
            );

        } catch (ApplicationException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ERROR"
            );
        }
    }

    private Document parsearXml(
            String xml
    ) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING,
                true
        );

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory
                .newDocumentBuilder()
                .parse(
                        new org.xml.sax.InputSource(
                                new StringReader(xml)
                        )
                );
    }

    private void validarEstructuraBasica(
            Document document,
            DocumentDefinitionModel definition
    ) {
        ElementoXsdModel raiz =
                obtenerRaiz(definition);

        org.w3c.dom.Element elementoRaiz =
                document.getDocumentElement();

        if (!Objects.equals(
                elementoRaiz.getLocalName() != null
                        ? elementoRaiz.getLocalName()
                        : elementoRaiz.getNodeName(),
                raiz.getNombre()
        )) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.RAIZ.INVALIDA"
            );
        }

        String namespaceEsperado =
                definition.getVersion()
                        .getNamespaceXml();

        if (namespaceEsperado != null
                && !namespaceEsperado.isBlank()
                && !Objects.equals(
                        namespaceEsperado,
                        elementoRaiz.getNamespaceURI()
                )) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.NAMESPACE.INVALIDO"
            );
        }
    }

    private void validarElementos(
            org.w3c.dom.Element elementoXml,
            ElementoXsdModel definicionPadre,
            DocumentDefinitionModel definition
    ) {
        ElementoXsdModel definicion =
                buscarDefinicionElemento(
                        elementoXml,
                        definicionPadre,
                        definition
                );

        if (definicion == null) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ELEMENTO.NO_DEFINIDO"
            );
        }

        validarValorElemento(
                elementoXml,
                definicion,
                definition
        );

        List<ElementoXsdModel> hijosDefinidos =
                obtenerHijos(
                        definicion,
                        definition
                );

        for (ElementoXsdModel hijoDefinido :
                hijosDefinidos) {

            List<org.w3c.dom.Element> hijosXml =
                    obtenerHijosXml(
                            elementoXml,
                            hijoDefinido.getNombre()
                    );

            validarOcurrencias(
                    hijoDefinido,
                    hijosXml.size()
            );

            for (org.w3c.dom.Element hijoXml :
                    hijosXml) {

                validarElementos(
                        hijoXml,
                        definicion,
                        definition
                );
            }
        }

        validarHijosNoDefinidos(
                elementoXml,
                hijosDefinidos,
                definition
        );
    }

    private ElementoXsdModel buscarDefinicionElemento(
            org.w3c.dom.Element elementoXml,
            ElementoXsdModel padre,
            DocumentDefinitionModel definition
    ) {
        return definition.getElementos()
                .stream()
                .filter(elemento -> {

                    if (padre == null) {
                        return elemento.getElementoPadreId() == null;
                    }

                    return Objects.equals(
                            elemento.getElementoPadreId(),
                            padre.getId()
                    );
                })
                .filter(elemento ->
                        Objects.equals(
                                elemento.getNombre(),
                                elementoXml.getLocalName() != null
                                        ? elementoXml.getLocalName()
                                        : elementoXml.getNodeName()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private void validarHijosNoDefinidos(
            org.w3c.dom.Element elementoXml,
            List<ElementoXsdModel> hijosDefinidos,
            DocumentDefinitionModel definition
    ) {
        org.w3c.dom.NodeList hijos =
                elementoXml.getChildNodes();

        for (int i = 0; i < hijos.getLength(); i++) {

            org.w3c.dom.Node nodo =
                    hijos.item(i);

            if (nodo.getNodeType()
                    != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }

            org.w3c.dom.Element hijoXml =
                    (org.w3c.dom.Element) nodo;

            String nombre =
                    hijoXml.getLocalName() != null
                            ? hijoXml.getLocalName()
                            : hijoXml.getNodeName();

            boolean definido =
                    hijosDefinidos.stream()
                            .anyMatch(elemento ->
                                    Objects.equals(
                                            elemento.getNombre(),
                                            nombre
                                    )
                            );

            if (!definido) {
                throw new ApplicationException(
                        "FACTUCORE.XML.VALIDACION.ELEMENTO.NO_DEFINIDO"
                );
            }
        }
    }

    private void validarOcurrencias(
            ElementoXsdModel definicion,
            int cantidad
    ) {
        Integer minimo =
                definicion.getMinOcurrencias();

        Integer maximo =
                definicion.getMaxOcurrencias();

        if (minimo != null && cantidad < minimo) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.OCURRENCIA.MINIMA"
            );
        }

        if (maximo != null
                && cantidad > maximo) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.OCURRENCIA.MAXIMA"
            );
        }

        if (Boolean.TRUE.equals(
                definicion.getObligatorio()
        ) && cantidad == 0) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ELEMENTO.REQUERIDO"
            );
        }

        if (!Boolean.TRUE.equals(
                definicion.getRepetible()
        ) && cantidad > 1) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ELEMENTO.NO_REPETIBLE"
            );
        }
    }

    private void validarValorElemento(
            org.w3c.dom.Element elementoXml,
            ElementoXsdModel definicion,
            DocumentDefinitionModel definition
    ) {
        boolean tieneHijos =
                !obtenerHijos(
                        definicion,
                        definition
                ).isEmpty();

        if (tieneHijos) {
            return;
        }

        String valor =
                elementoXml.getTextContent();

        if (valor == null) {
            valor = "";
        }

        validarLongitud(
                valor,
                definicion
        );

        validarTipo(
                valor,
                definicion
        );

        validarNumerico(
                valor,
                definicion
        );

        validarPatron(
                valor,
                definicion
        );

        validarEnumeracion(
                valor,
                definicion,
                definition
        );
    }

    private void validarLongitud(
            String valor,
            ElementoXsdModel definicion
    ) {
        if (definicion.getLongitudMinima() != null
                && valor.length()
                < definicion.getLongitudMinima()) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.LONGITUD.MINIMA"
            );
        }

        if (definicion.getLongitudMaxima() != null
                && valor.length()
                > definicion.getLongitudMaxima()) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.LONGITUD.MAXIMA"
            );
        }
    }

    private void validarTipo(
            String valor,
            ElementoXsdModel definicion
    ) {
        String tipo =
                definicion.getTipoDato();

        if (tipo == null || tipo.isBlank()) {
            return;
        }

        try {
            switch (tipo.toLowerCase()) {

                case "string":
                case "normalizedstring":
                case "token":
                case "text":
                    break;

                case "integer":
                case "int":
                case "long":
                    Long.parseLong(valor);
                    break;

                case "decimal":
                case "double":
                case "float":
                    new BigDecimal(valor);
                    break;

                case "boolean":
                    if (!valor.equals("true")
                            && !valor.equals("false")
                            && !valor.equals("1")
                            && !valor.equals("0")) {

                        throw new IllegalArgumentException();
                    }
                    break;

                case "date":
                    java.time.LocalDate.parse(valor);
                    break;

                case "datetime":
                case "dateTime":
                    java.time.LocalDateTime.parse(valor);
                    break;

                default:
                    break;
            }

        } catch (Exception exception) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.TIPO.INVALIDO"
            );
        }
    }

    private void validarNumerico(
            String valor,
            ElementoXsdModel definicion
    ) {
        if (definicion.getValorMinimo() == null
                && definicion.getValorMaximo() == null
                && definicion.getDigitosTotales() == null
                && definicion.getDecimales() == null) {

            return;
        }

        BigDecimal numero;

        try {
            numero = new BigDecimal(valor);
        } catch (Exception exception) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.NUMERICO.INVALIDO"
            );
        }

        if (definicion.getValorMinimo() != null
                && numero.compareTo(
                        definicion.getValorMinimo()
                ) < 0) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.VALOR.MINIMO"
            );
        }

        if (definicion.getValorMaximo() != null
                && numero.compareTo(
                        definicion.getValorMaximo()
                ) > 0) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.VALOR.MAXIMO"
            );
        }

        if (definicion.getDigitosTotales() != null) {

            int digitos =
                    contarDigitos(numero);

            if (digitos >
                    definicion.getDigitosTotales()) {

                throw new ApplicationException(
                        "FACTUCORE.XML.VALIDACION.DIGITOS.TOTALES"
                );
            }
        }

        if (definicion.getDecimales() != null) {

            int decimales =
                    Math.max(
                            0,
                            numero.scale()
                    );

            if (decimales >
                    definicion.getDecimales()) {

                throw new ApplicationException(
                        "FACTUCORE.XML.VALIDACION.DECIMALES"
                );
            }
        }
    }

    private int contarDigitos(
            BigDecimal numero
    ) {
        return numero
                .unscaledValue()
                .abs()
                .toString()
                .length();
    }

    private void validarPatron(
            String valor,
            ElementoXsdModel definicion
    ) {
        String patron =
                definicion.getPatron();

        if (patron == null || patron.isBlank()) {
            return;
        }

        if (!valor.matches(patron)) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.PATRON.INVALIDO"
            );
        }
    }

    private void validarEnumeracion(
            String valor,
            ElementoXsdModel definicion,
            DocumentDefinitionModel definition
    ) {
        List<String> valoresPermitidos =
                definition.getEnumeraciones()
                        .stream()
                        .filter(enumeracion ->
                                Objects.equals(
                                        enumeracion.getElementoXsdId(),
                                        definicion.getId()
                                )
                        )
                        .map(
                                EnumeracionXsdModel::getValor
                        )
                        .toList();

        if (!valoresPermitidos.isEmpty()
                && !valoresPermitidos.contains(valor)) {

            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ENUMERACION.INVALIDA"
            );
        }
    }

    private void validarAtributos(
            org.w3c.dom.Element elementoXml,
            DocumentDefinitionModel definition
    ) {
        ElementoXsdModel definicion =
                definition.getElementos()
                        .stream()
                        .filter(elemento ->
                                Objects.equals(
                                        elemento.getNombre(),
                                        elementoXml.getLocalName() != null
                                                ? elementoXml.getLocalName()
                                                : elementoXml.getNodeName()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        if (definicion == null) {
            return;
        }

        List<AtributoXsdModel> atributos =
                definition.getAtributos()
                        .stream()
                        .filter(atributo ->
                                Objects.equals(
                                        atributo.getElementoXsdId(),
                                        definicion.getId()
                                )
                        )
                        .toList();

        for (AtributoXsdModel atributo : atributos) {

            boolean existe =
                    elementoXml.hasAttribute(
                            atributo.getNombre()
                    );

            if (Boolean.TRUE.equals(
                    atributo.getObligatorio()
            ) && !existe
                    && atributo.getValorPredeterminado() == null) {

                throw new ApplicationException(
                        "FACTUCORE.XML.VALIDACION.ATRIBUTO.REQUERIDO"
                );
            }

            if (!existe) {
                continue;
            }

            String valor =
                    elementoXml.getAttribute(
                            atributo.getNombre()
                    );

            validarAtributoTipo(
                    valor,
                    atributo
            );

            validarAtributoPatron(
                    valor,
                    atributo
            );
        }

        validarAtributosNoDefinidos(
                elementoXml,
                atributos
        );
    }

    private void validarAtributosNoDefinidos(
            org.w3c.dom.Element elementoXml,
            List<AtributoXsdModel> atributos
    ) {
        org.w3c.dom.NamedNodeMap atributosXml =
                elementoXml.getAttributes();

        for (int i = 0;
             i < atributosXml.getLength();
             i++) {

            org.w3c.dom.Node atributoXml =
                    atributosXml.item(i);

            String namespace =
                    atributoXml.getNamespaceURI();

            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI
                    .equals(namespace)) {
                continue;
            }

            boolean definido =
                    atributos.stream()
                            .anyMatch(atributo ->
                                    Objects.equals(
                                            atributo.getNombre(),
                                            atributoXml.getNodeName()
                                    )
                            );

            if (!definido) {
                throw new ApplicationException(
                        "FACTUCORE.XML.VALIDACION.ATRIBUTO.NO_DEFINIDO"
                );
            }
        }
    }

    private void validarAtributoTipo(
            String valor,
            AtributoXsdModel atributo
    ) {
        String tipo =
                atributo.getTipoDato();

        if (tipo == null || tipo.isBlank()) {
            return;
        }

        try {
            switch (tipo.toLowerCase()) {

                case "string":
                case "normalizedstring":
                case "token":
                case "text":
                    break;

                case "integer":
                case "int":
                case "long":
                    Long.parseLong(valor);
                    break;

                case "decimal":
                case "double":
                case "float":
                    new BigDecimal(valor);
                    break;

                case "boolean":
                    if (!valor.equals("true")
                            && !valor.equals("false")
                            && !valor.equals("1")
                            && !valor.equals("0")) {

                        throw new IllegalArgumentException();
                    }
                    break;

                default:
                    break;
            }

        } catch (Exception exception) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ATRIBUTO.TIPO.INVALIDO"
            );
        }
    }

    private void validarAtributoPatron(
            String valor,
            AtributoXsdModel atributo
    ) {
        if (atributo.getPatron() == null
                || atributo.getPatron().isBlank()) {
            return;
        }

        if (!valor.matches(
                atributo.getPatron()
        )) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ATRIBUTO.PATRON.INVALIDO"
            );
        }
    }

    private ElementoXsdModel obtenerRaiz(
            DocumentDefinitionModel definition
    ) {
        String nombre =
                definition.getVersion()
                        .getElementoRaiz();

        return definition.getElementos()
                .stream()
                .filter(elemento ->
                        elemento.getElementoPadreId() == null
                )
                .filter(elemento ->
                        nombre == null
                                || nombre.isBlank()
                                || Objects.equals(
                                        elemento.getNombre(),
                                        nombre
                                )
                )
                .findFirst()
                .orElseThrow(() ->
                        new ApplicationException(
                                "FACTUCORE.XML.VALIDACION.ELEMENTO_RAIZ.NO_DEFINIDO"
                        )
                );
    }

    private List<ElementoXsdModel> obtenerHijos(
            ElementoXsdModel padre,
            DocumentDefinitionModel definition
    ) {
        return definition.getElementos()
                .stream()
                .filter(elemento ->
                        Objects.equals(
                                elemento.getElementoPadreId(),
                                padre.getId()
                        )
                )
                .sorted(
                        java.util.Comparator.comparing(
                                ElementoXsdModel::getOrden,
                                java.util.Comparator.nullsLast(
                                        Integer::compareTo
                                )
                        )
                )
                .toList();
    }

    private List<org.w3c.dom.Element> obtenerHijosXml(
            org.w3c.dom.Element padre,
            String nombre
    ) {
        java.util.ArrayList<org.w3c.dom.Element> resultado =
                new java.util.ArrayList<>();

        org.w3c.dom.NodeList hijos =
                padre.getChildNodes();

        for (int i = 0; i < hijos.getLength(); i++) {

            org.w3c.dom.Node nodo =
                    hijos.item(i);

            if (nodo.getNodeType()
                    != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }

            org.w3c.dom.Element elemento =
                    (org.w3c.dom.Element) nodo;

            String nombreXml =
                    elemento.getLocalName() != null
                            ? elemento.getLocalName()
                            : elemento.getNodeName();

            if (Objects.equals(
                    nombre,
                    nombreXml
            )) {
                resultado.add(elemento);
            }
        }

        return resultado;
    }
}