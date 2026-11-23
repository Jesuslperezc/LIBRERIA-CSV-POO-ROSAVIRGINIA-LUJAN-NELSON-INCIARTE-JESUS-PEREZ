package csvlib.nucleo;

import csvlib.modelo.TablaCSV;
import csvlib.modelo.Fila;

import java.io.PrintStream;
import java.util.List;

/**
 * Implementación por defecto de {@link ImpresorTabla} que imprime la tabla
 * en consola usando un formato de columnas alineadas con caracteres ASCII.
 */
public class ImpresorConsola implements ImpresorTabla {

    private final PrintStream salida;

    /** Imprime hacia {@code System.out}. */
    public ImpresorConsola() {
        this(System.out);
    }

    /** Imprime hacia el {@link PrintStream} indicado. */
    public ImpresorConsola(PrintStream salida) {
        if (salida == null) {
            throw new IllegalArgumentException("PrintStream no puede ser null.");
        }
        this.salida = salida;
    }

    @Override
    public void imprimir(TablaCSV tabla) {
        if (tabla == null) {
            salida.println("(tabla nula)");
            return;
        }
        if (tabla.cantidadFilas() == 0) {
            salida.println("(tabla vacía)");
            return;
        }

        List<String> encabezados = tabla.getEncabezados();
        int[] anchos = calcularAnchosColumnas(tabla);

        imprimirSeparador(anchos);
        imprimirFila(encabezados, anchos);
        imprimirSeparador(anchos);
        for (Fila fila : tabla.getFilas()) {
            imprimirFila(fila.getCeldas(), anchos);
        }
        imprimirSeparador(anchos);
    }

    private int[] calcularAnchosColumnas(TablaCSV tabla) {
        int columnas = tabla.cantidadColumnas();
        int[] anchos = new int[columnas];
        List<String> encabezados = tabla.getEncabezados();
        for (int c = 0; c < columnas; c++) {
            anchos[c] = encabezados.get(c).length();
        }
        for (Fila fila : tabla.getFilas()) {
            List<String> celdas = fila.getCeldas();
            for (int c = 0; c < columnas && c < celdas.size(); c++) {
                if (celdas.get(c).length() > anchos[c]) {
                    anchos[c] = celdas.get(c).length();
                }
            }
        }
        return anchos;
    }

    private void imprimirSeparador(int[] anchos) {
        StringBuilder sb = new StringBuilder("+");
        for (int ancho : anchos) {
            sb.append("-".repeat(ancho + 2)).append("+");
        }
        salida.println(sb);
    }

    private void imprimirFila(List<String> celdas, int[] anchos) {
        StringBuilder sb = new StringBuilder("|");
        for (int c = 0; c < anchos.length; c++) {
            String valor = c < celdas.size() ? celdas.get(c) : "";
            sb.append(" ").append(valor);
            int relleno = anchos[c] - valor.length();
            if (relleno > 0) {
                sb.append(" ".repeat(relleno));
            }
            sb.append(" |");
        }
        salida.println(sb);
    }
}
