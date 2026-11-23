package csvlib.nucleo;

/**
 * Contrato que define el procesamiento de una {@link csvlib.modelo.TablaCSV}
 * mediante una secuencia de extensiones (validadores, filtros y
 * transformadores).
 *
 * <p><b>DIP:</b> al definir esta interfaz, {@code ProcesadorCSV} puede depender
 * de la abstracción {@code ProcesamientoTuberia} en lugar de la clase concreta
 * {@link Tuberia}. Cualquier nueva implementación de procesamiento que respete
 * el contrato puede sustituir a {@code Tuberia} sin tocar el procesador.</p>
 *
 * <p><b>LSP:</b> toda implementación debe devolver una tabla nueva (no la
 * entrada) y no mutar la tabla recibida como argumento.</p>
 */
public interface ProcesamientoTuberia {

    /**
     * Procesa la tabla de entrada aplicando las extensiones registradas.
     *
     * @param entrada tabla a procesar; no debe ser {@code null}.
     * @return una nueva {@link csvlib.modelo.TablaCSV} con el resultado; la
     *         tabla original no se modifica.
     * @throws csvlib.modelo.ExcepcionValidacion si algún validador falla.
     */
    csvlib.modelo.TablaCSV procesar(csvlib.modelo.TablaCSV entrada);
}
