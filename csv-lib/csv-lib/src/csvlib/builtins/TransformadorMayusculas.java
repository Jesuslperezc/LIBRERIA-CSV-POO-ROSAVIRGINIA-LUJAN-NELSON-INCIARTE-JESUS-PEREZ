package csvlib.builtins;

import csvlib.extensiones.TransformadorCelda;
import csvlib.modelo.TablaCSV;

/**
 * Transformador que convierte a mayúsculas todas las celdas de las columnas
 * indicadas. Si no se especifica ninguna columna, se aplica a toda la tabla.
 *
 * <p><b>OCP:</b> nueva extensión sin tocar la librería.</p>
 */
public class TransformadorMayusculas implements TransformadorCelda {

    private final int[] indicesColumnasObjetivo;

    /** Aplica a todas las columnas. */
    public TransformadorMayusculas() {
        this.indicesColumnasObjetivo = null;
    }

    /** Aplica solo a los índices indicados (0-based). */
    public TransformadorMayusculas(int... indicesColumnas) {
        this.indicesColumnasObjetivo = indicesColumnas == null ? null : indicesColumnas.clone();
    }

    @Override
    public String transformar(String valorActual, int indiceFila, int indiceCol, TablaCSV contexto) {
        if (indicesColumnasObjetivo != null) {
            boolean coincide = false;
            for (int objetivo : indicesColumnasObjetivo) {
                if (objetivo == indiceCol) {
                    coincide = true;
                    break;
                }
            }
            if (!coincide) {
                return valorActual;
            }
        }
        return valorActual == null ? "" : valorActual.toUpperCase();
    }
}
