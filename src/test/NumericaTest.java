package test;

import main.domain.classes.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Batería de tests unitarios para la clase {@link Numerica}.
 *
 * <p>Estas pruebas validan la correcta inicialización de preguntas numéricas,
 * la coherencia del rango permitido y la lógica de validación de respuestas
 * frente a distintos tipos de entrada y configuraciones.</p>
 */
public class NumericaTest {

    /**
     * Verifica que el constructor con límites mínimo y máximo válidos
     * inicializa correctamente el enunciado, el tipo de pregunta y el rango.
     */
    @Test
    public void constructor_conMinYMaxValidos_deberiaInicializarCorrectamente() {
        final Numerica pregunta = new Numerica("Edad", 0.0, 120.0);

        assertEquals("Edad", pregunta.getEnunciado());
        assertEquals(TipoPregunta.NUMERICA, pregunta.getTipoPregunta());
        assertEquals(Double.valueOf(0.0), pregunta.getMin());
        assertEquals(Double.valueOf(120.0), pregunta.getMax());
    }

    /**
     * Comprueba que el constructor sin rango definido deja los límites
     * inferior y superior sin restricción.
     */
    @Test
    public void constructor_sinMinMax_deberiaDejarRangoSinRestringir() {
        final Numerica pregunta = new Numerica("Temperatura");

        assertNull(pregunta.getMin());
        assertNull(pregunta.getMax());
    }

    /**
     * Verifica que el constructor lanza una {@link IllegalArgumentException}
     * cuando el límite mínimo es mayor que el máximo.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructor_conMinMayorQueMax_deberiaLanzarExcepcion() {
        new Numerica("Edad", 10.0, 5.0);
    }

    /**
     * Comprueba que {@link Numerica#setRango(Double, Double)} actualiza
     * correctamente los límites del rango cuando los valores son válidos.
     */
    @Test
    public void setRango_valido_deberiaActualizarMinYMax() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        pregunta.setRango(-5.0, 5.0);

        assertEquals(Double.valueOf(-5.0), pregunta.getMin());
        assertEquals(Double.valueOf(5.0), pregunta.getMax());
    }

    /**
     * Verifica que {@link Numerica#setRango(Double, Double)} lanza una excepción
     * cuando el mínimo es mayor que el máximo.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setRango_conMinMayorQueMax_deberiaLanzarExcepcion() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        pregunta.setRango(10.0, 5.0);
    }

    /**
     * Comprueba que una respuesta {@code null} es válida cuando la pregunta
     * no es obligatoria.
     */
    @Test
    public void validarRespuesta_conNullYNoObligatoria_deberiaDevolverTrue() {
        final Numerica pregunta = new Numerica("Valor");
        pregunta.setObligatoria(false);

        final boolean valido = pregunta.validarRespuesta(null);

        assertTrue(valido);
    }

    /**
     * Verifica que una respuesta {@code null} se considera inválida
     * cuando la pregunta es obligatoria.
     */
    @Test
    public void validarRespuesta_conNullYObligatoria_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor");
        pregunta.setObligatoria(true);

        final boolean valido = pregunta.validarRespuesta(null);

        assertFalse(valido);
    }

    /**
     * Comprueba que un número dentro del rango definido se considera válido.
     */
    @Test
    public void validarRespuesta_conNumeroDentroDeRango_deberiaDevolverTrue() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        final boolean valido = pregunta.validarRespuesta(5);

        assertTrue(valido);
    }

    /**
     * Verifica que los valores exactamente iguales a los límites del rango
     * se consideran válidos.
     */
    @Test
    public void validarRespuesta_conNumeroIgualAlMinYMax_deberiaDevolverTrue() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        assertTrue(pregunta.validarRespuesta(0.0));
        assertTrue(pregunta.validarRespuesta(10.0));
    }

    /**
     * Comprueba que un número inferior al límite mínimo se considera inválido.
     */
    @Test
    public void validarRespuesta_conNumeroPorDebajoDeMin_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        final boolean valido = pregunta.validarRespuesta(-1);

        assertFalse(valido);
    }

    /**
     * Verifica que un número superior al límite máximo se considera inválido.
     */
    @Test
    public void validarRespuesta_conNumeroPorEncimaDeMax_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        final boolean valido = pregunta.validarRespuesta(11);

        assertFalse(valido);
    }

    /**
     * Comprueba que una cadena que representa un número válido se parsea
     * correctamente y se valida contra el rango definido.
     */
    @Test
    public void validarRespuesta_conStringNumericoValido_deberiaDevolverTrue() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        final boolean valido = pregunta.validarRespuesta(" 5.5 ");

        assertTrue(valido);
    }

    /**
     * Verifica que una cadena no numérica se considera inválida.
     */
    @Test
    public void validarRespuesta_conStringNoNumerico_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        final boolean valido = pregunta.validarRespuesta("abc");

        assertFalse(valido);
    }

    /**
     * Comprueba que valores de tipos no soportados se consideran inválidos.
     */
    @Test
    public void validarRespuesta_conTipoNoSoportado_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor", 0.0, 10.0);

        final boolean valido = pregunta.validarRespuesta(new Object());

        assertFalse(valido);
    }

    /**
     * Verifica que el valor {@link Double#NaN} se considera inválido
     * independientemente del rango definido.
     */
    @Test
    public void validarRespuesta_conNaN_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor");

        final boolean valido = pregunta.validarRespuesta(Double.NaN);

        assertFalse(valido);
    }

    /**
     * Comprueba que los valores infinitos se consideran inválidos.
     */
    @Test
    public void validarRespuesta_conInfinito_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor");

        assertFalse(pregunta.validarRespuesta(Double.POSITIVE_INFINITY));
        assertFalse(pregunta.validarRespuesta(Double.NEGATIVE_INFINITY));
    }

    /**
     * Verifica que, sin rango definido, cualquier número finito
     * se considera válido.
     */
    @Test
    public void validarRespuesta_sinRangoYNumeroFinito_deberiaDevolverTrue() {
        final Numerica pregunta = new Numerica("Valor");

        final boolean valido = pregunta.validarRespuesta(123.45);

        assertTrue(valido);
    }

    /**
     * Comprueba que, cuando solo hay límite mínimo definido,
     * los valores mayores o iguales a dicho mínimo son válidos.
     */
    @Test
    public void validarRespuesta_conSoloMinYValorMayorOIgualQueMin_deberiaSerValido() {
        final Numerica pregunta = new Numerica("Valor", 10.0, null);

        assertTrue(pregunta.validarRespuesta(10.0));
        assertTrue(pregunta.validarRespuesta(15.0));
    }

    /**
     * Verifica que, con solo límite mínimo definido,
     * los valores inferiores se consideran inválidos.
     */
    @Test
    public void validarRespuesta_conSoloMinYValorPorDebajoMin_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor", 10.0, null);

        final boolean valido = pregunta.validarRespuesta(5.0);

        assertFalse(valido);
    }

    /**
     * Comprueba que, cuando solo hay límite máximo definido,
     * los valores menores o iguales a dicho máximo son válidos.
     */
    @Test
    public void validarRespuesta_conSoloMaxYValorMenorOIgualQueMax_deberiaSerValido() {
        final Numerica pregunta = new Numerica("Valor", null, 10.0);

        assertTrue(pregunta.validarRespuesta(5.0));
        assertTrue(pregunta.validarRespuesta(10.0));
    }

    /**
     * Verifica que, con solo límite máximo definido,
     * los valores superiores se consideran inválidos.
     */
    @Test
    public void validarRespuesta_conSoloMaxYValorMayorQueMax_deberiaDevolverFalse() {
        final Numerica pregunta = new Numerica("Valor", null, 10.0);

        final boolean valido = pregunta.validarRespuesta(11.0);

        assertFalse(valido);
    }
}
