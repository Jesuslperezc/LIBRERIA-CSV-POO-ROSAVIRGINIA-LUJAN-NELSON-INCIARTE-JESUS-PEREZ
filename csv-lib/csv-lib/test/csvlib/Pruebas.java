package csvlib;

import csvlib.builtins.ExportadorJSON;
import csvlib.builtins.ExportadorMarkdown;
import csvlib.builtins.FiltroEdadNumerica;
import csvlib.builtins.TransformadorMayusculas;
import csvlib.builtins.TransformadorRecortarEspacios;
import csvlib.builtins.ValidadorColumnasMinimas;
import csvlib.builtins.ValidadorNoVacio;
import csvlib.extensiones.FiltroFila;
import csvlib.extensiones.TransformadorCelda;
import csvlib.extensiones.ValidadorCSV;
import csvlib.modelo.ExcepcionValidacion;
import csvlib.modelo.Fila;
import csvlib.modelo.TablaCSV;
import csvlib.nucleo.ImpresorConsola;
import csvlib.nucleo.LectorArchivo;
import csvlib.nucleo.ProcesadorCSV;
import csvlib.nucleo.Tuberia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**.
 *
 * <p>Detecta por reflexión todos los métodos estáticos sin parámetros llamados
 * {@code probarXxx} y los ejecuta. Si alguno lanza excepción o una aserción
 * falla, se reporta al final con el conteo de pasadas/fallidas.</p>
 */
public final class Pruebas {

    public static void main(String[] args) throws Exception {
        int pasaron = 0;
        int fallaron = 0;
        List<String> fallos = new ArrayList<>();

        for (Method m : Pruebas.class.getDeclaredMethods()) {
            if (!m.getName().startsWith("probar")) continue;
            if (m.getParameterCount() != 0) continue;
            if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
            m.setAccessible(true);
            String etiqueta = m.getName();
            try {
                m.invoke(null);
                System.out.println("[ OK ] " + etiqueta);
                pasaron++;
            } catch (InvocationTargetException ite) {
                Throwable causa = ite.getCause() != null ? ite.getCause() : ite;
                String msg = causa.getClass().getSimpleName() + ": " + causa.getMessage();
                System.out.println("[FAIL] " + etiqueta + " -> " + msg);
                fallos.add(etiqueta + " -> " + msg);
                fallaron++;
            } catch (Throwable t) {
                System.out.println("[FAIL] " + etiqueta + " -> " + t);
                fallos.add(etiqueta + " -> " + t);
                fallaron++;
            }
        }

        System.out.println("\n========================================");
        System.out.println("Resumen: " + pasaron + " OK, " + fallaron + " FAIL");
        if (!fallos.isEmpty()) {
            System.out.println("Fallos:");
            for (String f : fallos) {
                System.out.println("  - " + f);
            }
        }
        System.out.println("========================================");
        if (fallaron > 0) {
            System.exit(1);
        }
    }

    // -------------------- Utilidades de aserción --------------------

    private static void afirmar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError("afirmar falló: " + mensaje);
        }
    }

    private static void afirmarFalso(boolean condicion, String mensaje) {
        if (condicion) {
            throw new AssertionError("afirmarFalso falló: " + mensaje);
        }
    }

    private static void afirmarIgual(Object esperado, Object actual, String mensaje) {
        boolean eq = (esperado == null) ? actual == null : esperado.equals(actual);
        if (!eq) {
            throw new AssertionError("afirmarIgual falló: " + mensaje
                + " (esperado='" + esperado + "', actual='" + actual + "')");
        }
    }

    private static void afirmarNoNulo(Object valor, String mensaje) {
        if (valor == null) {
            throw new AssertionError("afirmarNoNulo falló: " + mensaje);
        }
    }

    private static void afirmarLanza(Class<? extends Throwable> esperada,
                                     Runnable runnable, String mensaje) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (esperada.isInstance(t)) {
                return;
            }
            throw new AssertionError("afirmarLanza falló (tipo erróneo): " + mensaje
                + " (se esperaba " + esperada.getSimpleName()
                + ", se obtuvo " + t.getClass().getSimpleName() + ")");
        }
        throw new AssertionError("afirmarLanza falló (no lanzó): " + mensaje);
    }

    // -------------------- Datos de prueba --------------------

    private static TablaCSV muestra() {
        TablaCSV t = new TablaCSV(List.of("id", "nombre", "edad"));
        t.agregarFila(List.of("1", "Ana", "30"));
        t.agregarFila(List.of("2", "Beto", "25"));
        t.agregarFila(List.of("3", "Carla", "40"));
        return t;
    }

    private static TablaCSV leerDe(String contenido) throws IOException {
        Path tmp = Files.createTempFile("csvlib-test-", ".csv");
        Files.writeString(tmp, contenido, StandardCharsets.UTF_8);
        return new LectorArchivo().leer(tmp.toString());
    }

    private static String imprimirAString(TablaCSV tabla) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ImpresorConsola(new PrintStream(baos, true, StandardCharsets.UTF_8)).imprimir(tabla);
        return baos.toString(StandardCharsets.UTF_8);
    }

    // ===================== Pruebas TablaCSV =====================

    public static void probarAgregarFilaIncrementaCantidad() {
        TablaCSV t = muestra();
        int antes = t.cantidadFilas();
        t.agregarFila(List.of("4", "Dani", "22"));
        afirmarIgual(antes + 1, t.cantidadFilas(), "agregarFila debe incrementar cantidadFilas");
    }

    public static void probarQuitarFilaDecrementaCantidad() {
        TablaCSV t = muestra();
        t.quitarFila(0);
        afirmarIgual(2, t.cantidadFilas(), "quitarFila debe decrementar cantidadFilas");
        afirmarIgual("Beto", t.getFila(0).getCelda(1), "La primera fila debe ser la segunda original");
    }

    public static void probarAgregarColumnaAnhadeEncabezado() {
        TablaCSV t = muestra();
        t.agregarColumna("pais", List.of("VE", "CO", "MX"));
        afirmarIgual(4, t.cantidadColumnas(), "agregarColumna debe incrementar cantidadColumnas");
        afirmarIgual("pais", t.getEncabezados().get(3), "agregarColumna debe poner el nombre al final");
        afirmarIgual("VE", t.getFila(0).getCelda(3), "agregarColumna debe rellenar celdas en orden");
    }

    public static void probarAgregarColumnaRellenaVaciosSiFaltanValores() {
        TablaCSV t = muestra();
        t.agregarColumna("x", List.of("solo-uno"));
        afirmarIgual("", t.getFila(1).getCelda(3), "agregarColumna rellena con '' si faltan valores");
        afirmarIgual("", t.getFila(2).getCelda(3), "agregarColumna rellena con '' si faltan valores");
    }

    public static void probarAgregarColumnaRechazaDuplicado() {
        TablaCSV t = muestra();
        afirmarLanza(IllegalArgumentException.class,
            () -> t.agregarColumna("id", List.of("9")), "agregarColumna rechaza nombre duplicado");
    }

    public static void probarQuitarColumnaDesplazaCeldas() {
        TablaCSV t = muestra();
        t.quitarColumna("nombre");
        afirmarIgual(2, t.cantidadColumnas(), "quitarColumna debe decrementar cantidadColumnas");
        afirmarIgual("1", t.getFila(0).getCelda(0), "quitarColumna debe preservar 'id'");
        afirmarIgual("30", t.getFila(0).getCelda(1), "quitarColumna debe recolocar 'edad'");
    }

    public static void probarIndiceColumnaPorNombre() {
        TablaCSV t = muestra();
        afirmarIgual(1, t.indiceColumna("nombre"), "indiceColumna devuelve la posición correcta");
        afirmarLanza(IllegalArgumentException.class,
            () -> t.indiceColumna("no-existe"), "indiceColumna lanza si la columna no existe");
    }

    public static void probarCopiarEsProfundo() {
        TablaCSV t = muestra();
        TablaCSV c = t.copiar();
        c.setCelda(0, "id", "999");
        afirmarIgual("1", t.getFila(0).getCelda(0), "copiar no debe compartir filas con el original");
    }

    public static void probarFilaInsertarYQuitarCelda() {
        Fila r = new Fila(Arrays.asList("a", "b", "c"));
        r.insertarCelda(1, "X");
        afirmarIgual(Arrays.asList("a", "X", "b", "c"), r.getCeldas(), "insertarCelda desplaza a la derecha");
        r.quitarCelda(0);
        afirmarIgual(Arrays.asList("X", "b", "c"), r.getCeldas(), "quitarCelda desplaza a la izquierda");
    }

    // ===================== Pruebas LectorArchivo =====================

    public static void probarLectorLeeEncabezadosYFilas() throws IOException {
        TablaCSV t = leerDe("id,nombre,edad\n1,Ana,30\n2,Beto,25\n");
        afirmarIgual(3, t.cantidadColumnas(), "Debe leer 3 columnas");
        afirmarIgual(2, t.cantidadFilas(), "Debe leer 2 filas");
        afirmarIgual("Ana", t.getFila(0).getCelda(1), "Primera fila, columna nombre");
    }

    public static void probarLectorSaltaLineasVacias() throws IOException {
        TablaCSV t = leerDe("a,b\n1,2\n\n3,4\n");
        afirmarIgual(2, t.cantidadFilas(), "Las líneas vacías no producen filas");
    }

    public static void probarLectorRellenaFilasCortas() throws IOException {
        TablaCSV t = leerDe("a,b,c\n1,2\n");
        afirmarIgual(3, t.getFila(0).tamanio(), "Las filas cortas se rellenan con ''");
        afirmarIgual("", t.getFila(0).getCelda(2), "La celda faltante debe ser ''");
    }

    public static void probarLectorTruncaFilasLargas() throws IOException {
        TablaCSV t = leerDe("a,b\n1,2,3,4\n");
        afirmarIgual(2, t.getFila(0).tamanio(), "Las filas largas se truncan al número de encabezados");
    }

    public static void probarLectorArchivoVacioLanza() throws IOException {
        Path tmp = Files.createTempFile("csvlib-test-empty-", ".csv");
        Files.writeString(tmp, "", StandardCharsets.UTF_8);
        final String ruta = tmp.toString();
        afirmarLanza(IllegalArgumentException.class,
            () -> {
                try { new LectorArchivo().leer(ruta); }
                catch (IOException e) { throw new RuntimeException(e); }
            },
            "Archivo vacío lanza IllegalArgumentException");
    }

    public static void probarLectorConSeparadorPersonalizado() throws IOException {
        Path tmp = Files.createTempFile("csvlib-test-semicolon-", ".csv");
        Files.writeString(tmp, "a;b;c\n1;2;3\n", StandardCharsets.UTF_8);
        TablaCSV t = new LectorArchivo(";").leer(tmp.toString());
        afirmarIgual(3, t.cantidadColumnas(), "Lector con separador ';' lee 3 columnas");
        afirmarIgual("2", t.getFila(0).getCelda(1), "Lector con ';' parsea correctamente");
    }

    // ===================== Pruebas ImpresorConsola =====================

    public static void probarImpresorImprimeEncabezadosYFilas() {
        TablaCSV t = new TablaCSV(List.of("id", "nombre"));
        t.agregarFila(List.of("1", "Ana"));
        t.agregarFila(List.of("200", "Beto"));
        String out = imprimirAString(t);
        afirmar(out.contains("id"), "La salida contiene el encabezado 'id'");
        afirmar(out.contains("nombre"), "La salida contiene el encabezado 'nombre'");
        afirmar(out.contains("Ana"), "La salida contiene la celda 'Ana'");
        afirmar(out.contains("200"), "La salida contiene la celda '200'");
    }

    public static void probarImpresorTablaVaciaMuestraMarcador() {
        TablaCSV t = new TablaCSV(List.of("a"));
        String out = imprimirAString(t);
        afirmar(out.contains("tabla vacía"), "Tabla sin filas imprime marcador");
    }

    public static void probarImpresorTablaNulaNoLanza() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ImpresorConsola(new PrintStream(baos)).imprimir(null);
        afirmar(baos.toString(StandardCharsets.UTF_8).contains("nula"),
            "Tabla null imprime marcador '(tabla nula)'");
    }

    // ===================== Pruebas Tuberia =====================

    public static void probarTuberiaVaciaDevuelveCopia() {
        TablaCSV original = muestra();
        Tuberia p = Tuberia.crear().construir();
        TablaCSV resultado = p.procesar(original);
        afirmarIgual(original.cantidadFilas(), resultado.cantidadFilas(), "Tuberia vacía conserva filas");
        afirmarIgual(original.cantidadColumnas(), resultado.cantidadColumnas(), "Tuberia vacía conserva columnas");
        afirmar(resultado != original, "Tuberia devuelve una tabla nueva");
    }

    public static void probarValidadorFallaAbortaTuberia() {
        TablaCSV original = muestra();
        Tuberia p = Tuberia.crear()
            .agregarValidador(tabla -> {
                if (tabla.cantidadFilas() == 0) {
                    throw new ExcepcionValidacion("vacía");
                }
            })
            .construir();
        // No lanza (muestra no está vacía).
        p.procesar(original);

        TablaCSV vacia = new TablaCSV(List.of("a"));
        afirmarLanza(ExcepcionValidacion.class,
            () -> p.procesar(vacia), "Validador que falla debe abortar con ExcepcionValidacion");
    }

    public static void probarFiltroEliminaFilasNoManteniendo() {
        TablaCSV original = muestra();
        FiltroFila edadNumerica = (fila, ctx) -> {
            int idx = ctx.indiceColumna("edad");
            try {
                Integer.parseInt(fila.getCelda(idx));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        };
        Tuberia p = Tuberia.crear().agregarFiltro(edadNumerica).construir();
        TablaCSV resultado = p.procesar(original);
        afirmarIgual(3, resultado.cantidadFilas(), "Todas las edades son numéricas: conserva 3 filas");
    }

    public static void probarMultiplesFiltrosSeCombinanConY() {
        TablaCSV original = muestra();
        FiltroFila edadNumerica = (fila, ctx) -> {
            try { Integer.parseInt(fila.getCelda(ctx.indiceColumna("edad"))); return true; }
            catch (Exception e) { return false; }
        };
        FiltroFila nombreEmpiezaPorA = (fila, ctx) ->
            fila.getCelda(ctx.indiceColumna("nombre")).toUpperCase().startsWith("A");

        Tuberia p = Tuberia.crear()
            .agregarFiltro(edadNumerica)
            .agregarFiltro(nombreEmpiezaPorA)
            .construir();
        TablaCSV resultado = p.procesar(original);
        afirmarIgual(1, resultado.cantidadFilas(), "AND de filtros: solo Ana cumple ambos");
        afirmarIgual("Ana", resultado.getFila(0).getCelda(1), "La fila final es Ana");
    }

    public static void probarTransformadorModificaCeldas() {
        TablaCSV original = muestra();
        TransformadorCelda mayus = (v, r, c, ctx) -> v == null ? "" : v.toUpperCase();
        Tuberia p = Tuberia.crear().agregarTransformador(mayus).construir();
        TablaCSV resultado = p.procesar(original);
        afirmarIgual("ANA", resultado.getFila(0).getCelda(1), "Transformador mayúsculas aplica a celdas");
        afirmarIgual("BETO", resultado.getFila(1).getCelda(1), "Transformador mayúsculas aplica a celdas");
    }

    public static void probarTablaOriginalNoSeMuta() {
        TablaCSV original = muestra();
        TransformadorCelda mayus = (v, r, c, ctx) -> v == null ? "" : v.toUpperCase();
        FiltroFila quitarAna = (fila, ctx) -> !fila.getCelda(ctx.indiceColumna("nombre")).equals("Ana");
        Tuberia p = Tuberia.crear()
            .agregarTransformador(mayus)
            .agregarFiltro(quitarAna)
            .construir();
        p.procesar(original);
        // El original sigue teniendo Ana y minúsculas.
        afirmarIgual("Ana", original.getFila(0).getCelda(1), "Tuberia no muta el original");
        afirmarIgual(3, original.cantidadFilas(), "Tuberia no elimina filas del original");
    }

    public static void probarLspSustituyendoBuiltInYAnonimo() {
        TablaCSV original = muestra();
        FiltroFila anonimo = new FiltroFila() {
            @Override public boolean mantener(Fila fila, TablaCSV ctx) {
                return !"Ana".equals(fila.getCelda(ctx.indiceColumna("nombre")));
            }
        };
        FiltroEdadNumerica builtIn = new FiltroEdadNumerica("edad");
        Tuberia p = Tuberia.crear().agregarFiltro(anonimo).agregarFiltro(builtIn).construir();
        TablaCSV resultado = p.procesar(original);
        // Ana (anon la descarta) y todas las edades son numéricas → quedan Beto y Carla
        afirmarIgual(2, resultado.cantidadFilas(), "Combinación anónimo + built-in funciona (LSP)");
        afirmarIgual("Beto", resultado.getFila(0).getCelda(1), "Queda Beto");
    }

    public static void probarDipTuberiaDependeSoloDeInterfaces() {
        ValidadorCSV v = tabla -> {};
        FiltroFila f = (r, c) -> true;
        TransformadorCelda t = (val, r, c, ctx) -> val;
        Tuberia p = Tuberia.crear()
            .agregarValidador(v).agregarFiltro(f).agregarTransformador(t).construir();
        afirmarNoNulo(p, "Tuberia construida con implementaciones lambda funciona");
    }

    // ===================== Pruebas ProcesadorCSV =====================

    public static void probarProcesadorConstructorPorDefectoCableaDefaults() {
        ProcesadorCSV p = new ProcesadorCSV();
        afirmarNoNulo(p.getLector(), "Lector por defecto cableado");
        afirmarNoNulo(p.getImpresor(), "Impresor por defecto cableado");
        afirmarNoNulo(p.getTuberia(), "Tuberia por defecto cableada");
    }

    public static void probarProcesadorCargarDevuelveTabla() throws IOException {
        Path tmp = Files.createTempFile("csvlib-proc-test-", ".csv");
        Files.writeString(tmp, "a,b\n1,2\n", StandardCharsets.UTF_8);
        ProcesadorCSV p = new ProcesadorCSV();
        TablaCSV t = p.cargar(tmp.toString());
        afirmarIgual(2, t.cantidadColumnas(), "cargar devuelve tabla con 2 columnas");
        afirmarIgual(1, t.cantidadFilas(), "cargar devuelve tabla con 1 fila");
    }

    public static void probarProcesadorProcesarAplicaTuberia() {
        TablaCSV t = new TablaCSV(List.of("a", "b"));
        t.agregarFila(List.of("1", "x"));
        t.agregarFila(List.of("2", "y"));

        Tuberia tuberia = Tuberia.crear()
            .agregarTransformador((val, r, c, ctx) -> val == null ? "" : val.toUpperCase())
            .construir();
        ProcesadorCSV proc = new ProcesadorCSV(new LectorArchivo(), new ImpresorConsola(), tuberia);
        TablaCSV out = proc.procesar(t);
        afirmarIgual("X", out.getFila(0).getCelda(1), "procesar aplica el transformador");
    }

    public static void probarProcesadorImprimirEscribeAImpresorInyectado() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ProcesadorCSV p = new ProcesadorCSV(
            new LectorArchivo(),
            new ImpresorConsola(new PrintStream(baos, true, StandardCharsets.UTF_8)),
            Tuberia.crear().construir());
        TablaCSV t = new TablaCSV(List.of("a"));
        t.agregarFila(List.of("1"));
        p.imprimir(t);
        String out = baos.toString(StandardCharsets.UTF_8);
        afirmar(out.contains("a") && out.contains("1"), "imprimir escribe al impresor inyectado");
    }

    public static void probarProcesadorExportarDevuelveString() {
        ProcesadorCSV p = new ProcesadorCSV();
        TablaCSV t = new TablaCSV(List.of("a"));
        t.agregarFila(List.of("1"));
        String json = p.exportar(t, new ExportadorJSON());
        afirmar(json.startsWith("[") && json.contains("\"a\""), "exportar JSON devuelve texto válido");
    }

    // ===================== Pruebas extensiones built-in =====================

    public static void probarValidadorColumnasMinimasPasaCuandoSuficientes() {
        TablaCSV t = new TablaCSV(List.of("a", "b"));
        new ValidadorColumnasMinimas(2).validar(t); // no lanza
        afirmar(true, "ValidadorColumnasMinimas(2) pasa con 2 columnas");
    }

    public static void probarValidadorColumnasMinimasFallaCuandoMenos() {
        TablaCSV t = new TablaCSV(List.of("a", "b"));
        afirmarLanza(ExcepcionValidacion.class,
            () -> new ValidadorColumnasMinimas(3).validar(t),
            "ValidadorColumnasMinimas(3) falla con 2 columnas");
    }

    public static void probarValidadorNoVacioDetectaBlancos() {
        TablaCSV t = new TablaCSV(List.of("id", "nombre"));
        t.agregarFila(List.of("1", ""));
        afirmarLanza(ExcepcionValidacion.class,
            () -> new ValidadorNoVacio("nombre").validar(t),
            "ValidadorNoVacio detecta celda vacía");
    }

    public static void probarValidadorNoVacioFallaSiColumnaNoExiste() {
        TablaCSV t = new TablaCSV(List.of("a"));
        afirmarLanza(ExcepcionValidacion.class,
            () -> new ValidadorNoVacio("x").validar(t),
            "ValidadorNoVacio falla si la columna no existe");
    }

    public static void probarFiltroEdadNumericaMantieneSoloEnteros() {
        TablaCSV t = new TablaCSV(List.of("v"));
        t.agregarFila(List.of("123"));
        t.agregarFila(List.of("abc"));
        t.agregarFila(List.of("0"));
        FiltroEdadNumerica f = new FiltroEdadNumerica("v");
        afirmar(f.mantener(t.getFila(0), t), "Filtra '123' -> true");
        afirmarFalso(f.mantener(t.getFila(1), t), "Filtra 'abc' -> false");
        afirmar(f.mantener(t.getFila(2), t), "Filtra '0' -> true");
    }

    public static void probarFiltroEdadNumericaColumnaInexistenteDevuelveFalse() {
        TablaCSV t = new TablaCSV(List.of("a"));
        t.agregarFila(List.of("1"));
        afirmarFalso(new FiltroEdadNumerica("b").mantener(t.getFila(0), t),
            "Filtro con columna inexistente devuelve false");
    }

    public static void probarTransformadorRecortarEspacios() {
        TablaCSV t = new TablaCSV(List.of("a"));
        t.agregarFila(List.of("x"));
        afirmarIgual("x", new TransformadorRecortarEspacios().transformar("  x  ", 0, 0, t),
            "TransformadorRecortarEspacios elimina espacios");
    }

    public static void probarTransformadorMayusculasTodasColumnas() {
        TablaCSV t = new TablaCSV(List.of("a"));
        t.agregarFila(List.of("x"));
        afirmarIgual("AB", new TransformadorMayusculas().transformar("ab", 0, 0, t),
            "TransformadorMayusculas aplica a todas las columnas");
    }

    public static void probarTransformadorMayusculasSoloColumnaEspecifica() {
        TablaCSV t = new TablaCSV(List.of("a", "b"));
        t.agregarFila(List.of("x", "y"));
        TransformadorMayusculas tr = new TransformadorMayusculas(1);
        afirmarIgual("x", tr.transformar("x", 0, 0, t), "TransformadorMayusculas deja columna 0 intacta");
        afirmarIgual("Y", tr.transformar("y", 0, 1, t), "TransformadorMayusculas aplica a columna 1");
    }

    public static void probarExportadorMarkdownFormato() {
        TablaCSV t = new TablaCSV(List.of("a", "b"));
        t.agregarFila(List.of("1", "2"));
        String out = new ExportadorMarkdown().exportar(t);
        afirmar(out.startsWith("| a | b |"), "Markdown empieza con encabezados");
        afirmar(out.contains("| --- | --- |"), "Markdown incluye separador");
        afirmar(out.contains("| 1 | 2 |"), "Markdown incluye filas");
    }

    public static void probarExportadorJsonEscapa() {
        TablaCSV t = new TablaCSV(List.of("k"));
        t.agregarFila(List.of("con \"comilla\" y \\ barra"));
        String out = new ExportadorJSON().exportar(t);
        afirmar(out.contains("\\\"comilla\\\""), "ExportadorJSON escapa comillas");
        afirmar(out.contains("\\\\ barra"), "ExportadorJSON escapa barras");
    }

    public static void probarExportadorJsonArrayOfObjects() {
        TablaCSV t = new TablaCSV(List.of("id", "nombre"));
        t.agregarFila(List.of("1", "Ana"));
        t.agregarFila(List.of("2", "Beto"));
        String out = new ExportadorJSON().exportar(t);
        afirmar(out.startsWith("[\n"), "ExportadorJSON empieza con [");
        afirmar(out.trim().endsWith("]"), "ExportadorJSON termina con ]");
        afirmar(out.contains("\"id\": \"1\""), "ExportadorJSON serializa primer objeto");
    }
}
