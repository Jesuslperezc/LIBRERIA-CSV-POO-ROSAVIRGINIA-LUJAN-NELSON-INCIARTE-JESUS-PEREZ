package csvlib.extensiones;

import csvlib.modelo.ExcepcionValidacion;
import csvlib.modelo.TablaCSV;

/**
 * Extensión que comprueba que la tabla cumple una regla antes de procesarla.
 */
public interface ValidadorCSV {

    /**
     * Comprueba la regla sobre la tabla.
     *
     * @throws ExcepcionValidacion si la regla no se cumple.
     */
    void validar(TablaCSV tabla) throws ExcepcionValidacion;
}
