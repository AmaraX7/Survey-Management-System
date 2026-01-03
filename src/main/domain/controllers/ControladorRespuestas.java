package main.domain.controllers;

import main.domain.classes.*;
import java.util.*;

/**
 * Controlador de lógica de negocio para la gestión de respuestas.
 * <p>
 * Esta clase centraliza la creación y validación de {@link Respuesta} asociadas a un
 * {@link UsuarioRespondedor} y una {@link Encuesta}.
 * <p>
 * No interactúa con persistencia: trabaja únicamente con objetos en memoria.
 */
public class ControladorRespuestas {

    /**
     * Construye un controlador de respuestas sin dependencias externas.
     */
    public ControladorRespuestas() {
        // Sin dependencias de persistencia
    }

    // ========== RESPONDER ENCUESTA ==========

    /**
     * Registra un conjunto de respuestas para una encuesta.
     * <p>
     * Para cada entrada (idPregunta -&gt; valor) se busca la {@link Pregunta} en la
     * {@link Encuesta}, se valida el valor con {@link Pregunta#validarRespuesta(Object)}
     * (si el valor no es {@code null}), y se crea una instancia de {@link Respuesta}.
     * <p>
     * Cada respuesta creada se añade al usuario mediante {@link UsuarioRespondedor#addRespuesta(String, Respuesta)}
     * y se devuelve en un mapa indexado por id de pregunta.
     *
     * @param usuario   usuario respondedor que responde la encuesta
     * @param encuesta  encuesta a la que pertenecen las preguntas
     * @param respuestas mapa con pares (idPregunta, valorRespuesta)
     * @return mapa con las {@link Respuesta} creadas, indexado por id de pregunta
     * @throws IllegalArgumentException si {@code usuario} o {@code encuesta} son {@code null},
     *                                  si una pregunta no existe en la encuesta,
     *                                  o si algún valor no es válido para su pregunta
     */
    public Map<String, Respuesta> responderEncuesta(
            UsuarioRespondedor usuario,
            Encuesta encuesta,
            Map<String, Object> respuestas) {

        if (usuario == null || encuesta == null) {
            throw new IllegalArgumentException("Usuario o encuesta no pueden ser null");
        }

        Map<String, Respuesta> respuestasCreadas = new HashMap<>();
        String idUsuario = usuario.getId();
        String idEncuesta = encuesta.getId();

        for (Map.Entry<String, Object> entry : respuestas.entrySet()) {
            String idPregunta = entry.getKey();
            Object valor = entry.getValue();

            Pregunta pregunta = buscarPregunta(encuesta, idPregunta);
            if (pregunta == null) {
                throw new IllegalArgumentException("Pregunta no encontrada: " + idPregunta);
            }

            if (valor != null && !pregunta.validarRespuesta(valor)) {
                throw new IllegalArgumentException(
                        "Valor no válido para pregunta " + idPregunta);
            }

            Respuesta respuesta = new Respuesta(
                    idUsuario, idPregunta, idEncuesta, valor);

            usuario.addRespuesta(idEncuesta, respuesta);
            respuestasCreadas.put(idPregunta, respuesta);
        }

        return respuestasCreadas;
    }

    // ========== RESPONDER PREGUNTA INDIVIDUAL ==========

    /**
     * Registra la respuesta a una pregunta individual dentro de una encuesta.
     * <p>
     * Busca la {@link Pregunta} por ID en la {@link Encuesta} y valida el valor
     * con {@link Pregunta#validarRespuesta(Object)} (si el valor no es {@code null}).
     * Si es válido, crea una {@link Respuesta} y la asocia al usuario mediante
     * {@link UsuarioRespondedor#addRespuesta(String, Respuesta)}.
     *
     * @param usuario   usuario respondedor
     * @param encuesta  encuesta a la que pertenece la pregunta
     * @param idPregunta identificador de la pregunta
     * @param valor     valor de la respuesta
     * @return la {@link Respuesta} creada
     * @throws IllegalArgumentException si {@code usuario} o {@code encuesta} son {@code null},
     *                                  si la pregunta no existe,
     *                                  o si el valor no es válido
     */
    public Respuesta responderPregunta(
            UsuarioRespondedor usuario,
            Encuesta encuesta,
            String idPregunta,
            Object valor) {

        if (usuario == null || encuesta == null) {
            throw new IllegalArgumentException("Usuario o encuesta no pueden ser null");
        }

        Pregunta pregunta = buscarPregunta(encuesta, idPregunta);
        if (pregunta == null) {
            throw new IllegalArgumentException("Pregunta no encontrada");
        }

        if (valor != null && !pregunta.validarRespuesta(valor)) {
            throw new IllegalArgumentException("Valor no válido");
        }

        Respuesta respuesta = new Respuesta(
                usuario.getId(),
                idPregunta,
                encuesta.getId(),
                valor
        );

        usuario.addRespuesta(encuesta.getId(), respuesta);
        return respuesta;
    }

    // ========== OBTENER RESPUESTAS ==========

    /**
     * Obtiene la lista de respuestas de un usuario para una encuesta concreta.
     *
     * @param usuario   usuario respondedor
     * @param idEncuesta identificador de la encuesta
     * @return lista de respuestas del usuario para la encuesta; si {@code usuario} es {@code null}, lista vacía
     */
    public List<Respuesta> obtenerRespuestas(
            UsuarioRespondedor usuario,
            String idEncuesta) {

        if (usuario == null) return new ArrayList<>();
        return usuario.getRespuestasEncuesta(idEncuesta);
    }

    // ========== UTILIDADES ==========

    /**
     * Busca una pregunta dentro de una encuesta por su identificador.
     *
     * @param encuesta   encuesta donde buscar
     * @param idPregunta identificador de la pregunta
     * @return la {@link Pregunta} encontrada o {@code null} si no existe
     */
    private Pregunta buscarPregunta(Encuesta encuesta, String idPregunta) {
        for (Pregunta p : encuesta.getPreguntas()) {
            if (p.getId().equals(idPregunta)) return p;
        }
        return null;
    }

    /**
     * Valida un conjunto de respuestas para una encuesta sin registrarlas.
     * <p>
     * Verifica que:
     * <ul>
     *   <li>La encuesta y el mapa de respuestas no sean {@code null}.</li>
     *   <li>Para cada idPregunta, exista una {@link Pregunta} en la encuesta.</li>
     *   <li>Si el valor no es {@code null}, la pregunta lo valide mediante
     *       {@link Pregunta#validarRespuesta(Object)}.</li>
     * </ul>
     *
     * @param encuesta   encuesta a validar
     * @param respuestas mapa de respuestas (idPregunta -&gt; valor)
     * @return {@code true} si todas las respuestas son coherentes y válidas; {@code false} en caso contrario
     */
    public boolean validarRespuestas(
            Encuesta encuesta,
            Map<String, Object> respuestas) {

        if (encuesta == null || respuestas == null) return false;

        for (Map.Entry<String, Object> entry : respuestas.entrySet()) {
            String idPregunta = entry.getKey();
            Object valor = entry.getValue();

            Pregunta pregunta = buscarPregunta(encuesta, idPregunta);
            if (pregunta == null) return false;
            if (valor != null && !pregunta.validarRespuesta(valor)) {
                return false;
            }
        }
        return true;
    }
}
