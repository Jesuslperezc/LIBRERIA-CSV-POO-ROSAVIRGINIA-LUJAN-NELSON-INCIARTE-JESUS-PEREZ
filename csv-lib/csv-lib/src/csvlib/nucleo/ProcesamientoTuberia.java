package csvlib.nucleo;

/**
 * Contrato que define el procesamiento de una {@link csvlib.modelo.TablaCSV}
 * mediante una secuencia de extensiones (validadores, filtros y
 * transformadores).

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
