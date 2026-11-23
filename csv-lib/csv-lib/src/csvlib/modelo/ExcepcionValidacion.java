package csvlib.modelo;

/**
 * Excepción lanzada por los validadores cuando la tabla no cumple una regla.

 */
public class ExcepcionValidacion extends RuntimeException {

    public ExcepcionValidacion(String mensaje) {
        super(mensaje);
    }

    public ExcepcionValidacion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
