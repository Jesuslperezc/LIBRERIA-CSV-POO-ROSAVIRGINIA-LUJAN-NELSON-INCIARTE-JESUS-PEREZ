package csvlib.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Una fila de la tabla CSV.
 *
 * Responsabilidad (SRP): guardar y exponer las celdas de una fila como
 * valores String. No sabe nada de columnas con nombre ni de la tabla
 * completa; eso le corresponde a {@link TablaCSV}.
 */
public final class Fila {

    private final List<String> celdas;
    private final boolean soloLectura;

    public Fila(List<String> celdas) {
        Objects.requireNonNull(celdas, "Las celdas no pueden ser null.");
        this.celdas = new ArrayList<>(celdas);
        this.soloLectura = false;
    }

    private Fila(List<String> celdas, boolean soloLectura) {
        this.celdas = soloLectura
            ? Collections.unmodifiableList(celdas)
            : new ArrayList<>(celdas);
        this.soloLectura = soloLectura;
    }

    /**
     * Devuelve una vista de solo lectura de la fila indicada: comparte los
     * datos con {@code original}, pero cualquier intento de mutación lanza
     * {@link UnsupportedOperationException}.
     */
    static Fila vistaSoloLectura(Fila original) {
        Objects.requireNonNull(original, "La fila original no puede ser null.");
        return new Fila(original.celdas, true);
    }

    /** Indica si esta fila es una vista de solo lectura. */
    public boolean esSoloLectura() {
        return soloLectura;
    }

    /** Devuelve una vista inmutable de las celdas. */
    public List<String> getCeldas() {
        return Collections.unmodifiableList(celdas);
    }

    public String getCelda(int indice) {
        return celdas.get(indice);
    }

    public void setCelda(int indice, String valor) {
        celdas.set(indice, valor);
    }

    /** Inserta una celda en la posición indicada (la fila crece). */
    public void insertarCelda(int indice, String valor) {
        if (indice < 0 || indice > celdas.size()) {
            throw new IndexOutOfBoundsException("Índice de celda inválido: " + indice);
        }
        celdas.add(indice, valor);
    }

    /** Agrega una celda al final de la fila. */
    public void agregarCelda(String valor) {
        celdas.add(valor);
    }

    /** Elimina la celda en la posición indicada (la fila se reduce). */
    public void quitarCelda(int indice) {
        if (indice < 0 || indice >= celdas.size()) {
            throw new IndexOutOfBoundsException("Índice de celda inválido: " + indice);
        }
        celdas.remove(indice);
    }

    public int tamanio() {
        return celdas.size();
    }

    /** Devuelve una copia independiente de la fila (patrón Prototype). */
    public Fila copiar() {
        return new Fila(new ArrayList<>(celdas));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fila otra)) return false;
        return celdas.equals(otra.celdas);
    }

    @Override
    public int hashCode() {
        return celdas.hashCode();
    }

    @Override
    public String toString() {
        return celdas.toString();
    }
}
