package csvlib.builtins;

import csvlib.extensiones.TransformadorCelda;
import csvlib.modelo.TablaCSV;

/**
 * Transformador que elimina espacios en blanco al inicio y final de cada
 * celda.
 */
public class TransformadorRecortarEspacios implements TransformadorCelda {

    @Override
    public String transformar(String valorActual, int indiceFila, int indiceCol, TablaCSV contexto) {
        return valorActual == null ? "" : valorActual.trim();
    }
}
