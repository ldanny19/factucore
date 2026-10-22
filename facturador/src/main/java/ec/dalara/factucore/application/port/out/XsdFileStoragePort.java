package ec.dalara.factucore.application.port.out;

import java.nio.file.Path;

public interface XsdFileStoragePort {

	Path guardar(String carpetaTipoDocumento, String nombreArchivo, byte[] contenido);

	void eliminar(Path ruta);
}
