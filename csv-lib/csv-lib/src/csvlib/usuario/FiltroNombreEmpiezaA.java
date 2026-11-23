package csvlib.usuario;

import csvlib.extensiones.FiltroFila;
import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;

/**
 * Extensión propia del usuario: filtro que conserva solo las filas cuyo
 * valor en la columna indicada, tras eliminar espacios en blanco al inicio
 * y al final, empieza por una letra determinada (en mayúsculas).
 *
 * <p><b>Este código vive en un paquete ajeno a {@code csvlib}:</b> demuestra
 * que la librería se extiende <i>sin alterar su propio código</i>, tal como
 * pide el enunciado. Solo implementa una interfaz pública
 * ({@link FiltroFila}) y se registra al construir la Tuberia.</p>
 *
 * <p><b>LSP:</b> respeta el contrato de {@link FiltroFila}: devuelve
 * {@code true} para conservar, {@code false} para descartar. Si la columna
 * no existe o la fila es más corta, devuelve {@code false} en lugar de
 * lanzar una excepción inesperada.</p>
 */
public final class FiltroNombreEmpiezaA implements FiltroFila {

    private final String nombreColumna;
    private final char letra;

    /** Filtra la columna indicada por la letra 'A'. */
    public FiltroNombreEmpiezaA(String nombreColumna) {
        this(nombreColumna, 'A');
    }

    public FiltroNombreEmpiezaA(String nombreColumna, char letra) {
        if (nombreColumna == null || nombreColumna.isBlank()) {
            throw new IllegalArgumentException("El nombre de columna no puede ser vacío.");
        }
        this.nombreColumna = nombreColumna;
        this.letra = Character.toUpperCase(letra);
    }

    @Override
    public boolean mantener(Fila fila, TablaCSV contexto) {
        int idx;
        try {
            idx = contexto.indiceColumna(nombreColumna);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (idx >= fila.tamanio()) {
            return false;
        }
        String valor = fila.getCelda(idx);
        if (valor == null || valor.isBlank()) {
            return false;
        }
        // trim() corrige el bug original: sin esto, filas como "  Andrea  "
        // se descartaban porque el filtro se ejecuta ANTES que el
        // TransformadorRecortarEspacios.
        String normalizado = valor.trim().toUpperCase();
        return !normalizado.isEmpty() && normalizado.charAt(0) == letra;
    }
}
