package main.domain.controllers;

import main.domain.classes.*;
import main.persistence.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Controlador principal de dominio (fachada del subsistema de dominio).
 * <p>
 * Esta clase centraliza las operaciones de alto nivel del sistema relacionadas con:
 * <ul>
 *   <li>Gestión de encuestas y preguntas</li>
 *   <li>Gestión de usuarios (admin/respondedor)</li>
 *   <li>Gestión de respuestas</li>
 *   <li>Ejecución y persistencia de resultados de clustering</li>
 *   <li>Importación y exportación de datos en CSV</li>
 *   <li>Serialización/deserialización a JSON con Gson</li>
 * </ul>
 * <p>
 * Implementa una solución simple para serializar {@link Pregunta} polimórfica:
 * en vez de serializar {@code Pregunta} directamente, se serializa cada tipo concreto
 * por separado, guardando explícitamente el campo "tipo" y el "data".
 * <p>
 * Nota: este controlador sí coordina con persistencia mediante {@link CtrlPersistencia}
 * y {@link PersistenciaCSV}.
 */
public class CtrlDominio {
    /**
     * Componente de persistencia principal para encuestas, usuarios, respuestas y resultados.
     */
    private final CtrlPersistencia persistencia;

    /**
     * Instancia Gson usada para serializar/deserializar JSON.
     */
    private final Gson gson;

    /**
     * Controlador de lógica de encuestas (sin persistencia).
     */
    private final ControladorEncuestas controladorEncuestas;

    /**
     * Controlador de lógica de usuarios (sin persistencia).
     */
    private final ControladorUsuarios controladorUsuarios;

    /**
     * Controlador de lógica de respuestas (sin persistencia).
     */
    private final ControladorRespuestas controladorRespuestas;

    /**
     * Controlador de alto nivel de clustering.
     */
    private final CtrlClustering controladorClustering;

    /**
     * Controlador encargado de importar datos desde CSV a objetos de dominio.
     */
    private final ControladorImportacion controladorImportacion;

    /**
     * Controlador encargado de preparar datos para exportación a CSV.
     */
    private final ControladorExportacion controladorExportacion;

    /**
     * Adaptador para resolver índices de preguntas delegando en {@link CtrlDominio}.
     */
    private final AdaptadorPregunta adaptadorPregunta;

    /**
     * Componente de persistencia para operaciones CSV.
     */
    private final PersistenciaCSV persistenciaCSV;

    /**
     * Construye el controlador principal del dominio.
     * <p>
     * Inicializa:
     * <ul>
     *   <li>Persistencia en ruta "./data"</li>
     *   <li>Gson con pretty printing</li>
     *   <li>Controladores de lógica en memoria</li>
     *   <li>Adaptador de índice de preguntas</li>
     *   <li>Persistencia CSV</li>
     * </ul>
     */
    public CtrlDominio() {
        this.persistencia = new CtrlPersistencia("./EXE/data");

        // ⭐ SOLUCIÓN SIMPLE: Solo pretty printing, sin adaptadores
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        this.controladorEncuestas = new ControladorEncuestas();
        this.controladorUsuarios = new ControladorUsuarios();
        this.controladorRespuestas = new ControladorRespuestas();
        this.controladorClustering = new CtrlClustering();
        this.controladorImportacion = new ControladorImportacion(
                controladorUsuarios,
                controladorEncuestas,
                controladorRespuestas
        );
        this.controladorExportacion = new ControladorExportacion();
        this.adaptadorPregunta = new AdaptadorPregunta(this);
        this.persistenciaCSV = new PersistenciaCSV();

    }

    // ========== SERIALIZACIÓN/DESERIALIZACIÓN PERSONALIZADA ==========

    /**
     * Serializa una encuesta a JSON manejando preguntas polimórficas.
     * <p>
     * Estructura:
     * <ul>
     *   <li>id, titulo, descripcion</li>
     *   <li>preguntas: array de objetos con {"tipo": "...", "data": ...}</li>
     *   <li>historialResultados: lista serializada directamente</li>
     * </ul>
     *
     * @param encuesta encuesta a serializar
     * @return representación JSON de la encuesta
     */
    private String encuestaToJson(Encuesta encuesta) {
        JsonObject obj = new JsonObject();

        obj.addProperty("id", encuesta.getId());
        obj.addProperty("titulo", encuesta.getTitulo());
        obj.addProperty("descripcion", encuesta.getDescripcion());

        // Serializar preguntas individualmente según su tipo
        JsonArray preguntasArray = new JsonArray();
        for (Pregunta p : encuesta.getPreguntas()) {
            JsonObject preguntaObj = new JsonObject();
            preguntaObj.addProperty("tipo", p.getTipoPregunta().name());

            // Serializar la pregunta como su tipo concreto
            JsonElement preguntaData = gson.toJsonTree(p, p.getClass());
            preguntaObj.add("data", preguntaData);

            preguntasArray.add(preguntaObj);
        }
        obj.add("preguntas", preguntasArray);

        // Serializar resultados de clustering
        obj.add("historialResultados", gson.toJsonTree(encuesta.getHistorialResultados()));

        return gson.toJson(obj);
    }

    /**
     * Deserializa una encuesta desde JSON reconstruyendo preguntas polimórficas.
     * <p>
     * Reconstruye:
     * <ul>
     *   <li>Datos básicos (id, titulo, descripcion)</li>
     *   <li>Lista de preguntas, usando {@link #deserializarPregunta(String, JsonElement)}</li>
     *   <li>Historial de clustering</li>
     * </ul>
     *
     * @param json representación JSON de la encuesta
     * @return encuesta reconstruida
     */
    private Encuesta jsonToEncuesta(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        String id = obj.get("id").getAsString();
        String titulo = obj.get("titulo").getAsString();
        String descripcion = obj.get("descripcion").getAsString();

        Encuesta encuesta = new Encuesta(titulo, descripcion);
        encuesta.setId(id);

        // Deserializar preguntas
        if (obj.has("preguntas")) {
            JsonArray preguntasArray = obj.getAsJsonArray("preguntas");
            for (JsonElement elem : preguntasArray) {
                JsonObject preguntaObj = elem.getAsJsonObject();
                String tipo = preguntaObj.get("tipo").getAsString();
                JsonElement preguntaData = preguntaObj.get("data");

                Pregunta pregunta = deserializarPregunta(tipo, preguntaData);
                if (pregunta != null) {
                    encuesta.agregarPregunta(pregunta);
                }
            }
        }

        // Deserializar historial de clustering
        if (obj.has("historialResultados")) {
            Type listType = new TypeToken<List<ResultadoClustering>>(){}.getType();
            List<ResultadoClustering> historial = gson.fromJson(
                    obj.get("historialResultados"), listType);
            for (ResultadoClustering resultado : historial) {
                encuesta.agregarResultado(resultado);
            }
        }

        return encuesta;
    }

    /**
     * Deserializa una pregunta a partir del nombre del tipo y su contenido JSON.
     * <p>
     * El parámetro {@code tipo} se mapea a {@link TipoPregunta} y se instancia la clase concreta:
     * {@link Numerica}, {@link CategoriaSimple}, {@link CategoriaMultiple}, {@link Ordinal}, {@link Libre}.
     *
     * @param tipo nombre del tipo (por ejemplo "NUMERICA", "CATEGORIA_SIMPLE", etc.)
     * @param data contenido JSON de la pregunta concreta
     * @return instancia de {@link Pregunta} o {@code null} si el tipo no es soportado
     * @throws IllegalArgumentException si {@code tipo} no corresponde a un valor de {@link TipoPregunta}
     */
    private Pregunta deserializarPregunta(String tipo, JsonElement data) {
        TipoPregunta tipoPregunta = TipoPregunta.valueOf(tipo);

        switch (tipoPregunta) {
            case NUMERICA:
                return gson.fromJson(data, Numerica.class);
            case CATEGORIA_SIMPLE:
                return gson.fromJson(data, CategoriaSimple.class);
            case CATEGORIA_MULTIPLE:
                return gson.fromJson(data, CategoriaMultiple.class);
            case ORDINAL:
                return gson.fromJson(data, Ordinal.class);
            case LIBRE:
                return gson.fromJson(data, Libre.class);
            default:
                return null;
        }
    }

    // ========== ENCUESTAS ==========

    /**
     * Crea una encuesta, la serializa a JSON y la guarda en persistencia.
     *
     * @param titulo       título de la encuesta
     * @param descripcion  descripción de la encuesta
     * @return encuesta creada
     * @throws IllegalArgumentException si el título es inválido (delegado a {@link ControladorEncuestas})
     */
    public Encuesta crearEncuesta(String titulo, String descripcion) {
        Encuesta encuesta = controladorEncuestas.crearEncuesta(titulo, descripcion);
        String json = encuestaToJson(encuesta);
        persistencia.guardarEncuesta(json);
        return encuesta;
    }

    /**
     * Obtiene una encuesta desde persistencia por su ID.
     *
     * @param idEncuesta identificador de la encuesta
     * @return encuesta reconstruida o {@code null} si no existe
     */
    public Encuesta obtenerEncuesta(String idEncuesta) {
        String json = persistencia.obtenerEncuesta(idEncuesta);
        if (json == null) return null;
        return jsonToEncuesta(json);
    }

    /**
     * Lista todas las encuestas almacenadas en persistencia.
     *
     * @return lista de encuestas reconstruidas
     */
    public List<Encuesta> listarEncuestas() {
        List<String> jsons = persistencia.obtenerTodasEncuestas();
        List<Encuesta> encuestas = new ArrayList<>();

        for (String json : jsons) {
            encuestas.add(jsonToEncuesta(json));
        }

        return encuestas;
    }

    /**
     * Modifica una encuesta existente y persiste los cambios.
     *
     * @param idEncuesta  identificador de la encuesta
     * @param titulo      nuevo título (puede ser {@code null})
     * @param descripcion nueva descripción (puede ser {@code null})
     * @return {@code true} si se modificó con éxito; {@code false} si la encuesta no existe
     */
    public boolean modificarEncuesta(String idEncuesta, String titulo, String descripcion) {
        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) return false;

        controladorEncuestas.modificarEncuesta(encuesta, titulo, descripcion);
        String json = encuestaToJson(encuesta);
        persistencia.guardarEncuesta(json);

        return true;
    }

    /**
     * Elimina una encuesta y, si se elimina correctamente, borra también
     * sus respuestas y resultados de clustering asociados en persistencia.
     *
     * @param idEncuesta identificador de la encuesta
     * @return {@code true} si se eliminó; {@code false} si no se pudo eliminar
     */
    public boolean eliminarEncuesta(String idEncuesta) {
        boolean eliminado = persistencia.eliminarEncuesta(idEncuesta);

        if (eliminado) {
            persistencia.eliminarRespuestasEncuesta(idEncuesta);
            persistencia.eliminarResultadosClustering(idEncuesta);
        }

        return eliminado;
    }

    // ========== PREGUNTAS ==========

    /**
     * Añade una pregunta a una encuesta existente y persiste los cambios.
     *
     * @param idEncuesta identificador de la encuesta
     * @param pregunta   pregunta a añadir
     * @return {@code true} si se añadió; {@code false} si la encuesta no existe
     * @throws IllegalArgumentException si encuesta o pregunta son inválidas (delegado a {@link ControladorEncuestas})
     */
    public boolean addPregunta(String idEncuesta, Pregunta pregunta) {
        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) return false;

        controladorEncuestas.agregarPregunta(encuesta, pregunta);
        String json = encuestaToJson(encuesta);
        persistencia.guardarEncuesta(json);

        return true;
    }

    /**
     * Modifica una pregunta en una encuesta por índice y persiste los cambios.
     *
     * @param idEncuesta identificador de la encuesta
     * @param index     índice de la pregunta a modificar
     * @param pregunta  nueva pregunta
     * @return {@code true} si se modificó; {@code false} si la encuesta no existe o hubo error
     */
    public boolean modificarPregunta(String idEncuesta, int index, Pregunta pregunta) {
        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) return false;

        try {
            controladorEncuestas.modificarPregunta(encuesta, index, pregunta);
            String json = encuestaToJson(encuesta);
            persistencia.guardarEncuesta(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Elimina una pregunta de una encuesta por índice y persiste los cambios.
     *
     * @param idEncuesta identificador de la encuesta
     * @param index     índice de la pregunta a eliminar
     * @return {@code true} si se eliminó; {@code false} si la encuesta no existe o hubo error
     */
    public boolean eliminarPregunta(String idEncuesta, int index) {
        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) return false;

        try {
            controladorEncuestas.eliminarPregunta(encuesta, index);
            String json = encuestaToJson(encuesta);
            persistencia.guardarEncuesta(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== USUARIOS ==========

    /**
     * Crea un usuario respondedor, valida que no exista previamente y lo guarda en persistencia.
     *
     * @param id       identificador del usuario
     * @param nombre   nombre del usuario
     * @param password contraseña del usuario
     * @throws IllegalArgumentException si ya existe un usuario con ese ID o si los datos son inválidos
     */
    public void crearUsuarioRespondedor(String id, String nombre, String password) {
        if (persistencia.existeUsuario(id)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese ID");
        }

        UsuarioRespondedor usuario =
                controladorUsuarios.crearUsuarioRespondedor(id, nombre, password);

        String json = gson.toJson(usuario);
        persistencia.guardarRespondedor(json);
    }

    /**
     * Crea un usuario administrador, valida que no exista previamente y lo guarda en persistencia.
     *
     * @param id       identificador del admin
     * @param nombre   nombre del admin
     * @param password contraseña del admin
     * @throws IllegalArgumentException si ya existe un usuario con ese ID o si los datos son inválidos
     */
    public void crearUsuarioAdmin(String id, String nombre, String password) {
        if (persistencia.existeUsuario(id)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese ID");
        }

        UsuarioAdmin admin =
                controladorUsuarios.crearUsuarioAdmin(id, nombre, password);

        String json = gson.toJson(admin);
        persistencia.guardarAdmin(json);
    }

    /**
     * Obtiene un usuario (admin o respondedor) por ID desde persistencia.
     *
     * @param idUsuario identificador del usuario
     * @return instancia de {@link Usuario} o {@code null} si no existe
     */
    public Usuario obtenerUsuario(String idUsuario) {
        String json = persistencia.obtenerUsuario(idUsuario);
        if (json == null) return null;

        if (persistencia.esAdmin(idUsuario)) {
            return gson.fromJson(json, UsuarioAdmin.class);
        } else {
            return gson.fromJson(json, UsuarioRespondedor.class);
        }
    }

    /**
     * Obtiene un usuario respondedor desde persistencia.
     *
     * @param idUsuario identificador del usuario respondedor
     * @return {@link UsuarioRespondedor} o {@code null} si no existe
     */
    public UsuarioRespondedor obtenerRespondedor(String idUsuario) {
        String json = persistencia.obtenerRespondedor(idUsuario);
        if (json == null) return null;
        return gson.fromJson(json, UsuarioRespondedor.class);
    }

    /**
     * Lista todos los usuarios (admins y respondedores) desde persistencia.
     *
     * @return lista de usuarios
     */
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();

        for (String json : persistencia.obtenerTodosAdmins()) {
            usuarios.add(gson.fromJson(json, UsuarioAdmin.class));
        }

        for (String json : persistencia.obtenerTodosRespondedores()) {
            usuarios.add(gson.fromJson(json, UsuarioRespondedor.class));
        }

        return usuarios;
    }

    /**
     * Lista todos los usuarios respondedores desde persistencia.
     *
     * @return lista de usuarios respondedores
     */
    public List<UsuarioRespondedor> listarRespondedores() {
        List<UsuarioRespondedor> usuarios = new ArrayList<>();

        for (String json : persistencia.obtenerTodosRespondedores()) {
            usuarios.add(gson.fromJson(json, UsuarioRespondedor.class));
        }

        return usuarios;
    }

    // ========== RESPUESTAS ==========

    /**
     * Registra respuestas de un usuario a una encuesta, validándolas con lógica de dominio y
     * persistiendo tanto las respuestas como el usuario actualizado.
     * <p>
     * Las respuestas se guardan como {@code Map<String,String>} usando serialización simple
     * (ver {@link #serializarValor(Object)}).
     *
     * @param idUsuario  identificador del usuario respondedor
     * @param idEncuesta identificador de la encuesta
     * @param respuestas mapa idPregunta -> valor (tipo depende de {@link TipoPregunta})
     * @throws IllegalArgumentException si usuario o encuesta no existen, o si hay valores inválidos
     */
    public void responderEncuesta(String idUsuario, String idEncuesta,
                                  Map<String, Object> respuestas) {
        UsuarioRespondedor usuario = obtenerRespondedor(idUsuario);
        Encuesta encuesta = obtenerEncuesta(idEncuesta);

        if (usuario == null || encuesta == null) {
            throw new IllegalArgumentException("Usuario o encuesta no encontrados");
        }

        controladorRespuestas.responderEncuesta(usuario, encuesta, respuestas);

        Map<String, String> respuestasStr = new HashMap<>();
        for (Map.Entry<String, Object> entry : respuestas.entrySet()) {
            respuestasStr.put(entry.getKey(), serializarValor(entry.getValue()));
        }

        persistencia.guardarRespuestas(idUsuario, idEncuesta, respuestasStr);

        String jsonUsuario = gson.toJson(usuario);
        persistencia.guardarRespondedor(jsonUsuario);
    }

    /**
     * Registra la respuesta de un usuario a una pregunta concreta dentro de una encuesta,
     * y persiste la respuesta junto con el usuario actualizado.
     *
     * @param idUsuario  identificador del usuario respondedor
     * @param idEncuesta identificador de la encuesta
     * @param idPregunta identificador de la pregunta
     * @param valor      valor de respuesta
     * @return objeto {@link Respuesta} creado
     * @throws IllegalArgumentException si usuario/encuesta/pregunta no existen o el valor es inválido
     */
    public Respuesta responderPregunta(String idUsuario, String idEncuesta,
                                       String idPregunta, Object valor) {
        UsuarioRespondedor usuario = obtenerRespondedor(idUsuario);
        Encuesta encuesta = obtenerEncuesta(idEncuesta);

        if (usuario == null || encuesta == null) {
            throw new IllegalArgumentException("Usuario o encuesta no encontrados");
        }

        Respuesta respuesta = controladorRespuestas.responderPregunta(
                usuario, encuesta, idPregunta, valor);

        Map<String, String> respuestasExistentes =
                persistencia.obtenerRespuestas(idUsuario, idEncuesta);
        respuestasExistentes.put(idPregunta, serializarValor(valor));
        persistencia.guardarRespuestas(idUsuario, idEncuesta, respuestasExistentes);

        String jsonUsuario = gson.toJson(usuario);
        persistencia.guardarRespondedor(jsonUsuario);

        return respuesta;
    }

    /**
     * Obtiene las respuestas de un usuario para una encuesta, deserializando los valores
     * guardados como String a {@link Object}.
     *
     * @param idUsuario  identificador del usuario
     * @param idEncuesta identificador de la encuesta
     * @return lista de respuestas reconstruidas (idPregunta + valor)
     */
    public List<Respuesta> obtenerRespuestasUsuario(String idUsuario, String idEncuesta) {
        Map<String, String> respuestasStr =
                persistencia.obtenerRespuestas(idUsuario, idEncuesta);

        List<Respuesta> respuestas = new ArrayList<>();
        for (Map.Entry<String, String> entry : respuestasStr.entrySet()) {
            Object valor = deserializarValor(entry.getValue());
            respuestas.add(new Respuesta(entry.getKey(), valor));
        }

        return respuestas;
    }

    /**
     * Obtiene los usuarios respondedores que han contestado una encuesta.
     *
     * @param idEncuesta identificador de la encuesta
     * @return lista de usuarios respondedores que respondieron
     */
    public List<UsuarioRespondedor> obtenerUsuariosQueRespondieron(String idEncuesta) {
        List<String> idsUsuarios =
                persistencia.obtenerUsuariosQueRespondieron(idEncuesta);

        List<UsuarioRespondedor> usuarios = new ArrayList<>();
        for (String idUsuario : idsUsuarios) {
            UsuarioRespondedor usuario = obtenerRespondedor(idUsuario);
            if (usuario != null) {
                usuarios.add(usuario);
            }
        }

        return usuarios;
    }

    // ========== CLUSTERING ==========

    /**
     * Ejecuta clustering sobre una encuesta, persiste los resultados y guarda el mejor resultado
     * en el historial de la encuesta.
     *
     * @param idEncuesta identificador de la encuesta
     * @param algoritmo  algoritmo a utilizar ("KMEANS", "KMEANS++", "KMEDOIDS" o alias numéricos)
     * @param kMax       K máximo a evaluar
     * @param maxIter    iteraciones máximas
     * @return lista de resultados (uno por cada K)
     * @throws IllegalArgumentException si la encuesta no existe
     */
    public List<ResultadoClustering> ejecutarClustering(
            String idEncuesta, String algoritmo, int kMax, int maxIter) {

        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) {
            throw new IllegalArgumentException("Encuesta no encontrada: " + idEncuesta);
        }

        List<UsuarioRespondedor> usuarios = obtenerUsuariosQueRespondieron(idEncuesta);

        List<ResultadoClustering> resultados = controladorClustering.ejecutarClustering(
                encuesta, usuarios, adaptadorPregunta, algoritmo, kMax, maxIter);

        String json = gson.toJson(resultados);
        persistencia.guardarResultadosClustering(encuesta.getId(), json);

        ResultadoClustering mejorGlobal =
                controladorClustering.encontrarMejorResultado(resultados);

        if (mejorGlobal != null) {
            encuesta.agregarResultado(mejorGlobal);
            String jsonEncuesta = encuestaToJson(encuesta);
            persistencia.guardarEncuesta(jsonEncuesta);
        }

        return resultados;
    }

    /**
     * Obtiene el historial de resultados de clustering persistidos para una encuesta.
     *
     * @param idEncuesta identificador de la encuesta
     * @return lista de resultados; lista vacía si no hay datos guardados
     */
    public List<ResultadoClustering> obtenerHistorialClustering(String idEncuesta) {
        String json = persistencia.obtenerResultadosClustering(idEncuesta);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<ResultadoClustering>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    /**
     * Obtiene el mejor resultado guardado para una encuesta, según silhouette.
     *
     * @param idEncuesta identificador de la encuesta
     * @return mejor resultado o {@code null} si no hay resultados
     */
    public ResultadoClustering obtenerMejorResultadoGuardado(String idEncuesta) {
        List<ResultadoClustering> resultados = obtenerHistorialClustering(idEncuesta);
        return controladorClustering.encontrarMejorResultado(resultados);
    }

    /**
     * Indica si existen resultados de clustering guardados para una encuesta.
     *
     * @param idEncuesta identificador de la encuesta
     * @return {@code true} si existen resultados; {@code false} en caso contrario
     */
    public boolean existenResultadosGuardados(String idEncuesta) {
        return persistencia.existenResultadosClustering(idEncuesta);
    }

    /**
     * Elimina el historial de clustering guardado en persistencia y limpia el historial en la encuesta.
     *
     * @param idEncuesta identificador de la encuesta
     * @return {@code true} siempre (siempre intenta limpiar)
     */
    public boolean limpiarHistorialClustering(String idEncuesta) {
        persistencia.eliminarResultadosClustering(idEncuesta);

        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta != null) {
            encuesta.limpiarHistorial();
            String json = encuestaToJson(encuesta);
            persistencia.guardarEncuesta(json);
        }

        return true;
    }

    // ========== ESTADÍSTICAS ==========

    /**
     * Obtiene estadísticas básicas de una encuesta.
     *
     * @param idEncuesta identificador de la encuesta
     * @return mapa con información (título, número de preguntas, número de respuestas, usuarios),
     *         o {@code null} si la encuesta no existe
     */
    public Map<String, Object> obtenerEstadisticasEncuesta(String idEncuesta) {
        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) return null;

        List<UsuarioRespondedor> usuarios = obtenerUsuariosQueRespondieron(idEncuesta);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("titulo", encuesta.getTitulo());
        estadisticas.put("numPreguntas", encuesta.getNumPreguntas());
        estadisticas.put("numRespuestas", usuarios.size());
        estadisticas.put("usuarios", usuarios);

        return estadisticas;
    }

    // ========== IMPORTAR/EXPORTAR CSV ==========

    /**
     * Exporta una encuesta a un archivo CSV (con encabezados).
     * <p>
     * Obtiene usuarios que han respondido, reconstruye sus respuestas y delega en:
     * <ul>
     *   <li>{@link ControladorExportacion#prepararExportacion(Encuesta, List, Map)}</li>
     *   <li>{@link PersistenciaCSV#escribirConEncabezados(String, String[], List)}</li>
     * </ul>
     *
     * @param idEncuesta  identificador de la encuesta
     * @param rutaArchivo ruta del archivo CSV destino
     * @throws java.io.IOException si ocurre un error de I/O al escribir el archivo
     * @throws IllegalArgumentException si la encuesta no existe
     */
    public void exportarEncuesta(String idEncuesta, String rutaArchivo)
            throws java.io.IOException {

        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) {
            throw new IllegalArgumentException("Encuesta no encontrada: " + idEncuesta);
        }

        List<UsuarioRespondedor> usuarios = obtenerUsuariosQueRespondieron(idEncuesta);

        Map<String, List<Respuesta>> respuestasPorUsuario = new HashMap<>();
        for (UsuarioRespondedor usuario : usuarios) {
            List<Respuesta> respuestas = obtenerRespuestasUsuario(usuario.getId(), idEncuesta);
            respuestasPorUsuario.put(usuario.getId(), respuestas);
        }

        ControladorExportacion.DatosExportacion datos =
                controladorExportacion.prepararExportacion(encuesta, usuarios, respuestasPorUsuario);

        persistenciaCSV.escribirConEncabezados(rutaArchivo, datos.encabezados, datos.filas);
        System.out.println("✓ Archivo CSV exportado: " + rutaArchivo);
    }

    /**
     * Importa un archivo CSV creando encuesta, usuarios y respuestas,
     * y persiste todo el resultado.
     *
     * @param rutaArchivo ruta al CSV con encabezados
     * @param titulo      título de la encuesta a crear
     * @param descripcion descripción de la encuesta a crear
     * @return encuesta creada e importada
     * @throws java.io.IOException si ocurre un error de I/O al leer el archivo
     */
    public Encuesta importarCSV(String rutaArchivo, String titulo, String descripcion)
            throws java.io.IOException {

        PersistenciaCSV.DatosCSV datosCSV = persistenciaCSV.leerConEncabezados(rutaArchivo);
        System.out.println("✓ Archivo CSV leído: " + datosCSV.getNumeroFilas() + " filas");

        ControladorImportacion.ResultadoImportacion resultado =
                controladorImportacion.procesarDatosCSV(datosCSV, titulo, descripcion);

        Encuesta encuesta = resultado.encuesta;

        String jsonEncuesta = encuestaToJson(encuesta);
        persistencia.guardarEncuesta(jsonEncuesta);
        System.out.println("✓ Encuesta guardada: " + encuesta.getId());

        for (UsuarioRespondedor usuario : resultado.usuariosCreados) {
            String jsonUsuario = gson.toJson(usuario);
            persistencia.guardarRespondedor(jsonUsuario);
        }
        System.out.println("✓ " + resultado.usuariosCreados.size() + " usuarios guardados");

        for (Map.Entry<String, Map<String, Object>> entry : resultado.respuestasPorUsuario.entrySet()) {
            String idUsuario = entry.getKey();
            Map<String, Object> respuestas = entry.getValue();

            Map<String, String> respuestasStr = new HashMap<>();
            for (Map.Entry<String, Object> respEntry : respuestas.entrySet()) {
                respuestasStr.put(respEntry.getKey(), serializarValor(respEntry.getValue()));
            }

            persistencia.guardarRespuestas(idUsuario, encuesta.getId(), respuestasStr);
        }
        System.out.println("✓ Respuestas guardadas");
        System.out.println("✓ Importación completada exitosamente");

        return encuesta;
    }

    /**
     * Obtiene información sobre la exportación de una encuesta, sin exportar.
     *
     * @param idEncuesta identificador de la encuesta
     * @return información de exportación o {@code null} si la encuesta no existe
     */
    public ControladorExportacion.InfoExportacion obtenerInfoExportacion(String idEncuesta) {
        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) return null;

        List<UsuarioRespondedor> usuarios = obtenerUsuariosQueRespondieron(idEncuesta);

        return controladorExportacion.obtenerInfoExportacion(encuesta, usuarios);
    }

    /**
     * Obtiene el lector CSV subyacente usado por {@link PersistenciaCSV}.
     *
     * @return lector CSV
     */
    public LectorCSV getLectorCSV() {
        return persistenciaCSV.getLector();
    }

    /**
     * Devuelve el objeto de persistencia CSV.
     *
     * @return persistencia CSV
     */
    public PersistenciaCSV getPersistenciaCSV() {
        return persistenciaCSV;
    }

    // ========== UTILIDADES DE SERIALIZACIÓN ==========

    /**
     * Serializa un valor de respuesta a {@link String} usando un formato simple.
     * <p>
     * Reglas:
     * <ul>
     *   <li>{@code null} -> ""</li>
     *   <li>{@link Set} -> "SET:" + elementos separados por "|||"</li>
     *   <li>Otros -> {@code toString()}</li>
     * </ul>
     *
     * @param valor valor a serializar
     * @return representación en String
     */
    private String serializarValor(Object valor) {
        if (valor == null) return "";

        if (valor instanceof Set) {
            Set<?> set = (Set<?>) valor;
            if (set.isEmpty()) return "";

            return "SET:" + String.join("|||",
                    set.stream().map(Object::toString).toArray(String[]::new));
        }

        return valor.toString();
    }

    /**
     * Deserializa un String previamente generado por {@link #serializarValor(Object)}.
     * <p>
     * Reglas:
     * <ul>
     *   <li>"" o nulo -> {@code null}</li>
     *   <li>"SET:..." -> {@link HashSet} de Strings</li>
     *   <li>Si parsea como double -> {@link Double}</li>
     *   <li>Si no -> {@link String}</li>
     * </ul>
     *
     * @param str string a deserializar
     * @return valor reconstruido
     */
    private Object deserializarValor(String str) {
        if (str == null || str.trim().isEmpty()) return null;

        str = str.trim();

        if (str.startsWith("SET:")) {
            String contenido = str.substring(4);
            if (contenido.isEmpty()) return new HashSet<>();
            return new HashSet<>(Arrays.asList(contenido.split("\\|\\|\\|")));
        }

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return str;
        }
    }

    // ========== GESTIÓN DEL SISTEMA ==========

    /**
     * Fuerza la carga inmediata de todos los datos desde persistencia.
     * <p>
     * Delegado a {@link CtrlPersistencia#cargarTodo()}.
     */
    public void cargarTodoInmediatamente() {
        persistencia.cargarTodo();
    }

    /**
     * Cierra el sistema guardando los datos en persistencia.
     * <p>
     * Delegado a {@link CtrlPersistencia#guardarTodo()}.
     */
    public void cerrarSistema() {
        persistencia.guardarTodo();
        System.out.println("✓ Sistema cerrado correctamente");
    }

    // ========== CONSULTAS ADICIONALES ==========

    /**
     * Indica si un usuario es administrador.
     *
     * @param id identificador del usuario
     * @return {@code true} si es admin; {@code false} en caso contrario
     */
    public boolean esAdmin(String id) {
        return persistencia.esAdmin(id);
    }

    /**
     * Indica si existe un usuario con un ID dado.
     *
     * @param id identificador del usuario
     * @return {@code true} si existe; {@code false} si no
     */
    public boolean existeUsuario(String id) {
        return persistencia.existeUsuario(id);
    }

    /**
     * Indica si existe una encuesta con un ID dado.
     *
     * @param id identificador de la encuesta
     * @return {@code true} si existe; {@code false} si no
     */
    public boolean existeEncuesta(String id) {
        return persistencia.existeEncuesta(id);
    }

    /**
     * Obtiene el índice de una pregunta dentro de una encuesta, dado su ID.
     *
     * @param idEncuesta identificador de la encuesta
     * @param idPregunta identificador de la pregunta
     * @return índice de la pregunta dentro de la encuesta
     * @throws IllegalArgumentException si la encuesta no existe
     */
    public int obtenerIndicePregunta(String idEncuesta, String idPregunta) {
        Encuesta encuesta = obtenerEncuesta(idEncuesta);
        if (encuesta == null) {
            throw new IllegalArgumentException("Encuesta no encontrada: " + idEncuesta);
        }

        return controladorEncuestas.obtenerIndicePregunta(encuesta, idPregunta);
    }
}
