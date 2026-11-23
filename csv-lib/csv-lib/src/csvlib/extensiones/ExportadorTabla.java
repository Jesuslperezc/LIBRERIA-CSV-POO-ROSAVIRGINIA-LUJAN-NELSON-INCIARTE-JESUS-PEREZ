package csvlib.extensiones;

import csvlib.modelo.TablaCSV;

/**
 * Extensión que exporta una {@link TablaCSV} a un formato distinto de la
 * impresión por consola.
 *
 */
public interface ExportadorTabla {

    /**
     * @return la representación de la tabla en el formato concreto.
     */
    String exportar(TablaCSV tabla);
}
