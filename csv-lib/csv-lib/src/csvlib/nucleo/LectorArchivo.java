package csvlib.nucleo;

import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación por defecto de {@link LectorCSV}.
 *
 * <p><b>SRP:</b> esta clase solo sabe parsear archivos CSV planos con
 * encabezado en la primera línea y separador configurable (coma por defecto).
 * No valida reglas de negocio, no transforma celdas, no imprime.</p>
 *
 * <p><b>OCP:</b> el separador es configurable en el constructor; si se
 * requiere un parser más complejo se crea otra implementación de
 * {@link LectorCSV} sin modificar esta clase.</p>
 *
 * <p><b>LSP:</b> cualquier sustituto de {@code LectorArchivo} que respete el
 * contrato {@link LectorCSV#leer(String)} puede usarse en su lugar sin
 * romper al cliente.</p>
 */
public class LectorArchivo implements LectorCSV {

    private final String separador;

    /** Crea un lector con separador coma (estándar CSV). */
    public LectorArchivo() {
        this(",");
    }

    /**
     * Crea un lector con el separador indicado (",", ";", "\t", ...).
     *
     * @throws IllegalArgumentException si el separador es {@code null}, vacío
     *         o tiene más de un carácter (la implementación solo divide por
     *         un único carácter; separadores multicaracter no se soportan).
     */
    public LectorArchivo(String separador) {
        if (separador == null || separador.isEmpty()) {
            throw new IllegalArgumentException("El separador no puede ser vacío.");
        }
        if (separador.length() != 1) {
            throw new IllegalArgumentException(
                "El separador debe ser un único carácter; se recibió: \""
                + separador + "\" (longitud " + separador.length() + ").");
        }
        this.separador = separador;
    }

    @Override
    public TablaCSV leer(String ruta) throws IOException {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("La ruta no puede ser vacía.");
        }
        List<String> lineas = Files.readAllLines(Paths.get(ruta), StandardCharsets.UTF_8);
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV está vacío: " + ruta);
        }

        List<String> encabezados = dividirLinea(lineas.get(0));
        if (encabezados.isEmpty()) {
            throw new IllegalArgumentException("El archivo no tiene encabezados válidos: " + ruta);
        }

        TablaCSV tabla = new TablaCSV(encabezados);
        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue; // saltar líneas vacías
            }
            List<String> celdas = dividirLinea(linea);
            // Normalizar: si la fila tiene menos celdas que los encabezados, rellenar.
            while (celdas.size() < encabezados.size()) {
                celdas.add("");
            }
            // Si tiene más, truncar.
            if (celdas.size() > encabezados.size()) {
                celdas = new ArrayList<>(celdas.subList(0, encabezados.size()));
            }
            tabla.agregarFila(new Fila(celdas));
        }
        return tabla;
    }

    /**
     * Divide una línea respetando comillas dobles (RFC 4180 básico):
     * un campo entrecomillado puede contener el separador.
     */
    private List<String> dividirLinea(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                entreComillas = !entreComillas;
            } else if (c == separador.charAt(0) && !entreComillas) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }
}
