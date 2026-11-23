package csvlib.nucleo;

import csvlib.modelo.TablaCSV;

/**
 * Contrato para imprimir o renderizar una {@link TablaCSV}.

 */
public interface ImpresorTabla {

    /** Imprime la tabla en el destino asociado a la implementación. */
    void imprimir(TablaCSV tabla);
}
