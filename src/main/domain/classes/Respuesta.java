package main.domain.classes;

import java.util.Objects;
import java.util.*;

/**
 * Representa la respuesta de un usuario a una pregunta dentro de una encuesta.
 * <p>
 * Una {@code Respuesta} vincula:
 * <ul>
 *   <li>Un usuario.</li>
 *   <li>Una pregunta.</li>
 *   <li>Una encuesta.</li>
 *   <li>Un valor de respuesta asociado.</li>
 * </ul>
 * <p>
 * La respuesta puede encontrarse contestada o no contestada, en función de si
 * el valor asociado es {@code null}.
 */
public class Respuesta {

    private String idUsuario;
    private String idPregunta;
    private String idEncuesta;
    private Object valor;
    private boolean contestada;

    /**
     * Crea una respuesta vacía, marcada como no contestada.
     */
    public Respuesta() {
        this.contestada = false;
    }

    /**
     * Crea una respuesta asociada a un usuario, una pregunta y una encuesta,
     * con un valor inicial.
     *
     * @param idUsuario  identificador del usuario
     * @param idPregunta identificador de la pregunta
     * @param idEncuesta identificador de la encuesta
     * @param valor      valor de la respuesta
     */
    public Respuesta(String idUsuario, String idPregunta, String idEncuesta, Object valor) {
        this.idUsuario = idUsuario;
        this.idPregunta = idPregunta;
        this.idEncuesta = idEncuesta;
        this.valor = valor;
        this.contestada = (valor != null);
    }

    /**
     * Crea una respuesta asociada a un usuario, una pregunta y una encuesta,
     * inicialmente no contestada.
     *
     * @param idUsuario  identificador del usuario
     * @param idPregunta identificador de la pregunta
     * @param idEncuesta identificador de la encuesta
     */
    public Respuesta(String idUsuario, String idPregunta, String idEncuesta) {
        this(idUsuario, idPregunta, idEncuesta, null);
    }

    /**
     * Crea una respuesta asociada únicamente a una pregunta y un valor.
     * <p>
     * Constructor pensado para su uso desde la capa de persistencia.
     *
     * @param idPregunta identificador de la pregunta
     * @param valor      valor de la respuesta
     */
    public Respuesta(String idPregunta, Object valor) {
        this.idPregunta = idPregunta;
        this.valor = valor;
        this.contestada = (valor != null);
    }

    /**
     * Devuelve el identificador del usuario que ha emitido la respuesta.
     *
     * @return identificador del usuario
     */
    public String getIdUsuario() {
        return idUsuario;
    }

    /**
     * Devuelve el identificador de la pregunta asociada a la respuesta.
     *
     * @return identificador de la pregunta
     */
    public String getIdPregunta() {
        return idPregunta;
    }

    /**
     * Devuelve el identificador de la encuesta asociada a la respuesta.
     *
     * @return identificador de la encuesta
     */
    public String getIdEncuesta() {
        return idEncuesta;
    }

    /**
     * Devuelve el valor de la respuesta.
     *
     * @return valor de la respuesta, o {@code null} si no está contestada
     */
    public Object getValor() {
        return valor;
    }

    /**
     * Indica si la respuesta ha sido contestada.
     *
     * @return {@code true} si la respuesta tiene valor; {@code false} en caso contrario
     */
    public boolean estaContestada() {
        return contestada;
    }

    /**
     * Establece el valor de la respuesta.
     * <p>
     * Al asignar un valor {@code null}, la respuesta pasa a considerarse no contestada.
     *
     * @param valor nuevo valor de la respuesta
     */
    public void setValor(Object valor) {
        this.valor = valor;
        this.contestada = (valor != null);
    }

    /**
     * Establece el identificador del usuario asociado a la respuesta.
     *
     * @param idUsuario identificador del usuario
     */
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Establece el identificador de la encuesta asociada a la respuesta.
     *
     * @param idEncuesta identificador de la encuesta
     */
    public void setIdEncuesta(String idEncuesta) {
        this.idEncuesta = idEncuesta;
    }

    /**
     * Limpia la respuesta, eliminando su valor y marcándola como no contestada.
     */
    public void limpiar() {
        this.valor = null;
        this.contestada = false;
    }

    /**
     * Compara dos respuestas por igualdad lógica.
     * <p>
     * Dos respuestas se consideran iguales si corresponden al mismo usuario,
     * pregunta y encuesta.
     *
     * @param o objeto a comparar
     * @return {@code true} si ambas respuestas son iguales; {@code false} en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Respuesta r = (Respuesta) o;
        return Objects.equals(idUsuario, r.idUsuario) &&
                Objects.equals(idPregunta, r.idPregunta) &&
                Objects.equals(idEncuesta, r.idEncuesta);
    }

    /**
     * Calcula el código hash de la respuesta en función de su identidad lógica.
     *
     * @return código hash de la respuesta
     */
    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idPregunta, idEncuesta);
    }

    /**
     * Devuelve una representación textual de la respuesta.
     *
     * @return representación en forma de {@code String}
     */
    @Override
    public String toString() {
        return "Respuesta{" +
                "idUsuario='" + idUsuario + '\'' +
                ", idPregunta='" + idPregunta + '\'' +
                ", idEncuesta='" + idEncuesta + '\'' +
                ", valor=" + valor +
                ", contestada=" + contestada +
                '}';
    }
}
