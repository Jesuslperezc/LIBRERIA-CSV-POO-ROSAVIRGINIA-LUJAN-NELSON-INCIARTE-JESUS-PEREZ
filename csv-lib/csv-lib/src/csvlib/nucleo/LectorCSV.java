package csvlib.nucleo;

import csvlib.modelo.TablaCSV;
import java.io.IOException;

/**
 * Contrato para leer un archivo CSV y construir una {@link TablaCSV}.
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
