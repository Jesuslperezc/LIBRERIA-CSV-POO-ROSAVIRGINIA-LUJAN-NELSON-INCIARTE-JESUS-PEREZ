package csvlib.extensiones;

import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;

/**
 * Extensión que decide si una fila debe conservarse o eliminarse.
 *
 * <p><b>OCP:</b> para crear un filtro nuevo basta implementar esta interfaz
 * y registrarlo en la Tuberia; no se modifica código existente.</p>
 *
 * <p><b>ISP:</b> el contrato se limita a una sola operación ({@code mantener}),
 * de modo que quien solo necesita filtrar no depende de métodos de
 * transformación o exportación.</p>
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
