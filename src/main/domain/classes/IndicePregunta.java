package main.domain.classes;

/**
 * Interfaz funcional para obtener el índice de una pregunta dentro de una encuesta.
 * <p>
 * Esta interfaz define un único método que permite resolver la posición
 * (índice) de una pregunta identificada por su ID dentro de una encuesta concreta.
 * <p>
 * Al ser una {@link FunctionalInterface}, puede implementarse mediante expresiones
 * lambda o referencias a métodos, facilitando el desacoplamiento entre componentes
 * del dominio y la lógica que conoce la estructura de la encuesta.
 */
@FunctionalInterface
public interface IndicePregunta {

    /**
     * Obtiene el índice de una pregunta dentro de una encuesta.
     *
     * @param idEncuesta identificador de la encuesta
     * @param idPregunta identificador de la pregunta
     * @return índice de la pregunta dentro de la encuesta
     */
    int obtenerIndice(String idEncuesta, String idPregunta);
}
