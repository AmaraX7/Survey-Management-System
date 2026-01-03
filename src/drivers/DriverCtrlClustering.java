package drivers;

import main.domain.controllers.*;
import main.domain.classes.*;
import java.util.*;

/**
 * Driver interactivo para probar el sistema de clustering
 * Utiliza CtrlDominio como único punto de entrada
 */
public class DriverCtrlClustering {

    private static CtrlDominio ctrlDominio;
    private static Scanner scanner;

    public static void main(String[] args) {
        ctrlDominio = new CtrlDominio();
        scanner = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    TEST CONTROLADOR CLUSTERING           ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        crearDatosPrueba();

        while (true) {
            mostrarMenu();
        }
    }

    // ============================================================
    // MENÚ PRINCIPAL
    // ============================================================

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ ---");
        System.out.println("1. Ejecutar clustering (K-Means)");
        System.out.println("2. Ejecutar clustering (K-Means++)");
        System.out.println("3. Ejecutar clustering (K-Medoids)");
        System.out.println("4. Comparar algoritmos (múltiples K)");
        System.out.println("5. Ver resultados guardados");
        System.out.println("6. Eliminar resultados guardados");
        System.out.println("7. Exportar datos a CSV");
        System.out.println("8. Salir");
        System.out.print("Opción: ");

        try {
            switch (leerEntero()) {
                case 1 -> ejecutarClustering("KMEANS");
                case 2 -> ejecutarClustering("KMEANS++");
                case 3 -> ejecutarClustering("KMEDOIDS");
                case 4 -> compararAlgoritmos();
                case 5 -> verResultadosGuardados();
                case 6 -> eliminarResultadosGuardados();
                case 7 -> exportarDatos();
                case 8 -> {
                    ctrlDominio.cerrarSistema();
                    System.out.println("¡Hasta luego!");
                    System.exit(0);
                }
                default -> System.out.println("❌ Opción inválida");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // CREAR DATOS DE PRUEBA
    // ============================================================

    private static void crearDatosPrueba() {
        System.out.println("\n📦 Creando datos de prueba...");

        try {
            Encuesta enc = ctrlDominio.crearEncuesta(
                    "Encuesta de Preferencias",
                    "Encuesta para análisis de clustering"
            );

            Pregunta p1 = new Numerica("Edad", 18.0, 80.0);
            ctrlDominio.addPregunta(enc.getId(), p1);

            Pregunta p2 = new Numerica("Ingresos", 0.0, 10000.0);
            ctrlDominio.addPregunta(enc.getId(), p2);

            Set<String> estudios = new LinkedHashSet<>();
            estudios.add("Primaria");
            estudios.add("Secundaria");
            estudios.add("Universidad");
            estudios.add("Postgrado");
            Pregunta p3 = new Ordinal("Estudios", estudios);
            ctrlDominio.addPregunta(enc.getId(), p3);

            Set<String> sn = new LinkedHashSet<>();
            sn.add("Sí");
            sn.add("No");
            Pregunta p4 = new CategoriaSimple("Vehículo", sn);
            ctrlDominio.addPregunta(enc.getId(), p4);

            String[][] datosUsuarios = {
                    {"user1", "Juan", "25", "1500", "Universidad", "Sí"},
                    {"user2", "María", "30", "2500", "Postgrado", "Sí"},
                    {"user3", "Pedro", "22", "1200", "Universidad", "No"},
                    {"user4", "Ana", "35", "3000", "Postgrado", "Sí"},
                    {"user5", "Luis", "28", "1800", "Universidad", "Sí"},
                    {"user6", "Carmen", "45", "4000", "Postgrado", "Sí"},
                    {"user7", "Miguel", "20", "800", "Secundaria", "No"},
                    {"user8", "Laura", "38", "3500", "Postgrado", "Sí"},
                    {"user9", "David", "24", "1400", "Universidad", "No"},
                    {"user10", "Sara", "50", "5000", "Postgrado", "Sí"}
            };

            // Refrescar encuesta para obtener preguntas con IDs
            enc = ctrlDominio.obtenerEncuesta(enc.getId());
            List<Pregunta> preguntas = enc.getPreguntas();

            for (String[] datos : datosUsuarios) {
                ctrlDominio.crearUsuarioRespondedor(datos[0], datos[1], "pass");

                Map<String, Object> respuestas = new HashMap<>();
                respuestas.put(preguntas.get(0).getId(), Double.parseDouble(datos[2]));
                respuestas.put(preguntas.get(1).getId(), Double.parseDouble(datos[3]));
                respuestas.put(preguntas.get(2).getId(), datos[4]);
                respuestas.put(preguntas.get(3).getId(), datos[5]);

                ctrlDominio.responderEncuesta(datos[0], enc.getId(), respuestas);
            }

            System.out.println("✓ 10 usuarios creados");
            System.out.println("✓ Encuesta lista para clustering");
            System.out.println("✓ ID de encuesta: " + enc.getId());

        } catch (Exception e) {
            System.out.println("❌ Error creando datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // EJECUTAR UN ALGORITMO
    // ============================================================

    private static void ejecutarClustering(String algoritmo) {
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();
        if (encuestas.isEmpty()) {
            System.out.println("❌ No hay encuestas");
            return;
        }

        System.out.println("\n--- ENCUESTAS DISPONIBLES ---");
        for (int i = 0; i < encuestas.size(); i++) {
            Encuesta e = encuestas.get(i);
            List<UsuarioRespondedor> usuarios =
                    ctrlDominio.obtenerUsuariosQueRespondieron(e.getId());
            System.out.println((i + 1) + ". " + e.getTitulo() +
                    " (" + usuarios.size() + " respuestas)");
        }

        System.out.print("\nNúmero de encuesta (ENTER=primera): ");
        scanner.nextLine();
        String seleccion = scanner.nextLine().trim();

        int idx = 0;
        if (!seleccion.isEmpty()) {
            try {
                idx = Integer.parseInt(seleccion) - 1;
                if (idx < 0 || idx >= encuestas.size()) {
                    System.out.println("❌ Índice inválido");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Número inválido");
                return;
            }
        }

        Encuesta encuesta = encuestas.get(idx);
        List<UsuarioRespondedor> usuarios =
                ctrlDominio.obtenerUsuariosQueRespondieron(encuesta.getId());

        if (usuarios.isEmpty()) {
            System.out.println("❌ No hay usuarios que hayan respondido");
            return;
        }

        System.out.print("K máximo: ");
        int kMax = leerEntero();

        System.out.print("MaxIter (ENTER para 100): ");
        scanner.nextLine();
        String maxIterStr = scanner.nextLine().trim();
        int maxIter = maxIterStr.isEmpty() ? 100 : Integer.parseInt(maxIterStr);

        System.out.println("\n⏳ Ejecutando clustering...");
        long inicio = System.currentTimeMillis();

        try {
            List<ResultadoClustering> resultados = ctrlDominio.ejecutarClustering(
                    encuesta.getId(),
                    algoritmo,
                    kMax,
                    maxIter
            );

            long tiempo = System.currentTimeMillis() - inicio;

            if (resultados.isEmpty()) {
                System.out.println("❌ No se obtuvieron resultados");
                return;
            }

            System.out.println("\n✓ Clustering completado en " + tiempo + " ms");
            System.out.println("\n--- RESULTADOS POR K ---");

            for (ResultadoClustering r : resultados) {
                System.out.printf("K=%d → Silhouette=%.4f, Inercia=%.2f, Iter=%d\n",
                        r.getK(), r.getSilhouette(), r.getInercia(), r.getNumIteraciones());
            }

            ResultadoClustering mejor = encontrarMejor(resultados);
            System.out.println("\n🏆 Mejor resultado: K=" + mejor.getK() +
                    " (Silhouette=" + String.format("%.4f", mejor.getSilhouette()) + ")");

            mostrarResultadoDetallado(mejor);

        } catch (Exception e) {
            System.out.println("❌ Error ejecutando clustering: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // COMPARAR ALGORITMOS
    // ============================================================

    private static void compararAlgoritmos() {
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();
        if (encuestas.isEmpty()) {
            System.out.println("❌ No hay encuestas");
            return;
        }

        Encuesta encuesta = encuestas.get(0);
        List<UsuarioRespondedor> usuarios =
                ctrlDominio.obtenerUsuariosQueRespondieron(encuesta.getId());

        if (usuarios.isEmpty()) {
            System.out.println("❌ No hay respuestas");
            return;
        }

        System.out.print("K máximo: ");
        int kMax = leerEntero();

        String[] algoritmos = {"KMEANS", "KMEANS++", "KMEDOIDS"};

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║   COMPARACIÓN DE ALGORITMOS              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        for (String alg : algoritmos) {
            System.out.println("--- " + alg + " ---");

            try {
                long inicio = System.currentTimeMillis();

                List<ResultadoClustering> resultados = ctrlDominio.ejecutarClustering(
                        encuesta.getId(),
                        alg,
                        kMax,
                        100
                );

                long tiempo = System.currentTimeMillis() - inicio;

                ResultadoClustering mejor = encontrarMejor(resultados);

                System.out.printf("Mejor K=%d, Silhouette=%.4f, Tiempo=%dms\n\n",
                        mejor.getK(), mejor.getSilhouette(), tiempo);

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }
    }

    // ============================================================
    // VER RESULTADOS GUARDADOS
    // ============================================================

    private static void verResultadosGuardados() {
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();
        List<Encuesta> encuestasConResultados = new ArrayList<>();

        System.out.println("\n--- ENCUESTAS CON RESULTADOS ---");

        for (Encuesta enc : encuestas) {
            if (ctrlDominio.existenResultadosGuardados(enc.getId())) {
                encuestasConResultados.add(enc);
                System.out.println("• " + enc.getTitulo() + " (" + enc.getId() + ")");
            }
        }

        if (encuestasConResultados.isEmpty()) {
            System.out.println("\n⚠️  No hay resultados guardados");
            return;
        }

        System.out.print("\nNúmero de encuesta para ver detalles (0 para cancelar): ");
        scanner.nextLine();
        String input = scanner.nextLine().trim();

        if (input.isEmpty() || input.equals("0")) return;

        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx < 0 || idx >= encuestasConResultados.size()) {
                System.out.println("❌ Índice inválido");
                return;
            }

            Encuesta enc = encuestasConResultados.get(idx);
            List<ResultadoClustering> resultados =
                    ctrlDominio.obtenerHistorialClustering(enc.getId());

            if (resultados.isEmpty()) {
                System.out.println("❌ No hay resultados para esta encuesta");
                return;
            }

            System.out.println("\n--- RESULTADOS GUARDADOS ---");
            for (ResultadoClustering r : resultados) {
                System.out.printf("K=%d, Algoritmo=%s, Silhouette=%.4f\n",
                        r.getK(), r.getAlgoritmo(), r.getSilhouette());
            }

            ResultadoClustering mejor = ctrlDominio.obtenerMejorResultadoGuardado(enc.getId());
            if (mejor != null) {
                System.out.println("\n🏆 Mejor resultado guardado:");
                mostrarResultadoDetallado(mejor);
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Número inválido");
        }
    }

    private static void eliminarResultadosGuardados() {
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();
        List<Encuesta> encuestasConResultados = new ArrayList<>();

        System.out.println("\n--- ENCUESTAS CON RESULTADOS ---");

        for (Encuesta enc : encuestas) {
            if (ctrlDominio.existenResultadosGuardados(enc.getId())) {
                encuestasConResultados.add(enc);
                System.out.println("• " + enc.getTitulo() + " (" + enc.getId() + ")");
            }
        }

        if (encuestasConResultados.isEmpty()) {
            System.out.println("\n⚠️  No hay resultados guardados");
            return;
        }

        System.out.print("\nNúmero de encuesta para eliminar resultados: ");
        scanner.nextLine();
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) return;

        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx < 0 || idx >= encuestasConResultados.size()) {
                System.out.println("❌ Índice inválido");
                return;
            }

            System.out.print("¿Confirmar eliminación? (s/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
                System.out.println("⚠️  Cancelado");
                return;
            }

            Encuesta enc = encuestasConResultados.get(idx);
            ctrlDominio.limpiarHistorialClustering(enc.getId());
            System.out.println("✓ Resultados eliminados");

        } catch (NumberFormatException e) {
            System.out.println("❌ Número inválido");
        }
    }

    // ============================================================
    // EXPORTAR DATOS
    // ============================================================

    private static void exportarDatos() {
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();
        if (encuestas.isEmpty()) {
            System.out.println("❌ No hay encuestas");
            return;
        }

        System.out.println("\n--- ENCUESTAS DISPONIBLES ---");
        for (int i = 0; i < encuestas.size(); i++) {
            Encuesta e = encuestas.get(i);
            System.out.println((i + 1) + ". " + e.getTitulo());
        }

        System.out.print("\nNúmero de encuesta a exportar: ");
        scanner.nextLine();
        String input = scanner.nextLine().trim();

        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx < 0 || idx >= encuestas.size()) {
                System.out.println("❌ Índice inválido");
                return;
            }

            Encuesta enc = encuestas.get(idx);

            ControladorExportacion.InfoExportacion info =
                    ctrlDominio.obtenerInfoExportacion(enc.getId());

            if (info == null || info.numeroUsuarios == 0) {
                System.out.println("❌ No hay respuestas para exportar");
                return;
            }

            System.out.println("\n" + info.toString());

            System.out.print("\nNombre del archivo (sin extensión): ");
            String nombreArchivo = scanner.nextLine().trim();

            if (nombreArchivo.isEmpty()) {
                nombreArchivo = "export_" + enc.getId();
            }

            String ruta = "./data/" + nombreArchivo + ".csv";

            ctrlDominio.exportarEncuesta(enc.getId(), ruta);
            System.out.println("✓ Datos exportados a: " + ruta);

        } catch (NumberFormatException e) {
            System.out.println("❌ Número inválido");
        } catch (Exception e) {
            System.out.println("❌ Error exportando: " + e.getMessage());
        }
    }

    // ============================================================
    // VISTAS Y UTILIDADES
    // ============================================================

    private static void mostrarResultadoDetallado(ResultadoClustering r) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       DETALLE DEL RESULTADO              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("Algoritmo: " + r.getAlgoritmo());
        System.out.println("K: " + r.getK());
        System.out.println("Iteraciones: " + r.getNumIteraciones());
        System.out.println("Silhouette: " + String.format("%.4f", r.getSilhouette()));
        System.out.println("Inercia: " + String.format("%.2f", r.getInercia()));

        System.out.println("\n--- Distribución de clusters ---");
        List<List<String>> grupos = r.getUsuariosPorGrupo();

        for (int g = 0; g < grupos.size(); g++) {
            List<String> usuarios = grupos.get(g);
            System.out.println("Cluster " + g + " (" + usuarios.size() + " usuarios):");

            int count = 0;
            for (String usuario : usuarios) {
                System.out.print("  " + usuario);
                count++;
                if (count % 5 == 0) System.out.println();
            }
            if (count % 5 != 0) System.out.println();
        }
    }

    private static ResultadoClustering encontrarMejor(List<ResultadoClustering> resultados) {
        if (resultados == null || resultados.isEmpty()) return null;

        ResultadoClustering mejor = resultados.get(0);
        for (ResultadoClustering r : resultados) {
            if (r.getSilhouette() > mejor.getSilhouette()) {
                mejor = r;
            }
        }
        return mejor;
    }

    private static int leerEntero() {
        while (!scanner.hasNextInt()) {
            System.out.print("⚠️  Número válido: ");
            scanner.next();
        }
        int n = scanner.nextInt();
        scanner.nextLine();
        return n;
    }
}