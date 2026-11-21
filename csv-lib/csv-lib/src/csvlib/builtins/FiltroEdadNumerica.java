package csvlib.builtins;

import csvlib.extensiones.FiltroFila;
import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;

/**
 * Filtro que conserva solo las filas en las que la columna indicada contiene
 * un valor numérico entero válido.
 *
 * <p><b>OCP:</b> nueva extensión sin tocar la librería.</p>
 *
 * <p><b>LSP:</b> respeta el contrato de {@link FiltroFila}: devuelve
 * {@code true} para conservar, {@code false} para descartar.</p>
 */
public class FiltroEdadNumerica implements FiltroFila {

    private final String nombreColumna;

    public FiltroEdadNumerica(String nombreColumna) {
        if (nombreColumna == null || nombreColumna.isBlank()) {
            throw new IllegalArgumentException("El nombre de columna no puede ser vacío.");
        }
        this.nombreColumna = nombreColumna;
    }

    @Override
    public boolean mantener(Fila fila, TablaCSV contexto) {
        int idx;
        try {
            idx = contexto.indiceColumna(nombreColumna);
        } catch (IllegalArgumentException e) {
            return false; // columna inexistente: la fila no se conserva
        }
        if (idx >= fila.tamanio()) {
            return false;
        }
        String valor = fila.getCelda(idx);
        if (valor == null || valor.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(valor.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
