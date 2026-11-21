package csvlib.nucleo;

import csvlib.modelo.TablaCSV;
import java.io.IOException;

/**
 * Contrato para leer un archivo CSV y construir una {@link TablaCSV}.
 *
 * <p><b>DIP:</b> las clases de alto nivel (p. ej. {@code ProcesadorCSV})
 * dependen de esta abstracción, no de una implementación concreta de lectura.
 * Cualquier nueva fuente (URL, base de datos, stream en memoria) puede
 * acoplarse implementando esta interfaz.</p>
 *
 * <p><b>ISP:</b> se separa deliberadamente de {@link ImpresorTabla}; un
 * cliente que solo necesita leer no se ve obligado a depender de métodos de
 * impresión.</p>
 */
public interface LectorCSV {

    /**
     * Lee el archivo ubicado en {@code ruta} y devuelve una {@link TablaCSV}
     * con sus datos representados como {@code String} (comportamiento por
     * defecto del enunciado).
     *
     * @throws IOException              si el archivo no existe o no se puede leer.
     * @throws IllegalArgumentException si el archivo está vacío o mal formado.
     */
    TablaCSV leer(String ruta) throws IOException;
}
