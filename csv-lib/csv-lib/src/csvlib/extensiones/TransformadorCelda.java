package csvlib.extensiones;

import csvlib.modelo.TablaCSV;

/**
 * Extensión que transforma el valor de una celda.
 *
 * <p><b>OCP:</b> para añadir una transformación nueva (parsear a número,
 * normalizar texto, calcular hash, ...) se implementa esta interfaz y se
 * registra en la Tuberia; la librería no se modifica.</p>
 *
 * <p><b>ISP:</b> el contrato se limita a una sola operación ({@code transformar}),
 * independiente de filtros y validadores.</p>
 */
public interface TransformadorCelda {

    /**
     * @param valorActual valor actual de la celda (siempre String).
     * @param indiceFila  índice de fila (0-based, sin contar encabezados).
     * @param indiceCol   índice de columna (0-based).
     * @param contexto    tabla completa.
     * @return el nuevo valor String para esa celda.
     */
    String transformar(String valorActual, int indiceFila, int indiceCol, TablaCSV contexto);
}
