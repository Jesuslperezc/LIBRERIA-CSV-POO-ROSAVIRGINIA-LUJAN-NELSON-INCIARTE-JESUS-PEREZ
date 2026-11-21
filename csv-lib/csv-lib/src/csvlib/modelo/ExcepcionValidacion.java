package csvlib.modelo;

/**
 * Excepción lanzada por los validadores cuando la tabla no cumple una regla.
 *
 * Responsabilidad (SRP): representar exclusivamente un fallo de validación;
 * no se usa para errores de lectura ni de impresión.
 */
public class ExcepcionValidacion extends RuntimeException {

    public ExcepcionValidacion(String mensaje) {
        super(mensaje);
    }

    public ExcepcionValidacion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
