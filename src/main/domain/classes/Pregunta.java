package main.domain.classes;

import java.util.UUID;

/**
 * Representa una pregunta genérica dentro del dominio del sistema.
 * <p>
 * Una {@code Pregunta} define el comportamiento común a todos los tipos de preguntas
 * (por ejemplo, categoría simple, múltiple, numérica, etc.), incluyendo:
 * <ul>
 *   <li>Identificación única.</li>
 *   <li>Enunciado textual.</li>
 *   <li>Carácter obligatorio u opcional.</li>
 *   <li>Validación de respuestas según el tipo concreto.</li>
 * </ul>
 * <p>
 * Es una clase abstracta que actúa como raíz de la jerarquía de preguntas del dominio.
 */
public abstract class Pregunta {

    private String id;
    private String enunciado;
    private boolean obligatoria;

    /**
     * Crea una pregunta no obligatoria con un enunciado dado.
     * <p>
     * Se genera automáticamente un identificador único para la pregunta.
     *
     * @param enunciado texto que describe la pregunta
     */
    protected Pregunta(String enunciado) {
        this.id = UUID.randomUUID().toString();
        this.enunciado = enunciado;
        this.obligatoria = false;
    }

    /**
     * Crea una pregunta con un enunciado y especifica si es obligatoria.
     * <p>
     * Se genera automáticamente un identificador único para la pregunta.
     *
     * @param enunciado   texto que describe la pregunta
     * @param obligatoria indica si la pregunta es obligatoria
     */
    protected Pregunta(String enunciado, boolean obligatoria) {
        this.id = UUID.randomUUID().toString();
        this.enunciado = enunciado;
        this.obligatoria = obligatoria;
    }

    /**
     * Devuelve el identificador único de la pregunta.
     *
     * @return identificador de la pregunta
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el enunciado de la pregunta.
     *
     * @return texto del enunciado
     */
    public String getEnunciado() {
        return enunciado;
    }

    /**
     * Indica si la pregunta es obligatoria.
     *
     * @return {@code true} si la pregunta es obligatoria; {@code false} en caso contrario
     */
    public boolean esObligatoria() {
        return obligatoria;
    }

    /**
     * Devuelve el tipo concreto de la pregunta.
     * <p>
     * Este método debe ser implementado por cada subclase concreta para indicar
     * su tipo dentro del dominio.
     *
     * @return tipo de pregunta
     */
    public abstract TipoPregunta getTipoPregunta();

    /**
     * Valida una respuesta proporcionada a la pregunta.
     * <p>
     * La validación concreta depende del tipo de pregunta (subclase).
     *
     * @param respuesta objeto que representa la respuesta del usuario
     * @return {@code true} si la respuesta es válida según las reglas de la pregunta;
     *         {@code false} en caso contrario
     */
    public abstract boolean validarRespuesta(Object respuesta);

    /**
     * Establece si la pregunta es obligatoria.
     *
     * @param obligatoria {@code true} para marcar la pregunta como obligatoria;
     *                    {@code false} para marcarla como opcional
     */
    public void setObligatoria(boolean obligatoria) {
        this.obligatoria = obligatoria;
    }

    /**
     * Modifica el enunciado de la pregunta.
     *
     * @param enunciado nuevo texto del enunciado
     * @throws IllegalArgumentException si el enunciado es {@code null} o vacío
     */
    public void setEnunciado(String enunciado) {
        if (enunciado == null || enunciado.trim().isEmpty()) {
            throw new IllegalArgumentException("El enunciado no puede estar vacío");
        }
        this.enunciado = enunciado;
    }

    /**
     * Modifica el identificador de la pregunta.
     *
     * @param id nuevo identificador
     */
    public void setId(String id) {
        this.id = id;
    }
}
