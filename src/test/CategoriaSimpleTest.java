package test;

import main.domain.classes.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase {@link CategoriaSimple}.
 *
 * <p>
 * Esta clase verifica el comportamiento completo de una pregunta de tipo
 * categoría simple, asegurando la correcta gestión de sus opciones y
 * la validación de respuestas.
 * </p>
 *
 * <p>
 * Se comprueban:
 * <ul>
 *   <li>Inicialización correcta del enunciado, tipo y conjunto de opciones.</li>
 *   <li>Encapsulamiento defensivo del conjunto de opciones.</li>
 *   <li>Operaciones de adición y eliminación de opciones.</li>
 *   <li>Validación de respuestas para preguntas obligatorias y opcionales.</li>
 *   <li>Sensibilidad a mayúsculas/minúsculas y control de tipos.</li>
 * </ul>
 * </p>
 */
public class CategoriaSimpleTest {

    /**
     * Verifica que el constructor inicializa correctamente el enunciado,
     * el tipo de pregunta y el conjunto de opciones.
     *
     * <p>
     * También comprueba que el conjunto de opciones se copia y no se referencia
     * directamente.
     * </p>
     */
    @Test
    public void constructor_deberiaInicializarCamposCorrectamente() {
        // Arrange
        final Set<String> opciones = new HashSet<>(Arrays.asList("Rojo", "Azul"));

        // Act
        final CategoriaSimple pregunta = new CategoriaSimple("Color favorito", opciones);

        // Assert
        assertEquals("El enunciado no es correcto", "Color favorito", pregunta.getEnunciado());
        assertEquals("El tipo de pregunta deberia ser CATEGORIA_SIMPLE",
                TipoPregunta.CATEGORIA_SIMPLE, pregunta.getTipoPregunta());
        assertEquals("Las opciones no se han copiado correctamente", opciones, pregunta.getOpciones());
    }

    /**
     * Comprueba que el constructor lanza {@link IllegalArgumentException}
     * cuando el conjunto de opciones es {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructor_conOpcionesNull_deberiaLanzarExcepcion() {
        new CategoriaSimple("Texto", null);
    }

    /**
     * Comprueba que el constructor lanza {@link IllegalArgumentException}
     * cuando el conjunto de opciones está vacío.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructor_conOpcionesVacias_deberiaLanzarExcepcion() {
        final Set<String> opciones = new HashSet<>();
        new CategoriaSimple("Texto", opciones);
    }

    /**
     * Verifica que {@link CategoriaSimple#getOpciones()} devuelve
     * una copia independiente del conjunto interno.
     */
    @Test
    public void getOpciones_deberiaDevolverCopiaIndependiente() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);

        final Set<String> copia = pregunta.getOpciones();
        copia.add("C");

        assertFalse(pregunta.getOpciones().contains("C"));
        assertEquals(2, pregunta.getOpciones().size());
    }

    /**
     * Comprueba que {@link CategoriaSimple#agregarOpciones(String)}
     * añade correctamente una opción válida.
     */
    @Test
    public void agregarOpciones_deberiaAnadirNuevaOpcionValida() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);

        pregunta.agregarOpciones("B");

        final Set<String> resultado = pregunta.getOpciones();
        assertTrue(resultado.contains("B"));
        assertEquals(2, resultado.size());
    }

    /**
     * Verifica que {@link CategoriaSimple#agregarOpciones(String)}
     * ignora valores inválidos como {@code null}, cadenas vacías o solo espacios.
     */
    @Test
    public void agregarOpciones_noDeberiaAnadirNullNiCadenasVacias() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);

        pregunta.agregarOpciones(null);
        pregunta.agregarOpciones("");
        pregunta.agregarOpciones("   ");

        final Set<String> resultado = pregunta.getOpciones();
        assertEquals(1, resultado.size());
        assertTrue(resultado.contains("A"));
    }

    /**
     * Comprueba que {@link CategoriaSimple#eliminarOpcion(String)}
     * elimina correctamente una opción existente.
     */
    @Test
    public void eliminarOpcion_deberiaEliminarOpcionSiExiste() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);

        pregunta.eliminarOpcion("A");

        final Set<String> resultado = pregunta.getOpciones();
        assertFalse(resultado.contains("A"));
        assertTrue(resultado.contains("B"));
        assertEquals(1, resultado.size());
    }

    /**
     * Verifica que eliminar una opción inexistente no modifica el conjunto.
     */
    @Test
    public void eliminarOpcion_noDeberiaFallarSiOpcionNoExiste() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);

        pregunta.eliminarOpcion("B");

        final Set<String> resultado = pregunta.getOpciones();
        assertEquals(1, resultado.size());
        assertTrue(resultado.contains("A"));
    }

    /**
     * Comprueba que una respuesta {@code null} es válida
     * cuando la pregunta no es obligatoria.
     */
    @Test
    public void validarRespuesta_noObligatoria_conNull_deberiaDevolverTrue() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(false);

        assertTrue(pregunta.validarRespuesta(null));
    }

    /**
     * Verifica que cadenas vacías o con espacios son válidas
     * en preguntas no obligatorias.
     */
    @Test
    public void validarRespuesta_noObligatoria_conCadenaVacia_deberiaDevolverTrue() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(false);

        assertTrue(pregunta.validarRespuesta(""));
        assertTrue(pregunta.validarRespuesta("   "));
    }

    /**
     * Comprueba que valores que no son {@link String}
     * se consideran inválidos aunque la pregunta no sea obligatoria.
     */
    @Test
    public void validarRespuesta_noObligatoria_conTipoNoString_deberiaDevolverFalse() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("1", "2"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(false);

        assertFalse(pregunta.validarRespuesta(1));
    }

    /**
     * Verifica que una opción no perteneciente al conjunto
     * se considera inválida aunque la pregunta no sea obligatoria.
     */
    @Test
    public void validarRespuesta_noObligatoria_conOpcionNoValida_deberiaDevolverFalse() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(false);

        assertFalse(pregunta.validarRespuesta("C"));
    }

    /**
     * Comprueba que una opción válida es aceptada
     * cuando la pregunta no es obligatoria.
     */
    @Test
    public void validarRespuesta_noObligatoria_conOpcionValida_deberiaDevolverTrue() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(false);

        assertTrue(pregunta.validarRespuesta("A"));
    }

    /**
     * Verifica que {@code null} no es válido
     * cuando la pregunta es obligatoria.
     */
    @Test
    public void validarRespuesta_obligatoria_conNull_deberiaDevolverFalse() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(true);

        assertFalse(pregunta.validarRespuesta(null));
    }

    /**
     * Comprueba que cadenas vacías o con espacios
     * no son válidas en preguntas obligatorias.
     */
    @Test
    public void validarRespuesta_obligatoria_conCadenaVacia_deberiaDevolverFalse() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(true);

        assertFalse(pregunta.validarRespuesta(""));
        assertFalse(pregunta.validarRespuesta("   "));
    }

    /**
     * Verifica que una opción fuera del conjunto
     * no es válida en preguntas obligatorias.
     */
    @Test
    public void validarRespuesta_obligatoria_conOpcionNoValida_deberiaDevolverFalse() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(true);

        assertFalse(pregunta.validarRespuesta("C"));
    }

    /**
     * Comprueba que una opción válida se acepta
     * cuando la pregunta es obligatoria.
     */
    @Test
    public void validarRespuesta_obligatoria_conOpcionValida_deberiaDevolverTrue() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaSimple pregunta = new CategoriaSimple("Texto", opciones);
        pregunta.setObligatoria(true);

        assertTrue(pregunta.validarRespuesta("B"));
    }

    /**
     * Verifica que la validación de respuestas es sensible
     * a mayúsculas y minúsculas.
     */
    @Test
    public void validarRespuesta_deberiaSerSensibleAMayusculasMinusculas() {
        final Set<String> opciones = new HashSet<>(Arrays.asList("Rojo"));
        final CategoriaSimple pregunta = new CategoriaSimple("Color", opciones);
        pregunta.setObligatoria(true);

        assertTrue(pregunta.validarRespuesta("Rojo"));
        assertFalse(pregunta.validarRespuesta("rojo"));
    }
}
