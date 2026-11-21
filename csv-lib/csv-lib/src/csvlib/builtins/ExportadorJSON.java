package csvlib.builtins;

import csvlib.extensiones.ExportadorTabla;
import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;

import java.util.List;

/**
 * Exportador que devuelve la tabla como un array JSON de objetos
 * (clave = encabezado, valor = celda).
 *
 * <p><b>OCP:</b> nueva extensión sin tocar la librería.</p>
 *
 * <p>No usa ninguna librería externa: serializa manualmente escapando los
 * caracteres especiales de JSON.</p>
 */
public class ExportadorJSON implements ExportadorTabla {

    @Override
    public String exportar(TablaCSV tabla) {
        if (tabla == null) return "[]";
        List<String> encabezados = tabla.getEncabezados();
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        List<Fila> filas = tabla.getFilas();
        for (int r = 0; r < filas.size(); r++) {
            Fila fila = filas.get(r);
            sb.append("  {");
            for (int c = 0; c < encabezados.size(); c++) {
                sb.append("\"").append(escapar(encabezados.get(c))).append("\": ");
                String v = c < fila.tamanio() ? fila.getCelda(c) : "";
                sb.append("\"").append(escapar(v)).append("\"");
                if (c < encabezados.size() - 1) sb.append(", ");
            }
            sb.append("}");
            if (r < filas.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
