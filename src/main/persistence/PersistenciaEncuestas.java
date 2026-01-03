package main.persistence;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Persistencia de encuestas en JSON
 * NO conoce la clase Encuesta directamente
 * Trabaja con Strings (JSON)
 */
public class PersistenciaEncuestas {
    private final Map<String, String> encuestasJSON;
    private final String directorioEncuestas;

    public PersistenciaEncuestas(String directorioData) {
        this.encuestasJSON = new HashMap<>();

        this.directorioEncuestas = directorioData + "/encuestas";

        try {
            Files.createDirectories(Paths.get(directorioEncuestas));
        } catch (IOException e) {
            throw new RuntimeException("Error creando directorio de encuestas", e);
        }
    }

    // ========== API (solo Strings) ==========

    /**
     * Guarda una encuesta como JSON String
     * @param encuestaJSON encuesta serializada como JSON
     */
    public void guardar(String encuestaJSON) {
        // Extraer el ID del JSON
        JsonObject obj = JsonParser.parseString(encuestaJSON).getAsJsonObject();
        String id = obj.get("id").getAsString();

        encuestasJSON.put(id, encuestaJSON);
        persistirEncuesta(id);
    }

    /**
     * Obtiene una encuesta como JSON String
     * @return JSON de la encuesta o null si no existe
     */
    public String obtener(String id) {
        return encuestasJSON.get(id);
    }

    /**
     * Obtiene todas las encuestas como lista de JSON Strings
     */
    public List<String> obtenerTodas() {
        return new ArrayList<>(encuestasJSON.values());
    }

    /**
     * Elimina una encuesta
     */
    public boolean eliminar(String id) {
        String eliminado = encuestasJSON.remove(id);
        if (eliminado != null) {
            eliminarArchivoEncuesta(id);
            return true;
        }
        return false;
    }

    /**
     * Verifica si existe una encuesta
     */
    public boolean existe(String id) {
        return encuestasJSON.containsKey(id);
    }

    /**
     * Cuenta las encuestas
     */
    public int contar() {
        return encuestasJSON.size();
    }

    // ========== PERSISTENCIA JSON ==========

    /**
     * Carga todas las encuestas desde archivos JSON individuales
     */
    public void cargar() {
        File directorio = new File(directorioEncuestas);
        if (!directorio.exists()) return;

        File[] archivos = directorio.listFiles((dir, name) -> name.endsWith(".json"));
        if (archivos == null) return;

        for (File archivo : archivos) {
            String id = archivo.getName().replace(".json", "");
            cargarEncuesta(id);
        }
    }

    /**
     * Carga una encuesta específica
     */
    private void cargarEncuesta(String id) {
        String rutaArchivo = obtenerRutaArchivo(id);
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) return;

        try {
            String json = new String(Files.readAllBytes(Paths.get(rutaArchivo)));
            encuestasJSON.put(id, json);
        } catch (IOException e) {
            System.err.println("Error cargando encuesta: " + id);
        }
    }

    /**
     * Persiste una encuesta a su archivo JSON individual
     */
    private void persistirEncuesta(String id) {
        String json = encuestasJSON.get(id);
        if (json == null || json.isEmpty()) return;

        String rutaArchivo = obtenerRutaArchivo(id);

        try {
            Path path = Paths.get(rutaArchivo);
            Files.createDirectories(path.getParent());

            // Formatear JSON con pretty printing
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = JsonParser.parseString(json);
            String jsonFormateado = gson.toJson(jsonElement);

            Files.write(path, jsonFormateado.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error guardando encuesta: " + id, e);
        }
    }

    /**
     * Elimina el archivo de una encuesta
     */
    private void eliminarArchivoEncuesta(String id) {
        try {
            Files.deleteIfExists(Paths.get(obtenerRutaArchivo(id)));
        } catch (IOException e) {
            System.err.println("Error eliminando archivo de encuesta: " + id);
        }
    }

    /**
     * Obtiene la ruta del archivo de una encuesta
     */
    private String obtenerRutaArchivo(String id) {
        return directorioEncuestas + "/" + id + ".json";
    }
}
