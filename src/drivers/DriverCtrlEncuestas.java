package drivers;

import main.domain.controllers.*;
import main.domain.classes.*;
import java.util.*;

/**
 * Driver interactivo para probar el sistema de encuestas
 * Utiliza CtrlDominio como único punto de entrada
 */
public class DriverCtrlEncuestas {

    private static CtrlDominio ctrlDominio;
    private static Scanner scanner;

    public static void main(String[] args) {
        ctrlDominio = new CtrlDominio();
        scanner = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    TEST GESTOR DE ENCUESTAS              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        while (true) {
            mostrarMenu();
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Crear encuesta");
        System.out.println("2. Listar encuestas");
        System.out.println("3. Ver detalle de encuesta");
        System.out.println("4. Modificar encuesta");
        System.out.println("5. Eliminar encuesta");
        System.out.println("6. Gestionar preguntas");
        System.out.println("7. Verificar existencia de encuesta");
        System.out.println("8. Mostrar estadísticas");
        System.out.println("9. Salir");
        System.out.print("Opción: ");

        try {
            switch (leerEntero()) {
                case 1 -> crearEncuesta();
                case 2 -> listarEncuestas();
                case 3 -> verDetalleEncuesta();
                case 4 -> modificarEncuesta();
                case 5 -> eliminarEncuesta();
                case 6 -> menuGestionPreguntas();
                case 7 -> verificarExistencia();
                case 8 -> mostrarEstadisticas();
                case 9 -> {
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

    // ==================== OPERACIONES CRUD ENCUESTAS ====================

    private static void crearEncuesta() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         CREAR ENCUESTA                   ║");
        System.out.println("╚══════════════════════════════════════════╝");

        System.out.print("Título: ");
        String titulo = scanner.nextLine().trim();

        System.out.print("Descripción: ");
        String descripcion = scanner.nextLine().trim();

        try {
            Encuesta encuesta = ctrlDominio.crearEncuesta(titulo, descripcion);
            System.out.println("\n✓ Encuesta creada exitosamente");
            System.out.println("  ID: " + encuesta.getId());
            System.out.println("  Título: " + encuesta.getTitulo());

            System.out.print("\n¿Deseas añadir preguntas ahora? (s/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
                añadirPreguntas(encuesta.getId());
            }
        } catch (Exception e) {
            System.out.println("❌ Error al crear encuesta: " + e.getMessage());
        }
    }

    private static void listarEncuestas() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       LISTA DE ENCUESTAS                 ║");
        System.out.println("╚══════════════════════════════════════════╝");

        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();

        if (encuestas.isEmpty()) {
            System.out.println("\n⚠️  No hay encuestas creadas");
            return;
        }

        System.out.println("\nTotal: " + encuestas.size() + " encuesta(s)\n");

        for (Encuesta e : encuestas) {
            System.out.println("─".repeat(50));
            System.out.println("📋 ID: " + e.getId());
            System.out.println("   Título: " + e.getTitulo());
            System.out.println("   Descripción: " + e.getDescripcion());
            System.out.println("   Preguntas: " + e.getNumPreguntas());
        }
        System.out.println("─".repeat(50));
    }

    private static void verDetalleEncuesta() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        Encuesta enc = ctrlDominio.obtenerEncuesta(id);
        if (enc == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       DETALLE DE ENCUESTA                ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("📋 ID: " + enc.getId());
        System.out.println("📋 Título: " + enc.getTitulo());
        System.out.println("📝 Descripción: " + enc.getDescripcion());
        System.out.println("📊 Total de preguntas: " + enc.getNumPreguntas());

        if (enc.getNumPreguntas() > 0) {
            System.out.println("\n--- PREGUNTAS ---");
            List<Pregunta> preguntas = enc.getPreguntas();
            for (int i = 0; i < preguntas.size(); i++) {
                Pregunta p = preguntas.get(i);
                System.out.println("\n" + (i + 1) + ". " + p.getEnunciado());
                System.out.println("   Tipo: " + obtenerNombreTipo(p));
                System.out.println("   ID: " + p.getId());
                System.out.println("   Obligatoria: " + (p.esObligatoria() ? "Sí" : "No"));
                mostrarDetallesPregunta(p);
            }
        }
    }

    private static void modificarEncuesta() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        Encuesta enc = ctrlDominio.obtenerEncuesta(id);
        if (enc == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       MODIFICAR ENCUESTA                 ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("Encuesta actual: " + enc.getTitulo());
        System.out.println("(Presiona ENTER para mantener el valor actual)\n");

        System.out.print("Nuevo título: ");
        String titulo = scanner.nextLine().trim();

        System.out.print("Nueva descripción: ");
        String descripcion = scanner.nextLine().trim();

        try {
            boolean ok = ctrlDominio.modificarEncuesta(
                    id,
                    titulo.isEmpty() ? null : titulo,
                    descripcion.isEmpty() ? null : descripcion
            );

            if (ok) {
                System.out.println("✓ Encuesta modificada exitosamente");
            } else {
                System.out.println("❌ No se pudo modificar la encuesta");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void eliminarEncuesta() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        Encuesta enc = ctrlDominio.obtenerEncuesta(id);
        if (enc == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        System.out.println("\n⚠️  ADVERTENCIA: Vas a eliminar la encuesta:");
        System.out.println("  Título: " + enc.getTitulo());
        System.out.println("  Preguntas: " + enc.getNumPreguntas());
        System.out.print("\n¿Confirmar eliminación? (escribe 'CONFIRMAR'): ");

        if (!scanner.nextLine().trim().equals("CONFIRMAR")) {
            System.out.println("⚠️  Eliminación cancelada");
            return;
        }

        try {
            boolean ok = ctrlDominio.eliminarEncuesta(id);
            if (ok) {
                System.out.println("✓ Encuesta eliminada exitosamente");
            } else {
                System.out.println("❌ No se pudo eliminar la encuesta");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verificarExistencia() {
        System.out.print("\nID de encuesta a verificar: ");
        String id = scanner.nextLine().trim();

        if (id.isEmpty()) {
            System.out.println("⚠️  ID vacío");
            return;
        }

        boolean existe = ctrlDominio.existeEncuesta(id);
        if (existe) {
            System.out.println("✓ La encuesta existe");
            Encuesta enc = ctrlDominio.obtenerEncuesta(id);
            System.out.println("  Título: " + enc.getTitulo());
        } else {
            System.out.println("❌ La encuesta NO existe");
        }
    }

    private static void mostrarEstadisticas() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         ESTADÍSTICAS                     ║");
        System.out.println("╚══════════════════════════════════════════╝");

        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();
        int total = encuestas.size();
        System.out.println("📊 Total de encuestas: " + total);

        if (total > 0) {
            int totalPreguntas = 0;
            int totalRespuestas = 0;

            for (Encuesta e : encuestas) {
                totalPreguntas += e.getNumPreguntas();
                List<UsuarioRespondedor> usuarios =
                        ctrlDominio.obtenerUsuariosQueRespondieron(e.getId());
                totalRespuestas += usuarios.size();
            }

            System.out.println("📊 Total de preguntas: " + totalPreguntas);
            System.out.println("📊 Total de respuestas: " + totalRespuestas);
            System.out.println("📊 Promedio de preguntas por encuesta: " +
                    String.format("%.2f", (double) totalPreguntas / total));
        }
    }

    // ==================== GESTIÓN DE PREGUNTAS ====================

    private static void menuGestionPreguntas() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        Encuesta enc = ctrlDominio.obtenerEncuesta(id);
        if (enc == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        while (true) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║   GESTIÓN DE PREGUNTAS                   ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("Encuesta: " + enc.getTitulo());
            System.out.println("Preguntas actuales: " + enc.getNumPreguntas());
            System.out.println("\n1. Ver preguntas");
            System.out.println("2. Añadir pregunta");
            System.out.println("3. Modificar pregunta");
            System.out.println("4. Eliminar pregunta");
            System.out.println("5. Eliminar todas las preguntas");
            System.out.println("6. ← Volver");
            System.out.print("Opción: ");

            switch (leerEntero()) {
                case 1 -> verPreguntasDetallado(id);
                case 2 -> añadirPreguntas(id);
                case 3 -> modificarPregunta(id);
                case 4 -> eliminarPregunta(id);
                case 5 -> eliminarTodasPreguntas(id);
                case 6 -> { return; }
                default -> System.out.println("❌ Opción inválida");
            }

            // Refrescar encuesta
            enc = ctrlDominio.obtenerEncuesta(id);
        }
    }

    private static void añadirPreguntas(String idEncuesta) {
        while (true) {
            System.out.println("\n--- TIPO DE PREGUNTA ---");
            System.out.println("1. Numérica");
            System.out.println("2. Texto libre");
            System.out.println("3. Categoría simple");
            System.out.println("4. Ordinal");
            System.out.println("5. Categoría múltiple");
            System.out.println("6. ← Terminar");
            System.out.print("Opción: ");

            int tipo = leerEntero();
            if (tipo == 6) break;

            System.out.print("\nEnunciado: ");
            String enunciado = scanner.nextLine().trim();

            System.out.print("¿Obligatoria? (s/n): ");
            boolean obligatoria = scanner.nextLine().trim().equalsIgnoreCase("s");

            try {
                Pregunta p = crearPregunta(tipo, enunciado);
                if (p != null) {
                    p.setObligatoria(obligatoria);
                    boolean ok = ctrlDominio.addPregunta(idEncuesta, p);

                    if (ok) {
                        System.out.println("✓ Pregunta añadida exitosamente");
                        System.out.println("  ID: " + p.getId());
                    } else {
                        System.out.println("❌ Error al añadir pregunta");
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static void modificarPregunta(String idEncuesta) {
        Encuesta enc = ctrlDominio.obtenerEncuesta(idEncuesta);
        List<Pregunta> preguntas = enc.getPreguntas();

        if (preguntas.isEmpty()) {
            System.out.println("\n⚠️  No hay preguntas para modificar");
            return;
        }

        System.out.println("\n--- PREGUNTAS ---");
        for (int i = 0; i < preguntas.size(); i++) {
            System.out.println((i + 1) + ". " + preguntas.get(i).getEnunciado());
        }

        System.out.print("\nNúmero de pregunta a modificar (0 para cancelar): ");
        int num = leerEntero();

        if (num == 0 || num < 1 || num > preguntas.size()) {
            System.out.println("⚠️  Cancelado");
            return;
        }

        int index = num - 1;
        Pregunta p = preguntas.get(index);

        System.out.println("\n📝 Modificando: " + p.getEnunciado());
        System.out.println("   (ENTER para mantener valor actual)\n");

        System.out.print("Nuevo enunciado: ");
        String nuevoEnunciado = scanner.nextLine().trim();
        if (!nuevoEnunciado.isEmpty()) {
            p.setEnunciado(nuevoEnunciado);
        }

        System.out.print("¿Obligatoria? (s/n/ENTER): ");
        String obligatoriaStr = scanner.nextLine().trim();
        if (obligatoriaStr.equalsIgnoreCase("s")) {
            p.setObligatoria(true);
        } else if (obligatoriaStr.equalsIgnoreCase("n")) {
            p.setObligatoria(false);
        }

        try {
            boolean ok = ctrlDominio.modificarPregunta(idEncuesta, index, p);
            System.out.println(ok ? "✓ Pregunta modificada" : "❌ Error al modificar");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void eliminarPregunta(String idEncuesta) {
        Encuesta enc = ctrlDominio.obtenerEncuesta(idEncuesta);
        List<Pregunta> preguntas = enc.getPreguntas();

        if (preguntas.isEmpty()) {
            System.out.println("\n⚠️  No hay preguntas para eliminar");
            return;
        }

        System.out.println("\n--- PREGUNTAS ---");
        for (int i = 0; i < preguntas.size(); i++) {
            System.out.println((i + 1) + ". " + preguntas.get(i).getEnunciado());
        }

        System.out.print("\nNúmero de pregunta a eliminar (0 para cancelar): ");
        int num = leerEntero();

        if (num == 0 || num < 1 || num > preguntas.size()) {
            System.out.println("⚠️  Cancelado");
            return;
        }

        int index = num - 1;
        System.out.print("¿Confirmar eliminación? (s/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println("⚠️  Cancelado");
            return;
        }

        try {
            boolean ok = ctrlDominio.eliminarPregunta(idEncuesta, index);
            System.out.println(ok ? "✓ Pregunta eliminada" : "❌ Error al eliminar");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void eliminarTodasPreguntas(String idEncuesta) {
        Encuesta enc = ctrlDominio.obtenerEncuesta(idEncuesta);

        if (enc.getNumPreguntas() == 0) {
            System.out.println("⚠️  La encuesta ya está vacía");
            return;
        }

        System.out.println("\n⚠️  ADVERTENCIA: Vas a eliminar " + enc.getNumPreguntas() + " preguntas");
        System.out.print("¿Confirmar limpieza? (escribe 'CONFIRMAR'): ");

        if (!scanner.nextLine().trim().equals("CONFIRMAR")) {
            System.out.println("⚠️  Cancelado");
            return;
        }

        try {
            int total = enc.getNumPreguntas();
            for (int i = total - 1; i >= 0; i--) {
                ctrlDominio.eliminarPregunta(idEncuesta, i);
            }
            System.out.println("✓ Todas las preguntas eliminadas");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verPreguntasDetallado(String idEncuesta) {
        Encuesta enc = ctrlDominio.obtenerEncuesta(idEncuesta);
        List<Pregunta> preguntas = enc.getPreguntas();

        if (preguntas.isEmpty()) {
            System.out.println("\n⚠️  Esta encuesta no tiene preguntas");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       PREGUNTAS DE LA ENCUESTA           ║");
        System.out.println("╚══════════════════════════════════════════╝");

        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);
            System.out.println("\n[" + (i + 1) + "] " + p.getEnunciado());
            System.out.println("    ID: " + p.getId());
            System.out.println("    Tipo: " + obtenerNombreTipo(p));
            System.out.println("    Obligatoria: " + (p.esObligatoria() ? "Sí" : "No"));
            mostrarDetallesPregunta(p);
        }
    }

    // ==================== UTILIDADES ====================

    private static Pregunta crearPregunta(int tipo, String enunciado) {
        try {
            return switch (tipo) {
                case 1 -> { // Numérica
                    System.out.print("Min (ENTER=sin límite): ");
                    String minS = scanner.nextLine().trim();
                    System.out.print("Max (ENTER=sin límite): ");
                    String maxS = scanner.nextLine().trim();
                    Double min = minS.isEmpty() ? null : Double.parseDouble(minS);
                    Double max = maxS.isEmpty() ? null : Double.parseDouble(maxS);
                    yield new Numerica(enunciado, min, max);
                }
                case 2 -> { // Texto
                    System.out.print("Longitud máxima (ENTER=1000): ");
                    String longS = scanner.nextLine().trim();
                    int longMax = longS.isEmpty() ? 1000 : Integer.parseInt(longS);
                    yield new Libre(enunciado, longMax);
                }
                case 3 -> new CategoriaSimple(enunciado, leerOpciones());
                case 4 -> new Ordinal(enunciado, leerOpciones());
                case 5 -> { // Categoría múltiple
                    Set<String> ops = leerOpciones();
                    System.out.print("Máx selecciones: ");
                    int max = leerEntero();
                    consumirLinea();
                    yield new CategoriaMultiple(enunciado, ops, max);
                }
                default -> {
                    System.out.println("❌ Tipo inválido");
                    yield null;
                }
            };
        } catch (Exception e) {
            System.out.println("❌ Error creando pregunta: " + e.getMessage());
            return null;
        }
    }

    private static Set<String> leerOpciones() {
        System.out.print("Número de opciones: ");
        int n = leerEntero();
        consumirLinea();
        Set<String> ops = new LinkedHashSet<>();
        for (int i = 0; i < n; i++) {
            System.out.print("Opción " + (i + 1) + ": ");
            ops.add(scanner.nextLine().trim());
        }
        return ops;
    }

    private static void mostrarDetallesPregunta(Pregunta p) {
        if (p instanceof Numerica) {
            Numerica n = (Numerica) p;
            if (n.getMin() != null || n.getMax() != null) {
                System.out.println("    Rango: [" + n.getMin() + ", " + n.getMax() + "]");
            }
        } else if (p instanceof Libre) {
            System.out.println("    Longitud máx: " + ((Libre) p).getLongitudMaxima());
        } else if (p instanceof CategoriaSimple) {
            System.out.println("    Opciones: " + ((CategoriaSimple) p).getOpciones());
        } else if (p instanceof Ordinal) {
            System.out.println("    Niveles: " + ((Ordinal) p).getOpciones());
        } else if (p instanceof CategoriaMultiple) {
            CategoriaMultiple cm = (CategoriaMultiple) p;
            System.out.println("    Opciones: " + cm.getOpciones());
            System.out.println("    Máx selecciones: " + cm.getMaxSelecciones());
        }
    }

    private static String pedirIdEncuesta() {
        listarEncuestas();
        System.out.print("\nID de encuesta: ");
        String id = scanner.nextLine().trim();
        return id.isEmpty() ? null : id;
    }

    private static String obtenerNombreTipo(Pregunta p) {
        if (p instanceof Numerica) return "Numérica";
        if (p instanceof Libre) return "Texto libre";
        if (p instanceof CategoriaSimple) return "Categoría simple";
        if (p instanceof Ordinal) return "Ordinal";
        if (p instanceof CategoriaMultiple) return "Categoría múltiple";
        return "Desconocido";
    }

    private static int leerEntero() {
        while (!scanner.hasNextInt()) {
            System.out.print("⚠️  Número válido: ");
            scanner.next();
        }
        int num = scanner.nextInt();
        scanner.nextLine();
        return num;
    }

    private static void consumirLinea() {
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}