package csvlib.nucleo;

import csvlib.extensiones.ExportadorTabla;
import csvlib.modelo.TablaCSV;

import java.io.IOException;
import java.util.Objects;

/**
 * Fachada de alto nivel (patrón Facade) que combina un {@link LectorCSV}, un
 * {@link ImpresorTabla} y un {@link ProcesamientoTuberia} para ofrecer las
 * cuatro funciones básicas del enunciado:
 *
 * <ol>
 *   <li><b>Función 1 — Obtener el archivo CSV y crear un objeto que lo
 *       represente</b>: {@link #cargar}.</li>
 *   <li><b>Función 2 — Permitir quitar o agregar filas o columnas</b>: esta
 *       función no se reimplementa aquí; es responsabilidad de la propia
 *       {@link TablaCSV}, cuyas operaciones {@code agregarFila},
 *       {@code quitarFila}, {@code agregarColumna} y {@code quitarColumna}
 *       modelan la estructura. La clase {@code TablaCSV} es {@code final}
 *       porque las operaciones estructurales son de dominio y no constituyen
 *       un punto de variabilidad; para modificar el contenido antes o
 *       después se usa la Tubería (Función 4).</li>
 *   <li><b>Función 3 — Imprimir en pantalla la tabla</b>: {@link #imprimir}.</li>
 *   <li><b>Función 4 — Permitir extender sus capacidades o modificar el
 *       resultado final mediante código propio</b>: {@link #procesar} (aplica
 *       los validadores, filtros y transformadores registrados en la
 *       Tubería) y {@link #exportar} (cambia el formato de salida).</li>
 * </ol>
 *
 * <p><b>DIP:</b> todas las dependencias son abstracciones inyectadas por
 * constructor: {@link LectorCSV}, {@link ImpresorTabla} y
 * {@link ProcesamientoTuberia} (no la clase concreta {@link Tuberia}). Se
 * ofrece un constructor de conveniencia que cablea las implementaciones por
 * defecto, de modo que un uso sencillo no requiere ensamblar nada, pero un
 * uso avanzado puede inyectar las suyas.</p>
 *
 * <p><b>SRP:</b> el procesador solo coordina; la lectura, impresión y
 * procesamiento recaen en sus colaboradores.</p>
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
     *
     * <p>Corresponde al primer aspecto de la Función 4 del enunciado:
     * "extender sus capacidades o modificar el resultado final mediante
     * código propio". El código propio se inyecta al construir la Tuberia.</p>
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
     *
     * <p>Corresponde al segundo aspecto de la Función 4 del enunciado:
     * "modificar el resultado final". El exportador se recibe como parámetro
     * (no se registra en la Tuberia) para mantener la separación entre
     * transformación y presentación: un mismo cliente puede usar varios
     * exportadores sobre la misma tabla sin reconstruir la Tuberia.</p>
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
