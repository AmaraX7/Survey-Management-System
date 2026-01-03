package main.persistence;

import java.io.*;
import java.util.*;

/**
 * Persistencia de respuestas en CSV.
 *
 * <p>Este componente persiste respuestas de usuarios a encuestas en un único fichero CSV.
 * Importante: esta clase <b>NO</b> conoce la clase de dominio {@code Respuesta} ni depende de ella.
 * Trabaja exclusivamente con tipos primitivos/estructuras básicas:
 * {@code Map<String, String>} donde la clave es {@code idPregunta} y el valor es la respuesta
 * ya serializada como {@link String}.</p>
 *
 * <h2>Modelo en memoria (cache)</h2>
 * <pre>
 * cache: idEncuesta -> idUsuario -> (idPregunta -> valorComoString)
 * </pre>
 *
 * <h2>Formato CSV</h2>
 * <pre>
 * ID_Encuesta, ID_Usuario, ID_Pregunta, Valor
 * </pre>
 *
 * <p>Al guardar, se reescribe el fichero completo a partir del {@code cache} (persistencia "full rewrite").
 * Al cargar, se reconstruye el {@code cache} leyendo el CSV.</p>
 */
public class PersistenciaRespuestas {
    /**
     * Cache principal en memoria:
     * {@code idEncuesta -> idUsuario -> (idPregunta -> valorComoString)}.
     */
    private final Map<String, Map<String, Map<String, String>>> cache;

    /** Ruta del archivo CSV que contiene todas las respuestas. */
    private final String archivoRespuestas;

    /** Lector CSV para leer todas las filas del fichero. */
    private final LectorCSV lector;

    /** Escritor CSV para sobrescribir el fichero con las filas actualizadas. */
    private final EscritorCSV escritor;

    /**
     * Construye el componente de persistencia de respuestas.
     *
     * <p>Inicializa el cache y define la ruta del fichero:
     * {@code <directorioData>/respuestas/todas_respuestas.csv}.</p>
     *
     * <p>También intenta crear el directorio {@code <directorioData>/respuestas} si no existe.</p>
     *
     * @param directorioData directorio base del sistema de persistencia (por ejemplo {@code "./data"}).
     * @throws RuntimeException si ocurre un error creando el directorio.
     */
    public PersistenciaRespuestas(String directorioData) {
        this.cache = new HashMap<>();

        String directorioRespuestas = directorioData + "/respuestas";
        this.archivoRespuestas = directorioRespuestas + "/todas_respuestas.csv";

        this.lector = new LectorCSV();
        this.escritor = new EscritorCSV();

        try {
            new File(directorioRespuestas).mkdirs();
        } catch (Exception e) {
            throw new RuntimeException("Error creando directorio", e);
        }
    }

    // ========== API (solo tipos primitivos) ==========

    /**
     * Guarda las respuestas (ya serializadas) de un usuario para una encuesta.
     *
     * <p>Semántica: reemplaza el conjunto completo de respuestas del usuario para esa encuesta
     * por el map proporcionado (copia defensiva). Después persiste todo el cache a disco.</p>
     *
     * @param idUsuario  identificador del usuario que responde.
     * @param idEncuesta identificador de la encuesta respondida.
     * @param respuestas mapa {@code idPregunta -> valorComoString}.
     */
    public void guardarRespuestas(String idUsuario, String idEncuesta,
                                  Map<String, String> respuestas) {
        Map<String, Map<String, String>> respuestasEncuesta =
                cache.computeIfAbsent(idEncuesta, k -> new HashMap<>());

        respuestasEncuesta.put(idUsuario, new HashMap<>(respuestas));
        persistirTodo();
    }

    /**
     * Obtiene las respuestas (serializadas) de un usuario para una encuesta.
     *
     * <p>Devuelve siempre una copia: modificar el map retornado no afecta al cache interno.</p>
     *
     * @param idUsuario  identificador del usuario.
     * @param idEncuesta identificador de la encuesta.
     * @return mapa {@code idPregunta -> valorComoString}. Si no hay datos, retorna un map vacío.
     */
    public Map<String, String> obtenerRespuestas(String idUsuario, String idEncuesta) {
        Map<String, Map<String, String>> respuestasEncuesta = cache.get(idEncuesta);
        if (respuestasEncuesta == null) return new HashMap<>();

        Map<String, String> respuestas = respuestasEncuesta.get(idUsuario);
        return respuestas != null ? new HashMap<>(respuestas) : new HashMap<>();
    }

    /**
     * Devuelve los IDs de usuarios que han respondido una encuesta (según el cache).
     *
     * @param idEncuesta identificador de la encuesta.
     * @return lista de IDs de usuario. Si no hay respuestas para esa encuesta, retorna lista vacía.
     */
    public List<String> obtenerUsuariosQueRespondieron(String idEncuesta) {
        Map<String, Map<String, String>> respuestasEncuesta = cache.get(idEncuesta);
        return respuestasEncuesta != null ?
                new ArrayList<>(respuestasEncuesta.keySet()) :
                new ArrayList<>();
    }

    /**
     * Elimina todas las respuestas asociadas a una encuesta.
     *
     * <p>Actualiza el cache y persiste el fichero completo.</p>
     *
     * @param idEncuesta identificador de la encuesta a eliminar.
     */
    public void eliminarRespuestasEncuesta(String idEncuesta) {
        cache.remove(idEncuesta);
        persistirTodo();
    }

    // ========== PERSISTENCIA CSV ==========

    /**
     * Persiste TODO el cache en el archivo CSV {@link #archivoRespuestas}.
     *
     * <p>Estrategia: se construye una lista de filas comenzando por el encabezado, y luego
     * se recorren todas las entradas del cache para producir filas del estilo:</p>
     *
     * <pre>
     * [ID_Encuesta, ID_Usuario, ID_Pregunta, Valor]
     * </pre>
     *
     * <p>Finalmente, se escribe el fichero completo mediante {@link EscritorCSV}.</p>
     *
     * <p>Nota: en caso de error, se imprime por stderr y no se relanza excepción (comportamiento actual).</p>
     */
    private void persistirTodo() {
        try {
            File archivo = new File(archivoRespuestas);
            archivo.getParentFile().mkdirs();

            List<String[]> filas = new ArrayList<>();
            filas.add(new String[]{"ID_Encuesta", "ID_Usuario", "ID_Pregunta", "Valor"});

            for (Map.Entry<String, Map<String, Map<String, String>>> entryEncuesta :
                    cache.entrySet()) {

                String idEncuesta = entryEncuesta.getKey();
                Map<String, Map<String, String>> respuestasUsuarios = entryEncuesta.getValue();

                for (Map.Entry<String, Map<String, String>> entryUsuario :
                        respuestasUsuarios.entrySet()) {

                    String idUsuario = entryUsuario.getKey();
                    Map<String, String> respuestas = entryUsuario.getValue();

                    for (Map.Entry<String, String> respuesta : respuestas.entrySet()) {
                        filas.add(new String[]{
                                idEncuesta,
                                idUsuario,
                                respuesta.getKey(),
                                respuesta.getValue()
                        });
                    }
                }
            }

            escritor.escribirArchivo(archivoRespuestas, filas);

        } catch (Exception e) {
            System.err.println("Error guardando respuestas: " + e.getMessage());
        }
    }

    /**
     * Carga las respuestas desde el archivo CSV {@link #archivoRespuestas} reconstruyendo el cache.
     *
     * <p>Si el archivo no existe, no hace nada.</p>
     *
     * <p>Asume que la primera línea es encabezado. Para cada fila posterior espera al menos 4 columnas:
     * {@code idEncuesta, idUsuario, idPregunta, valor}.</p>
     *
     * <p>Nota: en caso de error, se imprime por stderr y no se relanza excepción (comportamiento actual).</p>
     */
    public void cargar() {
        File archivo = new File(archivoRespuestas);
        if (!archivo.exists()) return;

        try {
            List<String[]> lineas = lector.leerArchivo(archivoRespuestas);
            if (lineas.size() <= 1) return;

            cache.clear();

            for (int i = 1; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                if (fila.length < 4) continue;

                String idEncuesta = fila[0].trim();
                String idUsuario = fila[1].trim();
                String idPregunta = fila[2].trim();
                String valor = fila[3];

                Map<String, Map<String, String>> respuestasEncuesta =
                        cache.computeIfAbsent(idEncuesta, k -> new HashMap<>());

                Map<String, String> respuestasUsuario =
                        respuestasEncuesta.computeIfAbsent(idUsuario, k -> new HashMap<>());

                respuestasUsuario.put(idPregunta, valor);
            }

        } catch (Exception e) {
            System.err.println("Error cargando respuestas: " + e.getMessage());
        }
    }

    /**
     * Fuerza el guardado del estado actual del cache en disco.
     *
     * <p>Actualmente equivale a llamar a {@link #persistirTodo()}.</p>
     */
    public void guardar() {
        persistirTodo();
    }
}
