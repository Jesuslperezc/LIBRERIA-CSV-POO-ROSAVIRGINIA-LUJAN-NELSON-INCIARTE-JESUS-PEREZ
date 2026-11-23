package csvlib.demo;

import csvlib.builtins.ExportadorJSON;
import csvlib.builtins.ExportadorMarkdown;
import csvlib.builtins.FiltroEdadNumerica;
import csvlib.builtins.TransformadorMayusculas;
import csvlib.builtins.TransformadorRecortarEspacios;
import csvlib.builtins.ValidadorColumnasMinimas;
import csvlib.builtins.ValidadorNoVacio;
import csvlib.modelo.TablaCSV;
import csvlib.nucleo.ImpresorConsola;
import csvlib.nucleo.LectorArchivo;
import csvlib.nucleo.ProcesadorCSV;
import csvlib.nucleo.Tuberia;
import csvlib.usuario.FiltroNombreEmpiezaA;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Demostración de uso de la librería.
 *
 * Cubre las cuatro funciones básicas del enunciado:
 * 
 *   Cargar un CSV en un objeto {@link TablaCSV}.
 *   Agregar/quitar filas y columnas.
 *   Imprimir la tabla en pantalla.
 *   Extender capacidades mediante código propio (validadores, filtros,
 *   transformadores, exportadores).
 * 
 */
public final class Principal {

    public static void main(String[] args) throws IOException {
        String rutaCsv = args.length > 0
            ? args[0]
            : Paths.get("data", "personas.csv").toString();

        System.out.println("===== 1. CARGA DEL CSV =====");
        // Función básica 1: obtener el archivo y crear el objeto.
        // Se cablean implementaciones por defecto (lector, impresor, Tuberia vacía).
        ProcesadorCSV procesadorBasico = new ProcesadorCSV();
        TablaCSV tabla = procesadorBasico.cargar(rutaCsv);
        System.out.println("Tabla cargada: " + tabla);

        System.out.println("\n===== 2. ESTRUCTURA: AGREGAR / QUITAR FILAS Y COLUMNAS =====");
        // Función básica 2: add/remove filas y columnas.
        TablaCSV t = tabla.copiar();
        System.out.println("Filas iniciales: " + t.cantidadFilas() + ", columnas: " + t.cantidadColumnas());

        // Agregar una columna nueva.
        t.agregarColumna("pais", List.of("Venezuela", "Venezuela", "Colombia", "Mexico"));
        System.out.println("Tras agregarColumna('pais'): columnas = " + t.cantidadColumnas());

        // Quitar una fila por índice (la última).
        t.quitarFila(t.cantidadFilas() - 1);
        System.out.println("Tras quitarFila(última): filas = " + t.cantidadFilas());

        // Quitar una columna por nombre.
        t.quitarColumna("email");
        System.out.println("Tras quitarColumna('email'): columnas = " + t.cantidadColumnas());

        // Agregar una fila nueva.
        t.agregarFila(List.of("5", "Ana", "25"));
        System.out.println("Tras agregarFila: filas = " + t.cantidadFilas());

        System.out.println("\n===== 3. IMPRESIÓN EN PANTALLA =====");
        // Función básica 3: imprimir.
        procesadorBasico.imprimir(t);

        System.out.println("\n===== 4. EXTENDER CAPACIDADES (código propio) =====");
        // Función básica 4: Tuberia con extensiones registradas (built-in y propias).
        Tuberia tuberia = Tuberia.crear()
            // validadores built-in
            .agregarValidador(new ValidadorColumnasMinimas(3))
            .agregarValidador(new ValidadorNoVacio("nombre"))
            // filtro built-in: solo filas con 'edad' numérica
            .agregarFiltro(new FiltroEdadNumerica("edad"))
            // FILTRO PROPIO del usuario (clase completa en csvlib.usuario):
            // conserva filas cuyo nombre, tras recortar espacios, empieza por 'A'.
            .agregarFiltro(new FiltroNombreEmpiezaA("nombre"))
            // transformadores built-in
            .agregarTransformador(new TransformadorRecortarEspacios())
            .agregarTransformador(new TransformadorMayusculas(1)) // solo columna 1 (nombre)
            .construir();

        // Procesador con dependencias inyectadas explícitamente:
        // mismo lector por defecto, mismo impresor, Tuberia extendida.
        ProcesadorCSV procesador = new ProcesadorCSV(
            new LectorArchivo(),
            new ImpresorConsola(),
            tuberia);

        System.out.println("Tabla original (antes de la Tuberia):");
        procesador.imprimir(tabla);

        System.out.println("\nTabla tras la Tuberia (validadores + filtros + transformadores):");
        TablaCSV procesada = procesador.procesar(tabla);
        procesador.imprimir(procesada);

        System.out.println("\n===== EXPORTACIÓN (extensión ExportadorTabla) =====");
        System.out.println("--- Markdown ---");
        System.out.println(procesador.exportar(procesada, new ExportadorMarkdown()));

        System.out.println("--- JSON ---");
        System.out.println(procesador.exportar(procesada, new ExportadorJSON()));

        System.out.println("===== FIN DEL DEMO =====");
    }
}
