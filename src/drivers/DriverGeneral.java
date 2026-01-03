package drivers;

import main.domain.controllers.*;
import main.domain.classes.*;
import main.persistence.LectorCSV;

import java.util.*;

/**
 * Driver interactivo para probar el sistema de encuestas
 * VERSIÓN OPTIMIZADA - Sin código duplicado
 */
public class DriverGeneral {

    private static CtrlDominio ctrl;
    private static Scanner scanner;
    private static String usuarioActualId = null;
    private static boolean esAdmin = false;

    public static void main(String[] args) {
        ctrl = new CtrlDominio();
        scanner = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println("  SISTEMA DE GESTIÓN DE ENCUESTAS");
        System.out.println("==============================================\n");

        while (true) {
            try {
                if (usuarioActualId == null) {
                    menuInicial();
                } else {
                    if (esAdmin) {
                        menuAdmin();
                    } else {
                        menuRespondedor();
                    }
                }
            } catch (Exception e) {
                System.out.println("\n❌ Error inesperado: " + e.getMessage());
            }
        }
    }

    // ==================== MENÚS ====================

    private static void menuInicial() {
        System.out.println("\n--- MENÚ INICIAL ---");
        System.out.println("1. Registrarse como Admin");
        System.out.println("2. Registrarse como Respondedor");
        System.out.println("3. Iniciar sesión");
        System.out.println("4. Salir");
        System.out.print("Opción: ");

        switch (leerEntero()) {
            case 1 -> registrarUsuario(true);
            case 2 -> registrarUsuario(false);
            case 3 -> iniciarSesion();
            case 4 -> {
                System.out.println("¡Hasta luego!");
                System.exit(0);
            }
            default -> System.out.println("❌ Opción inválida");
        }
    }

    private static void menuAdmin() {
        System.out.println("\n--- MENÚ ADMIN: " + usuarioActualId + " ---");
        System.out.println("1. Explorar encuestas");
        System.out.println("2. Gestión de Encuestas");

        System.out.println("3. Análisis y Resultados");
        System.out.println("4. Importar/Exportar");
        System.out.println("5. Cerrar sesión");
        System.out.print("Opción: ");

        switch (leerEntero()) {
            case 1 -> menuExplorarEncuestas();
            case 2 -> menuGestionEncuestas();
            case 3 -> menuAnalisis();
            case 4 -> menuImportarExportar();
            case 5 -> cerrarSesion();
            default -> System.out.println("❌ Opción inválida");
        }
    }

    private static void menuGestionEncuestas() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       GESTIÓN DE ENCUESTAS               ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("1. Crear encuesta nueva");
            System.out.println("2. Modificar encuesta (título/descripción)");
            System.out.println("3. Gestionar preguntas");
            System.out.println("4. Limpiar todas las preguntas");
            System.out.println("5. Eliminar encuesta completa");
            System.out.println("6. ← Volver al menú principal");
            System.out.print("Opción: ");

            switch (leerEntero()) {
                case 1 -> crearEncuesta();
                case 2 -> modificarEncuesta();
                case 3 -> gestionarPreguntas();
                case 4 -> limpiarPreguntas();
                case 5 -> eliminarEncuesta();
                case 6 -> { return; }
                default -> System.out.println("❌ Opción inválida");
            }
        }
    }
    private static void menuAnalisis() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       ANÁLISIS Y RESULTADOS              ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("1. Ver todas las respuestas");
            System.out.println("2. Ver estadísticas");
            System.out.println("3. Ejecutar clustering");
            System.out.println("4. Ver historial de clustering");
            System.out.println("5. Ver mejor resultado guardado");  // ← NUEVO
            System.out.println("6. Limpiar historial de clustering");
            System.out.println("7. ← Volver al menú principal");  // ← CAMBIO: de 6 a 7
            System.out.print("Opción: ");

            switch (leerEntero()) {
                case 1 -> verTodasLasRespuestas();
                case 2 -> verEstadisticas();
                case 3 -> ejecutarClustering();
                case 4 -> verHistorialClustering();
                case 5 -> verMejorResultadoGuardado();  // ← NUEVO
                case 6 -> limpiarHistorialClustering();
                case 7 -> { return; }
                default -> System.out.println("❌ Opción inválida");
            }
        }
    }

    private static void menuImportarExportar() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       IMPORTAR / EXPORTAR                ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("1. Importar encuesta desde CSV");
            System.out.println("2. Exportar encuesta a CSV");
            System.out.println("3. ← Volver al menú principal");
            System.out.print("Opción: ");

            switch (leerEntero()) {
                case 1 -> importarCSV();
                case 2 -> exportarCSV();
                case 3 -> { return; }
                default -> System.out.println("❌ Opción inválida");
            }
        }
    }


    private static void menuRespondedor() {
        System.out.println("\n--- MENÚ RESPONDEDOR: " + usuarioActualId + " ---");
        System.out.println("1. Explorar encuestas");
        System.out.println("2. Responder encuesta");
        System.out.println("3. Ver mis respuestas");
        System.out.println("4. Cerrar sesión");
        System.out.print("Opción: ");

        switch (leerEntero()) {
            case 1 -> menuExplorarEncuestas();
            case 2 -> responderEncuesta();
            case 3 -> verMisRespuestas();
            case 4 -> cerrarSesion();
            default -> System.out.println("❌ Opción inválida");
        }
    }

    private static void menuExplorarEncuestas() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       EXPLORAR ENCUESTAS                 ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("1. Listar todas las encuestas");
            System.out.println("2. Ver detalle de una encuesta");
            System.out.println("3. ← Volver al menú principal");
            System.out.print("Opción: ");

            switch (leerEntero()) {
                case 1 -> listarEncuestas();
                case 2 -> verDetalleEncuesta();
                case 3 -> { return; }
                default -> System.out.println("❌ Opción inválida");
            }
        }
    }

    // ==================== GESTIÓN DE USUARIOS ====================

    private static void registrarUsuario(boolean esAdmin) {
        System.out.print("\nID de usuario: ");
        String id = scanner.nextLine().trim();
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            if (esAdmin) {
                ctrl.crearUsuarioAdmin(id, nombre, password);
                DriverGeneral.esAdmin = true;
            } else {
                ctrl.crearUsuarioRespondedor(id, nombre, password);
                DriverGeneral.esAdmin = false;
            }
            usuarioActualId = id;
            System.out.println("✓ Usuario creado exitosamente");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void iniciarSesion() {
        System.out.print("\nID de usuario: ");
        String id = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            Usuario usuario = ctrl.obtenerUsuario(id);
            if (usuario == null || !usuario.getPassword().equals(password)) {
                System.out.println("❌ Credenciales inválidas");
                return;
            }

            usuarioActualId = id;
            esAdmin = usuario instanceof UsuarioAdmin;
            System.out.println("✓ Bienvenido, " + usuario.getNombre());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void cerrarSesion() {
        usuarioActualId = null;
        esAdmin = false;
        System.out.println("✓ Sesión cerrada");
    }

    // ==================== GESTIÓN DE ENCUESTAS ====================

    private static void crearEncuesta() {
        System.out.print("\nTítulo: ");
        String titulo = scanner.nextLine().trim();
        System.out.print("Descripción: ");
        String descripcion = scanner.nextLine().trim();

        try {
            Encuesta enc = ctrl.crearEncuesta(titulo, descripcion);
            System.out.println("✓ Encuesta creada: " + enc.getId());

            System.out.print("¿Añadir preguntas ahora? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                añadirPreguntasInteractivo(enc.getId());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void listarEncuestas() {
        try {
            List<Encuesta> encuestas = ctrl.listarEncuestas();
            if (encuestas.isEmpty()) {
                System.out.println("\n⚠️  No hay encuestas");
                return;
            }

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       ENCUESTAS DISPONIBLES              ║");
            System.out.println("╚══════════════════════════════════════════╝");

            for (Encuesta e : encuestas) {
                System.out.println("\n📋 " + e.getId());
                System.out.println("   " + e.getTitulo());
                System.out.println("   Preguntas: " + e.getNumPreguntas());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verDetalleEncuesta() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            Encuesta enc = ctrl.obtenerEncuesta(id);
            if (enc == null) {
                System.out.println("❌ Encuesta no encontrada");
                return;
            }

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       DETALLE DE ENCUESTA                ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("📋 Título: " + enc.getTitulo());
            System.out.println("📝 Descripción: " + enc.getDescripcion());
            System.out.println("📊 Total de preguntas: " + enc.getNumPreguntas());

            List<Pregunta> preguntas = enc.getPreguntas();
            if (preguntas.isEmpty()) {
                System.out.println("\n⚠️  Esta encuesta no tiene preguntas");
                return;
            }

            System.out.println("\n--- PREGUNTAS ---");
            for (int i = 0; i < preguntas.size(); i++) {
                Pregunta p = preguntas.get(i);
                System.out.println("\n" + (i + 1) + ". " + p.getEnunciado());
                System.out.println("   Tipo: " + obtenerNombreTipo(p));
                System.out.println("   Obligatoria: " + (p.esObligatoria() ? "Sí" : "No"));

                // Mostrar opciones si las tiene
                if (p instanceof CategoriaSimple) {
                    System.out.println("   Opciones: " + ((CategoriaSimple) p).getOpciones());
                } else if (p instanceof Ordinal) {
                    System.out.println("   Niveles: " + ((Ordinal) p).getOpciones());
                } else if (p instanceof CategoriaMultiple) {
                    CategoriaMultiple cm = (CategoriaMultiple) p;
                    System.out.println("   Opciones: " + cm.getOpciones());
                    System.out.println("   Máx selecciones: " + cm.getMaxSelecciones());
                } else if (p instanceof Numerica) {
                    Numerica n = (Numerica) p;
                    if (n.getMin() != null || n.getMax() != null) {
                        System.out.println("   Rango: [" + n.getMin() + ", " + n.getMax() + "]");
                    }
                }
            }

            // Mostrar estadísticas de respuestas
            List<UsuarioRespondedor> respondedores = ctrl.obtenerUsuariosQueRespondieron(id);
            System.out.println("\n👥 Usuarios que respondieron: " + respondedores.size());

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void modificarEncuesta() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        System.out.print("Nuevo título (Enter para mantener): ");
        String titulo = scanner.nextLine().trim();
        System.out.print("Nueva descripción (Enter para mantener): ");
        String desc = scanner.nextLine().trim();

        try {
            boolean ok = ctrl.modificarEncuesta(id,
                    titulo.isEmpty() ? null : titulo,
                    desc.isEmpty() ? null : desc);
            System.out.println(ok ? "✓ Modificada" : "❌ Error");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void eliminarEncuesta() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        System.out.print("¿Confirmar eliminación? (s/n): ");
        if (!scanner.nextLine().equalsIgnoreCase("s")) {
            System.out.println("⚠️  Cancelado");
            return;
        }

        try {
            boolean ok = ctrl.eliminarEncuesta(id);
            System.out.println(ok ? "✓ Eliminada" : "❌ Error");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    private static void limpiarPreguntas() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            Encuesta enc = ctrl.obtenerEncuesta(id);
            if (enc == null) {
                System.out.println("❌ Encuesta no encontrada");
                return;
            }

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

            // Eliminar preguntas de atrás hacia adelante para evitar problemas de índices
            int numPreguntas = enc.getNumPreguntas();
            for (int i = numPreguntas - 1; i >= 0; i--) {
                ctrl.eliminarPregunta(id, i);
            }

            System.out.println("✓ Todas las preguntas han sido eliminadas");
            System.out.println("  La encuesta '" + enc.getTitulo() + "' está ahora vacía");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ==================== GESTIÓN DE PREGUNTAS (SUBMENÚ) ====================

    private static void gestionarPreguntas() {
        String idEncuesta = pedirIdEncuesta();
        if (idEncuesta == null) return;

        Encuesta enc = ctrl.obtenerEncuesta(idEncuesta);
        if (enc == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        while (true) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║   GESTIONAR PREGUNTAS: " + truncar(enc.getTitulo(), 18));
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("1. Ver preguntas");
            System.out.println("2. Añadir pregunta");
            System.out.println("3. Modificar pregunta");
            System.out.println("4. Eliminar pregunta");
            System.out.println("5. Volver al menú principal");
            System.out.print("Opción: ");

            int opt = leerEntero();

            switch (opt) {
                case 1 -> verPreguntasDetallado(idEncuesta);
                case 2 -> añadirPreguntasInteractivo(idEncuesta);
                case 3 -> modificarPregunta(idEncuesta);
                case 4 -> eliminarPreguntaInteractivo(idEncuesta);
                case 5 -> {
                    return; // Volver al menú principal
                }
                default -> System.out.println("❌ Opción inválida");
            }
        }
    }
    private static void verPreguntasDetallado(String idEncuesta) {
        try {
            Encuesta enc = ctrl.obtenerEncuesta(idEncuesta);
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
                System.out.println("    Tipo: " + obtenerNombreTipo(p));
                System.out.println("    Obligatoria: " + (p.esObligatoria() ? "Sí" : "No"));

                if (p instanceof CategoriaSimple) {
                    System.out.println("    Opciones: " + ((CategoriaSimple) p).getOpciones());
                } else if (p instanceof Ordinal) {
                    System.out.println("    Niveles: " + ((Ordinal) p).getOpciones());
                } else if (p instanceof CategoriaMultiple) {
                    CategoriaMultiple cm = (CategoriaMultiple) p;
                    System.out.println("    Opciones: " + cm.getOpciones());
                    System.out.println("    Máx selecciones: " + cm.getMaxSelecciones());
                } else if (p instanceof Numerica) {
                    Numerica n = (Numerica) p;
                    if (n.getMin() != null || n.getMax() != null) {
                        System.out.println("    Rango: [" + n.getMin() + ", " + n.getMax() + "]");
                    }
                } else if (p instanceof Libre) {
                    System.out.println("    Longitud máx: " + ((Libre) p).getLongitudMaxima());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void modificarPregunta(String idEncuesta) {
        try {
            Encuesta enc = ctrl.obtenerEncuesta(idEncuesta);
            List<Pregunta> preguntas = enc.getPreguntas();

            if (preguntas.isEmpty()) {
                System.out.println("\n⚠️  No hay preguntas para modificar");
                return;
            }

            // Mostrar preguntas
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
            System.out.println("   (Presiona ENTER para mantener el valor actual)\n");

            // Modificar enunciado
            System.out.print("Nuevo enunciado: ");
            String nuevoEnunciado = scanner.nextLine().trim();
            if (!nuevoEnunciado.isEmpty()) {
                p.setEnunciado(nuevoEnunciado);
            }

            // Modificar obligatoriedad
            System.out.print("¿Obligatoria? (s/n/ENTER para mantener): ");
            String obligatoriaStr = scanner.nextLine().trim();
            if (obligatoriaStr.equalsIgnoreCase("s")) {
                p.setObligatoria(true);
            } else if (obligatoriaStr.equalsIgnoreCase("n")) {
                p.setObligatoria(false);
            }

            // Modificar atributos específicos según tipo
            if (p instanceof Numerica) {
                Numerica n = (Numerica) p;
                System.out.print("Nuevo mínimo (ENTER para mantener [" + n.getMin() + "]): ");
                String minS = scanner.nextLine().trim();
                if (!minS.isEmpty()) {
                    n.setMin(minS.equals("null") ? null : Double.parseDouble(minS));
                }

                System.out.print("Nuevo máximo (ENTER para mantener [" + n.getMax() + "]): ");
                String maxS = scanner.nextLine().trim();
                if (!maxS.isEmpty()) {
                    n.setMax(maxS.equals("null") ? null : Double.parseDouble(maxS));
                }
            } else if (p instanceof Libre) {
                Libre l = (Libre) p;
                System.out.print("Nueva longitud máxima (ENTER para mantener [" + l.getLongitudMaxima() + "]): ");
                String longS = scanner.nextLine().trim();
                if (!longS.isEmpty()) {
                    l.setLongitudMaxima(Integer.parseInt(longS));
                }
            } else if (p instanceof CategoriaSimple) {
                System.out.print("¿Modificar opciones? (s/n): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
                    ((CategoriaSimple) p).setOpciones(leerOpciones());
                }
            } else if (p instanceof Ordinal) {
                System.out.print("¿Modificar niveles? (s/n): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
                    ((Ordinal) p).setOpciones(leerOpciones());
                }
            } else if (p instanceof CategoriaMultiple) {
                CategoriaMultiple cm = (CategoriaMultiple) p;
                System.out.print("¿Modificar opciones? (s/n): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
                    cm.setOpciones(leerOpciones());
                }
                System.out.print("Nuevo máximo de selecciones (ENTER para mantener [" + cm.getMaxSelecciones() + "]): ");
                String maxS = scanner.nextLine().trim();
                if (!maxS.isEmpty()) {
                    cm.setMaxSelecciones(Integer.parseInt(maxS));
                }
            }

            boolean ok = ctrl.modificarPregunta(idEncuesta, index, p);
            System.out.println(ok ? "✓ Pregunta modificada exitosamente" : "❌ Error al modificar");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void eliminarPreguntaInteractivo(String idEncuesta) {
        try {
            Encuesta enc = ctrl.obtenerEncuesta(idEncuesta);
            List<Pregunta> preguntas = enc.getPreguntas();

            if (preguntas.isEmpty()) {
                System.out.println("\n⚠️  No hay preguntas para eliminar");
                return;
            }

            // Mostrar preguntas
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
            Pregunta p = preguntas.get(index);

            System.out.print("¿Confirmar eliminación de '" + truncar(p.getEnunciado(), 40) + "'? (s/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
                System.out.println("⚠️  Cancelado");
                return;
            }

            boolean ok = ctrl.eliminarPregunta(idEncuesta, index);
            System.out.println(ok ? "✓ Pregunta eliminada" : "❌ Error al eliminar");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void añadirPreguntasInteractivo(String idEncuesta) {
        while (true) {
            System.out.println("\n--- TIPO DE PREGUNTA ---");
            System.out.println("1. Numérica");
            System.out.println("2. Texto libre");
            System.out.println("3. Categoría simple");
            System.out.println("4. Ordinal");
            System.out.println("5. Categoría múltiple");
            System.out.println("6. Terminar");
            System.out.print("Opción: ");

            int tipo = leerEntero();
            if (tipo == 6) break;

            System.out.print("Enunciado: ");
            String enunciado = scanner.nextLine().trim();
            System.out.print("¿Obligatoria? (s/n): ");
            boolean obligatoria = scanner.nextLine().equalsIgnoreCase("s");

            try {
                Pregunta p = crearPregunta(tipo, enunciado);
                if (p != null) {
                    p.setObligatoria(obligatoria);
                    boolean ok = ctrl.addPregunta(idEncuesta, p);
                    System.out.println(ok ? "✓ Añadida" : "❌ Error");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }
    private static Pregunta crearPregunta(int tipo, String enunciado) {
        try {
            return switch (tipo) {
                case 1 -> { // Numérica
                    System.out.print("Min (Enter=sin límite): ");
                    String minS = scanner.nextLine().trim();
                    System.out.print("Max (Enter=sin límite): ");
                    String maxS = scanner.nextLine().trim();
                    Double min = minS.isEmpty() ? null : Double.parseDouble(minS);
                    Double max = maxS.isEmpty() ? null : Double.parseDouble(maxS);
                    yield new Numerica(enunciado, min, max);
                }
                case 2 -> { // Texto
                    System.out.print("Longitud máxima (Enter=1000): ");
                    String longS = scanner.nextLine().trim();
                    int longMax = longS.isEmpty() ? 1000 : Integer.parseInt(longS);
                    yield new Libre(enunciado, longMax);
                }
                case 3 -> new CategoriaSimple(enunciado, leerOpciones()); // Cat simple
                case 4 -> new Ordinal(enunciado, leerOpciones()); // Ordinal
                case 5 -> { // Cat múltiple
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
        Set<String> ops = new HashSet<>();
        for (int i = 0; i < n; i++) {
            System.out.print("Opción " + (i + 1) + ": ");
            ops.add(scanner.nextLine().trim());
        }
        return ops;
    }


    // ==================== RESPONDER ENCUESTAS ====================
    private static void responderEncuesta() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            Encuesta enc = ctrl.obtenerEncuesta(id);
            if (enc == null || enc.getNumPreguntas() == 0) {
                System.out.println("❌ Encuesta inválida o sin preguntas");
                return;
            }

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║  RESPONDIENDO: " + truncar(enc.getTitulo(), 25));
            System.out.println("╚══════════════════════════════════════════╝");

            Map<String, Object> respuestas = new HashMap<>();
            for (Pregunta p : enc.getPreguntas()) {
                System.out.println("\n❓ " + p.getEnunciado());
                if (p.esObligatoria()) System.out.println("   (⚠️  Obligatoria)");

                Object resp = leerRespuesta(p);
                if (resp != null) {
                    respuestas.put(p.getId(), resp);
                }
            }

            ctrl.responderEncuesta(usuarioActualId, id, respuestas);
            System.out.println("\n✓ Encuesta respondida");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static Object leerRespuesta(Pregunta p) {
        if (!p.esObligatoria()) {
            System.out.println("   (Presiona ENTER para saltar)");
        }

        try {
            if (p instanceof Numerica) {
                Numerica n = (Numerica) p;
                System.out.print("➤ Número");
                if (n.getMin() != null && n.getMax() != null) {
                    System.out.print(" [" + n.getMin() + "-" + n.getMax() + "]");
                }
                System.out.print(": ");

                String input = scanner.nextLine().trim();
                if (input.isEmpty() && !p.esObligatoria()) {
                    return null; // Saltar pregunta
                }
                return Double.parseDouble(input);
            }

            if (p instanceof Libre) {
                System.out.print("➤ Texto: ");
                String texto = scanner.nextLine().trim();
                if (texto.isEmpty() && !p.esObligatoria()) {
                    return null;
                }
                return texto;
            }

            if (p instanceof CategoriaSimple) {
                return elegirOpcion(new ArrayList<>(((CategoriaSimple) p).getOpciones()),
                        !p.esObligatoria());
            }

            if (p instanceof Ordinal) {
                return elegirOpcion(new ArrayList<>(((Ordinal) p).getOpciones()),
                        !p.esObligatoria());
            }

            if (p instanceof CategoriaMultiple) {
                CategoriaMultiple cm = (CategoriaMultiple) p;
                System.out.println("   (Máx " + cm.getMaxSelecciones() + " opciones)");
                return elegirOpciones(new ArrayList<>(cm.getOpciones()),
                        cm.getMaxSelecciones(),
                        !p.esObligatoria());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        return null;
    }
    private static String elegirOpcion(List<String> opciones, boolean permitirSaltar) {
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("   " + (i + 1) + ". " + opciones.get(i));
        }
        if (permitirSaltar) {
            System.out.println("   0. Saltar pregunta");
        }
        System.out.print("➤ Elige: ");

        String input = scanner.nextLine().trim();

        if (permitirSaltar && (input.isEmpty() || input.equals("0"))) {
            return null;
        }

        try {
            int idx = Integer.parseInt(input) - 1;
            return (idx >= 0 && idx < opciones.size()) ? opciones.get(idx) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Set<String> elegirOpciones(List<String> opciones, int max, boolean permitirSaltar) {
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("   " + (i + 1) + ". " + opciones.get(i));
        }
        if (permitirSaltar) {
            System.out.println("   (Presiona ENTER para saltar)");
        }
        System.out.print("➤ Números separados por comas: ");

        String input = scanner.nextLine().trim();

        if (permitirSaltar && input.isEmpty()) {
            return null;
        }

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

    private static void verMisRespuestas() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            List<Respuesta> resp = ctrl.obtenerRespuestasUsuario(usuarioActualId, id);
            if (resp.isEmpty()) {
                System.out.println("\n⚠️  No has respondido esta encuesta");
                return;
            }

            Encuesta enc = ctrl.obtenerEncuesta(id);
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       TUS RESPUESTAS                     ║");
            System.out.println("╚══════════════════════════════════════════╝");

            for (Respuesta r : resp) {
                Pregunta p = buscarPregunta(enc, r.getIdPregunta());
                if (p != null) {
                    System.out.println("\n❓ " + p.getEnunciado());
                    System.out.println("   ➤ " + formatearValor(r.getValor()));
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verTodasLasRespuestas() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            Encuesta enc = ctrl.obtenerEncuesta(id);
            List<UsuarioRespondedor> usuarios = ctrl.obtenerUsuariosQueRespondieron(id);

            if (usuarios.isEmpty()) {
                System.out.println("\n⚠️  Nadie ha respondido esta encuesta");
                return;
            }

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       TODAS LAS RESPUESTAS               ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("Encuesta: " + enc.getTitulo());
            System.out.println("Total de respuestas: " + usuarios.size());

            for (Usuario u : usuarios) {
                System.out.println("\n👤 " + u.getNombre() + " (ID: " + u.getId() + ")");
                System.out.println("─".repeat(50));

                List<Respuesta> respuestas = ctrl.obtenerRespuestasUsuario(u.getId(), id);
                for (Respuesta r : respuestas) {
                    Pregunta p = buscarPregunta(enc, r.getIdPregunta());
                    if (p != null) {
                        System.out.println("   • " + truncar(p.getEnunciado(), 35));
                        System.out.println("     → " + formatearValor(r.getValor()));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
// ========== EXPORTAR CSV ==========
private static void exportarCSV() {
    System.out.println("\n╔═══════════════════════════════════════════╗");
    System.out.println("║       EXPORTAR ENCUESTA A CSV             ║");
    System.out.println("╚═══════════════════════════════════════════╝");

    String id = pedirIdEncuesta();
    if (id == null) return;

    try {
        // Mostrar información previa
        ControladorExportacion.InfoExportacion info =
                ctrl.obtenerInfoExportacion(id);

        if (info == null) {
            System.out.println("❌ Encuesta no encontrada");
            return;
        }

        if (info.numeroUsuarios == 0) {
            System.out.println("❌ La encuesta no tiene respuestas para exportar");
            return;
        }

        System.out.println("\n📊 Información de la exportación:");
        System.out.println(info);  // Usa el toString()

        // Pedir nombre del archivo
        System.out.print("\nNombre del archivo (sin extensión): ");
        String nombreArchivo = scanner.nextLine().trim();

        if (nombreArchivo.isEmpty()) {
            nombreArchivo = "encuesta_" + id;
        }

        String rutaArchivo = "./data/" + nombreArchivo + ".csv";

        // Confirmar exportación
        System.out.print("\n¿Exportar a '" + rutaArchivo + "'? (s/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println("⚠️  Exportación cancelada");
            return;
        }

        // Exportar
        System.out.println("\n⏳ Exportando datos...");
        ctrl.exportarEncuesta(id, rutaArchivo);

        System.out.println("\n✅ ¡Exportación exitosa!");
        System.out.println("📁 Archivo guardado en: " + rutaArchivo);
        System.out.println("   • Formato: CSV estándar");
        System.out.println("   • Primera fila: nombres de columnas");
        System.out.println("   • Primera columna: IDs de usuario");

    } catch (java.io.IOException e) {
        System.out.println("❌ Error al exportar: " + e.getMessage());
    } catch (Exception e) {
        System.out.println("❌ Error inesperado: " + e.getMessage());
        e.printStackTrace();
    }
}
    // ==================== ESTADÍSTICAS ====================

    private static void verEstadisticas() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            Map<String, Object> stats = ctrl.obtenerEstadisticasEncuesta(id);
            if (stats == null) {
                System.out.println("❌ Encuesta no encontrada");
                return;
            }

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       ESTADÍSTICAS                       ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("📊 " + stats.get("titulo"));
            System.out.println("📊 Preguntas: " + stats.get("numPreguntas"));
            System.out.println("📊 Respuestas: " + stats.get("numRespuestas"));

            @SuppressWarnings("unchecked")
            List<Usuario> users = (List<Usuario>) stats.get("usuarios");
            if (users != null && !users.isEmpty()) {
                System.out.println("\n👥 Respondieron:");
                users.forEach(u -> System.out.println("   - " + u.getNombre()));
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ==================== ANÁLISIS Y RESULTADOS ====================

    private static void ejecutarClustering() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        System.out.println("\n1. K-Means\n2. K-Means++\n3. K-Medoids");
        System.out.print("Algoritmo: ");
        int alg = leerEntero();

        System.out.print("Número máximo de clusters: ");
        int k = leerEntero();

        try {
            System.out.println("\n⏳ Ejecutando clustering con múltiples K...");
            String algoritmo;
            switch (alg) {
                case 1 -> algoritmo = "KMEANS";
                case 2 -> algoritmo = "KMEANS++";
                case 3 -> algoritmo = "KMEDOIDS";
                default -> {
                    System.out.println("❌ Algoritmo inválido");
                    return;
                }
            }

            List<ResultadoClustering> resultados = ctrl.ejecutarClustering(id, algoritmo, k, 100);

            if (resultados.isEmpty()) {
                System.out.println("❌ No se pudieron generar resultados");
                return;
            }

            // Encontrar el mejor
            ResultadoClustering mejor = resultados.get(0);
            for (ResultadoClustering res : resultados) {
                if (res.getSilhouette() > mejor.getSilhouette()) {
                    mejor = res;
                }
            }

            // Mostrar tabla comparativa
            mostrarTablaComparativa(resultados, mejor.getK());

            // Mostrar detalles del mejor
            mostrarDetallesClustering(mejor);

            System.out.println("\n💾 Resultados guardados en memoria");
            System.out.println("   Puedes verlos en 'Ver historial de clustering'");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void verHistorialClustering() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            List<ResultadoClustering> historial = ctrl.obtenerHistorialClustering(id);

            if (historial.isEmpty()) {
                System.out.println("\n⚠️  No hay resultados de clustering para esta encuesta");
                System.out.println("   Ejecuta primero un clustering desde la opción 3");
                return;
            }

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       HISTORIAL DE CLUSTERING            ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("Total de ejecuciones guardadas: " + historial.size());

            if (ctrl.existenResultadosGuardados(id)) {
                System.out.println("📊 Fuente: PersistenciaClustering (memoria)");
            }
            System.out.println();

            // Mostrar tabla de resultados
            System.out.println("  #  │ Algoritmo    │  K  │ Silhouette │  Inercia");
            System.out.println("─────┼──────────────┼─────┼────────────┼──────────");

            for (int i = 0; i < historial.size(); i++) {
                ResultadoClustering res = historial.get(i);
                System.out.printf("%4d │ %-12s │ %3d │   %.3f    │  %.2f%n",
                        (i + 1),
                        res.getAlgoritmo(),
                        res.getK(),
                        res.getSilhouette(),
                        res.getInercia());
            }

            System.out.print("\n¿Ver detalles de alguna ejecución? (número/n): ");
            String input = scanner.nextLine().trim();

            if (!input.equalsIgnoreCase("n")) {
                try {
                    int idx = Integer.parseInt(input) - 1;
                    if (idx >= 0 && idx < historial.size()) {
                        mostrarDetallesClustering(historial.get(idx));
                    } else {
                        System.out.println("❌ Número fuera de rango");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Número inválido");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void limpiarHistorialClustering() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            // Verificar si hay resultados
            if (!ctrl.existenResultadosGuardados(id)) {
                System.out.println("\n⚠️  No hay historial para limpiar");
                return;
            }

            // Mostrar cuántos hay
            List<ResultadoClustering> historial = ctrl.obtenerHistorialClustering(id);

            System.out.println("\n⚠️  ADVERTENCIA: Vas a eliminar " + historial.size() +
                    " resultado(s) de clustering");
            System.out.println("   Esto incluye resultados de PersistenciaClustering y de la Encuesta");
            System.out.print("¿Confirmar limpieza? (s/n): ");

            if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
                System.out.println("⚠️  Cancelado");
                return;
            }

            boolean ok = ctrl.limpiarHistorialClustering(id);

            if (ok) {
                System.out.println("✓ Historial limpiado exitosamente");
                System.out.println("  • PersistenciaClustering: limpiada");
                System.out.println("  • Historial de Encuesta: limpiado");
            } else {
                System.out.println("❌ Error al limpiar");
            }

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void verMejorResultadoGuardado() {
        String id = pedirIdEncuesta();
        if (id == null) return;

        try {
            if (!ctrl.existenResultadosGuardados(id)) {
                System.out.println("\n⚠️  No hay resultados guardados para esta encuesta");
                return;
            }

            ResultadoClustering mejor = ctrl.obtenerMejorResultadoGuardado(id);

            if (mejor == null) {
                System.out.println("❌ Error al obtener el mejor resultado");
                return;
            }

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       MEJOR RESULTADO GUARDADO           ║");
            System.out.println("╚══════════════════════════════════════════╝");

            mostrarDetallesClustering(mejor);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    /**
     * Muestra tabla comparativa de todos los K probados
     */
    private static void mostrarTablaComparativa(List<ResultadoClustering> resultados, int kOptimo) {
        System.out.println("\n╔═══════════════════════════════════════════════╗");
        System.out.println("║       COMPARACIÓN DE DIFERENTES K's           ║");
        System.out.println("╚═══════════════════════════════════════════════╝");
        System.out.println("  K  │  Silhouette  │  Inercia   │  Iteraciones");
        System.out.println("─────┼──────────────┼────────────┼──────────────");

        for (ResultadoClustering res : resultados) {
            int k = res.getK();
            String marca = (k == kOptimo) ? " ⭐ ÓPTIMO" : "";

            System.out.printf("%4d │    %.3f     │   %.2f    │     %3d     %s%n",
                    k,
                    res.getSilhouette(),
                    res.getInercia(),
                    res.getNumIteraciones(),
                    marca);
        }

        System.out.println("\n✅ K óptimo seleccionado: " + kOptimo +
                " (mayor Silhouette Score)");
    }

    private static void mostrarDetallesClustering(ResultadoClustering res) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       RESULTADO DEL CLUSTERING           ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("🔹 Algoritmo: " + res.getAlgoritmo());
        System.out.println("🔹 K óptimo: " + res.getK());
        System.out.println("🔹 Iteraciones: " + res.getNumIteraciones());
        System.out.println("📊 Silhouette Score: " + String.format("%.3f", res.getSilhouette()));
        System.out.println("📊 Inercia: " + String.format("%.2f", res.getInercia()));

        // Mostrar usuarios por cluster
        System.out.println("\n🔹 Distribución de Clusters:");
        List<List<String>> usuariosPorGrupo = res.getUsuariosPorGrupo();

        for (int i = 0; i < usuariosPorGrupo.size(); i++) {
            List<String> usuariosCluster = usuariosPorGrupo.get(i);
            System.out.println("\n   📍 Cluster " + (i + 1) + " (" + usuariosCluster.size() + " usuarios):");

            for (String idUsuario : usuariosCluster) {
                Usuario u = ctrl.obtenerUsuario(idUsuario);
                System.out.println("      • " + (u != null ? u.getNombre() : idUsuario));
            }
        }

        // Opción para ver centros
        System.out.print("\n¿Deseas ver los centros de los clusters? (s/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            mostrarCentrosClusters(res);
        }
    }

    private static void mostrarCentrosClusters(ResultadoClustering res) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       CENTROS DE CLUSTERS                ║");
        System.out.println("╚══════════════════════════════════════════╝");

        Object[][] centers = res.getCenters();
        for (int i = 0; i < centers.length; i++) {
            System.out.println("\n🎯 Centro del Cluster " + (i + 1) + ":");
            Object[] centro = centers[i];

            for (int j = 0; j < centro.length; j++) {
                System.out.println("   P" + (j + 1) + ": " + formatearValor(centro[j]));
            }
        }
    }
// ========== IMPORTAR CSV ==========

    private static void importarCSV() {
        System.out.println("\n╔═══════════════════════════════════════════╗");
        System.out.println("║       IMPORTAR ENCUESTA DESDE CSV        ║");
        System.out.println("╚═══════════════════════════════════════════╝");

        System.out.print("Ruta del archivo CSV: ");
        String rutaArchivo = scanner.nextLine().trim();

        try {
            // 1. Leer y mostrar información previa
            System.out.println("\n⏳ Analizando archivo...");

            LectorCSV.DatosCSV info = ctrl.getLectorCSV().leerConEncabezados(rutaArchivo);

            System.out.println("✓ Archivo válido");
            System.out.println("\n📊 Información del CSV:");
            System.out.println(info);  // Usa el toString()

            // 2. Confirmar importación
            System.out.print("\n¿Deseas continuar con la importación? (s/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
                System.out.println("⚠️  Importación cancelada");
                return;
            }

            // 3. Pedir título y descripción
            System.out.print("\nTítulo de la encuesta (Enter para usar 'Dataset Importado'): ");
            String titulo = scanner.nextLine().trim();
            if (titulo.isEmpty()) {
                titulo = "Dataset Importado";
            }

            System.out.print("Descripción (Enter para descripción por defecto): ");
            String descripcion = scanner.nextLine().trim();
            if (descripcion.isEmpty()) {
                descripcion = "Importado desde archivo CSV";
            }

            // 4. Importar
            System.out.println("\n⏳ Importando datos...");
            System.out.println("   ℹ️  Los tipos de pregunta se inferirán automáticamente");

            Encuesta encuesta = ctrl.importarCSV(rutaArchivo, titulo, descripcion);

            // 5. Mostrar resultado
            System.out.println("\n✅ ¡Importación exitosa!");
            System.out.println("╔═══════════════════════════════════════════╗");
            System.out.println("║       ENCUESTA IMPORTADA                  ║");
            System.out.println("╚═══════════════════════════════════════════╝");
            System.out.println("📋 ID: " + encuesta.getId());
            System.out.println("📋 Título: " + encuesta.getTitulo());
            System.out.println("📋 Preguntas: " + encuesta.getNumPreguntas());


            List<UsuarioRespondedor> usuarios = ctrl.obtenerUsuariosQueRespondieron(encuesta.getId());
            System.out.println("👥 Usuarios: " + usuarios.size());


            System.out.println("\n📊 Tipos de preguntas inferidas:");
            List<Pregunta> preguntas = encuesta.getPreguntas();
            for (int i = 0; i < preguntas.size(); i++) {
                Pregunta p = preguntas.get(i);
                String tipo = obtenerTipoPreguntaAbreviado(p);
                System.out.println("   " + (i+1) + ". [" + tipo + "] " + p.getEnunciado());
            }

        } catch (java.io.FileNotFoundException e) {
            System.out.println("❌ Archivo no encontrado: " + e.getMessage());
        } catch (java.io.IOException e) {
            System.out.println("❌ Error al leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());

        }
    }

// ========== UTILIDAD ==========

    private static String obtenerTipoPreguntaAbreviado(Pregunta p) {
        if (p instanceof Numerica) return "NUM";
        if (p instanceof Libre) return "TXT";
        if (p instanceof CategoriaSimple) return "CAT";
        if (p instanceof Ordinal) return "ORD";
        if (p instanceof CategoriaMultiple) return "MUL";
        return "???";
    }

    private static String pedirIdEncuesta() {
        listarEncuestas();
        System.out.print("\nID de encuesta: ");
        String id = scanner.nextLine().trim();
        return id.isEmpty() ? null : id;
    }

    private static Pregunta buscarPregunta(Encuesta enc, String idPregunta) {
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
        if (valor == null) return "null";
        if (valor instanceof Set) {
            Set<?> set = (Set<?>) valor;
            if (set.isEmpty()) return "null";
            return set.toString();
        }
        if (valor instanceof Double) {
            return String.format("%.2f", (Double) valor);
        }
        return valor.toString();
    }

    private static String truncar(String str, int max) {
        if (str == null) return "";
        return str.length() <= max ? str : str.substring(0, max - 2) + "..";
    }

    private static int leerEntero() {
        while (!scanner.hasNextInt()) {
            System.out.print("⚠️  Número válido: ");
            scanner.next();
        }
        int num = scanner.nextInt();
        scanner.nextLine();  // ← Consume el '\n'
        return num;
    }

    private static void consumirLinea() {
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}