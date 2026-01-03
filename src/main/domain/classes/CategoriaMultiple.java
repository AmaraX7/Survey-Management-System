package main.domain.classes;

import java.util.*;

/**
 * Representa una pregunta de tipo categoría múltiple.
 * <p>
 * Una pregunta de categoría múltiple permite seleccionar varias opciones
 * de entre un conjunto predefinido, con un límite máximo de selecciones.
 * <p>
 * La validez de una respuesta depende de:
 * <ul>
 *   <li>La obligatoriedad de la pregunta.</li>
 *   <li>Que la respuesta sea un conjunto.</li>
 *   <li>Que no exceda el número máximo de selecciones permitidas.</li>
 *   <li>Que todas las opciones pertenezcan al conjunto de opciones válidas.</li>
 * </ul>
 */
public class CategoriaMultiple extends Pregunta {

    private Set<String> opciones;
    private int maxSelecciones;

    /**
     * Crea una pregunta de categoría múltiple.
     *
     * @param enunciado       enunciado de la pregunta
     * @param opciones        conjunto de opciones válidas
     * @param maxSelecciones  número máximo de opciones seleccionables
     * @throws IllegalArgumentException si {@code maxSelecciones <= 0}
     */
    public CategoriaMultiple(String enunciado, Set<String> opciones, int maxSelecciones) {
        super(enunciado);

        if (opciones == null) {
            this.opciones = new HashSet<>();
        } else {
            this.opciones = new HashSet<>(opciones);
        }

        if (maxSelecciones <= 0) {
            throw new IllegalArgumentException("maxSelecciones debe ser > 0");
        }
        this.maxSelecciones = maxSelecciones;
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

    /**
     * Establece el número máximo de selecciones permitidas.
     *
     * @param maxSelecciones número máximo de selecciones
     * @throws IllegalArgumentException si {@code maxSelecciones < 1}
     */
    public void setMaxSelecciones(int maxSelecciones) {
        if (maxSelecciones < 1) {
            throw new IllegalArgumentException("maxSelecciones debe ser al menos 1");
        }
        this.maxSelecciones = maxSelecciones;
    }

    /**
     * Devuelve el número máximo de selecciones permitidas.
     *
     * @return número máximo de selecciones
     */
    public int getMaxSelecciones() {
        return maxSelecciones;
    }

    /**
     * Devuelve el tipo de la pregunta.
     *
     * @return tipo {@link TipoPregunta#CATEGORIA_MULTIPLE}
     */
    @Override
    public TipoPregunta getTipoPregunta() {
        return TipoPregunta.CATEGORIA_MULTIPLE;
    }

    /**
     * Valida una respuesta proporcionada a la pregunta de categoría múltiple.
     * <p>
     * La respuesta es válida si:
     * <ul>
     *   <li>Es {@code null} y la pregunta no es obligatoria.</li>
     *   <li>Es un conjunto no vacío (si la pregunta es obligatoria).</li>
     *   <li>No excede el número máximo de selecciones permitidas.</li>
     *   <li>Todas las opciones son {@code String} y pertenecen al conjunto válido.</li>
     * </ul>
     *
     * @param respuesta objeto que representa la respuesta del usuario
     * @return {@code true} si la respuesta es válida; {@code false} en caso contrario
     */
    @Override
    public boolean validarRespuesta(Object respuesta) {
        // Si es null, válido solo si NO es obligatoria
        if (respuesta == null) {
            return !esObligatoria();
        }

        // Debe ser un Set
        if (!(respuesta instanceof Set<?> conjunto)) {
            return false;
        }

        // Si está vacío, válido solo si NO es obligatoria
        if (conjunto.isEmpty()) {
            return !esObligatoria();
        }

        // No puede exceder el máximo de selecciones
        if (conjunto.size() > maxSelecciones) {
            return false;
        }

        // Validar que todas las opciones existen y son Strings
        for (Object opcion : conjunto) {
            if (!(opcion instanceof String)) {
                return false;
            }
            if (!opciones.contains((String) opcion)) {
                return false;
            }
        }

        return true;
    }
}
