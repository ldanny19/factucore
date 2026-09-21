package ec.dalara.factucore.adapter.out.xml;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;

@Component
public class XmlValidatorAdapter implements XmlValidatorPort {

    @Override
    public void validar(
            String xml,
            String contenidoXsd
    ) {
        if (xml == null || xml.isBlank()) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.XML.REQUERIDO"
            );
        }

        if (contenidoXsd == null || contenidoXsd.isBlank()) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.XSD.REQUERIDO"
            );
        }

        try {
            SchemaFactory schemaFactory =
                    SchemaFactory.newInstance(
                            XMLConstants.W3C_XML_SCHEMA_NS_URI
                    );

            schemaFactory.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    ""
            );

            schemaFactory.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    ""
            );

            Schema schema =
                    schemaFactory.newSchema(
                            new StreamSource(
                                    new StringReader(contenidoXsd)
                            )
                    );

            var validator = schema.newValidator();

            validator.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    ""
            );

            validator.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    ""
            );

            validator.validate(
                    new StreamSource(
                            new StringReader(xml)
                    )
            );

        } catch (SAXException exception) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ERROR"
            );

        } catch (Exception exception) {
            throw new ApplicationException(
                    "FACTUCORE.XML.VALIDACION.ERROR"
            );
        }
    }
}