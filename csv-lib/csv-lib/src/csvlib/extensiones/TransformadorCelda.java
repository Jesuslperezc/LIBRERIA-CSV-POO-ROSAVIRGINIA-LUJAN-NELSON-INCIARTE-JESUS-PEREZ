package csvlib.extensiones;

import csvlib.modelo.TablaCSV;

/**
 * Extensión que transforma el valor de una celda.
 *
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
