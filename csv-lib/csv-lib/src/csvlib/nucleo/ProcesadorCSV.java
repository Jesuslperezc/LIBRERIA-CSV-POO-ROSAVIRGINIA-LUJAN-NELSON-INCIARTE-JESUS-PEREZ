package csvlib.nucleo;

import csvlib.extensiones.ExportadorTabla;
import csvlib.modelo.TablaCSV;

import java.io.IOException;
import java.util.Objects;

/**
 * Fachada de alto nivel (patrón Facade)

 */
public final class ProcesadorCSV {

    private final LectorCSV lector;
    private final ImpresorTabla impresor;
    private final ProcesamientoTuberia tuberia;

    /** Constructor con dependencias inyectadas explícitamente. */
    public ProcesadorCSV(LectorCSV lector, ImpresorTabla impresor, ProcesamientoTuberia tuberia) {
        this.lector = Objects.requireNonNull(lector, "LectorCSV no puede ser null.");
        this.impresor = Objects.requireNonNull(impresor, "ImpresorTabla no puede ser null.");
        this.tuberia = Objects.requireNonNull(tuberia, "ProcesamientoTuberia no puede ser null.");
    }

    /**
     * Constructor de conveniencia: usa lector por defecto, impresor por
     * defecto (consola) y una Tuberia vacía (sin extensiones).
     */
    public ProcesadorCSV() {
        this(new LectorArchivo(),
             new ImpresorConsola(),
             Tuberia.crear().construir());
    }

    // ----------------------- Función 1 ----------------------

    /**
     * Lee el CSV indicado y devuelve el objeto que lo representa.
     *
     * @throws IOException              si el archivo no se puede leer.
     * @throws IllegalArgumentException si el archivo está vacío o mal formado.
     */
    public TablaCSV cargar(String ruta) throws IOException {
        return lector.leer(ruta);
    }

    // ----------------------- Función 3 ----------------------

    /** Imprime la tabla usando el {@link ImpresorTabla} configurado. */
    public void imprimir(TablaCSV tabla) {
        impresor.imprimir(tabla);
    }

    // ----------------------- Función 4 ----------------------

    /**
     * Devuelve la tabla procesada por la Tuberia (validadores + filtros +
     * transformadores). La tabla original no se modifica.

     */
    public TablaCSV procesar(TablaCSV tabla) {
        return tuberia.procesar(tabla);
    }

    /**
     * Atajo equivalente a {@code imprimir(procesar(tabla))}: procesa e imprime
     * el resultado en una sola llamada.
     */
    public void procesarEImprimir(TablaCSV tabla) {
        impresor.imprimir(tuberia.procesar(tabla));
    }

    /**
     * Exporta la tabla usando el {@link ExportadorTabla} indicado.

     */
    public String exportar(TablaCSV tabla, ExportadorTabla exportador) {
        Objects.requireNonNull(exportador, "ExportadorTabla no puede ser null.");
        return exportador.exportar(tabla);
    }

    // ----------------------- Accesores ----------------------

    public LectorCSV getLector() {
        return lector;
    }

    public ImpresorTabla getImpresor() {
        return impresor;
    }

    public ProcesamientoTuberia getTuberia() {
        return tuberia;
    }
}
