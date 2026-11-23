package csvlib.builtins;

import csvlib.extensiones.ValidadorCSV;
import csvlib.modelo.ExcepcionValidacion;
import csvlib.modelo.TablaCSV;

/**
 * Validador que comprueba que la tabla tenga al menos un número mínimo de
 * columnas.
 *
 */
public class ValidadorColumnasMinimas implements ValidadorCSV {

    private final int minimo;

    public ValidadorColumnasMinimas(int minimo) {
        if (minimo < 1) {
            throw new IllegalArgumentException("El mínimo debe ser >= 1.");
        }
        this.minimo = minimo;
    }

    @Override
    public void validar(TablaCSV tabla) throws ExcepcionValidacion {
        if (tabla.cantidadColumnas() < minimo) {
            throw new ExcepcionValidacion(
                "La tabla tiene " + tabla.cantidadColumnas()
                + " columnas; se requieren al menos " + minimo + ".");
        }
    }
}
