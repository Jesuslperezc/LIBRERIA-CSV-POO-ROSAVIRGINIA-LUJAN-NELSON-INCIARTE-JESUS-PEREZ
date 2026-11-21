package csvlib.nucleo;

import csvlib.modelo.TablaCSV;

/**
 * Contrato para imprimir o renderizar una {@link TablaCSV}.
 *
 * <p><b>DIP:</b> {@code ProcesadorCSV} depende de esta abstracción, no de una
 * implementación concreta. Se pueden añadir impresores (HTML, Markdown,
 * logger, ...) sin tocar el código existente.</p>
 *
 * <p><b>ISP:</b> separado de {@link LectorCSV}; un cliente que solo imprime
 * no necesita conocer métodos de lectura.</p>
 */
public interface ImpresorTabla {

    /** Imprime la tabla en el destino asociado a la implementación. */
    void imprimir(TablaCSV tabla);
}
