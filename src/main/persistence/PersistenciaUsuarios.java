package main.persistence;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Componente de persistencia de usuarios basado en JSON.
 *
 * <p>Esta clase mantiene en memoria dos mapas {@code Map<String, String>} que asocian el
 * identificador {@code id} de cada usuario con su representación JSON (en forma de {@link String}).</p>
 *
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Almacenar en memoria los JSON de respondedores y administradores.</li>
 *   <li>Guardar en disco dichos JSON en ficheros separados.</li>
 *   <li>Cargar desde disco los JSON y reconstruir los mapas en memoria.</li>
 * </ul>
 *
 * <p>Nota: Esta clase trabaja con {@link String} que contienen JSON y utiliza {@link Gson}
 * para pretty-printing al persistir.</p>
 */
public class PersistenciaUsuarios {
    /** Mapa {@code id -> JSON} para usuarios respondedores. */
    private final Map<String, String> respondedoresJSON;

    /** Mapa {@code id -> JSON} para usuarios administradores. */
    private final Map<String, String> adminsJSON;

    /** Ruta del archivo JSON donde se guardan los respondedores. */
    private final String archivoRespondedores;

    /** Ruta del archivo JSON donde se guardan los administradores. */
    private final String archivoAdmins;

    /** Instancia de Gson usada para escribir JSON con formato legible. */
    private final Gson gson;

    /**
     * Construye el componente de persistencia de usuarios.
     *
     * <p>Inicializa las estructuras en memoria, construye las rutas a los archivos en el
     * subdirectorio {@code /usuarios} dentro de {@code directorioData}, e intenta crear
     * dicho directorio si no existe.</p>
     *
     * @param directorioData ruta base del directorio de datos del sistema (por ejemplo, {@code "./data"}).
     * @throws RuntimeException si ocurre un error creando el directorio de usuarios.
     */
    public PersistenciaUsuarios(String directorioData) {
        this.respondedoresJSON = new HashMap<>();
        this.adminsJSON = new HashMap<>();

        String directorioUsuarios = directorioData + "/usuarios";
        this.archivoRespondedores = directorioUsuarios + "/respondedores.json";
        this.archivoAdmins = directorioUsuarios + "/administradores.json";
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.createDirectories(Paths.get(directorioUsuarios));
        } catch (IOException e) {
            throw new RuntimeException("Error creando directorio", e);
        }
    }

    /**
     * Guarda (en memoria y en disco) un usuario respondedor a partir de su JSON.
     *
     * <p>Extrae el campo {@code id} del JSON recibido, lo usa como clave en el mapa
     * {@code respondedoresJSON}, y persiste el conjunto completo en el archivo de respondedores.</p>
     *
     * @param usuarioJSON JSON del usuario respondedor (debe contener el campo {@code "id"}).
     * @throws com.google.gson.JsonParseException si el JSON no es válido o no tiene el formato esperado.
     * @throws NullPointerException si el JSON no contiene el campo {@code "id"}.
     * @throws RuntimeException si ocurre un error de E/S al guardar en disco.
     */
    public void guardarRespondedor(String usuarioJSON) {
        // Extraer el ID del JSON
        JsonObject obj = JsonParser.parseString(usuarioJSON).getAsJsonObject();
        String id = obj.get("id").getAsString();

        respondedoresJSON.put(id, usuarioJSON);
        guardarRespondedores();
    }

    /**
     * Guarda (en memoria y en disco) un usuario administrador a partir de su JSON.
     *
     * <p>Extrae el campo {@code id} del JSON recibido, lo usa como clave en el mapa
     * {@code adminsJSON}, y persiste el conjunto completo en el archivo de administradores.</p>
     *
     * @param adminJSON JSON del usuario administrador (debe contener el campo {@code "id"}).
     * @throws com.google.gson.JsonParseException si el JSON no es válido o no tiene el formato esperado.
     * @throws NullPointerException si el JSON no contiene el campo {@code "id"}.
     * @throws RuntimeException si ocurre un error de E/S al guardar en disco.
     */
    public void guardarAdmin(String adminJSON) {
        JsonObject obj = JsonParser.parseString(adminJSON).getAsJsonObject();
        String id = obj.get("id").getAsString();

        adminsJSON.put(id, adminJSON);
        guardarAdmins();
    }

    /**
     * Obtiene el JSON de un usuario por su {@code id}, buscando primero en administradores
     * y si no existe, en respondedores.
     *
     * @param id identificador del usuario.
     * @return JSON del usuario si existe; {@code null} en caso contrario.
     */
    public String obtener(String id) {
        String json = adminsJSON.get(id);
        if (json != null) return json;
        return respondedoresJSON.get(id);
    }

    /**
     * Obtiene el JSON de un usuario respondedor por {@code id}.
     *
     * @param id identificador del usuario respondedor.
     * @return JSON del respondedor si existe; {@code null} si no existe.
     */
    public String obtenerRespondedor(String id) {
        return respondedoresJSON.get(id);
    }

    /**
     * Obtiene el JSON de un usuario administrador por {@code id}.
     *
     * @param id identificador del usuario administrador.
     * @return JSON del administrador si existe; {@code null} si no existe.
     */
    public String obtenerAdmin(String id) {
        return adminsJSON.get(id);
    }

    /**
     * Devuelve todos los JSON de usuarios respondedores.
     *
     * @return lista con copias de los valores del mapa de respondedores.
     */
    public List<String> obtenerTodosRespondedores() {
        return new ArrayList<>(respondedoresJSON.values());
    }

    /**
     * Devuelve todos los JSON de usuarios administradores.
     *
     * @return lista con copias de los valores del mapa de administradores.
     */
    public List<String> obtenerTodosAdmins() {
        return new ArrayList<>(adminsJSON.values());
    }

    /**
     * Devuelve todos los usuarios (administradores y respondedores) en una única lista.
     *
     * @return lista con los JSON de administradores seguidos de los JSON de respondedores.
     */
    public List<String> obtenerTodos() {
        List<String> todos = new ArrayList<>();
        todos.addAll(adminsJSON.values());
        todos.addAll(respondedoresJSON.values());
        return todos;
    }

    /**
     * Indica si existe algún usuario (admin o respondedor) con el {@code id} dado.
     *
     * @param id identificador del usuario.
     * @return {@code true} si el id existe en cualquiera de los dos mapas; {@code false} en caso contrario.
     */
    public boolean existe(String id) {
        return adminsJSON.containsKey(id) || respondedoresJSON.containsKey(id);
    }

    /**
     * Indica si el usuario con {@code id} dado es administrador.
     *
     * @param id identificador del usuario.
     * @return {@code true} si el id pertenece a un administrador; {@code false} en caso contrario.
     */
    public boolean esAdmin(String id) {
        return adminsJSON.containsKey(id);
    }

    /**
     * Carga desde disco los usuarios respondedores y administradores en memoria.
     *
     * <p>Equivale a ejecutar {@link #cargarRespondedores()} y {@link #cargarAdmins()}.</p>
     *
     * @throws RuntimeException si ocurre un error de E/S al leer archivos.
     */
    public void cargar() {
        cargarRespondedores();
        cargarAdmins();
    }

    /**
     * Guarda en disco los usuarios respondedores y administradores presentes en memoria.
     *
     * <p>Equivale a ejecutar {@link #guardarRespondedores()} y {@link #guardarAdmins()}.</p>
     *
     * @throws RuntimeException si ocurre un error de E/S al escribir archivos.
     */
    public void guardar() {
        guardarRespondedores();
        guardarAdmins();
    }

    /**
     * Carga el archivo de respondedores desde disco y reconstruye el mapa {@code respondedoresJSON}.
     *
     * <p>Si el archivo no existe, el método no hace nada.</p>
     *
     * @throws RuntimeException si ocurre un error leyendo el archivo.
     */
    private void cargarRespondedores() {
        File archivo = new File(archivoRespondedores);
        if (!archivo.exists()) return;

        try (Reader reader = new FileReader(archivo)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            respondedoresJSON.clear();

            for (JsonElement elem : array) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.get("id").getAsString();
                respondedoresJSON.put(id, obj.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error cargando respondedores", e);
        }
    }

    /**
     * Carga el archivo de administradores desde disco y reconstruye el mapa {@code adminsJSON}.
     *
     * <p>Si el archivo no existe, el método no hace nada.</p>
     *
     * @throws RuntimeException si ocurre un error leyendo el archivo.
     */
    private void cargarAdmins() {
        File archivo = new File(archivoAdmins);
        if (!archivo.exists()) return;

        try (Reader reader = new FileReader(archivo)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            adminsJSON.clear();

            for (JsonElement elem : array) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.get("id").getAsString();
                adminsJSON.put(id, obj.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error cargando admins", e);
        }
    }

    /**
     * Persiste en disco el conjunto completo de respondedores almacenados en memoria.
     *
     * <p>Escribe un {@link JsonArray} donde cada elemento es el JSON parseado de un respondedor.
     * El archivo se escribe con pretty printing usando {@link #gson}.</p>
     *
     * @throws RuntimeException si ocurre un error de E/S al escribir el archivo.
     */
    private void guardarRespondedores() {
        try {
            File archivo = new File(archivoRespondedores);
            archivo.getParentFile().mkdirs();

            JsonArray array = new JsonArray();
            for (String json : respondedoresJSON.values()) {
                array.add(JsonParser.parseString(json));
            }

            try (Writer writer = new FileWriter(archivo)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error guardando respondedores", e);
        }
    }

    /**
     * Persiste en disco el conjunto completo de administradores almacenados en memoria.
     *
     * <p>Escribe un {@link JsonArray} donde cada elemento es el JSON parseado de un administrador.
     * El archivo se escribe con pretty printing usando {@link #gson}.</p>
     *
     * @throws RuntimeException si ocurre un error de E/S al escribir el archivo.
     */
    private void guardarAdmins() {
        try {
            File archivo = new File(archivoAdmins);
            archivo.getParentFile().mkdirs();

            JsonArray array = new JsonArray();
            for (String json : adminsJSON.values()) {
                array.add(JsonParser.parseString(json));
            }

            try (Writer writer = new FileWriter(archivo)) {
                gson.toJson(array, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error guardando admins", e);
        }
    }
}
