package main.domain.classes;

import java.util.*;

/**
 * Enum que define los distintos tipos de preguntas soportados por el sistema.
 * <p>
 * Cada valor representa una categoría concreta de pregunta dentro del dominio
 * y se utiliza para identificar su comportamiento específico, especialmente
 * en la validación de respuestas y en la lógica de presentación.
 */
public enum TipoPregunta {

    /** Pregunta con respuesta numérica. */
    NUMERICA,

    /** Pregunta con respuesta ordinal (valores ordenados). */
    ORDINAL,

    /** Pregunta de selección única entre un conjunto de opciones. */
    CATEGORIA_SIMPLE,

    /** Pregunta de selección múltiple entre un conjunto de opciones. */
    CATEGORIA_MULTIPLE,

    /** Pregunta de respuesta libre en formato texto. */
    LIBRE
}

