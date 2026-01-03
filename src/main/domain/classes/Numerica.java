package main.domain.classes;

import java.util.*;

/**
 * Representa una pregunta de tipo numérico.
 * <p>
 * Una pregunta numérica permite responder con un valor numérico que puede estar
 * opcionalmente restringido por un rango mínimo y/o máximo.
 * <p>
 * La validación de la respuesta comprueba tanto el tipo del valor como el
 * cumplimiento de las restricciones definidas.
 */
public class Numerica extends Pregunta {

    private Double min;
    private Double max;

    /**
     * Crea una pregunta numérica con un rango de valores opcional.
     *
     * @param enunciado enunciado de la pregunta
     * @param min       valor mínimo permitido (puede ser {@code null})
     * @param max       valor máximo permitido (puede ser {@code null})
     * @throws IllegalArgumentException si {@code min > max}
     */
    public Numerica(String enunciado, Double min, Double max) {
        super(enunciado);
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("Rango numérico inválido: min > max");
        }
        this.min = min;
        this.max = max;
    }

    /**
     * Crea una pregunta numérica sin restricciones de rango.
     *
     * @param enunciado enunciado de la pregunta
     */
    public Numerica(String enunciado) {
        this(enunciado, null, null);
    }

    /**
     * Devuelve el valor mínimo permitido.
     *
     * @return valor mínimo, o {@code null} si no existe restricción
     */
    public Double getMin() {
        return min;
    }

    /**
     * Devuelve el valor máximo permitido.
     *
     * @return valor máximo, o {@code null} si no existe restricción
     */
    public Double getMax() {
        return max;
    }

    /**
     * Establece el valor mínimo permitido.
     *
     * @param min nuevo valor mínimo
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * Establece el valor máximo permitido.
     *
     * @param max nuevo valor máximo
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * Devuelve el tipo de la pregunta.
     *
     * @return tipo {@link TipoPregunta#NUMERICA}
     */
    @Override
    public TipoPregunta getTipoPregunta() {
        return TipoPregunta.NUMERICA;
    }

    /**
     * Valida una respuesta proporcionada a la pregunta numérica.
     * <p>
     * La respuesta es válida si:
     * <ul>
     *   <li>Es {@code null} y la pregunta no es obligatoria.</li>
     *   <li>Es un número o una cadena convertible a número.</li>
     *   <li>No es {@code NaN} ni infinita.</li>
     *   <li>Respeta las restricciones de rango, si existen.</li>
     * </ul>
     *
     * @param valor objeto que representa la respuesta del usuario
     * @return {@code true} si la respuesta es válida; {@code false} en caso contrario
     */
    @Override
    public boolean validarRespuesta(Object valor) {
        if (valor == null) {
            boolean obl = esObligatoria();
            return !obl;
        }

        Double valorNum = null;

        if (valor instanceof Number) {
            valorNum = ((Number) valor).doubleValue();
        } else if (valor instanceof String) {
            try {
                valorNum = Double.valueOf(((String) valor).trim());
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return false;
        }

        if (valorNum.isNaN() || valorNum.isInfinite()) return false;

        if (min != null && valorNum < min) return false;
        if (max != null && valorNum > max) return false;

        return true;
    }

    /**
     * Establece el rango de valores permitidos para la pregunta.
     *
     * @param min valor mínimo permitido (puede ser {@code null})
     * @param max valor máximo permitido (puede ser {@code null})
     * @throws IllegalArgumentException si {@code min > max}
     */
    public void setRango(Double min, Double max) {
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("Rango inválido");
        }
        this.min = min;
        this.max = max;
    }
}
