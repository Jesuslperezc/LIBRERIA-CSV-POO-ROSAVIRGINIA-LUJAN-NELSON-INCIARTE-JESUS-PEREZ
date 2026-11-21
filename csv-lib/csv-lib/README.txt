csv-lib — Librería extensible para procesamiento de CSV (Java puro, sin frameworks)
===================================================================================

Requisitos: JDK 17+ (no se usan frameworks ni librerías externas; ni siquiera
JUnit: el corredor de pruebas es propio y usa aserciones manuales).

Compilar y ejecutar
-------------------
    ./compile.sh              # compila src + test a ./out
    ./run-tests.sh            # ejecuta csvlib.Pruebas (36 pruebas)
    ./run-demo.sh             # ejecuta csvlib.demo.Principal con data/personas.csv


Estructura del proyecto
-----------------------
5 carpetas por responsabilidad + 1 de pruebas:

    csv-lib/
    ├── data/
    │   └── personas.csv                  CSV de ejemplo para la demo
    │
    ├── src/csvlib/
    │   ├── modelo/                       DATOS (SRP)
    │   │   ├── Fila.java                 una fila de celdas String
    │   │   ├── TablaCSV.java             la tabla: add/remove filas y columnas
    │   │   └── ExcepcionValidacion.java  excepción de dominio
    │   │
    │   ├── extensiones/                  CONTRATOS PARA EXTENDER (ISP + OCP)
    │   │   ├── FiltroFila.java           interface: mantener(fila, contexto)?
    │   │   ├── TransformadorCelda.java   interface: transformar(valor, ...)
    │   │   ├── ValidadorCSV.java         interface: validar(tabla)
    │   │   └── ExportadorTabla.java      interface: exportar(tabla) -> String
    │   │
    │   ├── nucleo/                       MOTOR (Facade, Builder, Pipeline)
    │   │   ├── LectorCSV.java            interface: leer(ruta) -> TablaCSV
    │   │   ├── LectorArchivo.java        impl por defecto (parsea CSV)
    │   │   ├── ImpresorTabla.java        interface: imprimir(tabla)
    │   │   ├── ImpresorConsola.java      impl por defecto (consola ASCII)
    │   │   ├── Tuberia.java              orquesta extensiones + ConstructorTuberia
    │   │   └── ProcesadorCSV.java        fachada: cargar, procesar, imprimir, exportar
    │   │
    │   ├── builtins/                     EXTENSIONES DE EJEMPLO (OCP)
    │   │   ├── ValidadorColumnasMinimas.java
    │   │   ├── ValidadorNoVacio.java
    │   │   ├── FiltroEdadNumerica.java
    │   │   ├── TransformadorRecortarEspacios.java
    │   │   ├── TransformadorMayusculas.java
    │   │   ├── ExportadorMarkdown.java
    │   │   └── ExportadorJSON.java
    │   │
    │   └── demo/
    │       └── Principal.java            demo que usa las 4 funciones + extensión propia
    │
    └── test/csvlib/
        └── Pruebas.java                  corredor con aserciones manuales


Las cuatro funciones básicas del enunciado
------------------------------------------
1. Obtener el archivo CSV y crear un objeto:
       ProcesadorCSV proc = new ProcesadorCSV();
       TablaCSV tabla = proc.cargar("data/personas.csv");
   (por defecto todos los datos son String)

2. Quitar o agregar filas o columnas (operaciones sobre el objeto):
       tabla.agregarFila(...);
       tabla.quitarFila(0);
       tabla.agregarColumna("pais", Arrays.asList("VE","CO","MX"));
       tabla.quitarColumna("email");

3. Imprimir en pantalla la tabla:
       proc.imprimir(tabla);

4. Extender capacidades o modificar el resultado final con código propio:
       Tuberia tuberia = Tuberia.crear()
           .agregarValidador(new ValidadorColumnasMinimas(3))
           .agregarFiltro(new FiltroEdadNumerica("edad"))
           .agregarFiltro((fila, ctx) -> {                 // <-- tu propio filtro
               String nombre = fila.getCelda(ctx.indiceColumna("nombre"));
               return nombre.startsWith("A");
           })
           .agregarTransformador(new TransformadorMayusculas())
           .construir();
       ProcesadorCSV proc = new ProcesadorCSV(
           new LectorArchivo(), new ImpresorConsola(), tuberia);
       TablaCSV procesada = proc.procesar(tabla);   // aplica tu código sin tocar la librería
       String json = proc.exportar(procesada, new ExportadorJSON());


Principios SOLID aplicados
--------------------------
S (Responsabilidad única):
   Cada clase hace una sola cosa. Fila y TablaCSV solo guardan datos;
   LectorArchivo solo lee; ImpresorConsola solo imprime; Tuberia solo coordina;
   cada extensión solo valida, filtra, transforma o exporta.

O (Abierto/Cerrado):
   Para añadir una capacidad nueva (un filtro nuevo, un transformador nuevo,
   un exportador a otro formato) creas una clase nueva que implemente la
   interfaz correspondiente y la registras en Tuberia.crear(). NO modificas
   ninguna clase existente de la librería.

L (Sustitución de Liskov):
   Cualquier implementación de una interfaz puede sustituir a otra. La
   propia Tuberia mezcla filtros built-in con filtros anónimos del usuario
   sin distinguirlos. Verificado en Pruebas.probarLspSustituyendoBuiltInYAnonimo.

I (Segregación de interfaces):
   Cuatro interfaces chiquitas, una por política: FiltroFila,
   TransformadorCelda, ValidadorCSV, ExportadorTabla. Además LectorCSV e
   ImpresorTabla están separados: quien solo lee no depende de imprimir.

D (Inversión de dependencias):
   ProcesadorCSV y Tuberia dependen solo de interfaces, no de clases
   concretas. Las implementaciones se inyectan por constructor.


Patrones de diseño aplicados
----------------------------
Strategy         Las interfaces de 'extensiones' y 'nucleo' (LectorCSV,
                 ImpresorTabla, FiltroFila, TransformadorCelda, ValidadorCSV,
                 ExportadorTabla) son estrategias intercambiables.

Builder          Tuberia.crear() devuelve un ConstructorTuberia con API fluida
                 que construye una Tuberia inmutable paso a paso.

Facade           ProcesadorCSV es una fachada que oculta la coordinación entre
                 lector + impresor + Tuberia tras 4 métodos simples:
                 cargar, procesar, imprimir, exportar.

Registry         La Tuberia mantiene tres listas inmutables (validadores,
                 filtros, transformadores) que actúan como registros. El
                 ConstructorTuberia es el mecanismo de alta.

Template Method  Tuberia.procesar() define el esqueleto fijo del algoritmo
(variante por    en 3 etapas: validar -> filtrar -> transformar. Cada etapa
composición)     se delega a estrategias inyectadas (no a subclases).

Chain of Resp.   Validadores y filtros se procesan en cadena. Si un validador
                 lanza, se corta la cadena. Si un filtro rechaza, no se
                 evalúan los siguientes sobre esa fila.

Prototype        TablaCSV.copiar() y Fila.copiar() devuelven copias profundas.
                 La Tuberia las usa para no mutar la tabla original del cliente.

Dependency       Los constructores de ProcesadorCSV e ImpresorConsola reciben
Injection        sus colaboradores por parámetro.


Cómo extender la librería con tu propio código
----------------------------------------------
1. Implementar la interfaz adecuada (en cualquier paquete tuyo):
     - csvlib.extensiones.FiltroFila          para filtrar filas
     - csvlib.extensiones.TransformadorCelda  para transformar celdas
     - csvlib.extensiones.ValidadorCSV        para validar la tabla
     - csvlib.extensiones.ExportadorTabla     para exportar a otro formato

2. Registrar la instancia al construir la Tuberia:
     Tuberia tuberia = Tuberia.crear()
         .agregarFiltro(new MiFiltro())
         .agregarTransformador(new MiTransformador())
         .construir();

3. Cablear la Tuberia en un ProcesadorCSV:
     ProcesadorCSV proc = new ProcesadorCSV(
         new LectorArchivo(), new ImpresorConsola(), tuberia);

4. Usar:
     TablaCSV t = proc.cargar("archivo.csv");
     TablaCSV procesada = proc.procesar(t);
     proc.imprimir(procesada);
     String md = proc.exportar(procesada, new ExportadorMarkdown());

Ver Principal.java para un ejemplo completo, incluido un filtro definido con
una clase anónima directamente en el código de usuario (sin crear archivo
nuevo, sin tocar la librería).
