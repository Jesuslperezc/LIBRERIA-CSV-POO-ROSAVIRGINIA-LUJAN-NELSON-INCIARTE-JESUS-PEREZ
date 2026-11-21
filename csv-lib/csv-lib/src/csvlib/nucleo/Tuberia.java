package csvlib.nucleo;

import csvlib.extensiones.FiltroFila;
import csvlib.extensiones.TransformadorCelda;
import csvlib.extensiones.ValidadorCSV;
import csvlib.modelo.ExcepcionValidacion;
import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orquesta la ejecución ordenada de las extensiones registradas sobre una
 * {@link TablaCSV}.
 *
 * <p>Orden de aplicación (esqueleto fijo, patrón Template Method por
 * composición):</p>
 * <ol>
 *   <li><b>Validadores</b> — lanzan {@link ExcepcionValidacion} si la tabla no
 *       cumple. No modifican la tabla. (Cadena con cortocircuito.)</li>
 *   <li><b>Filtros de fila</b> — producen una nueva tabla sin las filas que no
 *       cumplen los predicados. La tabla original no se muta. (Cadena AND.)</li>
 *   <li><b>Transformadores de celda</b> — aplican cada transformador sobre
 *       cada celda de la tabla filtrada, en el orden de registro.</li>
 * </ol>
 *
 * <p><b>SRP:</b> la Tuberia SOLO coordina; no valida, no filtra, no
 * transforma por sí mismo: delega en las extensiones.</p>
 *
 * <p><b>OCP:</b> nuevos tipos de extensión se incorporan implementando la
 * interfaz correspondiente y registrándola aquí; la Tuberia no cambia.</p>
 *
 * <p><b>DIP:</b> depende exclusivamente de las abstracciones
 * {@link ValidadorCSV}, {@link FiltroFila} y {@link TransformadorCelda}, nunca
 * de clases concretas.</p>
 *
 * <p><b>LSP:</b> cualquier implementación de esas interfaces puede sustituir
 * a otra en las listas internas sin romper el contrato.</p>
 */
public final class Tuberia {

    private final List<ValidadorCSV> validadores;
    private final List<FiltroFila> filtros;
    private final List<TransformadorCelda> transformadores;

    private Tuberia(List<ValidadorCSV> validadores,
                    List<FiltroFila> filtros,
                    List<TransformadorCelda> transformadores) {
        this.validadores = List.copyOf(validadores);
        this.filtros = List.copyOf(filtros);
        this.transformadores = List.copyOf(transformadores);
    }

    public List<ValidadorCSV> getValidadores() {
        return validadores;
    }

    public List<FiltroFila> getFiltros() {
        return filtros;
    }

    public List<TransformadorCelda> getTransformadores() {
        return transformadores;
    }

    /**
     * Procesa la tabla de entrada aplicando las extensiones registradas.
     *
     * @return una NUEVA {@link TablaCSV} con el resultado; la tabla original
     *         no se modifica (patrón Prototype aplicado al inicio).
     * @throws ExcepcionValidacion si algún validador falla.
     */
    public TablaCSV procesar(TablaCSV entrada) {
        Objects.requireNonNull(entrada, "La tabla de entrada no puede ser null.");

        // 1. Validaciones (no mutan). Cadena con cortocircuito.
        for (ValidadorCSV v : validadores) {
            v.validar(entrada);
        }

        // 2. Filtrado de filas sobre una copia profunda (Prototype).
        TablaCSV trabajo = entrada.copiar();
        List<Fila> conservadas = new ArrayList<>();
        for (Fila fila : trabajo.getFilas()) {
            boolean mantener = true;
            for (FiltroFila f : filtros) {
                if (!f.mantener(fila, trabajo)) {
                    mantener = false;
                    break; // cortocircuito: si un filtro rechaza, no se evalúan los demás
                }
            }
            if (mantener) {
                conservadas.add(fila);
            }
        }
        trabajo.limpiarFilas();
        for (Fila f : conservadas) {
            trabajo.agregarFila(f);
        }

        // 3. Transformaciones de celda (Strategy secuencial).
        for (TransformadorCelda t : transformadores) {
            for (int r = 0; r < trabajo.cantidadFilas(); r++) {
                Fila fila = trabajo.getFila(r);
                for (int c = 0; c < fila.tamanio(); c++) {
                    String original = fila.getCelda(c);
                    String transformado = t.transformar(original, r, c, trabajo);
                    fila.setCelda(c, transformado == null ? "" : transformado);
                }
            }
        }

        return trabajo;
    }

    /** Crea un constructor fluido para ensamblar una Tuberia. */
    public static ConstructorTuberia crear() {
        return new ConstructorTuberia();
    }

    /**
     * Constructor fluido (patrón Builder).
     *
     * <p><b>SRP:</b> se encarga exclusivamente de recolectar las extensiones y
     * construir una {@link Tuberia} inmutable; no ejecuta nada.</p>
     */
    public static final class ConstructorTuberia {

        private final List<ValidadorCSV> validadores = new ArrayList<>();
        private final List<FiltroFila> filtros = new ArrayList<>();
        private final List<TransformadorCelda> transformadores = new ArrayList<>();

        public ConstructorTuberia agregarValidador(ValidadorCSV validador) {
            if (validador == null) {
                throw new IllegalArgumentException("Validador no puede ser null.");
            }
            validadores.add(validador);
            return this;
        }

        public ConstructorTuberia agregarFiltro(FiltroFila filtro) {
            if (filtro == null) {
                throw new IllegalArgumentException("Filtro no puede ser null.");
            }
            filtros.add(filtro);
            return this;
        }

        public ConstructorTuberia agregarTransformador(TransformadorCelda transformador) {
            if (transformador == null) {
                throw new IllegalArgumentException("Transformador no puede ser null.");
            }
            transformadores.add(transformador);
            return this;
        }

        public Tuberia construir() {
            return new Tuberia(validadores, filtros, transformadores);
        }
    }
}
