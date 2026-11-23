package csvlib.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Representa un archivo CSV cargado en memoria: una lista de encabezados
 * y una lista de filas, todo como String (comportamiento por defecto del
 * enunciado).
 *
 * Responsabilidad (SRP): solo modelar los datos y las operaciones básicas
 * de estructura que pide el enunciado:
 *   - quitar o agregar filas
 *   - quitar o agregar columnas
 *   - leer y escribir celdas
 * No lee archivos, no imprime, no valida, no transforma: esas
 * responsabilidades están en el paquete {@code nucleo} y en el paquete
 * {@code extensiones}.
 */
public final class TablaCSV {

    private final List<String> encabezados;
    private final List<Fila> filas;

    public TablaCSV(List<String> encabezados) {
        Objects.requireNonNull(encabezados, "Los encabezados no pueden ser null.");
        this.encabezados = new ArrayList<>(encabezados);
        this.filas = new ArrayList<>();
    }

    public TablaCSV(List<String> encabezados, List<Fila> filas) {
        Objects.requireNonNull(encabezados, "Los encabezados no pueden ser null.");
        Objects.requireNonNull(filas, "Las filas no pueden ser null.");
        this.encabezados = new ArrayList<>(encabezados);
        this.filas = new ArrayList<>(filas);
    }

    // ----------------------- Columnas -----------------------

    public List<String> getEncabezados() {
        return Collections.unmodifiableList(encabezados);
    }

    public int cantidadColumnas() {
        return encabezados.size();
    }

    /** Devuelve el índice de una columna por nombre. */
    public int indiceColumna(String nombre) {
        int idx = encabezados.indexOf(nombre);
        if (idx < 0) {
            throw new IllegalArgumentException("Columna no encontrada: " + nombre);
        }
        return idx;
    }

    /**
     * Agrega una columna al final de la tabla.
     * Si faltan valores para alguna fila, se rellena con "".
     */
    public TablaCSV agregarColumna(String nombre, List<String> valores) {
        Objects.requireNonNull(nombre, "El nombre de columna no puede ser null.");
        if (encabezados.contains(nombre)) {
            throw new IllegalArgumentException("Columna ya existe: " + nombre);
        }
        encabezados.add(nombre);
        List<String> seguros = valores == null ? List.of() : valores;
        for (int i = 0; i < filas.size(); i++) {
            String valor = i < seguros.size() ? seguros.get(i) : "";
            filas.get(i).agregarCelda(valor);
        }
        return this;
    }

    /** Inserta una columna en la posición indicada. */
    public TablaCSV agregarColumnaEn(String nombre, int indice, List<String> valores) {
        Objects.requireNonNull(nombre, "El nombre de columna no puede ser null.");
        if (encabezados.contains(nombre)) {
            throw new IllegalArgumentException("Columna ya existe: " + nombre);
        }
        if (indice < 0 || indice > encabezados.size()) {
            throw new IndexOutOfBoundsException("Índice de columna inválido: " + indice);
        }
        encabezados.add(indice, nombre);
        List<String> seguros = valores == null ? List.of() : valores;
        for (int i = 0; i < filas.size(); i++) {
            String valor = i < seguros.size() ? seguros.get(i) : "";
            filas.get(i).insertarCelda(indice, valor);
        }
        return this;
    }

    /** Elimina la columna con el nombre indicado. */
    public TablaCSV quitarColumna(String nombre) {
        return quitarColumnaEn(indiceColumna(nombre));
    }

    /** Elimina la columna en la posición indicada. */
    public TablaCSV quitarColumnaEn(int indice) {
        if (indice < 0 || indice >= encabezados.size()) {
            throw new IndexOutOfBoundsException("Índice de columna inválido: " + indice);
        }
        encabezados.remove(indice);
        for (Fila fila : filas) {
            fila.quitarCelda(indice);
        }
        return this;
    }

    // ------------------------- Filas ------------------------

    public List<Fila> getFilas() {
        return Collections.unmodifiableList(filas);
    }

    public int cantidadFilas() {
        return filas.size();
    }

    /**
     * Devuelve una <b>vista de solo lectura</b> de la fila en la posición
     * indicada. La vista comparte los datos con la fila interna, pero
     * cualquier intento de mutarla ({@code setCelda}, {@code insertarCelda},
     * {@code agregarCelda}, {@code quitarCelda}) lanza
     * {@link UnsupportedOperationException}. Esto evita que el cliente modifique
     * celdas sin pasar por la tabla.
     *
     * <p>Para obtener una fila modificable de forma segura, usar
     * {@link #copiar()} y luego operar sobre la copia.</p>
     */
    public Fila getFila(int indice) {
        if (indice < 0 || indice >= filas.size()) {
            throw new IndexOutOfBoundsException("Índice de fila inválido: " + indice);
        }
        return Fila.vistaSoloLectura(filas.get(indice));
    }

    /** Agrega una fila al final de la tabla. */
    public TablaCSV agregarFila(Fila fila) {
        Objects.requireNonNull(fila, "La fila no puede ser null.");
        filas.add(fila);
        return this;
    }

    /** Agrega una fila construida a partir de una lista de valores. */
    public TablaCSV agregarFila(List<String> valores) {
        Objects.requireNonNull(valores, "Los valores no pueden ser null.");
        filas.add(new Fila(valores));
        return this;
    }

    /** Inserta una fila en la posición indicada. */
    public TablaCSV agregarFilaEn(Fila fila, int indice) {
        Objects.requireNonNull(fila, "La fila no puede ser null.");
        filas.add(indice, fila);
        return this;
    }

    /** Elimina la fila en la posición indicada. */
    public TablaCSV quitarFila(int indice) {
        if (indice < 0 || indice >= filas.size()) {
            throw new IndexOutOfBoundsException("Índice de fila inválido: " + indice);
        }
        filas.remove(indice);
        return this;
    }

    /** Elimina todas las filas que cumplan el predicado. */
    public TablaCSV quitarFilas(Predicate<Fila> predicado) {
        Objects.requireNonNull(predicado, "El predicado no puede ser null.");
        filas.removeIf(predicado);
        return this;
    }

    /** Elimina todas las filas. Conserva los encabezados. */
    public TablaCSV limpiarFilas() {
        filas.clear();
        return this;
    }

    // ------------------------ Celdas ------------------------

    /**
     * Acceso interno a la fila mutable subyacente (sin envolver en vista de
     * solo lectura). Usado por las operaciones de la propia tabla que sí
     * necesitan mutar la fila.
     */
    private Fila filaInterna(int indiceFila) {
        if (indiceFila < 0 || indiceFila >= filas.size()) {
            throw new IndexOutOfBoundsException("Índice de fila inválido: " + indiceFila);
        }
        return filas.get(indiceFila);
    }

    public String getCelda(int indiceFila, String nombreColumna) {
        return filaInterna(indiceFila).getCelda(indiceColumna(nombreColumna));
    }

    public TablaCSV setCelda(int indiceFila, String nombreColumna, String valor) {
        filaInterna(indiceFila).setCelda(indiceColumna(nombreColumna), valor);
        return this;
    }

    /**
     * Devuelve un mapa "nombreColumna -> valor" para una fila dada.
     * Útil para que las extensiones operen por nombre sin manejar índices.
     */
    public Map<String, String> filaComoMapa(int indiceFila) {
        Fila fila = filaInterna(indiceFila);
        Map<String, String> mapa = new LinkedHashMap<>();
        for (int i = 0; i < encabezados.size() && i < fila.tamanio(); i++) {
            mapa.put(encabezados.get(i), fila.getCelda(i));
        }
        return mapa;
    }

    /**
     * Devuelve una copia profunda de la tabla (patrón Prototype).
     * Permite que la Tuberia trabaje sobre una copia sin mutar la original.
     */
    public TablaCSV copiar() {
        List<Fila> copiadas = new ArrayList<>();
        for (Fila f : filas) {
            copiadas.add(f.copiar());
        }
        return new TablaCSV(new ArrayList<>(encabezados), copiadas);
    }

    @Override
    public String toString() {
        return "TablaCSV{encabezados=" + encabezados + ", filas=" + filas.size() + "}";
    }
}
