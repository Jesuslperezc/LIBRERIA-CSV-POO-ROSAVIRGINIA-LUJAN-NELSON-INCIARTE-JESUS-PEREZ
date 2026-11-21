package csvlib.builtins;

import csvlib.extensiones.TransformadorCelda;
import csvlib.modelo.TablaCSV;

/**
 * Transformador que elimina espacios en blanco al inicio y final de cada
 * celda.
 *
 * <p><b>OCP:</b> nueva extensión sin tocar la librería.</p>
 */
public class TransformadorRecortarEspacios implements TransformadorCelda {

    @Override
    public String transformar(String valorActual, int indiceFila, int indiceCol, TablaCSV contexto) {
        return valorActual == null ? "" : valorActual.trim();
    }
}
