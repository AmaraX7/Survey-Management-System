package test;

import main.domain.classes.*;

import org.junit.Test;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Batería de tests unitarios para la clase {@link Ordinal}.
 *
 * <p>Estas pruebas verifican la correcta inicialización de preguntas ordinales
 * y la lógica de validación de respuestas, asegurando que únicamente se aceptan
 * opciones válidas del conjunto definido y que se respeta la obligatoriedad.</p>
 */
public class OrdinalTest {

    /**
     * Comprueba que el constructor inicializa correctamente el enunciado
     * y el conjunto de opciones ordinales proporcionado.
     */
    @Test
    public void constructor_deberiaInicializarCamposCorrectamente() {
        final Set<String> opciones = Set.of("Malo", "Regular", "Bueno");

        final Ordinal pregunta = new Ordinal("Valoracion", opciones);

        assertEquals("Valoracion", pregunta.getEnunciado());
        assertEquals(opciones, pregunta.getOpciones());
    }

    /**
     * Verifica que {@link Ordinal#validarRespuesta(Object)} devuelve {@code true}
     * cuando la respuesta pertenece al conjunto de opciones definidas.
     */
    @Test
    public void validarRespuesta_conOpcionValida_deberiaDevolverTrue() {
        final Set<String> opciones = Set.of("Malo", "Regular", "Bueno");
        final Ordinal pregunta = new Ordinal("Valoracion", opciones);

        final boolean valido = pregunta.validarRespuesta("Regular");

        assertTrue(valido);
    }

    /**
     * Comprueba que una respuesta que no pertenece al conjunto de opciones
     * definidas se considera inválida.
     */
    @Test
    public void validarRespuesta_conOpcionNoValida_deberiaDevolverFalse() {
        final Set<String> opciones = Set.of("Malo", "Regular", "Bueno");
        final Ordinal pregunta = new Ordinal("Valoracion", opciones);

        final boolean valido = pregunta.validarRespuesta("Excelente");

        assertFalse(valido);
    }

    /**
     * Verifica que una respuesta cuyo tipo no es {@link String}
     * se considera inválida.
     */
    @Test
    public void validarRespuesta_conObjetoNoString_deberiaDevolverFalse() {
        final Set<String> opciones = Set.of("Malo", "Regular", "Bueno");
        final Ordinal pregunta = new Ordinal("Valoracion", opciones);

        final boolean valido = pregunta.validarRespuesta(1);

        assertFalse(valido);
    }

    /**
     * Comprueba que una respuesta {@code null} es válida cuando la pregunta
     * no es obligatoria.
     */
    @Test
    public void validarRespuesta_conNull_noObligatoria_deberiaDevolverTrue() {
        final Set<String> opciones = Set.of("Malo", "Regular", "Bueno");
        final Ordinal pregunta = new Ordinal("Valoracion", opciones);

        final boolean valido = pregunta.validarRespuesta(null);

        assertTrue(valido);
    }

    /**
     * Verifica que una respuesta {@code null} se considera inválida
     * cuando la pregunta es obligatoria.
     */
    @Test
    public void validarRespuesta_conNull_obligatoria_deberiaDevolverFalse() {
        final Set<String> opciones = Set.of("Malo", "Regular", "Bueno");
        final Ordinal pregunta = new Ordinal("Valoracion", opciones);
        pregunta.setObligatoria(true);

        final boolean valido = pregunta.validarRespuesta(null);

        assertFalse(valido);
    }
}
