package main.domain.classes;

import java.util.*;

/**
 * Representa una pregunta de tipo ordinal.
 * <p>
 * Una pregunta ordinal permite seleccionar una única opción de entre un conjunto
 * de valores ordenados o categorizados. La respuesta es válida únicamente si
 * pertenece al conjunto de opciones definidas.
 */
public final class Ordinal extends Pregunta {

    private Set<String> opciones;

    /**
     * Crea una pregunta ordinal con un enunciado y un conjunto de opciones.
     *
     * @param texto    enunciado de la pregunta
     * @param opciones conjunto de opciones válidas
     */
    public Ordinal(String texto, Set<String> opciones) {
        super(texto);
        this.opciones = new HashSet<>(opciones);
    }

    /**
     * Valida una respuesta proporcionada a la pregunta ordinal.
     * <p>
     * La respuesta es válida si:
     * <ul>
     *   <li>Es {@code null} y la pregunta no es obligatoria.</li>
     *   <li>Es una cadena no vacía y pertenece al conjunto de opciones.</li>
     * </ul>
     *
     * @param respuesta objeto que representa la respuesta del usuario
     * @return {@code true} si la respuesta es válida; {@code false} en caso contrario
     */
    public boolean validarRespuesta(Object respuesta) {
        if (respuesta == null) {
            return !esObligatoria();
        }
        if (!(respuesta instanceof String)) return false;
        String valor = (String) respuesta;

        if (valor.trim().isEmpty()) {
            return !esObligatoria();
        }

        return opciones.contains(valor);
    }

    /**
     * Devuelve el tipo de la pregunta.
     *
     * @return tipo {@link TipoPregunta#ORDINAL}
     */
    public TipoPregunta getTipoPregunta() {
        return TipoPregunta.ORDINAL;
    }

    /**
     * Devuelve el conjunto de opciones válidas de la pregunta.
     * <p>
     * Se devuelve una copia para preservar el encapsulamiento.
     *
     * @return conjunto de opciones válidas
     */
    public Set<String> getOpciones() {
        return new HashSet<>(opciones);
    }

    /**
     * Establece el conjunto de opciones válidas de la pregunta.
     *
     * @param opciones nuevo conjunto de opciones
     */
    public void setOpciones(Set<String> opciones) {
        this.opciones = new HashSet<>(opciones);
    }
}
