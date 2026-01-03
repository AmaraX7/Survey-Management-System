package test;

import main.domain.classes.*;

import org.junit.Test;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase {@link Respuesta}.
 *
 * <p>
 * Esta clase valida el comportamiento completo de una respuesta dentro del sistema,
 * cubriendo tanto su ciclo de vida como su identidad lógica.
 * </p>
 *
 * <p>
 * Se comprueban específicamente:
 * <ul>
 *   <li>Inicialización correcta de los identificadores (usuario, pregunta, encuesta).</li>
 *   <li>Gestión del estado {@code contestada} en función del valor asociado.</li>
 *   <li>Comportamiento de los métodos {@code setValor()} y {@code limpiar()}.</li>
 *   <li>Contrato {@code equals}/{@code hashCode}, basado exclusivamente en las IDs.</li>
 *   <li>Comparaciones con {@code null}, misma referencia y objetos de otras clases.</li>
 *   <li>Contenido informativo del método {@code toString()}.</li>
 * </ul>
 * </p>
 */
public class RespuestaTest {

    /**
     * Verifica que el constructor con valor inicializa correctamente todos los campos
     * y marca la respuesta como contestada.
     *
     * <p>
     * Caso nominal de uso: una respuesta creada directamente con un valor asociado.
     * </p>
     */
    @Test
    public void constructorConValor_deberiaInicializarCamposCorrectamenteYMarcarContestada() {
        // Arrange & Act
        final Respuesta r = new Respuesta("u1", "p1", "e1", "valor");

        // Assert
        assertEquals("El idUsuario no es correcto", "u1", r.getIdUsuario());
        assertEquals("El idPregunta no es correcto", "p1", r.getIdPregunta());
        assertEquals("El idEncuesta no es correcto", "e1", r.getIdEncuesta());
        assertEquals("El valor no es correcto", "valor", r.getValor());
        assertTrue("Deberia marcarse como contestada cuando valor != null", r.estaContestada());
    }

    /**
     * Comprueba que el constructor sin valor inicial deja la respuesta
     * en estado no contestado.
     *
     * <p>
     * Representa el estado inicial de una respuesta aún no respondida.
     * </p>
     */
    @Test
    public void constructorSinValor_deberiaInicializarConValorNullYNoContestada() {
        // Arrange & Act
        final Respuesta r = new Respuesta("u1", "p1", "e1");

        // Assert
        assertEquals("El idUsuario no es correcto", "u1", r.getIdUsuario());
        assertEquals("El idPregunta no es correcto", "p1", r.getIdPregunta());
        assertEquals("El idEncuesta no es correcto", "e1", r.getIdEncuesta());
        assertNull("El valor deberia ser null", r.getValor());
        assertFalse("Deberia estar no contestada si se construye sin valor", r.estaContestada());
    }

    /**
     * Verifica que {@link Respuesta#setValor(Object)} con un valor no nulo
     * actualiza correctamente el valor y marca la respuesta como contestada.
     */
    @Test
    public void setValor_conValorNoNull_deberiaActualizarValorYMarcarContestada() {
        // Arrange
        final Respuesta r = new Respuesta("u1", "p1", "e1");

        // Act
        r.setValor(123);

        // Assert
        assertEquals("El valor no se ha actualizado correctamente", 123, r.getValor());
        assertTrue("Deberia estar contestada cuando se asigna valor != null", r.estaContestada());
    }

    /**
     * Comprueba que {@link Respuesta#setValor(Object)} con {@code null}
     * deja la respuesta en estado no contestado.
     */
    @Test
    public void setValor_conNull_deberiaPonerValorNullYNoContestada() {
        // Arrange
        final Respuesta r = new Respuesta("u1", "p1", "e1", "algo");

        // Act
        r.setValor(null);

        // Assert
        assertNull("El valor deberia ser null despues de setValor(null)", r.getValor());
        assertFalse("Deberia estar no contestada cuando valor es null", r.estaContestada());
    }

    /**
     * Verifica que el método {@link Respuesta#limpiar()}
     * elimina el valor y marca la respuesta como no contestada.
     *
     * <p>
     * Es equivalente semánticamente a {@code setValor(null)}.
     * </p>
     */
    @Test
    public void limpiar_deberiaPonerValorNullYNoContestada() {
        // Arrange
        final Respuesta r = new Respuesta("u1", "p1", "e1", "valor");

        // Act
        r.limpiar();

        // Assert
        assertNull("El valor deberia ser null despues de limpiar", r.getValor());
        assertFalse("Deberia estar no contestada despues de limpiar", r.estaContestada());
    }

    /**
     * Comprueba que {@link Respuesta#equals(Object)} considera iguales dos respuestas
     * con las mismas IDs, independientemente de su valor.
     *
     * <p>
     * También valida que {@code hashCode} coincide en este caso.
     * </p>
     */
    @Test
    public void equals_mismasIds_deberiaSerTrueAunqueCambioElValor() {
        // Arrange
        final Respuesta r1 = new Respuesta("u1", "p1", "e1", "v1");
        final Respuesta r2 = new Respuesta("u1", "p1", "e1", "v2");

        // Act
        final boolean iguales = r1.equals(r2);

        // Assert
        assertTrue("Dos respuestas con mismas ids deberian ser iguales aunque cambie el valor", iguales);
        assertEquals("hashCode deberia coincidir cuando las ids son iguales",
                r1.hashCode(), r2.hashCode());
    }

    /**
     * Verifica que {@link Respuesta#equals(Object)} devuelve {@code false}
     * cuando el {@code idUsuario} es distinto.
     */
    @Test
    public void equals_conDistintoIdUsuario_deberiaSerFalse() {
        // Arrange
        final Respuesta r1 = new Respuesta("u1", "p1", "e1", "v1");
        final Respuesta r2 = new Respuesta("u2", "p1", "e1", "v1");

        // Act
        final boolean iguales = r1.equals(r2);

        // Assert
        assertFalse("Respuestas con distinto idUsuario no deberian ser iguales", iguales);
    }

    /**
     * Verifica que {@link Respuesta#equals(Object)} devuelve {@code false}
     * cuando el {@code idPregunta} es distinto.
     */
    @Test
    public void equals_conDistintoIdPregunta_deberiaSerFalse() {
        // Arrange
        final Respuesta r1 = new Respuesta("u1", "p1", "e1", "v1");
        final Respuesta r2 = new Respuesta("u1", "p2", "e1", "v1");

        // Act
        final boolean iguales = r1.equals(r2);

        // Assert
        assertFalse("Respuestas con distinto idPregunta no deberian ser iguales", iguales);
    }

    /**
     * Verifica que {@link Respuesta#equals(Object)} devuelve {@code false}
     * cuando el {@code idEncuesta} es distinto.
     */
    @Test
    public void equals_conDistintoIdEncuesta_deberiaSerFalse() {
        // Arrange
        final Respuesta r1 = new Respuesta("u1", "p1", "e1", "v1");
        final Respuesta r2 = new Respuesta("u1", "p1", "e2", "v1");

        // Act
        final boolean iguales = r1.equals(r2);

        // Assert
        assertFalse("Respuestas con distinto idEncuesta no deberian ser iguales", iguales);
    }

    /**
     * Comprueba la propiedad reflexiva del método {@link Respuesta#equals(Object)}.
     */
    @Test
    public void equals_conMismaReferencia_deberiaSerTrue() {
        // Arrange
        final Respuesta r = new Respuesta("u1", "p1", "e1", "v1");

        // Act
        final boolean iguales = r.equals(r);

        // Assert
        assertTrue("Una instancia deberia ser igual a si misma", iguales);
    }

    /**
     * Verifica que {@link Respuesta#equals(Object)} devuelve {@code false}
     * al compararse con {@code null}.
     */
    @Test
    public void equals_conNull_deberiaSerFalse() {
        // Arrange
        final Respuesta r = new Respuesta("u1", "p1", "e1", "v1");

        // Act
        final boolean iguales = r.equals(null);

        // Assert
        assertFalse("Una respuesta nunca deberia ser igual a null", iguales);
    }

    /**
     * Verifica que {@link Respuesta#equals(Object)} devuelve {@code false}
     * al compararse con un objeto de otra clase.
     */
    @Test
    public void equals_conObjetoDeOtraClase_deberiaSerFalse() {
        // Arrange
        final Respuesta r = new Respuesta("u1", "p1", "e1", "v1");
        final Object otro = new Object();

        // Act
        final boolean iguales = r.equals(otro);

        // Assert
        assertFalse("Una respuesta no deberia ser igual a un objeto de otra clase", iguales);
    }

    /**
     * Comprueba explícitamente que {@link Respuesta#hashCode()}
     * coincide para respuestas con las mismas IDs.
     */
    @Test
    public void hashCode_mismasIds_deberiaSerIgual() {
        // Arrange
        final Respuesta r1 = new Respuesta("u1", "p1", "e1", "v1");
        final Respuesta r2 = new Respuesta("u1", "p1", "e1", "v2");

        // Act
        final int h1 = r1.hashCode();
        final int h2 = r2.hashCode();

        // Assert
        assertEquals("hashCode deberia ser igual cuando las ids son iguales", h1, h2);
    }

    /**
     * Verifica que {@link Respuesta#toString()} incluye los campos relevantes
     * y refleja correctamente el estado de respuesta contestada.
     */
    @Test
    public void toString_deberiaIncluirCamposMasRelevantes() {
        // Arrange
        final Respuesta r = new Respuesta("u1", "p1", "e1", "valor");

        // Act
        final String texto = r.toString();

        // Assert
        assertTrue("toString deberia contener el idUsuario", texto.contains("u1"));
        assertTrue("toString deberia contener el idPregunta", texto.contains("p1"));
        assertTrue("toString deberia contener el idEncuesta", texto.contains("e1"));
        assertTrue("toString deberia contener el valor", texto.contains("valor"));
        assertTrue("toString deberia reflejar que esta contestada",
                texto.contains("contestada=true"));
    }

    /**
     * Verifica que {@link Respuesta#toString()} refleja correctamente
     * el estado no contestado cuando el valor es {@code null}.
     */
    @Test
    public void toString_conValorNull_deberiaReflejarContestadaFalse() {
        // Arrange
        final Respuesta r = new Respuesta("u1", "p1", "e1");

        // Act
        final String texto = r.toString();

        // Assert
        assertTrue("toString deberia reflejar contestada=false",
                texto.contains("contestada=false"));
    }
}
