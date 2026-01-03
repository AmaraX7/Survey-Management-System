package main.domain.classes;

/**
 * Representa una pregunta de respuesta libre en formato texto.
 * <p>
 * Una pregunta libre permite al usuario introducir un texto arbitrario,
 * con una longitud máxima configurable. La respuesta se considera válida
 * si no supera dicha longitud y respeta la obligatoriedad de la pregunta.
 */
public class Libre extends Pregunta {

    private int longitudMaxima;

    /**
     * Crea una pregunta de respuesta libre con una longitud máxima por defecto.
     *
     * @param enunciado enunciado de la pregunta
     */
    public Libre(String enunciado) {
        super(enunciado);
        this.longitudMaxima = 1000;
    }

    /**
     * Crea una pregunta de respuesta libre con una longitud máxima específica.
     *
     * @param enunciado       enunciado de la pregunta
     * @param longitudMaxima  longitud máxima permitida para la respuesta
     */
    public Libre(String enunciado, int longitudMaxima) {
        super(enunciado);
        this.longitudMaxima = longitudMaxima;
    }

    /**
     * Devuelve la longitud máxima permitida para la respuesta.
     *
     * @return longitud máxima de la respuesta
     */
    public int getLongitudMaxima() {
        return longitudMaxima;
    }

    /**
     * Establece la longitud máxima permitida para la respuesta.
     *
     * @param longitudMaxima nueva longitud máxima
     */
    public void setLongitudMaxima(int longitudMaxima) {
        this.longitudMaxima = longitudMaxima;
    }

    /**
     * Valida una respuesta proporcionada a la pregunta libre.
     * <p>
     * La respuesta es válida si:
     * <ul>
     *   <li>Es {@code null} y la pregunta no es obligatoria.</li>
     *   <li>Su longitud no supera la longitud máxima permitida.</li>
     * </ul>
     *
     * @param valor objeto que representa la respuesta del usuario
     * @return {@code true} si la respuesta es válida; {@code false} en caso contrario
     */
    @Override
    public boolean validarRespuesta(Object valor) {
        if (valor == null) {
            return !esObligatoria();
        }
        String texto = valor.toString();
        return texto.length() <= longitudMaxima;
    }

    /**
     * Devuelve el tipo de la pregunta.
     *
     * @return tipo {@link TipoPregunta#LIBRE}
     */
    @Override
    public TipoPregunta getTipoPregunta() {
        return TipoPregunta.LIBRE;
    }
}
