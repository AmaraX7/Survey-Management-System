package main.domain.classes;

import java.util.*;

/**
 * Representa una pregunta de tipo categoría simple.
 * <p>
 * Una pregunta de categoría simple permite seleccionar una única opción
 * de entre un conjunto predefinido de opciones válidas.
 * <p>
 * La validez de la respuesta depende de:
 * <ul>
 *   <li>La obligatoriedad de la pregunta.</li>
 *   <li>Que la respuesta sea una cadena no vacía.</li>
 *   <li>Que la opción seleccionada pertenezca al conjunto de opciones válidas.</li>
 * </ul>
 */
public class CategoriaSimple extends Pregunta {

    private Set<String> opciones;

    /**
     * Crea una pregunta de categoría simple.
     *
     * @param enunciado enunciado de la pregunta
     * @param opciones  conjunto de opciones válidas
     * @throws IllegalArgumentException si {@code opciones} es {@code null} o está vacío
     */
    public CategoriaSimple(String enunciado, Set<String> opciones) {
        super(enunciado);
        if (opciones == null || opciones.isEmpty()) {
            throw new IllegalArgumentException("Tiene que proporcionar al menos una opción.");
        }
        this.opciones = new HashSet<>(opciones);
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
     * Añade una nueva opción válida a la pregunta.
     * <p>
     * La opción solo se añade si no es {@code null} ni una cadena vacía o con espacios.
     *
     * @param nuevaOpcion opción a añadir
     */
    public void agregarOpciones(String nuevaOpcion) {
        if (nuevaOpcion != null && !nuevaOpcion.trim().isEmpty()) {
            this.opciones.add(nuevaOpcion);
        }
    }

    /**
     * Elimina una opción del conjunto de opciones válidas.
     *
     * @param opcion opción a eliminar
     */
    public void eliminarOpcion(String opcion) {
        opciones.remove(opcion);
    }

    /**
     * Devuelve el tipo de la pregunta.
     *
     * @return tipo {@link TipoPregunta#CATEGORIA_SIMPLE}
     */
    @Override
    public TipoPregunta getTipoPregunta() {
        return TipoPregunta.CATEGORIA_SIMPLE;
    }

    /**
     * Valida una respuesta proporcionada a la pregunta de categoría simple.
     * <p>
     * La respuesta es válida si:
     * <ul>
     *   <li>Es {@code null} y la pregunta no es obligatoria.</li>
     *   <li>Es una cadena no vacía (si la pregunta es obligatoria).</li>
     *   <li>La opción pertenece al conjunto de opciones válidas.</li>
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

        // Debe ser un String (UNA SOLA opción)
        if (!(respuesta instanceof String)) {
            return false;
        }

        String opcionSeleccionada = (String) respuesta;

        // Si está vacío, válido solo si NO es obligatoria
        if (opcionSeleccionada.trim().isEmpty()) {
            return !esObligatoria();
        }

        // Validar que la opción exista en las opciones válidas
        return opciones.contains(opcionSeleccionada);
    }
}
