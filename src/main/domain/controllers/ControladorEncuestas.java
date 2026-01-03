package main.domain.controllers;

import main.domain.classes.*;
import java.util.*;

/**
 * Controlador de lógica de negocio para la gestión de encuestas.
 * <p>
 * Esta clase encapsula operaciones típicas de negocio sobre {@link Encuesta} y {@link Pregunta},
 * operando exclusivamente con objetos en memoria. No realiza operaciones de persistencia ni
 * depende de repositorios/DAO.
 * <p>
 * Su objetivo es centralizar validaciones y acciones de modificación/consulta, evitando que la
 * capa de presentación o la capa de aplicación manipulen directamente la lógica interna del dominio.
 */
public class ControladorEncuestas {

    /**
     * Construye un controlador de encuestas sin dependencias externas.
     * <p>
     * Se asume que la persistencia (si existe) se gestiona fuera de esta clase.
     */
    public ControladorEncuestas() {
        // Sin dependencias de persistencia
    }

    // ========== OPERACIONES DE NEGOCIO ==========

    /**
     * Crea una nueva encuesta con el título y descripción indicados.
     * <p>
     * Realiza una validación mínima del título (no puede ser {@code null} ni vacío).
     *
     * @param titulo       título de la encuesta
     * @param descripcion  descripción de la encuesta (puede ser {@code null})
     * @return una nueva instancia de {@link Encuesta}
     * @throws IllegalArgumentException si {@code titulo} es {@code null} o está vacío
     */
    public Encuesta crearEncuesta(String titulo, String descripcion) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        return new Encuesta(titulo, descripcion);
    }

    /**
     * Modifica el título y/o la descripción de una encuesta existente.
     * <p>
     * Solo actualiza aquellos campos cuyo valor nuevo no sea {@code null}.
     *
     * @param encuesta         encuesta a modificar
     * @param nuevoTitulo      nuevo título (si es {@code null}, no se modifica)
     * @param nuevaDescripcion nueva descripción (si es {@code null}, no se modifica)
     * @throws IllegalArgumentException si {@code encuesta} es {@code null}
     */
    public void modificarEncuesta(Encuesta encuesta, String nuevoTitulo, String nuevaDescripcion) {
        if (encuesta == null) {
            throw new IllegalArgumentException("Encuesta no puede ser null");
        }

        if (nuevoTitulo != null) {
            encuesta.setTitulo(nuevoTitulo);
        }
        if (nuevaDescripcion != null) {
            encuesta.setDescripcion(nuevaDescripcion);
        }
    }

    /**
     * Agrega una pregunta a una encuesta.
     *
     * @param encuesta encuesta destino
     * @param pregunta pregunta a añadir
     * @throws IllegalArgumentException si {@code encuesta} o {@code pregunta} son {@code null}
     */
    public void agregarPregunta(Encuesta encuesta, Pregunta pregunta) {
        if (encuesta == null || pregunta == null) {
            throw new IllegalArgumentException("Encuesta y pregunta no pueden ser null");
        }
        encuesta.agregarPregunta(pregunta);
    }

    /**
     * Modifica (sustituye) una pregunta existente de la encuesta en el índice indicado.
     *
     * @param encuesta      encuesta a modificar
     * @param indice        índice de la pregunta a sustituir
     * @param nuevaPregunta nueva pregunta que reemplazará la existente
     * @throws IllegalArgumentException si {@code encuesta} o {@code nuevaPregunta} son {@code null}
     * @throws IndexOutOfBoundsException si {@code indice} no está en el rango válido
     */
    public void modificarPregunta(Encuesta encuesta, int indice, Pregunta nuevaPregunta) {
        if (encuesta == null || nuevaPregunta == null) {
            throw new IllegalArgumentException("Encuesta y pregunta no pueden ser null");
        }

        if (indice < 0 || indice >= encuesta.getNumPreguntas()) {
            throw new IndexOutOfBoundsException("Índice de pregunta inválido: " + indice);
        }

        encuesta.modificarPregunta(nuevaPregunta, indice);
    }

    /**
     * Elimina una pregunta de la encuesta por índice.
     *
     * @param encuesta encuesta a modificar
     * @param indice   índice de la pregunta a eliminar
     * @throws IllegalArgumentException si {@code encuesta} es {@code null}
     */
    public void eliminarPregunta(Encuesta encuesta, int indice) {
        if (encuesta == null) {
            throw new IllegalArgumentException("Encuesta no puede ser null");
        }
        encuesta.eliminarPregunta(indice);
    }

    // ========== VALIDACIONES Y UTILIDADES ==========

    /**
     * Valida si una encuesta cumple requisitos mínimos.
     * <p>
     * Actualmente comprueba:
     * <ul>
     *   <li>La encuesta no es {@code null}.</li>
     *   <li>El título no es {@code null} ni vacío (tras {@code trim()}).</li>
     * </ul>
     *
     * @param encuesta encuesta a validar
     * @return {@code true} si pasa las validaciones mínimas; {@code false} en caso contrario
     */
    public boolean validarEncuesta(Encuesta encuesta) {
        if (encuesta == null) return false;
        if (encuesta.getTitulo() == null || encuesta.getTitulo().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Busca una pregunta dentro de una encuesta por su identificador.
     *
     * @param encuesta   encuesta donde buscar
     * @param idPregunta identificador de la pregunta
     * @return la {@link Pregunta} si se encuentra; {@code null} en caso contrario
     */
    public Pregunta buscarPregunta(Encuesta encuesta, String idPregunta) {
        if (encuesta == null || idPregunta == null) return null;

        for (Pregunta p : encuesta.getPreguntas()) {
            if (p.getId().equals(idPregunta)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Obtiene el índice (posición) de una pregunta dentro de una encuesta a partir de su ID.
     *
     * @param encuesta   encuesta donde buscar
     * @param idPregunta identificador de la pregunta
     * @return índice de la pregunta, o {@code -1} si no se encuentra o si los parámetros son {@code null}
     */
    public int obtenerIndicePregunta(Encuesta encuesta, String idPregunta) {
        if (encuesta == null || idPregunta == null) return -1;

        List<Pregunta> preguntas = encuesta.getPreguntas();
        for (int i = 0; i < preguntas.size(); i++) {
            if (preguntas.get(i).getId().equals(idPregunta)) {
                return i;
            }
        }
        return -1;
    }
}
