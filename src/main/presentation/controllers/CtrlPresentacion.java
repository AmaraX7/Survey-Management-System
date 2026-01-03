package main.presentation.controllers;

import javafx.scene.Scene;
import javafx.stage.Stage;
import main.domain.classes.*;
import main.domain.controllers.CtrlDominio;
import main.presentation.views.*;

import java.util.*;

/**
 * Controlador de presentación (JavaFX) responsable de:
 * <ul>
 *   <li>Gestionar la navegación entre escenas/vistas.</li>
 *   <li>Mantener el estado de sesión del usuario (id actual y rol administrador).</li>
 * </ul>
 * <p>
 * Este controlador actúa como “puente” entre la capa de presentación y el dominio
 * mediante un {@link CtrlDominio}. Las vistas nunca deberían invocar directamente
 * lógica de dominio compleja: lo hacen a través de este controlador.
 */
public class CtrlPresentacion {

    /** Escenario principal de la aplicación (ventana base). */
    private final Stage primaryStage;

    /** Controlador de dominio con la lógica de negocio. */
    private final CtrlDominio ctrlDominio;

    // ==================== Estado de sesión ====================

    /** Identificador del usuario actualmente autenticado; {@code null} si no hay sesión iniciada. */
    private String usuarioActualId;

    /** Indica si el usuario actual tiene rol de administrador. */
    private boolean esAdmin;

    // ==================== Construcción / Arranque ====================

    /**
     * Crea el controlador de presentación asociado a un {@link Stage}.
     * <p>
     * Inicializa el controlador de dominio y el estado de sesión en modo “no autenticado”.
     *
     * @param primaryStage {@link Stage} principal sobre el que se muestran las escenas
     * @throws IllegalArgumentException si {@code primaryStage} es {@code null}
     */
    public CtrlPresentacion(Stage primaryStage) {
        if (primaryStage == null) {
            throw new IllegalArgumentException("primaryStage no puede ser null");
        }
        this.primaryStage = primaryStage;
        this.ctrlDominio = new CtrlDominio();
        this.usuarioActualId = null;
        this.esAdmin = false;

        configurarVentana();
    }

    /**
     * Configura propiedades visuales básicas del {@link Stage} principal:
     * título y dimensiones mínimas.
     */
    private void configurarVentana() {
        primaryStage.setTitle("Sistema de Gestión de Encuestas");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
    }

    /**
     * Inicia la aplicación mostrando el menú inicial y haciendo visible la ventana principal.
     */
    public void iniciar() {
        mostrarMenuInicial();
        primaryStage.show();
    }

    // ==================== NAVEGACIÓN PRINCIPAL ====================

    /**
     * Navega al menú inicial (pantalla de entrada).
     */
    public void mostrarMenuInicial() {
        MenuInicialView view = new MenuInicialView(this);
        cambiarEscena(view.getScene(), "Menú Inicial");
    }

    /**
     * Navega al menú principal correspondiente al rol del usuario.
     * <p>
     * Además, establece el estado de sesión local.
     *
     * @param userId   id del usuario autenticado
     * @param esAdmin  {@code true} si el usuario es administrador; {@code false} si es respondedor
     */
    public void mostrarMenuPrincipal(String userId, boolean esAdmin) {
        this.usuarioActualId = userId;
        this.esAdmin = esAdmin;

        if (esAdmin) {
            MenuAdminView view = new MenuAdminView(this);
            cambiarEscena(view.getScene(), "Menú Administrador");
        } else {
            MenuRespondedorView view = new MenuRespondedorView(this);
            cambiarEscena(view.getScene(), "Menú Respondedor");
        }
    }

    // ==================== CU: USUARIO GENERAL ====================

    /**
     * Muestra la vista de registro para administradores.
     */
    public void mostrarRegistroAdmin() {
        RegistroView view = new RegistroView(this, true);
        cambiarEscena(view.getScene(), "Registro - Administrador");
    }

    /**
     * Muestra la vista de registro para respondedores.
     */
    public void mostrarRegistroRespondedor() {
        RegistroView view = new RegistroView(this, false);
        cambiarEscena(view.getScene(), "Registro - Respondedor");
    }

    /**
     * Muestra la vista de inicio de sesión.
     */
    public void mostrarLogin() {
        LoginView view = new LoginView(this);
        cambiarEscena(view.getScene(), "Iniciar Sesión");
    }

    /**
     * Cierra la sesión actual y vuelve al menú inicial.
     * <p>
     * Limpia el identificador de usuario y el flag de administrador.
     */
    public void cerrarSesion() {
        this.usuarioActualId = null;
        this.esAdmin = false;
        mostrarMenuInicial();
    }

    // ==================== CU: EXPLORAR ENCUESTAS ====================

    /**
     * Punto de entrada del caso de uso “Explorar encuestas”.
     * Actualmente delega en {@link #mostrarListarEncuestas()}.
     */
    public void mostrarExplorarEncuestas() {
        mostrarListarEncuestas();
    }

    /**
     * Muestra la vista con el listado de encuestas disponibles.
     */
    public void mostrarListarEncuestas() {
        ListarEncuestasView view = new ListarEncuestasView(this);
        cambiarEscena(view.getScene(), "Explorar Encuestas");
    }

    /**
     * Muestra la vista con el detalle de una encuesta concreta.
     *
     * @param idEncuesta id de la encuesta a consultar
     */
    public void mostrarDetalleEncuesta(String idEncuesta) {
        DetalleEncuestaView view = new DetalleEncuestaView(this, idEncuesta);
        cambiarEscena(view.getScene(), "Detalle de Encuesta");
    }

    // ==================== CU: GESTIÓN DE ENCUESTAS (ADMIN) ====================

    /**
     * Muestra la vista de gestión de encuestas (administración).
     */
    public void mostrarGestionEncuestas() {
        GestionEncuestasView view = new GestionEncuestasView(this);
        cambiarEscena(view.getScene(), "Gestión de Encuestas");
    }

    /**
     * Muestra la vista de creación de encuestas (administración).
     */
    public void mostrarCrearEncuesta() {
        CrearEncuestaView view = new CrearEncuestaView(this);
        cambiarEscena(view.getScene(), "Crear Encuesta");
    }

    /**
     * Muestra la vista para modificar una encuesta existente (administración).
     *
     * @param idEncuesta id de la encuesta a modificar
     */
    public void mostrarGestionarEncuesta(String idEncuesta) {
        GestionarEncuestaView view = new GestionarEncuestaView(this, idEncuesta);
        cambiarEscena(view.getScene(), "Modificar Encuesta");
    }

    // ==================== CU: GESTIONAR PREGUNTAS (ADMIN) ====================

    /**
     * Muestra el menú principal de gestión de preguntas (administración).
     */
    public void mostrarGestionarPreguntasMenu() {
        GestionarPreguntasMenuView view = new GestionarPreguntasMenuView(this);
        cambiarEscena(view.getScene(), "Gestionar Preguntas");
    }

    /**
     * Muestra el detalle de preguntas de una encuesta concreta (administración).
     *
     * @param idEncuesta id de la encuesta a gestionar
     */
    public void mostrarGestionarPreguntasDetalle(String idEncuesta) {
        GestionarPreguntasView view = new GestionarPreguntasView(this, idEncuesta);
        cambiarEscena(view.getScene(), "Gestionar Preguntas - Detalle");
    }

    /**
     * Método antiguo para navegar a la gestión de preguntas.
     * <p>
     * Conservado por compatibilidad: delega en {@link #mostrarGestionarPreguntasDetalle(String)}.
     *
     * @param idEncuesta id de la encuesta a gestionar
     * @deprecated usar {@link #mostrarGestionarPreguntasDetalle(String)}
     */
    @Deprecated
    public void mostrarGestionarPreguntas(String idEncuesta) {
        mostrarGestionarPreguntasDetalle(idEncuesta);
    }

    // ==================== CU: ANÁLISIS Y RESULTADOS (ADMIN) ====================

    /**
     * Muestra la vista de análisis y resultados (administración).
     */
    public void mostrarAnalisisResultados() {
        AnalisisResultadosView view = new AnalisisResultadosView(this);
        cambiarEscena(view.getScene(), "Análisis y Resultados");
    }

    /**
     * Muestra la vista para ver respuestas de una encuesta (administración).
     *
     * @param idEncuesta id de la encuesta
     */
    public void mostrarVerRespuestas(String idEncuesta) {
        VerRespuestasView view = new VerRespuestasView(this, idEncuesta);
        cambiarEscena(view.getScene(), "Ver Respuestas");
    }

    /**
     * Muestra la vista de estadísticas de una encuesta (administración).
     *
     * @param idEncuesta id de la encuesta
     */
    public void mostrarEstadisticas(String idEncuesta) {
        EstadisticasView view = new EstadisticasView(this, idEncuesta);
        cambiarEscena(view.getScene(), "Estadísticas");
    }

    /**
     * Muestra la vista de clustering de una encuesta (administración).
     *
     * @param idEncuesta id de la encuesta
     */
    public void mostrarClustering(String idEncuesta) {
        ClusteringView view = new ClusteringView(this, idEncuesta);
        cambiarEscena(view.getScene(), "Análisis de Clustering");
    }

    // ==================== CU: IMPORTAR/EXPORTAR (ADMIN) ====================

    /**
     * Muestra la vista de menú de importar/exportar (administración).
     */
    public void mostrarImportarExportar() {
        ImportarExportarView view = new ImportarExportarView(this);
        cambiarEscena(view.getScene(), "Importar/Exportar");
    }

    /**
     * Muestra la vista para importar encuestas desde CSV (administración).
     */
    public void mostrarImportarCSV() {
        ImportarCSVView view = new ImportarCSVView(this);
        cambiarEscena(view.getScene(), "Importar CSV");
    }

    /**
     * Muestra la vista para exportar encuestas a CSV (administración).
     */
    public void mostrarExportarCSV() {
        ExportarCSVView view = new ExportarCSVView(this);
        cambiarEscena(view.getScene(), "Exportar CSV");
    }

    // ==================== CU: RESPONDER ENCUESTA (RESPONDEDOR) ====================

    /**
     * Muestra la vista para responder una encuesta.
     *
     * @param idEncuesta id de la encuesta
     */
    public void mostrarResponderEncuesta(String idEncuesta) {
        ResponderEncuestaView view = new ResponderEncuestaView(this, idEncuesta);
        cambiarEscena(view.getScene(), "Responder Encuesta");
    }

    // ==================== MÉTODOS PARA LAS VISTAS (SOLO PRIMITIVOS/STRINGS) ====================

    /**
     * Obtiene el nombre del usuario actualmente autenticado.
     *
     * @return nombre del usuario; si no hay sesión o no se encuentra, devuelve {@code "Usuario"}
     */
    public String obtenerNombreUsuarioActual() {
        if (usuarioActualId == null) return "Usuario";

        Usuario usuario = ctrlDominio.obtenerUsuario(usuarioActualId);
        return usuario != null ? usuario.getNombre() : "Usuario";
    }

    /**
     * Registra un nuevo usuario (administrador o respondedor).
     * <p>
     * Devuelve {@code "OK"} en caso de éxito, o un mensaje textual si se produce un error
     * de validación o una excepción.
     *
     * @param id       identificador único de usuario
     * @param nombre   nombre visible del usuario
     * @param password contraseña en claro introducida por el usuario
     * @param esAdmin  {@code true} para crear administrador; {@code false} para respondedor
     * @return {@code "OK"} si se registró correctamente, o un mensaje de error
     */
    public String registrarUsuario(String id, String nombre, String password, boolean esAdmin) {
        try {
            if (ctrlDominio.existeUsuario(id)) {
                return "El ID de usuario '" + id + "' ya está en uso.\n\n" +
                        "Por favor, elige un ID diferente.";
            }

            if (esAdmin) {
                ctrlDominio.crearUsuarioAdmin(id, nombre, password);
            } else {
                ctrlDominio.crearUsuarioRespondedor(id, nombre, password);
            }

            return "OK";

        } catch (IllegalArgumentException ex) {
            return "Error de validación: " + ex.getMessage();
        } catch (Exception ex) {
            return "Error al registrar usuario: " + ex.getMessage();
        }
    }

    /**
     * Verifica credenciales de login.
     *
     * @param id       id del usuario
     * @param password contraseña en claro introducida por el usuario
     * @return {@code "OK"} si las credenciales son correctas; en caso contrario, un mensaje de error
     */
    public String verificarLogin(String id, String password) {
        try {
            Usuario usuario = ctrlDominio.obtenerUsuario(id);

            if (usuario == null) {
                return "Usuario no encontrado";
            }

            if (!usuario.verificarPassword(password)) {
                return "Contraseña incorrecta";
            }

            return "OK";

        } catch (Exception ex) {
            return "Error al verificar credenciales: " + ex.getMessage();
        }
    }

    /**
     * Determina si un usuario es administrador.
     *
     * @param id id del usuario
     * @return {@code true} si el usuario es administrador; {@code false} en caso contrario
     */
    public boolean esUsuarioAdmin(String id) {
        return ctrlDominio.esAdmin(id);
    }

    /**
     * Lista todas las encuestas disponibles para la vista.
     * <p>
     * Cada elemento contiene:
     * <ul>
     *   <li>{@code id}</li>
     *   <li>{@code titulo}</li>
     *   <li>{@code descripcion}</li>
     *   <li>{@code numPreguntas}</li>
     * </ul>
     *
     * @return lista de mapas con datos básicos de las encuestas
     */
    public List<Map<String, Object>> listarEncuestas() {
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Encuesta enc : encuestas) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("id", enc.getId());
            datos.put("titulo", enc.getTitulo());
            datos.put("descripcion", enc.getDescripcion());
            datos.put("numPreguntas", enc.getNumPreguntas());
            resultado.add(datos);
        }

        return resultado;
    }

    /**
     * Obtiene el detalle de una encuesta.
     * <p>
     * El mapa resultante contiene:
     * <ul>
     *   <li>{@code id}</li>
     *   <li>{@code titulo}</li>
     *   <li>{@code descripcion}</li>
     *   <li>{@code numPreguntas}</li>
     *   <li>{@code preguntas}: lista con información básica (id, enunciado, tipo, obligatoria)</li>
     * </ul>
     *
     * @param idEncuesta id de la encuesta
     * @return mapa con el detalle; {@code null} si la encuesta no existe
     */
    public Map<String, Object> obtenerDetalleEncuesta(String idEncuesta) {
        Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);
        if (encuesta == null) return null;

        Map<String, Object> datos = new HashMap<>();
        datos.put("id", encuesta.getId());
        datos.put("titulo", encuesta.getTitulo());
        datos.put("descripcion", encuesta.getDescripcion());
        datos.put("numPreguntas", encuesta.getNumPreguntas());

        // Lista de preguntas con información básica
        List<Map<String, String>> preguntas = new ArrayList<>();
        for (Pregunta p : encuesta.getPreguntas()) {
            Map<String, String> preguntaData = new HashMap<>();
            preguntaData.put("id", p.getId());
            preguntaData.put("enunciado", p.getEnunciado());
            preguntaData.put("tipo", p.getTipoPregunta().name());
            preguntaData.put("obligatoria", String.valueOf(p.esObligatoria()));
            preguntas.add(preguntaData);
        }
        datos.put("preguntas", preguntas);

        return datos;
    }

    /**
     * Crea una nueva encuesta.
     *
     * @param titulo      título de la encuesta
     * @param descripcion descripción de la encuesta
     * @return id de la encuesta creada; {@code null} si ocurre un error
     */
    public String crearEncuesta(String titulo, String descripcion) {
        try {
            Encuesta encuesta = ctrlDominio.crearEncuesta(titulo, descripcion);
            return encuesta.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Modifica el título y descripción de una encuesta existente.
     *
     * @param idEncuesta   id de la encuesta
     * @param titulo       nuevo título
     * @param descripcion  nueva descripción
     * @return {@code true} si se modificó correctamente; {@code false} en caso contrario
     */
    public boolean modificarEncuesta(String idEncuesta, String titulo, String descripcion) {
        return ctrlDominio.modificarEncuesta(idEncuesta, titulo, descripcion);
    }

    /**
     * Elimina una encuesta.
     *
     * @param idEncuesta id de la encuesta
     * @return {@code true} si se eliminó correctamente; {@code false} en caso contrario
     */
    public boolean eliminarEncuesta(String idEncuesta) {
        return ctrlDominio.eliminarEncuesta(idEncuesta);
    }

    /**
     * Obtiene datos de una encuesta preparados para la vista de respuestas.
     * <p>
     * Devuelve un mapa con:
     * <ul>
     *   <li>{@code titulo}</li>
     *   <li>{@code numUsuarios}</li>
     *   <li>{@code usuarios}: lista de mapas con id, nombre y lista de respuestas (pregunta/valor)</li>
     * </ul>
     *
     * @param idEncuesta id de la encuesta
     * @return mapa con datos; {@code null} si la encuesta no existe
     */
    public Map<String, Object> obtenerDatosEncuesta(String idEncuesta) {
        Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);
        if (encuesta == null) return null;

        List<UsuarioRespondedor> usuarios = ctrlDominio.obtenerUsuariosQueRespondieron(idEncuesta);

        Map<String, Object> datos = new HashMap<>();
        datos.put("titulo", encuesta.getTitulo());
        datos.put("numUsuarios", usuarios.size());

        List<Map<String, Object>> usuariosDatos = new ArrayList<>();
        for (UsuarioRespondedor usuario : usuarios) {
            Map<String, Object> usuarioData = new HashMap<>();
            usuarioData.put("id", usuario.getId());
            usuarioData.put("nombre", usuario.getNombre());

            List<Respuesta> respuestas = ctrlDominio.obtenerRespuestasUsuario(usuario.getId(), idEncuesta);
            List<Map<String, String>> respuestasDatos = new ArrayList<>();

            for (Respuesta r : respuestas) {
                Pregunta p = buscarPregunta(encuesta, r.getIdPregunta());
                if (p != null) {
                    Map<String, String> respuestaData = new HashMap<>();
                    respuestaData.put("pregunta", p.getEnunciado());
                    respuestaData.put("valor", formatearValor(r.getValor()));
                    respuestasDatos.add(respuestaData);
                }
            }

            usuarioData.put("respuestas", respuestasDatos);
            usuariosDatos.add(usuarioData);
        }

        datos.put("usuarios", usuariosDatos);
        return datos;
    }

    /**
     * Obtiene datos de una encuesta preparados para la vista de responder encuesta.
     * <p>
     * Devuelve un mapa con:
     * <ul>
     *   <li>{@code titulo}</li>
     *   <li>{@code preguntas}: lista de mapas con id, enunciado, obligatoria, tipo
     *       y campos adicionales según el tipo concreto.</li>
     * </ul>
     *
     * @param idEncuesta id de la encuesta
     * @return mapa preparado para la vista; {@code null} si no existe o no tiene preguntas
     */
    public Map<String, Object> obtenerDatosEncuestaParaResponder(String idEncuesta) {
        Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);
        if (encuesta == null || encuesta.getNumPreguntas() == 0) return null;

        Map<String, Object> datos = new HashMap<>();
        datos.put("titulo", encuesta.getTitulo());

        List<Map<String, Object>> preguntasDatos = new ArrayList<>();
        for (Pregunta p : encuesta.getPreguntas()) {
            Map<String, Object> preguntaData = new HashMap<>();
            preguntaData.put("id", p.getId());
            preguntaData.put("enunciado", p.getEnunciado());
            preguntaData.put("obligatoria", p.esObligatoria());
            preguntaData.put("tipo", p.getTipoPregunta().name());

            // Datos específicos según tipo
            if (p instanceof Numerica) {
                Numerica n = (Numerica) p;
                preguntaData.put("min", n.getMin());
                preguntaData.put("max", n.getMax());
            }

            if (p instanceof Libre) {
                Libre l = (Libre) p;
                preguntaData.put("longitudMaxima", l.getLongitudMaxima());
            }

            if (p instanceof CategoriaSimple) {
                CategoriaSimple cs = (CategoriaSimple) p;
                preguntaData.put("opciones", new ArrayList<>(cs.getOpciones()));
            }

            if (p instanceof Ordinal) {
                Ordinal o = (Ordinal) p;
                preguntaData.put("opciones", new ArrayList<>(o.getOpciones()));
            }

            if (p instanceof CategoriaMultiple) {
                CategoriaMultiple cm = (CategoriaMultiple) p;
                preguntaData.put("opciones", new ArrayList<>(cm.getOpciones()));
                preguntaData.put("maxSelecciones", cm.getMaxSelecciones());
            }

            preguntasDatos.add(preguntaData);
        }

        datos.put("preguntas", preguntasDatos);
        return datos;
    }

    /**
     * Envía las respuestas del usuario autenticado para una encuesta concreta.
     *
     * @param idEncuesta id de la encuesta respondida
     * @param respuestas mapa con respuestas, en el formato esperado por el dominio
     * @return {@code true} si se envió correctamente; {@code false} si ocurre un error
     */
    public boolean enviarRespuestasEncuesta(String idEncuesta, Map<String, Object> respuestas) {
        try {
            ctrlDominio.responderEncuesta(usuarioActualId, idEncuesta, respuestas);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Indica si el usuario autenticado ya respondió una encuesta.
     *
     * @param idEncuesta id de la encuesta
     * @return {@code true} si ya respondió; {@code false} en caso contrario o si no hay sesión iniciada
     */
    public boolean usuarioYaRespondio(String idEncuesta) {
        if (usuarioActualId == null) return false;

        List<Respuesta> respuestas = ctrlDominio.obtenerRespuestasUsuario(usuarioActualId, idEncuesta);
        return !respuestas.isEmpty();
    }

    /**
     * Obtiene estadísticas de una encuesta.
     *
     * @param idEncuesta id de la encuesta
     * @return mapa con datos estadísticos (estructura definida por el dominio)
     */
    public Map<String, Object> obtenerEstadisticasEncuesta(String idEncuesta) {
        return ctrlDominio.obtenerEstadisticasEncuesta(idEncuesta);
    }

    /**
     * Importa una encuesta desde un CSV.
     *
     * @param rutaArchivo ruta al archivo CSV
     * @param titulo      título para la encuesta importada
     * @param descripcion descripción para la encuesta importada
     * @return id de la encuesta creada tras la importación
     * @throws Exception si el dominio lanza un error durante la importación
     */
    public String importarCSV(String rutaArchivo, String titulo, String descripcion) throws Exception {
        Encuesta encuesta = ctrlDominio.importarCSV(rutaArchivo, titulo, descripcion);
        return encuesta.getId();
    }

    /**
     * Exporta una encuesta a CSV en la ruta indicada.
     *
     * @param idEncuesta  id de la encuesta a exportar
     * @param rutaArchivo ruta destino del CSV
     * @throws Exception si ocurre un error durante la exportación
     */
    public void exportarCSV(String idEncuesta, String rutaArchivo) throws Exception {
        ctrlDominio.exportarEncuesta(idEncuesta, rutaArchivo);
    }

    /**
     * Obtiene una vista previa formateada del contenido de un CSV.
     * <p>
     * Muestra encabezados y hasta 10 filas, con un resumen de tamaño al final.
     *
     * @param rutaArchivo ruta al CSV
     * @return texto con la vista previa o un mensaje de error
     */
    public String obtenerVistaPreviaCSV(String rutaArchivo) {
        try {
            var lector = ctrlDominio.getLectorCSV();
            var datos = lector.leerConEncabezados(rutaArchivo);

            StringBuilder sb = new StringBuilder();

            // Encabezados
            String[] encabezados = datos.getEncabezados();
            sb.append(String.join(" | ", encabezados)).append("\n");
            sb.append("-".repeat(80)).append("\n");

            // Primeras 10 filas
            var filas = datos.getFilas();
            int maxFilas = Math.min(10, filas.size());

            for (int i = 0; i < maxFilas; i++) {
                var fila = filas.get(i);
                sb.append(fila.getIdUsuario());

                String[] respuestas = fila.getRespuestas();
                for (String respuesta : respuestas) {
                    sb.append(" | ").append(respuesta);
                }
                sb.append("\n");
            }

            if (filas.size() > 10) {
                sb.append("\n... (").append(filas.size() - 10).append(" filas más)");
            }

            sb.append("\n\nTotal: ").append(filas.size()).append(" filas, ");
            sb.append(encabezados.length).append(" columnas");

            return sb.toString();

        } catch (Exception e) {
            return "Error al leer el archivo:\n" + e.getMessage();
        }
    }

    /**
     * Valida si un archivo CSV existe, es legible y cumple restricciones de tamaño.
     *
     * @param rutaArchivo ruta del archivo
     * @return {@code "OK"} si el archivo es válido, o un mensaje de error en caso contrario
     */
    public String validarArchivoCSV(String rutaArchivo) {
        java.io.File archivo = new java.io.File(rutaArchivo);

        if (!archivo.exists()) {
            return "El archivo no existe:\n" + rutaArchivo;
        }

        if (!archivo.canRead()) {
            return "No se puede leer el archivo:\n" + rutaArchivo;
        }

        if (archivo.length() == 0) {
            return "El archivo está vacío";
        }

        if (archivo.length() > 100 * 1024 * 1024) { // 100 MB
            return "El archivo es demasiado grande (máximo 100 MB)\n" +
                    "Tamaño actual: " + (archivo.length() / (1024 * 1024)) + " MB";
        }

        return "OK";
    }

    /**
     * Obtiene el número de usuarios que respondieron una encuesta.
     *
     * @param idEncuesta id de la encuesta
     * @return número de respuestas (usuarios respondedores) para la encuesta
     */
    public int obtenerNumeroRespuestas(String idEncuesta) {
        List<UsuarioRespondedor> usuarios = ctrlDominio.obtenerUsuariosQueRespondieron(idEncuesta);
        return usuarios.size();
    }

    /**
     * Modifica una encuesta existente.
     *
     * @param idEncuesta  id de la encuesta
     * @param titulo      nuevo título
     * @param descripcion nueva descripción
     * @return {@code true} si se modificó correctamente; {@code false} en caso contrario
     */
    public boolean modificarEncuestaCompleta(String idEncuesta, String titulo, String descripcion) {
        try {
            return ctrlDominio.modificarEncuesta(idEncuesta, titulo, descripcion);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina una pregunta de una encuesta.
     *
     * @param idEncuesta id de la encuesta
     * @param indice    índice de la pregunta a eliminar
     * @return {@code true} si se eliminó; {@code false} en caso contrario
     */
    public boolean eliminarPregunta(String idEncuesta, int indice) {
        return ctrlDominio.eliminarPregunta(idEncuesta, indice);
    }

    /**
     * Obtiene todas las encuestas exportables con metadatos útiles para la vista de exportación.
     * <p>
     * Se filtran las encuestas sin preguntas y las que no tienen usuarios respondientes.
     *
     * @return lista de mapas con: id, titulo, numPreguntas, numUsuarios
     */
    public List<Map<String, Object>> listarEncuestasParaExportar() {
        List<Encuesta> encuestas = ctrlDominio.listarEncuestas();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Encuesta enc : encuestas) {
            if (enc.getNumPreguntas() > 0) {
                var info = ctrlDominio.obtenerInfoExportacion(enc.getId());

                if (info != null && info.numeroUsuarios > 0) {
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("id", enc.getId());
                    datos.put("titulo", enc.getTitulo());
                    datos.put("numPreguntas", info.numeroPreguntas);
                    datos.put("numUsuarios", info.numeroUsuarios);
                    resultado.add(datos);
                }
            }
        }

        return resultado;
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Cambia la escena actual del {@link Stage} principal y actualiza el título.
     *
     * @param scene  nueva escena a mostrar
     * @param titulo subtítulo contextual para componer el título de la ventana
     */
    private void cambiarEscena(Scene scene, String titulo) {
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sistema de Encuestas - " + titulo);
    }

    /**
     * Busca una pregunta dentro de una encuesta por su id.
     *
     * @param enc        encuesta en la que buscar
     * @param idPregunta id de la pregunta
     * @return instancia {@link Pregunta} si existe; {@code null} en caso contrario
     */
    private Pregunta buscarPregunta(Encuesta enc, String idPregunta) {
        return enc.getPreguntas().stream()
                .filter(p -> p.getId().equals(idPregunta))
                .findFirst()
                .orElse(null);
    }

    /**
     * Formatea un valor de respuesta para ser mostrado en la UI.
     * <ul>
     *   <li>{@code null} → "(Sin respuesta)"</li>
     *   <li>{@link Set} vacío → "(Sin respuesta)"</li>
     *   <li>{@link Double} entero → sin decimales</li>
     *   <li>{@link Double} no entero → con 2 decimales</li>
     * </ul>
     *
     * @param valor valor a formatear
     * @return representación textual para mostrar
     */
    private String formatearValor(Object valor) {
        if (valor == null) return "(Sin respuesta)";

        if (valor instanceof Set) {
            Set<?> set = (Set<?>) valor;
            if (set.isEmpty()) return "(Sin respuesta)";
            return String.join(", ", set.stream()
                    .map(Object::toString)
                    .toArray(String[]::new));
        }

        if (valor instanceof Double) {
            double d = (Double) valor;
            if (d == Math.floor(d)) {
                return String.valueOf((int) d);
            }
            return String.format("%.2f", d);
        }

        return valor.toString();
    }

    // ==================== GETTERS ====================

    /**
     * Devuelve el id del usuario actualmente autenticado.
     *
     * @return id del usuario actual; {@code null} si no hay sesión iniciada
     */
    public String getUsuarioActualId() {
        return usuarioActualId;
    }

    /**
     * Indica si el usuario actual es administrador.
     *
     * @return {@code true} si el usuario actual es admin; {@code false} en caso contrario
     */
    public boolean esAdmin() {
        return esAdmin;
    }

    /**
     * Devuelve el {@link Stage} principal de la aplicación.
     *
     * @return stage principal
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Añade una pregunta a una encuesta.
     * <p>
     * El mapa {@code datosPregunta} debe contener:
     * <ul>
     *   <li>{@code tipo}: "NUMERICA", "LIBRE", "CATEGORIA_SIMPLE", "ORDINAL", "CATEGORIA_MULTIPLE"</li>
     *   <li>{@code enunciado}: texto de la pregunta</li>
     *   <li>{@code obligatoria}: {@link Boolean}</li>
     *   <li>y campos específicos según el tipo (p. ej. min/max, opciones, maxSelecciones...)</li>
     * </ul>
     *
     * @param idEncuesta    id de la encuesta
     * @param datosPregunta mapa con los datos necesarios para construir la pregunta
     * @return {@code "OK"} si se añadió correctamente, o un mensaje de error
     */
    public String addPregunta(String idEncuesta, Map<String, Object> datosPregunta) {
        try {
            String tipo = (String) datosPregunta.get("tipo");
            String enunciado = (String) datosPregunta.get("enunciado");
            Boolean obligatoria = (Boolean) datosPregunta.get("obligatoria");

            // Crear la pregunta según el tipo
            Pregunta pregunta = null;

            switch (tipo) {
                case "NUMERICA": {
                    Double min = (Double) datosPregunta.get("min");
                    Double max = (Double) datosPregunta.get("max");
                    pregunta = new Numerica(enunciado, min, max);
                    break;
                }

                case "LIBRE": {
                    Integer longitudMaxima = (Integer) datosPregunta.get("longitudMaxima");
                    pregunta = new Libre(enunciado, longitudMaxima);
                    break;
                }

                case "CATEGORIA_SIMPLE": {
                    @SuppressWarnings("unchecked")
                    List<String> opciones = (List<String>) datosPregunta.get("opciones");
                    pregunta = new CategoriaSimple(enunciado, new LinkedHashSet<>(opciones));
                    break;
                }

                case "ORDINAL": {
                    @SuppressWarnings("unchecked")
                    List<String> opciones = (List<String>) datosPregunta.get("opciones");
                    pregunta = new Ordinal(enunciado, new LinkedHashSet<>(opciones));
                    break;
                }

                case "CATEGORIA_MULTIPLE": {
                    @SuppressWarnings("unchecked")
                    List<String> opciones = (List<String>) datosPregunta.get("opciones");
                    Integer maxSelecciones = (Integer) datosPregunta.get("maxSelecciones");
                    pregunta = new CategoriaMultiple(enunciado, new LinkedHashSet<>(opciones), maxSelecciones);
                    break;
                }

                default:
                    return "Tipo de pregunta no válido: " + tipo;
            }

            if (pregunta != null) {
                pregunta.setObligatoria(obligatoria);
                boolean exito = ctrlDominio.addPregunta(idEncuesta, pregunta);
                return exito ? "OK" : "Error al add la pregunta";
            }

            return "No se pudo crear la pregunta";

        } catch (IllegalArgumentException e) {
            return "Error de validación: " + e.getMessage();
        } catch (Exception e) {
            return "Error al add pregunta: " + e.getMessage();
        }
    }

    /**
     * Ejecuta clustering para una encuesta y devuelve resultados en formato apto para UI.
     *
     * @param idEncuesta id de la encuesta
     * @param algoritmo  nombre/identificador del algoritmo a ejecutar
     * @param kMax       máximo valor de k a evaluar
     * @param maxIter    número máximo de iteraciones del algoritmo de clustering
     * @return lista de mapas con: k, algoritmo, silhouette, usuariosPorGrupo
     * @throws Exception si el dominio lanza un error durante la ejecución
     */
    public List<Map<String, Object>> ejecutarClusteringYObtenerResultados(
            String idEncuesta, String algoritmo, int kMax, int maxIter) throws Exception {

        List<ResultadoClustering> resultados = ctrlDominio.ejecutarClustering(
                idEncuesta, algoritmo, kMax, maxIter);

        return convertirResultadosClusteringAMaps(resultados);
    }

    /**
     * Obtiene el historial de ejecuciones de clustering de una encuesta en formato UI.
     *
     * @param idEncuesta id de la encuesta
     * @return lista de mapas con: k, algoritmo, silhouette, usuariosPorGrupo
     */
    public List<Map<String, Object>> obtenerHistorialClustering(String idEncuesta) {
        List<ResultadoClustering> resultados = ctrlDominio.obtenerHistorialClustering(idEncuesta);
        return convertirResultadosClusteringAMaps(resultados);
    }

    /**
     * Elimina el historial de clustering asociado a una encuesta.
     *
     * @param idEncuesta id de la encuesta
     * @return {@code true} si se limpió correctamente; {@code false} en caso contrario
     */
    public boolean limpiarHistorialClustering(String idEncuesta) {
        return ctrlDominio.limpiarHistorialClustering(idEncuesta);
    }

    /**
     * Obtiene el nombre de un usuario por su identificador.
     *
     * @param idUsuario id del usuario
     * @return nombre del usuario; si no existe, {@code "Usuario desconocido"}
     */
    public String obtenerNombreUsuarioPorId(String idUsuario) {
        Usuario usuario = ctrlDominio.obtenerUsuario(idUsuario);
        return usuario != null ? usuario.getNombre() : "Usuario desconocido";
    }

    /**
     * Convierte una lista de {@link ResultadoClustering} a estructura {@link Map} para consumo de vistas.
     *
     * @param resultados resultados devueltos por el dominio
     * @return lista de mapas con claves: k, algoritmo, silhouette, usuariosPorGrupo
     */
    private List<Map<String, Object>> convertirResultadosClusteringAMaps(List<ResultadoClustering> resultados) {
        List<Map<String, Object>> maps = new ArrayList<>();

        for (ResultadoClustering r : resultados) {
            Map<String, Object> map = new HashMap<>();
            map.put("k", r.getK());
            map.put("algoritmo", r.getAlgoritmo());
            map.put("silhouette", r.getSilhouette());
            map.put("usuariosPorGrupo", r.getUsuariosPorGrupo());
            maps.add(map);
        }

        return maps;
    }

    /**
     * Muestra la vista de “Añadir pregunta” para una encuesta concreta.
     *
     * @param idEncuesta id de la encuesta
     */
    public void mostrarAddPregunta(String idEncuesta) {
        AddPreguntaView vista = new AddPreguntaView(this, idEncuesta);
        cambiarEscena(vista.getScene(), "Add Pregunta");
    }

    /**
     * Modifica el enunciado de una pregunta de una encuesta.
     * <p>
     * Nota: este método modifica el objeto {@link Pregunta} recuperado del dominio.
     * Si el dominio requiere persistencia explícita, esta operación debería delegarse
     * a {@link CtrlDominio} para mantener consistencia.
     *
     * @param idEncuesta     id de la encuesta
     * @param index          índice de la pregunta dentro de la lista
     * @param nuevoEnunciado nuevo enunciado
     * @return {@code true} si se pudo modificar; {@code false} si la encuesta/pregunta no existe o hay error
     */
    public boolean modificarEnunciadoPregunta(String idEncuesta, int index, String nuevoEnunciado) {
        try {
            Encuesta encuesta = ctrlDominio.obtenerEncuesta(idEncuesta);
            if (encuesta == null) return false;

            List<Pregunta> preguntas = encuesta.getPreguntas();
            if (index < 0 || index >= preguntas.size()) return false;

            Pregunta pregunta = preguntas.get(index);
            pregunta.setEnunciado(nuevoEnunciado);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}