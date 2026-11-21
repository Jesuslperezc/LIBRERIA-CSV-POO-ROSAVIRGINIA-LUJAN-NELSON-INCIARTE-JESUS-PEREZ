package csvlib.extensiones;

import csvlib.modelo.TablaCSV;

/**
 * Extensión que exporta una {@link TablaCSV} a un formato distinto de la
 * impresión por consola.
 *
 * <p><b>OCP:</b> para añadir un formato nuevo (JSON, Markdown, HTML, XML,
 * ...) se implementa esta interfaz y se usa directamente, sin modificar la
 * librería.</p>
 *
 * <p><b>ISP:</b> separado de {@code ImpresorTabla}: el exportador devuelve un
 * String (no imprime), lo que permite reutilizarlo en pipelines, archivos,
 * respuestas HTTP, etc.</p>
 */
public interface ExportadorTabla {

    /**
     * @return la representación de la tabla en el formato concreto.
     */
    String exportar(TablaCSV tabla);
}
