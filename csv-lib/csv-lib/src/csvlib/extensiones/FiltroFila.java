package csvlib.extensiones;

import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;

/**
 * Extensión que decide si una fila debe conservarse o eliminarse.

 */
public interface FiltroFila {

    /**
     * @param fila     fila a evaluar.
     * @param contexto tabla completa (útil para inspeccionar encabezados).
     * @return {@code true} si la fila debe conservarse, {@code false} si debe
     *         eliminarse del resultado.
     */
    boolean mantener(Fila fila, TablaCSV contexto);
}
