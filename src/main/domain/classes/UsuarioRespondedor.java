package main.domain.classes;

import java.util.*;

/**
 * Representa un usuario que responde encuestas dentro del sistema.
 * <p>
 * Un {@code UsuarioRespondedor} es una especialización de {@link Usuario} que
 * mantiene las respuestas que el usuario ha proporcionado, organizadas por
 * encuesta.
 */
public class UsuarioRespondedor extends Usuario {

    /**
     * Mapa que asocia cada encuesta con la lista de respuestas del usuario.
     * La clave es el identificador de la encuesta.
     */
    private final Map<String, List<Respuesta>> respuestasPorEncuesta;

    /**
     * Crea un usuario respondedor con identificador y nombre.
     *
     * @param id     identificador único del usuario
     * @param nombre nombre del usuario
     */
    public UsuarioRespondedor(String id, String nombre) {
        super(id, nombre);
        this.respuestasPorEncuesta = new HashMap<>();
    }

    /**
     * Crea un usuario respondedor con identificador, nombre y contraseña.
     *
     * @param id       identificador único del usuario
     * @param nombre   nombre del usuario
     * @param password contraseña del usuario
     */
    public UsuarioRespondedor(String id, String nombre, String password) {
        super(id, nombre, password);
        this.respuestasPorEncuesta = new HashMap<>();
    }

    /**
     * Añade una respuesta del usuario a una encuesta concreta.
     *
     * @param idEncuesta identificador de la encuesta
     * @param respuesta  respuesta proporcionada por el usuario
     */
    public void addRespuesta(String idEncuesta, Respuesta respuesta) {
        respuestasPorEncuesta
                .computeIfAbsent(idEncuesta, k -> new ArrayList<>())
                .add(respuesta);
    }

    /**
     * Devuelve las respuestas del usuario para una encuesta concreta.
     * <p>
     * Se devuelve una copia de la lista para preservar el encapsulamiento.
     *
     * @param idEncuesta identificador de la encuesta
     * @return lista de respuestas del usuario para la encuesta indicada
     */
    public List<Respuesta> getRespuestasEncuesta(String idEncuesta) {
        return new ArrayList<>(
                respuestasPorEncuesta.getOrDefault(idEncuesta, new ArrayList<>())
        );
    }

    /**
     * Indica si el usuario ha respondido al menos una pregunta de una encuesta.
     *
     * @param idEncuesta identificador de la encuesta
     * @return {@code true} si el usuario ha respondido la encuesta;
     *         {@code false} en caso contrario
     */
    public boolean haRespondidoEncuesta(String idEncuesta) {
        return respuestasPorEncuesta.containsKey(idEncuesta)
                && !respuestasPorEncuesta.get(idEncuesta).isEmpty();
    }

    /**
     * Devuelve el número de encuestas que el usuario ha respondido.
     *
     * @return número de encuestas respondidas
     */
    public int getNumeroEncuestasRespondidas() {
        int contador = 0;
        for (List<Respuesta> lista : respuestasPorEncuesta.values()) {
            if (lista != null && !lista.isEmpty()) {
                contador++;
            }
        }
        return contador;
    }

    /**
     * Elimina todas las respuestas asociadas a una encuesta concreta.
     *
     * @param idEncuesta identificador de la encuesta
     */
    public void eliminarRespuestasEncuesta(String idEncuesta) {
        respuestasPorEncuesta.remove(idEncuesta);
    }
}
