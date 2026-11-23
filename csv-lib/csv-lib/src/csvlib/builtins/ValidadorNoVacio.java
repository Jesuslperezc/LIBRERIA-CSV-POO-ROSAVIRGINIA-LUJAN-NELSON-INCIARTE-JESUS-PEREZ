package csvlib.builtins;

import csvlib.extensiones.ValidadorCSV;
import csvlib.modelo.ExcepcionValidacion;
import csvlib.modelo.TablaCSV;

/**
 * Validador que comprueba que ninguna celda esté vacía en las columnas
 * indicadas.
 *
 */
public class ValidadorNoVacio implements ValidadorCSV {

    private final String[] columnasRequeridas;

    public ValidadorNoVacio(String... columnasRequeridas) {
        if (columnasRequeridas == null || columnasRequeridas.length == 0) {
            throw new IllegalArgumentException("Se requiere al menos una columna.");
        }
        this.columnasRequeridas = columnasRequeridas;
    }

    @Override
    public void validar(TablaCSV tabla) throws ExcepcionValidacion {
        for (String columna : columnasRequeridas) {
            int idx;
            try {
                idx = tabla.indiceColumna(columna);
            } catch (IllegalArgumentException e) {
                throw new ExcepcionValidacion("Columna requerida no existe: " + columna);
            }
            for (int r = 0; r < tabla.cantidadFilas(); r++) {
                String valor = tabla.getFila(r).getCelda(idx);
                if (valor == null || valor.isBlank()) {
                    throw new ExcepcionValidacion(
                        "Celda vacía en columna '" + columna + "', fila " + r + ".");
                }
            }
        }
    }
}
