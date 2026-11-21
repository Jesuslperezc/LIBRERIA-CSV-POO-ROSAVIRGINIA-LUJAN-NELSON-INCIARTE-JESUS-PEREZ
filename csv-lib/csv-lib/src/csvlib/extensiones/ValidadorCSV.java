package csvlib.extensiones;

import csvlib.modelo.ExcepcionValidacion;
import csvlib.modelo.TablaCSV;

/**
 * Extensión que comprueba que la tabla cumple una regla antes de procesarla.
 *
 * <p><b>OCP:</b> nuevas reglas (columnas mínimas, no nulos, dominios
 * permitidos, ...) se añaden implementando esta interfaz sin tocar el código
 * base.</p>
 *
 * <p><b>ISP:</b> separado de filtros y transformadores; quien solo necesita
 * validar no depende de métodos de mutación.</p>
 */
public interface ValidadorCSV {

    /**
     * Comprueba la regla sobre la tabla.
     *
     * @throws ExcepcionValidacion si la regla no se cumple.
     */
    void validar(TablaCSV tabla) throws ExcepcionValidacion;
}
