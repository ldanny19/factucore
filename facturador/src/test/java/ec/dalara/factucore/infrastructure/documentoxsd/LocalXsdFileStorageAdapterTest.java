package ec.dalara.factucore.infrastructure.documentoxsd;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalXsdFileStorageAdapterTest {

	@TempDir
	Path directorioTemporal;

	@Test
	void guardaElArchivoEnLaCarpetaDelTipoYPermiteEliminarlo() throws Exception {
		var adapter = new LocalXsdFileStorageAdapter(directorioTemporal.toString());
		byte[] contenido = "<xs:schema/>".getBytes(StandardCharsets.UTF_8);

		Path ruta = adapter.guardar("factura", "factura.xsd", contenido);

		assertTrue(ruta.startsWith(directorioTemporal.resolve("factura")));
		assertArrayEquals(contenido, Files.readAllBytes(ruta));

		adapter.eliminar(ruta);

		assertFalse(Files.exists(ruta));
	}
}
