package drivers;

import main.domain.controllers.*;
import main.domain.classes.*;
import java.util.*;

/**
 * Driver interactivo para probar el sistema de respuestas
 * Utiliza CtrlDominio como único punto de entrada
 */
public class DriverCtrlRespuestas {

    private static CtrlDominio ctrlDominio;
    private static Scanner scanner;

    public static void main(String[] args) {
        ctrlDominio = new CtrlDominio();
        scanner = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      TEST GESTOR DE RESPUESTAS           ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        crearDatosPrueba();

        while (true) {
            mostrarMenu();
        }
    }

    private static void crearDatosPrueba() {
        System.out.println("📦 Creando datos de prueba...\n");

        try {
            // Crear usuarios
            ctrlDominio.crearUsuarioRespondedor("user1", "Juan Pérez", "pass123");
            ctrlDominio.crearUsuarioRespondedor("user2", "María García", "pass456");
            ctrlDominio.crearUsuarioAdmin("admin1", "Admin Principal", "admin123");

            // Crear encuesta de ejemplo
            Encuesta enc = ctrlDominio.crearEncuesta(
                    "Encuesta de Satisfacción",
                    "Evalúa tu experiencia"
            );

            // Añadir preguntas
            Pregunta p1 = new Numerica("¿Qué puntuación das al servicio?", 0.0, 10.0);
            p1.setObligatoria(true);
            ctrlDominio.addPregunta(enc.getId(), p1);

            Set<String> opcionesSino = new LinkedHashSet<>();
            opcionesSino.add("Sí");
            opcionesSino.add("No");
            Pregunta p2 = new CategoriaSimple("¿Recomendarías nuestro servicio?", opcionesSino);
            ctrlDominio.addPregunta(enc.getId(), p2);

            Pregunta p3 = new Libre("Comentarios adicionales", 500);
            ctrlDominio.addPregunta(enc.getId(), p3);

            Set<String> aspectos = new LinkedHashSet<>();
            aspectos.add("Precio");
            aspectos.add("Calidad");
            aspectos.add("Atención al cliente");
            aspectos.add("Rapidez");
            Pregunta p4 = new CategoriaMultiple("¿Qué aspectos te gustaron más?", aspectos, 3);
            ctrlDominio.addPregunta(enc.getId(), p4);

            System.out.println("✓ Datos de prueba creados:");
            System.out.println("  • 2 usuarios respondedores (user1, user2)");
            System.out.println("  • 1 usuario admin (admin1)");
            System.out.println("  • 1 encuesta con 4 preguntas");
            System.out.println("  • ID encuesta: " + enc.getId() + "\n");

        } catch (Exception e) {
            System.out.println("❌ Error al crear datos de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Responder una pregunta");
        System.out.println("2. Responder encuesta completa");
        System.out.println("3. Ver respuestas de un usuario");
        System.out.println("4. Ver todas las respuestas de una encuesta");
        System.out.println("5. Obtener índice de pregunta");
        System.out.println("6. Listar usuarios y encuestas disponibles");
        System.out.println("7. Ver estadísticas");
        System.out.println("8. Salir");
        System.out.print("Opción: ");

        try {
            switch (leerEntero()) {
                case 1 -> responderUnaPregunta();
                case 2 -> responderEncuestaCompleta();
                case 3 -> verRespuestasUsuario();
                case 4 -> verRespuestasEncuesta();
                case 5 -> obtenerIndicePregunta();
                case 6 -> listarDisponibles();
                case 7 -> mostrarEstadisticas();
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

    // ==================== RESPONDER PREGUNTAS ====================

    private static void responderUnaPregunta() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       RESPONDER UNA PREGUNTA             ║");
        System.out.println("╚══════════════════════════════════════════╝");

        System.out.print("ID de usuario: ");
        String idUsuario = scanner.nextLine().trim();

        UsuarioRespondedor usuario = ctrlDominio.obtenerRespondedor(idUsuario);
        if (usuario == null) {
            System.out.println("❌ Usuario no encontrado o no es respondedor");
            return;
        }

        System.out.print("ID de encuesta: ");
        String idEncuesta = scanner.nextLine().trim();

        Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);
        if (encuesta == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        List<Pregunta> preguntas = encuesta.getPreguntas();
        if (preguntas.isEmpty()) {
            System.out.println("❌ La encuesta no tiene preguntas");
            return;
        }

        System.out.println("\n--- PREGUNTAS DISPONIBLES ---");
        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);
            System.out.println((i + 1) + ". " + p.getEnunciado() +
                    " [" + obtenerNombreTipo(p) + "]");
        }

        System.out.print("\nNúmero de pregunta: ");
        int num = leerEntero();
        consumirLinea();

        if (num < 1 || num > preguntas.size()) {
            System.out.println("❌ Número inválido");
            return;
        }

        Pregunta pregunta = preguntas.get(num - 1);
        System.out.println("\n❓ " + pregunta.getEnunciado());

        Object valor = leerRespuesta(pregunta);

        try {
            ctrlDominio.responderPregunta(idUsuario, idEncuesta, pregunta.getId(), valor);

            System.out.println("\n✓ Respuesta registrada exitosamente");
            System.out.println("  Usuario: " + usuario.getNombre());
            System.out.println("  Pregunta: " + pregunta.getEnunciado());
            System.out.println("  Respuesta: " + formatearValor(valor));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void responderEncuestaCompleta() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║    RESPONDER ENCUESTA COMPLETA           ║");
        System.out.println("╚══════════════════════════════════════════╝");

        System.out.print("ID de usuario: ");
        String idUsuario = scanner.nextLine().trim();

        UsuarioRespondedor usuario = ctrlDominio.obtenerRespondedor(idUsuario);
        if (usuario == null) {
            System.out.println("❌ Usuario no encontrado o no es respondedor");
            return;
        }

        System.out.print("ID de encuesta: ");
        String idEncuesta = scanner.nextLine().trim();

        Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);
        if (encuesta == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        List<Pregunta> preguntas = encuesta.getPreguntas();
        if (preguntas.isEmpty()) {
            System.out.println("❌ La encuesta no tiene preguntas");
            return;
        }

        System.out.println("\n📋 Encuesta: " + encuesta.getTitulo());
        System.out.println("   " + encuesta.getDescripcion());
        System.out.println("\nTotal de preguntas: " + preguntas.size());

        Map<String, Object> respuestas = new HashMap<>();

        for (Pregunta p : preguntas) {
            System.out.println("\n❓ " + p.getEnunciado());
            if (p.esObligatoria()) {
                System.out.println("   (⚠️  OBLIGATORIA)");
            }
            System.out.println("   Tipo: " + obtenerNombreTipo(p));

            Object valor = leerRespuesta(p);
            if (valor != null || p.esObligatoria()) {
                respuestas.put(p.getId(), valor);
            }
        }

        try {
            ctrlDominio.responderEncuesta(idUsuario, idEncuesta, respuestas);
            System.out.println("\n✓ Encuesta respondida exitosamente");
            System.out.println("  Usuario: " + usuario.getNombre());
            System.out.println("  Encuesta: " + encuesta.getTitulo());
            System.out.println("  Respuestas registradas: " + respuestas.size());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ==================== VER RESPUESTAS ====================

    private static void verRespuestasUsuario() {
        System.out.print("\nID de usuario: ");
        String idUsuario = scanner.nextLine().trim();

        UsuarioRespondedor usuario = ctrlDominio.obtenerRespondedor(idUsuario);
        if (usuario == null) {
            System.out.println("❌ Usuario no encontrado o no es respondedor");
            return;
        }

        System.out.print("ID de encuesta: ");
        String idEncuesta = scanner.nextLine().trim();

        List<Respuesta> respuestas = ctrlDominio.obtenerRespuestasUsuario(idUsuario, idEncuesta);

        if (respuestas.isEmpty()) {
            System.out.println("\n⚠️  Este usuario no ha respondido esta encuesta");
            return;
        }

        Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       RESPUESTAS DEL USUARIO             ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("Usuario: " + usuario.getNombre());
        System.out.println("Encuesta: " + (encuesta != null ? encuesta.getTitulo() : idEncuesta));
        System.out.println("Total de respuestas: " + respuestas.size());

        for (Respuesta r : respuestas) {
            Pregunta p = buscarPregunta(encuesta, r.getIdPregunta());

            System.out.println("\n─".repeat(50));
            if (p != null) {
                System.out.println("❓ " + p.getEnunciado());
                System.out.println("   Tipo: " + obtenerNombreTipo(p));
            } else {
                System.out.println("❓ Pregunta: " + r.getIdPregunta());
            }
            System.out.println("   ➤ Respuesta: " + formatearValor(r.getValor()));
        }
    }

    private static void verRespuestasEncuesta() {
        System.out.print("\nID de encuesta: ");
        String idEncuesta = scanner.nextLine().trim();

        Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);
        if (encuesta == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        List<UsuarioRespondedor> usuariosQueRespondieron =
                ctrlDominio.obtenerUsuariosQueRespondieron(idEncuesta);

        if (usuariosQueRespondieron.isEmpty()) {
            System.out.println("\n⚠️  Nadie ha respondido esta encuesta");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║    TODAS LAS RESPUESTAS - ENCUESTA       ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("Encuesta: " + encuesta.getTitulo());
        System.out.println("Total de usuarios: " + usuariosQueRespondieron.size());

        for (UsuarioRespondedor usuario : usuariosQueRespondieron) {
            System.out.println("\n" + "═".repeat(50));
            System.out.println("👤 " + usuario.getNombre() + " (" + usuario.getId() + ")");
            System.out.println("═".repeat(50));

            List<Respuesta> respuestas = ctrlDominio.obtenerRespuestasUsuario(
                    usuario.getId(), idEncuesta
            );

            for (Respuesta r : respuestas) {
                Pregunta p = buscarPregunta(encuesta, r.getIdPregunta());
                if (p != null) {
                    System.out.println("   • " + p.getEnunciado());
                    System.out.println("     → " + formatearValor(r.getValor()));
                }
            }
        }
    }

    // ==================== OTRAS FUNCIONES ====================

    private static void obtenerIndicePregunta() {
        System.out.print("\nID de encuesta: ");
        String idEncuesta = scanner.nextLine().trim();

        Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);
        if (encuesta == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        List<Pregunta> preguntas = encuesta.getPreguntas();
        if (preguntas.isEmpty()) {
            System.out.println("❌ La encuesta no tiene preguntas");
            return;
        }

        System.out.println("\n--- PREGUNTAS ---");
        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);
            System.out.println((i + 1) + ". " + p.getEnunciado() + " (ID: " + p.getId() + ")");
        }

        System.out.print("\nID de pregunta: ");
        String idPregunta = scanner.nextLine().trim();

        int indice = ctrlDominio.obtenerIndicePregunta(idEncuesta, idPregunta);

        if (indice >= 0) {
            System.out.println("\n✓ Pregunta encontrada");
            System.out.println("  Índice: " + indice);
            System.out.println("  Posición: " + (indice + 1) + " de " + preguntas.size());
        } else {
            System.out.println("❌ Pregunta no encontrada en la encuesta");
        }
    }

    private static void listarDisponibles() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║    USUARIOS Y ENCUESTAS DISPONIBLES      ║");
        System.out.println("╚══════════════════════════════════════════╝");

        System.out.println("\n👤 USUARIOS RESPONDEDORES:");
        List<UsuarioRespondedor> respondedores = ctrlDominio.listarRespondedores();

        if (respondedores.isEmpty()) {
            System.out.println("  (No hay usuarios respondedores)");
        } else {
            for (UsuarioRespondedor u : respondedores) {
                System.out.println("  • " + u.getId() + " - " + u.getNombre());
            }
        }

        System.out.println("\n📋 ENCUESTAS:");
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();

        if (encuestas.isEmpty()) {
            System.out.println("  (No hay encuestas)");
        } else {
            for (Encuesta e : encuestas) {
                System.out.println("  • " + e.getId() + " - " + e.getTitulo() +
                        " (" + e.getNumPreguntas() + " preguntas)");
            }
        }
    }

    private static void mostrarEstadisticas() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         ESTADÍSTICAS                     ║");
        System.out.println("╚══════════════════════════════════════════╝");

        List<UsuarioRespondedor> respondedores = ctrlDominio.listarRespondedores();
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();

        int totalRespondedores = respondedores.size();
        int totalRespuestas = 0;
        int totalEncuestasRespondidas = 0;

        for (Encuesta enc : encuestas) {
            List<UsuarioRespondedor> usuariosEnEsta =
                    ctrlDominio.obtenerUsuariosQueRespondieron(enc.getId());
            totalEncuestasRespondidas += usuariosEnEsta.size();

            for (UsuarioRespondedor u : usuariosEnEsta) {
                totalRespuestas += ctrlDominio.obtenerRespuestasUsuario(
                        u.getId(), enc.getId()).size();
            }
        }

        System.out.println("📊 Usuarios respondedores: " + totalRespondedores);
        System.out.println("📊 Encuestas disponibles: " + encuestas.size());
        System.out.println("📊 Total de respuestas: " + totalRespuestas);

        if (totalRespondedores > 0 && totalEncuestasRespondidas > 0) {
            System.out.println("📊 Promedio encuestas/usuario: " +
                    String.format("%.2f", (double) totalEncuestasRespondidas / totalRespondedores));
        }

        if (totalEncuestasRespondidas > 0) {
            System.out.println("📊 Promedio respuestas/encuesta: " +
                    String.format("%.2f", (double) totalRespuestas / totalEncuestasRespondidas));
        }

        if (!encuestas.isEmpty()) {
            System.out.println("\n📋 Detalle por encuesta:");
            for (Encuesta enc : encuestas) {
                List<UsuarioRespondedor> usuariosEnEsta =
                        ctrlDominio.obtenerUsuariosQueRespondieron(enc.getId());
                System.out.println("  • " + enc.getTitulo() + ": " +
                        usuariosEnEsta.size() + " usuarios respondieron");
            }
        }
    }

    // ==================== UTILIDADES ====================

    private static Object leerRespuesta(Pregunta p) {
        try {
            if (p instanceof Numerica) {
                Numerica n = (Numerica) p;
                System.out.print("➤ Número");
                if (n.getMin() != null && n.getMax() != null) {
                    System.out.print(" [" + n.getMin() + "-" + n.getMax() + "]");
                }
                System.out.print(": ");
                double valor = scanner.nextDouble();
                consumirLinea();
                return valor;
            }

            if (p instanceof Libre) {
                System.out.print("➤ Texto: ");
                return scanner.nextLine().trim();
            }

            if (p instanceof CategoriaSimple) {
                return elegirOpcion(new ArrayList<>(((CategoriaSimple) p).getOpciones()));
            }

            if (p instanceof Ordinal) {
                return elegirOpcion(new ArrayList<>(((Ordinal) p).getOpciones()));
            }

            if (p instanceof CategoriaMultiple) {
                CategoriaMultiple cm = (CategoriaMultiple) p;
                System.out.println("   (Máx " + cm.getMaxSelecciones() + " opciones)");
                return elegirOpciones(new ArrayList<>(cm.getOpciones()), cm.getMaxSelecciones());
            }
        } catch (Exception e) {
            System.out.println("❌ Error leyendo respuesta: " + e.getMessage());
            consumirLinea();
        }
        return null;
    }

    private static String elegirOpcion(List<String> opciones) {
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("   " + (i + 1) + ". " + opciones.get(i));
        }
        System.out.print("➤ Elige (número): ");
        int idx = leerEntero() - 1;
        consumirLinea();
        return (idx >= 0 && idx < opciones.size()) ? opciones.get(idx) : null;
    }

    private static Set<String> elegirOpciones(List<String> opciones, int max) {
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("   " + (i + 1) + ". " + opciones.get(i));
        }
        System.out.print("➤ Números separados por comas: ");
        String input = scanner.nextLine().trim();

        Set<String> sel = new HashSet<>();
        for (String num : input.split(",")) {
            try {
                int idx = Integer.parseInt(num.trim()) - 1;
                if (idx >= 0 && idx < opciones.size()) {
                    sel.add(opciones.get(idx));
                }
            } catch (NumberFormatException e) { }
        }

        return sel.size() <= max ? sel : null;
    }

    private static Pregunta buscarPregunta(Encuesta enc, String idPregunta) {
        if (enc == null) return null;
        return enc.getPreguntas().stream()
                .filter(p -> p.getId().equals(idPregunta))
                .findFirst()
                .orElse(null);
    }

    private static String obtenerNombreTipo(Pregunta p) {
        if (p instanceof Numerica) return "Numérica";
        if (p instanceof Libre) return "Texto libre";
        if (p instanceof CategoriaSimple) return "Categoría simple";
        if (p instanceof Ordinal) return "Ordinal";
        if (p instanceof CategoriaMultiple) return "Categoría múltiple";
        return "Desconocido";
    }

    private static String formatearValor(Object valor) {
        if (valor == null) return "(sin respuesta)";
        if (valor instanceof Set) {
            Set<?> set = (Set<?>) valor;
            if (set.isEmpty()) return "[]";
            return set.toString();
        }
        if (valor instanceof Double) {
            return String.format("%.2f", (Double) valor);
        }
        return valor.toString();
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