package main.persistence;

import java.io.IOException;
import java.util.*;

/**
 * Controlador ÚNICO de persistencia del sistema.
 *
 * <p>Responsabilidad principal: actuar como <b>fachada</b> (Facade) de la capa de persistencia.</p>
 * <ul>
 *   <li>Los controladores de dominio (p.ej. {@code CtrlDominio}) <b>SOLO</b> deben hablar con esta clase.</li>
 *   <li>Esta clase delega en persistencias especializadas:
 *       {@link PersistenciaEncuestas}, {@link PersistenciaUsuarios},
 *       {@link PersistenciaRespuestas}, {@link PersistenciaClustering} y {@link PersistenciaCSV}.</li>
 *   <li>Implementa <b>carga bajo demanda</b> (lazy loading) mediante flags para encuestas/usuarios/respuestas.</li>
 * </ul>
 *
 * <h2>Diseño</h2>
 * <ul>
 *   <li><b>Patrón Facade:</b> simplifica el acceso a persistencia ofreciendo una API única.</li>
 *   <li><b>Lazy Loading:</b> evita cargar todo de disco si no es necesario.</li>
 *   <li><b>Delegación:</b> cada persistencia especializada se encarga de su propio formato (JSON/CSV).</li>
 * </ul>
 *
 * <h2>Notas importantes</h2>
 * <ul>
 *   <li>Encuestas/Usuarios/Respuestas usan flags y se cargan al primer acceso.</li>
 *   <li>Clustering no usa flag aquí porque {@link PersistenciaClustering} ya carga en el constructor (según tu implementación).</li>
 *   <li>CSV aquí actúa como utilitario de lectura/escritura de ficheros (no “cachea” estado de dominio).</li>
 * </ul>
 */
public class CtrlPersistencia {
    /**
     * Persistencia especializada de encuestas (JSON por encuesta).
     * Guarda y carga encuestas como {@code String} JSON.
     */
    private final PersistenciaEncuestas persistenciaEncuestas;

    /**
     * Persistencia especializada de usuarios (JSON).
     * Guarda y carga usuarios como {@code String} JSON.
     */
    private final PersistenciaUsuarios persistenciaUsuarios;

    /**
     * Persistencia especializada de respuestas (CSV global).
     * Guarda y carga respuestas como estructuras primitivas:
     * {@code Map<idPregunta, valorComoString>}.
     */
    private final PersistenciaRespuestas persistenciaRespuestas;

    /**
     * Persistencia especializada de resultados de clustering (JSON).
     * Guarda y carga resultados como {@code String} JSON.
     */
    private final PersistenciaClustering persistenciaClustering;

    /**
     * Utilidad de lectura/escritura CSV (no conoce dominio).
     * Se usa para import/export y operaciones CSV genéricas.
     */
    private final PersistenciaCSV persistenciaCSV;

    /**
     * Flag de estado: indica si las encuestas ya fueron cargadas desde disco a memoria.
     */
    private boolean encuestasCargadas = false;

    /**
     * Flag de estado: indica si los usuarios ya fueron cargados desde disco a memoria.
     */
    private boolean usuariosCargados = false;

    /**
     * Flag de estado: indica si las respuestas ya fueron cargadas desde disco a memoria.
     */
    private boolean respuestasCargadas = false;

    /**
     * Construye el controlador de persistencia y crea todas las persistencias especializadas.
     *
     * @param directorioData directorio raíz donde se guardarán los datos (p.ej. "./data").
     */
    public CtrlPersistencia(String directorioData) {
        this.persistenciaEncuestas = new PersistenciaEncuestas(directorioData);
        this.persistenciaUsuarios = new PersistenciaUsuarios(directorioData);
        this.persistenciaRespuestas = new PersistenciaRespuestas(directorioData);
        this.persistenciaClustering = new PersistenciaClustering(directorioData + "/clustering");
        this.persistenciaCSV = new PersistenciaCSV();
    }

    /**
     * Fuerza la carga completa en memoria de encuestas, usuarios y respuestas.
     *
     * <p>Equivalente a llamar a {@link #cargarEncuestas()},
     * {@link #cargarUsuarios()} y {@link #cargarRespuestas()}.</p>
     */
    public void cargarTodo() {
        cargarEncuestas();
        cargarUsuarios();
        cargarRespuestas();
    }

    // =========================================================
    // ENCUESTAS (delegación + lazy load)
    // =========================================================

    /**
     * Guarda una encuesta serializada en JSON.
     *
     * @param encuestaJSON encuesta en formato JSON.
     */
    public void guardarEncuesta(String encuestaJSON) {
        asegurarEncuestasCargadas();
        persistenciaEncuestas.guardar(encuestaJSON);
    }

    /**
     * Obtiene una encuesta serializada en JSON.
     *
     * @param id identificador de la encuesta.
     * @return JSON de la encuesta o {@code null} si no existe.
     */
    public String obtenerEncuesta(String id) {
        asegurarEncuestasCargadas();
        return persistenciaEncuestas.obtener(id);
    }

    /**
     * Devuelve todas las encuestas como lista de JSON Strings.
     *
     * @return lista con JSON de encuestas.
     */
    public List<String> obtenerTodasEncuestas() {
        asegurarEncuestasCargadas();
        return persistenciaEncuestas.obtenerTodas();
    }

    /**
     * Elimina una encuesta por id.
     *
     * @param id id de la encuesta.
     * @return {@code true} si se eliminó, {@code false} si no existía.
     */
    public boolean eliminarEncuesta(String id) {
        asegurarEncuestasCargadas();
        return persistenciaEncuestas.eliminar(id);
    }

    /**
     * Comprueba si existe una encuesta en la caché.
     *
     * @param id id de la encuesta.
     * @return {@code true} si existe, {@code false} en caso contrario.
     */
    public boolean existeEncuesta(String id) {
        asegurarEncuestasCargadas();
        return persistenciaEncuestas.existe(id);
    }

    /**
     * Asegura que las encuestas estén cargadas; si no, las carga.
     */
    private void asegurarEncuestasCargadas() {
        if (!encuestasCargadas) {
            cargarEncuestas();
        }
    }

    /**
     * Carga todas las encuestas desde disco a memoria.
     */
    private void cargarEncuestas() {
        persistenciaEncuestas.cargar();
        encuestasCargadas = true;
        System.out.println("✓ Encuestas cargadas");
    }

    // =========================================================
    // USUARIOS (delegación + lazy load)
    // =========================================================

    /**
     * Guarda un usuario respondedor serializado en JSON.
     *
     * @param usuarioJSON usuario en formato JSON.
     */
    public void guardarRespondedor(String usuarioJSON) {
        asegurarUsuariosCargados();
        persistenciaUsuarios.guardarRespondedor(usuarioJSON);
    }

    /**
     * Guarda un usuario administrador serializado en JSON.
     *
     * @param adminJSON admin en formato JSON.
     */
    public void guardarAdmin(String adminJSON) {
        asegurarUsuariosCargados();
        persistenciaUsuarios.guardarAdmin(adminJSON);
    }

    /**
     * Obtiene un usuario (admin o respondedor) en JSON.
     *
     * @param id id del usuario.
     * @return JSON del usuario o {@code null} si no existe.
     */
    public String obtenerUsuario(String id) {
        asegurarUsuariosCargados();
        return persistenciaUsuarios.obtener(id);
    }

    /**
     * Obtiene un usuario respondedor en JSON.
     *
     * @param id id del usuario.
     * @return JSON del respondedor o {@code null} si no existe.
     */
    public String obtenerRespondedor(String id) {
        asegurarUsuariosCargados();
        return persistenciaUsuarios.obtenerRespondedor(id);
    }

    /**
     * Obtiene un usuario administrador en JSON.
     *
     * @param id id del usuario admin.
     * @return JSON del admin o {@code null} si no existe.
     */
    public String obtenerAdmin(String id) {
        asegurarUsuariosCargados();
        return persistenciaUsuarios.obtenerAdmin(id);
    }

    /**
     * Obtiene todos los respondedores en JSON.
     *
     * @return lista de JSON Strings.
     */
    public List<String> obtenerTodosRespondedores() {
        asegurarUsuariosCargados();
        return persistenciaUsuarios.obtenerTodosRespondedores();
    }

    /**
     * Obtiene todos los administradores en JSON.
     *
     * @return lista de JSON Strings.
     */
    public List<String> obtenerTodosAdmins() {
        asegurarUsuariosCargados();
        return persistenciaUsuarios.obtenerTodosAdmins();
    }

    /**
     * Obtiene todos los usuarios (admins + respondedores) en JSON.
     *
     * @return lista de JSON Strings.
     */
    public List<String> obtenerTodosUsuarios() {
        asegurarUsuariosCargados();
        return persistenciaUsuarios.obtenerTodos();
    }

    /**
     * Comprueba si existe un usuario (admin o respondedor).
     *
     * @param id id del usuario.
     * @return {@code true} si existe.
     */
    public boolean existeUsuario(String id) {
        asegurarUsuariosCargados();
        return persistenciaUsuarios.existe(id);
    }

    /**
     * Determina si un usuario dado es administrador (según caché de persistencia).
     *
     * @param id id del usuario.
     * @return {@code true} si es admin.
     */
    public boolean esAdmin(String id) {
        asegurarUsuariosCargados();
        return persistenciaUsuarios.esAdmin(id);
    }

    /**
     * Asegura que los usuarios estén cargados; si no, los carga.
     */
    private void asegurarUsuariosCargados() {
        if (!usuariosCargados) {
            cargarUsuarios();
        }
    }

    /**
     * Carga usuarios desde disco a memoria.
     */
    private void cargarUsuarios() {
        persistenciaUsuarios.cargar();
        usuariosCargados = true;
        System.out.println("✓ Usuarios cargados");
    }

    // =========================================================
    // RESPUESTAS (delegación + lazy load)
    // =========================================================

    /**
     * Guarda las respuestas de un usuario para una encuesta.
     *
     * @param idUsuario id del usuario.
     * @param idEncuesta id de la encuesta.
     * @param respuestas mapa {@code idPregunta -> valorComoString}.
     */
    public void guardarRespuestas(String idUsuario, String idEncuesta,
                                  Map<String, String> respuestas) {
        asegurarRespuestasCargadas();
        persistenciaRespuestas.guardarRespuestas(idUsuario, idEncuesta, respuestas);
    }

    /**
     * Obtiene las respuestas de un usuario para una encuesta.
     *
     * @param idUsuario id del usuario.
     * @param idEncuesta id de la encuesta.
     * @return mapa {@code idPregunta -> valorComoString} (copia defensiva).
     */
    public Map<String, String> obtenerRespuestas(String idUsuario, String idEncuesta) {
        asegurarRespuestasCargadas();
        return persistenciaRespuestas.obtenerRespuestas(idUsuario, idEncuesta);
    }

    /**
     * Devuelve la lista de IDs de usuarios que han respondido una encuesta (según persistencia).
     *
     * @param idEncuesta id de la encuesta.
     * @return lista de ids de usuarios.
     */
    public List<String> obtenerUsuariosQueRespondieron(String idEncuesta) {
        asegurarRespuestasCargadas();
        return persistenciaRespuestas.obtenerUsuariosQueRespondieron(idEncuesta);
    }

    /**
     * Elimina todas las respuestas asociadas a una encuesta.
     *
     * @param idEncuesta id de la encuesta.
     */
    public void eliminarRespuestasEncuesta(String idEncuesta) {
        asegurarRespuestasCargadas();
        persistenciaRespuestas.eliminarRespuestasEncuesta(idEncuesta);
    }

    /**
     * Asegura que las respuestas estén cargadas; si no, las carga.
     */
    private void asegurarRespuestasCargadas() {
        if (!respuestasCargadas) {
            cargarRespuestas();
        }
    }

    /**
     * Carga respuestas desde disco a memoria.
     */
    private void cargarRespuestas() {
        persistenciaRespuestas.cargar();
        respuestasCargadas = true;
        System.out.println("✓ Respuestas cargadas");
    }

    // =========================================================
    // CLUSTERING (delegación)
    // =========================================================

    /**
     * Guarda resultados de clustering asociados a una encuesta.
     *
     * @param idEncuesta id de la encuesta.
     * @param resultadosJSON resultados en formato JSON.
     */
    public void guardarResultadosClustering(String idEncuesta, String resultadosJSON) {
        persistenciaClustering.guardarResultados(idEncuesta, resultadosJSON);
    }

    /**
     * Obtiene resultados de clustering guardados.
     *
     * @param idEncuesta id de la encuesta.
     * @return JSON de resultados o {@code null} si no hay.
     */
    public String obtenerResultadosClustering(String idEncuesta) {
        return persistenciaClustering.obtenerResultados(idEncuesta);
    }

    /**
     * Indica si existen resultados de clustering guardados para una encuesta.
     *
     * @param idEncuesta id de la encuesta.
     * @return {@code true} si existen resultados no vacíos.
     */
    public boolean existenResultadosClustering(String idEncuesta) {
        return persistenciaClustering.existenResultados(idEncuesta);
    }

    /**
     * Elimina los resultados de clustering guardados para una encuesta.
     *
     * @param idEncuesta id de la encuesta.
     */
    public void eliminarResultadosClustering(String idEncuesta) {
        persistenciaClustering.eliminarResultados(idEncuesta);
    }

    /**
     * Devuelve el conjunto de encuestas que tienen resultados de clustering guardados.
     *
     * @return set de IDs de encuestas.
     */
    public Set<String> obtenerEncuestasConClustering() {
        return persistenciaClustering.obtenerEncuestasConResultados();
    }

    // =========================================================
    // CSV (delegación)
    // =========================================================

    /**
     * Lee un archivo CSV con encabezados.
     *
     * <p>La primera fila se interpreta como encabezados, y las siguientes como filas de datos.
     * La implementación concreta delega en {@link PersistenciaCSV#leerConEncabezados(String)}.</p>
     *
     * @param rutaArchivo ruta del archivo a leer.
     * @return {@link PersistenciaCSV.DatosCSV} con encabezados y filas.
     * @throws IOException si ocurre un error leyendo el archivo.
     */
    public PersistenciaCSV.DatosCSV leerCSV(String rutaArchivo) throws IOException {
        return persistenciaCSV.leerConEncabezados(rutaArchivo);
    }

    /**
     * Escribe un archivo CSV con encabezados.
     *
     * @param rutaArchivo ruta del archivo a escribir.
     * @param encabezados primera fila del CSV.
     * @param filas filas de datos (sin incluir encabezados).
     * @throws IOException si ocurre un error escribiendo el archivo.
     */
    public void escribirCSV(String rutaArchivo, String[] encabezados,
                            List<String[]> filas) throws IOException {
        persistenciaCSV.escribirConEncabezados(rutaArchivo, encabezados, filas);
    }

    /**
     * Lee un archivo CSV completo (sin estructura especial de encabezados).
     *
     * @param rutaArchivo ruta del archivo.
     * @return lista de filas crudas ({@code String[]} por fila).
     * @throws IOException si ocurre un error leyendo el archivo.
     */
    public List<String[]> leerCSVRaw(String rutaArchivo) throws IOException {
        return persistenciaCSV.leerArchivo(rutaArchivo);
    }

    /**
     * Escribe un archivo CSV completo (sin separar encabezados).
     *
     * @param rutaArchivo ruta del archivo.
     * @param filas filas crudas ({@code String[]} por fila).
     * @throws IOException si ocurre un error escribiendo el archivo.
     */
    public void escribirCSVRaw(String rutaArchivo, List<String[]> filas) throws IOException {
        persistenciaCSV.escribirArchivo(rutaArchivo, filas);
    }

    // =========================================================
    // GUARDADO FINAL
    // =========================================================

    /**
     * Fuerza el guardado “final” (flush) de la persistencia que esté cargada.
     *
     * <p>Solo guarda usuarios y respuestas si fueron cargados previamente.
     * Esto evita I/O innecesario si no se usaron en la ejecución.</p>
     */
    public void guardarTodo() {
        if (usuariosCargados) {
            persistenciaUsuarios.guardar();
            System.out.println("✓ Usuarios guardados");
        }

        if (respuestasCargadas) {
            persistenciaRespuestas.guardar();
            System.out.println("✓ Respuestas guardadas");
        }

        System.out.println("✓ Toda la persistencia guardada");
    }
}
