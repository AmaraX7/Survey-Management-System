package test;

import main.domain.classes.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Batería de tests unitarios para validar el comportamiento de la clase {@link Libre}.
 *
 * <p>Las pruebas verifican la correcta inicialización de la pregunta libre, la gestión
 * de la longitud máxima permitida y la lógica de validación de respuestas, incluyendo
 * el tratamiento de valores nulos, respuestas obligatorias y la conversión a texto
 * mediante {@code toString()}.</p>
 */
public class LibreTest {

    /**
     * Comprueba que el constructor que solo recibe el enunciado inicializa
     * correctamente la pregunta con tipo {@link TipoPregunta#LIBRE} y
     * una longitud máxima por defecto.
     */
    @Test
    public void constructorSimple_deberiaPonerLongitudMaximaPorDefecto() {
        final Libre pregunta = new Libre("Texto libre");

        assertEquals("Texto libre", pregunta.getEnunciado());
        assertEquals(TipoPregunta.LIBRE, pregunta.getTipoPregunta());
        assertEquals(1000, pregunta.getLongitudMaxima());
    }

    /**
     * Verifica que el constructor que recibe explícitamente la longitud máxima
     * almacena correctamente dicho valor.
     */
    @Test
    public void constructorConLongitud_deberiaGuardarLongitudEspecificada() {
        final Libre pregunta = new Libre("Texto libre", 50);

        assertEquals(50, pregunta.getLongitudMaxima());
    }

    /**
     * Comprueba que {@link Libre#setLongitudMaxima(int)} actualiza correctamente
     * el límite máximo de longitud permitido para las respuestas.
     */
    @Test
    public void setLongitudMaxima_deberiaActualizarValor() {
        final Libre pregunta = new Libre("Texto");

        pregunta.setLongitudMaxima(10);

        assertEquals(10, pregunta.getLongitudMaxima());
    }

    /**
     * Verifica que una respuesta {@code null} es válida cuando la pregunta
     * no es obligatoria.
     */
    @Test
    public void validarRespuesta_conNull_noObligatoria_deberiaDevolverTrue() {
        final Libre pregunta = new Libre("Texto");

        final boolean valido = pregunta.validarRespuesta(null);

        assertTrue(valido);
    }

    /**
     * Comprueba que una respuesta {@code null} se considera inválida cuando
     * la pregunta es obligatoria.
     */
    @Test
    public void validarRespuesta_conNull_obligatoria_deberiaDevolverFalse() {
        final Libre pregunta = new Libre("Texto");
        pregunta.setObligatoria(true);

        final boolean valido = pregunta.validarRespuesta(null);

        assertFalse(valido);
    }

    /**
     * Verifica que una respuesta cuya longitud es menor que la longitud máxima
     * permitida se considera válida.
     */
    @Test
    public void validarRespuesta_textoDentroDeLongitud_deberiaDevolverTrue() {
        final Libre pregunta = new Libre("Texto", 5);

        final boolean valido = pregunta.validarRespuesta("Hola");

        assertTrue(valido);
    }

    /**
     * Comprueba que una respuesta cuya longitud es exactamente igual a la
     * longitud máxima permitida también se considera válida.
     */
    @Test
    public void validarRespuesta_textoExactamenteEnLongitudMaxima_deberiaDevolverTrue() {
        final Libre pregunta = new Libre("Texto", 4);

        final boolean valido = pregunta.validarRespuesta("Hola");

        assertTrue(valido);
    }

    /**
     * Verifica que una respuesta cuya longitud supera la longitud máxima
     * permitida se considera inválida.
     */
    @Test
    public void validarRespuesta_textoMayorQueLongitudMaxima_deberiaDevolverFalse() {
        final Libre pregunta = new Libre("Texto", 3);

        final boolean valido = pregunta.validarRespuesta("Hola");

        assertFalse(valido);
    }

    /**
     * Comprueba que, cuando el valor de la respuesta no es una instancia de
     * {@link String}, se utiliza {@code toString()} para validar su longitud.
     */
    @Test
    public void validarRespuesta_conObjetoNoString_usaToStringYCompruebaLongitud() {
        final Libre pregunta = new Libre("Texto", 3);
        final Object valor = 123;

        final boolean valido = pregunta.validarRespuesta(valor);

        assertTrue(valido);
    }
}
