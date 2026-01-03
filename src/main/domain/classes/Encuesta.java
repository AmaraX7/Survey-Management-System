package main.domain.classes;

import java.util.*;
import java.util.List;
import java.util.UUID;

/**
 * Representa una encuesta del sistema.
 * <p>
 * Una encuesta está compuesta por un conjunto ordenado de {@link Pregunta},
 * un identificador único, un título y una descripción. Además, mantiene un
 * historial de resultados de clustering asociados a la encuesta.
 */
public class Encuesta {

    private String id;
    private String titulo;
    private String descripcion;
    private final List<Pregunta> preguntas; // referencia a la clase preguntas.
    private final List<ResultadoClustering> historialResultados;

    /**
     * Crea una nueva encuesta con un identificador único generado automáticamente.
     *
     * @param titulo       título de la encuesta
     * @param descripcion descripción de la encuesta
     */
    public Encuesta(String titulo, String descripcion)
    {
        this.id = UUID.randomUUID().toString();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.preguntas = new ArrayList<>();
        this.historialResultados = new ArrayList<>();
    }

    /**
     * Devuelve el identificador único de la encuesta.
     *
     * @return identificador de la encuesta
     */
    public String getId()
    {
        return id;
    }

    /**
     * Devuelve el título de la encuesta.
     *
     * @return título de la encuesta
     */
    public String getTitulo()
    {
        return titulo;
    }

    /**
     * Devuelve la descripción de la encuesta.
     *
     * @return descripción de la encuesta
     */
    public String getDescripcion()
    {
        return descripcion;
    }

    /**
     * Devuelve la lista de preguntas de la encuesta.
     *
     * @return lista de preguntas
     */
    public List<Pregunta> getPreguntas()
    {
        return preguntas;
    }

    /**
     * Devuelve el número total de preguntas de la encuesta.
     *
     * @return número de preguntas
     */
    public Integer getNumPreguntas()
    {
        return preguntas.size();
    }

    /**
     * Devuelve la pregunta situada en una posición concreta.
     *
     * @param index índice de la pregunta
     * @return pregunta en la posición indicada
     */
    public Pregunta getPregunta(int index)
    {
        return preguntas.get(index);
    }

    /**
     * Establece el título de la encuesta.
     *
     * @param titulo nuevo título de la encuesta
     */
    public void setTitulo(String titulo)
    {
        this.titulo = titulo;
    }

    /**
     * Establece el identificador de la encuesta.
     *
     * @param id nuevo identificador
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Establece la descripción de la encuesta.
     *
     * @param descripcion nueva descripción
     */
    public void setDescripcion(String descripcion)
    {
        this.descripcion = descripcion;
    }

    /**
     * Añade una pregunta a la encuesta.
     *
     * @param pregunta pregunta a añadir
     */
    public void agregarPregunta(Pregunta pregunta)
    {
        preguntas.add(pregunta);
    }

    /**
     * Modifica una pregunta existente en una posición concreta.
     *
     * @param pregunta nueva pregunta
     * @param index    índice de la pregunta a modificar
     */
    public void modificarPregunta(Pregunta pregunta, int index)
    {
        if (index >= 0 && index < preguntas.size())
        {
            preguntas.set(index, pregunta);
        }
    }

    /**
     * Elimina una pregunta de la encuesta por su índice.
     *
     * @param index índice de la pregunta a eliminar
     */
    public void eliminarPregunta(int index)
    {
        if (index >= 0 && index < preguntas.size())
        {
            preguntas.remove(index);
        }
    }

    /**
     * Elimina todas las preguntas de la encuesta.
     */
    public void eliminarTodasPreguntas()
    {
        preguntas.clear();
    }

    /**
     * Devuelve el índice de una pregunta dentro de la encuesta.
     *
     * @param pregunta pregunta cuyo índice se desea obtener
     * @return índice de la pregunta o {@code -1} si no se encuentra
     */
    public int getIndicePregunta(Pregunta pregunta) {
        if (pregunta == null) {
            return -1;
        }

        for (int i = 0; i < preguntas.size(); i++) {
            if (preguntas.get(i).getId().equals(pregunta.getId())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Añade un resultado de clustering al historial de la encuesta.
     *
     * @param resultado resultado de clustering a añadir
     */
    public void agregarResultado(ResultadoClustering resultado) {
        this.historialResultados.add(resultado);
    }

    /**
     * Devuelve el historial completo de resultados de clustering.
     * <p>
     * Se devuelve una copia para evitar modificaciones externas.
     *
     * @return lista de resultados de clustering
     */
    public List<ResultadoClustering> getHistorialResultados() {
        return new ArrayList<>(historialResultados);
    }

    /**
     * Devuelve el último resultado de clustering ejecutado sobre la encuesta.
     *
     * @return último resultado o {@code null} si no existen resultados
     */
    public ResultadoClustering getUltimoResultado() {
        return historialResultados.isEmpty() ? null :
                historialResultados.getLast();
    }

    /**
     * Elimina todo el historial de resultados de clustering de la encuesta.
     */
    public void limpiarHistorial() {
        historialResultados.clear();
    }
}

