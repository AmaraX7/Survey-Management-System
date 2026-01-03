package main.persistence;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Persistencia en JSON para resultados de clustering.
 * <p>
 * Esta clase almacena y recupera resultados de clustering asociados a una encuesta, usando como
 * clave el {@code idEncuesta}. No conoce clases del dominio como {@code ResultadoClustering};
 * trabaja únicamente con {@link String} que contienen JSON.
 * </p>
 *
 * <p>
 * Diseño:
 * </p>
 * <ul>
 *   <li>Cache en memoria: {@code Map<String, String>} donde la clave es {@code idEncuesta} y el valor es
 *       el JSON de resultados.</li>
 *   <li>Persistencia en disco: un archivo por encuesta con nombre:
 *       {@code clustering_<idEncuesta>.json} dentro de {@code directorioClustering}.</li>
 * </ul>
 *
 * <p>
 * Responsabilidades:
 * </p>
 * <ul>
 *   <li>Guardar resultados (JSON) para una encuesta y persistirlos a disco.</li>
 *   <li>Cargar todos los resultados existentes desde disco al arrancar.</li>
 *   <li>Consultar si existen resultados guardados.</li>
 *   <li>Eliminar resultados (cache + archivo).</li>
 * </ul>
 *
 * <p>
 * NOTA: Esta clase no realiza validación semántica del JSON; solo lo trata como texto.
 * </p>
 */
public class PersistenciaClustering {
    /**
     * Cache en memoria: idEncuesta -> JSON de resultados de clustering.
     */
    private final Map<String, String> resultadosJSON;

    /**
     * Directorio base donde se guardan los archivos de clustering.
     */
    private final String directorioClustering;

    /**
     * Crea la persistencia de clustering inicializando el directorio y cargando todos los resultados
     * existentes desde disco.
     *
     * @param directorioClustering ruta del directorio donde se guardarán los archivos de clustering.
     * @throws RuntimeException si no se puede crear el directorio.
     */
    public PersistenciaClustering(String directorioClustering) {
        this.resultadosJSON = new HashMap<>();
        this.directorioClustering = directorioClustering;

        try {
            Files.createDirectories(Paths.get(directorioClustering));
        } catch (IOException e) {
            throw new RuntimeException("Error creando directorio de clustering", e);
        }

        cargarTodos();
    }

    // ========== API (solo Strings) ==========

    /**
     * Guarda los resultados de clustering de una encuesta como JSON.
     * <p>
     * Almacena el JSON en la cache en memoria y lo persiste en disco en un archivo llamado:
     * {@code clustering_<idEncuesta>.json}.
     * </p>
     *
     * @param idEncuesta identificador de la encuesta a la que pertenecen los resultados.
     * @param resultadosJSON resultados serializados como JSON (texto).
     * @throws IllegalArgumentException si {@code idEncuesta} o {@code resultadosJSON} son null.
     * @throws RuntimeException si ocurre un error al escribir en disco.
     */
    public void guardarResultados(String idEncuesta, String resultadosJSON) {
        if (idEncuesta == null || resultadosJSON == null) {
            throw new IllegalArgumentException("idEncuesta y resultados no pueden ser null");
        }

        this.resultadosJSON.put(idEncuesta, resultadosJSON);
        persistirEncuesta(idEncuesta);
    }

    /**
     * Obtiene los resultados de clustering de una encuesta como JSON.
     *
     * @param idEncuesta identificador de la encuesta.
     * @return JSON de resultados, o {@code null} si {@code idEncuesta} es null o no existe.
     */
    public String obtenerResultados(String idEncuesta) {
        if (idEncuesta == null) return null;
        return resultadosJSON.get(idEncuesta);
    }

    /**
     * Indica si existen resultados guardados para una encuesta.
     * <p>
     * Devuelve {@code true} si en la cache existe la clave {@code idEncuesta} y su valor no es
     * null ni vacío.
     * </p>
     *
     * @param idEncuesta identificador de la encuesta.
     * @return {@code true} si existen resultados guardados; {@code false} en caso contrario.
     */
    public boolean existenResultados(String idEncuesta) {
        if (idEncuesta == null) return false;
        return resultadosJSON.containsKey(idEncuesta)
                && resultadosJSON.get(idEncuesta) != null
                && !resultadosJSON.get(idEncuesta).isEmpty();
    }

    /**
     * Elimina todos los resultados de clustering de una encuesta.
     * <p>
     * Borra tanto la entrada en cache como el archivo correspondiente en disco.
     * Si {@code idEncuesta} es null no hace nada.
     * </p>
     *
     * @param idEncuesta identificador de la encuesta.
     */
    public void eliminarResultados(String idEncuesta) {
        if (idEncuesta != null) {
            resultadosJSON.remove(idEncuesta);
            eliminarArchivoEncuesta(idEncuesta);
        }
    }

    /**
     * Devuelve el conjunto de IDs de encuestas que tienen resultados guardados en cache.
     *
     * @return conjunto con los IDs de encuestas con resultados guardados.
     */
    public Set<String> obtenerEncuestasConResultados() {
        return new HashSet<>(resultadosJSON.keySet());
    }

    // ========== PERSISTENCIA JSON ==========

    /**
     * Carga todos los archivos JSON de resultados presentes en el directorio.
     * <p>
     * Busca archivos con patrón: {@code clustering_*.json}. Por cada archivo, extrae el
     * {@code idEncuesta} del nombre y lo carga en la cache.
     * </p>
     */
    private void cargarTodos() {
        File directorio = new File(directorioClustering);
        if (!directorio.exists()) return;

        File[] archivos = directorio.listFiles((dir, name) ->
                name.startsWith("clustering_") && name.endsWith(".json"));

        if (archivos == null) return;

        for (File archivo : archivos) {
            String nombreArchivo = archivo.getName();
            // Extraer ID de encuesta: clustering_enc_001.json -> enc_001
            String idEncuesta = nombreArchivo
                    .replace("clustering_", "")
                    .replace(".json", "");

            cargarEncuesta(idEncuesta);
        }
    }

    /**
     * Carga los resultados de clustering de una encuesta concreta desde su archivo en disco.
     * <p>
     * Si el archivo no existe, no hace nada. Si falla la lectura, imprime un mensaje en stderr
     * y no lanza excepción (tolerancia a errores).
     * </p>
     *
     * @param idEncuesta identificador de la encuesta.
     */
    private void cargarEncuesta(String idEncuesta) {
        String rutaArchivo = obtenerRutaArchivo(idEncuesta);
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) return;

        try {
            String json = new String(Files.readAllBytes(Paths.get(rutaArchivo)));
            resultadosJSON.put(idEncuesta, json);
        } catch (IOException e) {
            System.err.println("Error cargando resultados de clustering: " + idEncuesta);
        }
    }

    /**
     * Persiste en disco los resultados de clustering asociados a una encuesta.
     * <p>
     * Escribe el JSON tal cual, sin pretty printing ni validación.
     * Si el JSON es null o vacío, no hace nada.
     * </p>
     *
     * @param idEncuesta identificador de la encuesta.
     * @throws RuntimeException si ocurre un error al escribir en disco.
     */
    private void persistirEncuesta(String idEncuesta) {
        String json = resultadosJSON.get(idEncuesta);
        if (json == null || json.isEmpty()) return;

        String rutaArchivo = obtenerRutaArchivo(idEncuesta);

        try {
            Path path = Paths.get(rutaArchivo);
            Files.createDirectories(path.getParent());
            Files.write(path, json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error guardando resultados de clustering: " + idEncuesta, e);
        }
    }

    /**
     * Elimina el archivo JSON de clustering asociado a una encuesta.
     * <p>
     * Si falla, imprime un mensaje en stderr y no lanza excepción.
     * </p>
     *
     * @param idEncuesta identificador de la encuesta.
     */
    private void eliminarArchivoEncuesta(String idEncuesta) {
        try {
            Files.deleteIfExists(Paths.get(obtenerRutaArchivo(idEncuesta)));
        } catch (IOException e) {
            System.err.println("Error eliminando archivo de clustering: " + idEncuesta);
        }
    }

    /**
     * Construye la ruta del archivo JSON de clustering para una encuesta concreta.
     *
     * @param idEncuesta identificador de la encuesta.
     * @return ruta completa del archivo, con nombre {@code clustering_<idEncuesta>.json}.
     */
    private String obtenerRutaArchivo(String idEncuesta) {
        return directorioClustering + "/clustering_" + idEncuesta + ".json";
    }
}
