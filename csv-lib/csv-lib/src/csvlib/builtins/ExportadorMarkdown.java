package csvlib.builtins;

import csvlib.extensiones.ExportadorTabla;
import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;

import java.util.List;

/**
 * Exportador que devuelve la tabla como una tabla Markdown.
 *
 * <p><b>OCP:</b> nueva extensión sin tocar la librería.</p>
 *
 * <p><b>ISP:</b> implementa solo {@link ExportadorTabla}; no depende de
 * filtros, transformadores ni validadores.</p>
 */
public class ExportadorMarkdown implements ExportadorTabla {

    @Override
    public String exportar(TablaCSV tabla) {
        if (tabla == null) return "";
        StringBuilder sb = new StringBuilder();
        List<String> encabezados = tabla.getEncabezados();

        // Encabezado
        sb.append("| ");
        for (int i = 0; i < encabezados.size(); i++) {
            sb.append(escapar(encabezados.get(i)));
            sb.append(i < encabezados.size() - 1 ? " | " : " |\n");
        }
        // Separador
        sb.append("|");
        for (int i = 0; i < encabezados.size(); i++) {
            sb.append(" --- |");
        }
        sb.append("\n");
        // Filas
        for (Fila fila : tabla.getFilas()) {
            List<String> celdas = fila.getCeldas();
            sb.append("| ");
            for (int i = 0; i < encabezados.size(); i++) {
                String v = i < celdas.size() ? celdas.get(i) : "";
                sb.append(escapar(v));
                sb.append(i < encabezados.size() - 1 ? " | " : " |\n");
            }
        }
        return sb.toString();
    }

    /**
     * Escapa el carácter pipe para no romper la estructura de la tabla
     * Markdown. También reemplaza los saltos de línea por &lt;br&gt; para
     * mantener cada fila en una sola línea.
     */
    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("|", "\\|")
                .replace("\n", "<br>")
                .replace("\r", "");
    }
}
