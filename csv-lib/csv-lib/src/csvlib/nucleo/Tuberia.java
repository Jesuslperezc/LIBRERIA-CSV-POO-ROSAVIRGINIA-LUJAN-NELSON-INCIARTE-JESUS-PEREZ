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
 */
public final class Tuberia implements ProcesamientoTuberia {

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
    @Override
    public TablaCSV procesar(TablaCSV entrada) {
        Objects.requireNonNull(entrada, "La tabla de entrada no puede ser null.");

        // 1. Validaciones (no mutan). Cadena con cortocircuito.
        for (ValidadorCSV v : validadores) {
            v.validar(entrada);
        }

        // 2. Filtrado de filas sobre una copia profunda (Prototype).
        TablaCSV trabajo = entrada.copiar();
        List<Fila> conservadas = new ArrayList<>();
        // getFilas() devuelve una lista inmutable de las filas internas (las
        // propias Filas siguen siendo mutables; lo que no se puede es añadir
        // o quitar filas a través de esa lista).
        List<Fila> filasInternas = trabajo.getFilas();
        for (Fila fila : filasInternas) {
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
        // Se itera de nuevo sobre getFilas() para obtener las filas internas
        // mutables y poder transformar sus celdas (getFila(int) devuelve una
        // vista de solo lectura).
        for (TransformadorCelda t : transformadores) {
            List<Fila> filas = trabajo.getFilas();
            for (int r = 0; r < filas.size(); r++) {
                Fila fila = filas.get(r);
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
