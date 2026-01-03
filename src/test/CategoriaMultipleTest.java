package test;

import main.domain.classes.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase CategoriaMultiple.
 * Se validan los comportamientos clave relacionados con:
 *  - Correcta inicialización de atributos.
 *  - Gestión defensiva de colecciones internas.
 *  - Validación robusta de respuestas según reglas de negocio.
 *  - Manejo adecuado de casos límite y entradas inválidas.
 */
public class CategoriaMultipleTest {

    /**
     * Comprueba: que el constructor asigna correctamente el enunciado, el tipo de pregunta,
     * el valor de maxSelecciones y el conjunto de opciones proporcionado.
     * Por qué: es fundamental garantizar que, dada una configuración válida, la instancia
     * se cree en un estado totalmente coherente y usable por las capas superiores del sistema.
     * Casos límite: se evalúa que las opciones se copien correctamente y no se compartan referencias,
     * lo cual evita mutaciones externas no deseadas sobre el estado interno.
     */
    @Test
    public void constructor_deberiaInicializarCamposCorrectamente() {
        // Arrange
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B", "C"));

        // Act
        final CategoriaMultiple pregunta = new CategoriaMultiple("Elige varias", opciones, 2);

        // Assert
        assertEquals("El enunciado no es correcto", "Elige varias", pregunta.getEnunciado());
        assertEquals("El tipo de pregunta deberia ser CATEGORIA_MULTIPLE",
                TipoPregunta.CATEGORIA_MULTIPLE, pregunta.getTipoPregunta());
        assertEquals("El maximo de selecciones no es correcto", 2, pregunta.getMaxSelecciones());
        assertEquals("Las opciones no se han copiado correctamente", opciones, pregunta.getOpciones());
    }

    /**
     * Comprueba: que si el constructor recibe un conjunto de opciones null, la clase inicializa
     * internamente un conjunto vacío en lugar de dejar el atributo en null.
     * Por qué: previene NullPointerException en accesos futuros y asegura que el objeto mantiene
     * un estado estable incluso cuando las entradas no son completas o provienen de datos corruptos.
     * Casos límite: opciones = null → resultado debe ser un conjunto vacío pero no null.
     */
    @Test
    public void constructor_conOpcionesNull_deberiaCrearConjuntoVacio() {
        // Arrange & Act
        final CategoriaMultiple pregunta = new CategoriaMultiple("Texto", null, 1);

        // Assert
        assertTrue("Las opciones deberian estar vacias", pregunta.getOpciones().isEmpty());
    }

    /**
     * Comprueba: que un valor no positivo para maxSelecciones provoca una IllegalArgumentException.
     * Por qué: el límite de selecciones debe ser un entero estrictamente positivo; permitir otros valores
     * comprometería la lógica de validación y el significado semántico del atributo.
     * Casos límite: maxSelecciones <= 0.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructor_conMaxSeleccionesNoPositivo_deberiaLanzarExcepcion() {
        new CategoriaMultiple("Texto", new HashSet<>(Arrays.asList("A", "B")), 0);

        // Assert manejado por expected
    }

    /**
     * Comprueba: que getOpciones() devuelve una copia independiente del conjunto interno.
     * Por qué: es indispensable para garantizar encapsulamiento. Si se devolviera la referencia interna,
     * el cliente podría alterarla y modificar el estado privado de la pregunta, rompiendo invariantes.
     * Casos límite: se modifica el conjunto devuelto y se verifica que la estructura interna permanece intacta.
     */
    @Test
    public void getOpciones_deberiaDevolverCopiaIndependiente() {
        // Arrange
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaMultiple pregunta = new CategoriaMultiple("Texto", opciones, 2);

        // Act
        final Set<String> devueltas = pregunta.getOpciones();
        devueltas.add("C");

        // Assert
        assertFalse("Modificar el set devuelto no deberia afectar al interno",
                pregunta.getOpciones().contains("C"));
    }

    /**
     * Comprueba: que una respuesta válida, formada por un conjunto de opciones permitido y con
     * tamaño <= maxSelecciones, se considera correcta.
     * Por qué: este es el caso nominal principal del método validarRespuesta, y confirma que las
     * reglas de negocio básicas funcionan correctamente.
     * Casos límite: la respuesta incluye exactamente el número máximo permitido de selecciones.
     */
    @Test
    public void validarRespuesta_conSetValidoDentroDeMaxYOpciones_deberiaDevolverTrue() {
        // Arrange
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B", "C"));
        final CategoriaMultiple pregunta = new CategoriaMultiple("Texto", opciones, 2);
        final Set<String> respuesta = new HashSet<>(Arrays.asList("A", "C"));

        // Act
        final boolean valido = pregunta.validarRespuesta(respuesta);

        // Assert
        assertTrue("La respuesta deberia ser valida", valido);
    }

    /**
     * Comprueba: que una respuesta null es válida cuando la pregunta no es obligatoria.
     * Por qué: permite distinguir entre preguntas opcionales y obligatorias, reflejando
     * el comportamiento habitual en sistemas de encuestas donde el usuario puede omitir preguntas.
     * Casos límite: respuesta = null, la pregunta no se marca como obligatoria.
     */
    @Test
    public void validarRespuesta_conNull_noObligatoria_deberiaDevolverTrue() {
        // Arrange
        final CategoriaMultiple pregunta =
                new CategoriaMultiple("Texto", new HashSet<>(Arrays.asList("A", "B")), 1);

        // Act
        final boolean valido = pregunta.validarRespuesta(null);

        // Assert
        assertTrue("La respuesta null debería ser válida para una pregunta no obligatoria", valido);
    }

 
    /**
     * Comprueba: que una respuesta null se considera inválida cuando la pregunta es obligatoria.
     * Por qué: garantiza que el sistema exige al menos una respuesta en las preguntas obligatorias,
     * preservando la integridad de los datos recogidos.
     * Casos límite: obligatoria = true y respuesta = null.
     */
    @Test
    public void validarRespuesta_conNull_obligatoria_deberiaDevolverFalse() {
        // Arrange
        final CategoriaMultiple pregunta =
                new CategoriaMultiple("Texto", new HashSet<>(Arrays.asList("A", "B")), 1);
        pregunta.setObligatoria(true);

        // Act
        final boolean valido = pregunta.validarRespuesta(null);

        // Assert
        assertFalse("La respuesta null no debería ser válida para una pregunta obligatoria", valido);
    }

    /**
     * Comprueba: que si la respuesta no es un Set, se considera automáticamente inválida.
     * Por qué: refuerza la validación del tipo de dato esperado, evitando errores de tipo silenciosos.
     * Casos límite: respuesta de tipo String en lugar de Set<String>.
     */
    @Test
    public void validarRespuesta_conObjetoNoSet_deberiaDevolverFalse() {
        // Arrange
        final CategoriaMultiple pregunta =
                new CategoriaMultiple("Texto", new HashSet<>(Arrays.asList("A")), 1);

        // Act
        final boolean valido = pregunta.validarRespuesta("A");

        // Assert
        assertFalse("Una respuesta que no sea Set no deberia ser valida", valido);
    }

    /**
     * Comprueba: que una respuesta con más elementos que el máximo permitido se rechaza.
     * Por qué: asegura que la restricción definida por maxSelecciones se respeta estrictamente.
     * Casos límite: tamaño de respuesta = maxSelecciones + 1.
     */
    @Test
    public void validarRespuesta_conMasElementosQueMax_deberiaDevolverFalse() {
        // Arrange
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B", "C"));
        final CategoriaMultiple pregunta = new CategoriaMultiple("Texto", opciones, 2);
        final Set<String> respuesta = new HashSet<>(Arrays.asList("A", "B", "C"));

        // Act
        final boolean valido = pregunta.validarRespuesta(respuesta);

        // Assert
        assertFalse("Una respuesta con mas elementos que el maximo no deberia ser valida", valido);
    }

    /**
     * Comprueba: que una respuesta se considera inválida si contiene al menos una opción que no pertenece
     * al conjunto de opciones definidas originalmente para la pregunta.
     * Por qué: es esencial para evitar inconsistencias como seleccionar valores que nunca fueron ofrecidos
     * al usuario en la encuesta real.
     * Casos límite: mezcla de respuesta válida + opción inexistente.
     */
    @Test
    public void validarRespuesta_conOpcionNoValida_deberiaDevolverFalse() {
        // Arrange
        final Set<String> opciones = new HashSet<>(Arrays.asList("A", "B"));
        final CategoriaMultiple pregunta = new CategoriaMultiple("Texto", opciones, 2);
        final Set<String> respuesta = new HashSet<>(Arrays.asList("A", "C")); // "C" no forma parte del conjunto válido

        // Act
        final boolean valido = pregunta.validarRespuesta(respuesta);

        // Assert
        assertFalse("Una respuesta con opciones fuera del conjunto valido no deberia ser valida", valido);
    }
}