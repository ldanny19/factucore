package ec.dalara.factucore.application.port.out;

import java.io.InputStream;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdDefinitionSource;

public interface XsdParserPort {
    XsdDefinitionSource parse(InputStream inputStream, String systemId);
}
