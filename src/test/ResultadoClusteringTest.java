package test;

import main.domain.classes.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase {@link ResultadoClustering}.
 *
 * <p>
 * Se validan principalmente:
 * <ul>
 *   <li>Inicialización completa del resultado (grupos, centros, métricas, metadatos).</li>
 *   <li>Comportamiento frente a {@code null} y arrays vacíos en {@code groups}/{@code centers}.</li>
 *   <li>Gestión de metadatos de contexto: {@code idEncuesta} e {@code idsUsuarios}.</li>
 *   <li>Lógica de agrupación de usuarios por grupo.</li>
 *   <li>Efectos de aliasing al no realizar copia defensiva de arrays.</li>
 * </ul>
 * </p>
 */
public class ResultadoClusteringTest {

    /**
     * Verifica que el constructor completo de {@link ResultadoClustering}
     * inicializa correctamente todos los campos principales.
     *
     * <p>
     * Se comprueba la correcta asignación de:
     * <ul>
     *   <li>Grupos</li>
     *   <li>Centros</li>
     *   <li>Silhouette</li>
     *   <li>Algoritmo</li>
     *   <li>k</li>
     *   <li>Número de iteraciones</li>
     *   <li>Inercia</li>
     * </ul>
     * </p>
     *
     * <p>
     * Además, se valida que {@code idEncuesta} permanece inicialmente en {@code null}.
     * </p>
     */
    @Test
    public void constructorEnriquecido_deberiaInicializarTodosLosCampos() {
        // Arrange
        final int[] groups = new int[] {0, 1, 0, 1};
        final Object[][] centers = new Object[][] {
                {5.0, 10.0},
                {15.0, 20.0}
        };
        final double silhouette = 0.82;
        final String algoritmo = "KMedoids";
        final int k = 2;
        final int numIteraciones = 15;
        final double inercia = 12.34;

        // Act
        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, silhouette,
                algoritmo, k, numIteraciones, inercia
        );

        // Assert
        assertArrayEquals("groups no coincide", groups, resultado.getGroups());
        assertTrue("centers no coincide", Arrays.deepEquals(centers, resultado.getCenters()));
        assertEquals("silhouette no coincide", silhouette, resultado.getSilhouette(), 0.000001);
        assertEquals("algoritmo no coincide", algoritmo, resultado.getAlgoritmo());
        assertEquals("k no coincide", k, resultado.getK());
        assertEquals("numIteraciones no coincide", numIteraciones, resultado.getNumIteraciones());
        assertEquals("inercia no coincide", inercia, resultado.getInercia(), 0.000001);
        assertNull("idEncuesta debería ser null inicialmente", resultado.getIdEncuesta());
    }

    /**
     * Comprueba que el constructor permite valores {@code null} en
     * {@code groups} y {@code centers} sin lanzar excepciones.
     *
     * <p>
     * Este test valida que el objeto puede representar resultados parciales
     * o incompletos sin romper el flujo de ejecución.
     * </p>
     */
    @Test
    public void constructorEnriquecido_conValoresNull_deberiaPermitirNullEnArrays() {
        // Arrange
        final int[] groups = null;
        final Object[][] centers = null;
        final double silhouette = 0.0;

        // Act
        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, silhouette,
                "KMeans", 2, 10, 5.0
        );

        // Assert
        assertNull("groups debería ser null", resultado.getGroups());
        assertNull("centers debería ser null", resultado.getCenters());
        assertEquals("silhouette debería ser 0.0", 0.0, resultado.getSilhouette(), 0.000001);
    }

    /**
     * Verifica que el constructor conserva arrays vacíos
     * sin transformarlos en {@code null}.
     *
     * <p>
     * Este comportamiento permite distinguir entre ausencia de datos
     * ({@code null}) y presencia de datos sin elementos (array vacío).
     * </p>
     */
    @Test
    public void constructorEnriquecido_conArraysVacios_deberiaGuardarArraysVacios() {
        // Arrange
        final int[] groups = new int[0];
        final Object[][] centers = new Object[0][0];
        final double silhouette = 1.0;

        // Act
        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, silhouette,
                "KMeans", 0, 0, 0.0
        );

        // Assert
        assertNotNull("groups no debería ser null", resultado.getGroups());
        assertEquals("groups debería estar vacío", 0, resultado.getGroups().length);

        assertNotNull("centers no debería ser null", resultado.getCenters());
        assertEquals("centers debería tener longitud 0", 0, resultado.getCenters().length);

        assertEquals("silhouette debería ser 1.0", 1.0, resultado.getSilhouette(), 0.000001);
    }

    /**
     * Comprueba que {@link ResultadoClustering#setIdEncuesta(String)}
     * almacena correctamente el identificador de la encuesta.
     */
    @Test
    public void setIdEncuesta_deberiaGuardarElId() {
        // Arrange
        final int[] groups = new int[] {0, 1, 0};
        final Object[][] centers = new Object[][] { {1}, {2} };
        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, 0.5,
                "KMeans", 2, 5, 3.0
        );
        final String idEncuesta = "encuesta_123";

        // Act
        resultado.setIdEncuesta(idEncuesta);

        // Assert
        assertEquals("El idEncuesta no coincide", idEncuesta, resultado.getIdEncuesta());
    }

    /**
     * Verifica que {@link ResultadoClustering#setIdsUsuarios(List)}
     * almacena correctamente la lista de identificadores de usuario.
     *
     * <p>
     * Se valida tanto el tamaño como el orden de los IDs almacenados.
     * </p>
     */
    @Test
    public void setIdsUsuarios_deberiaGuardarLaListaDeIds() {
        // Arrange
        final int[] groups = new int[] {0, 1, 0};
        final Object[][] centers = new Object[][] { {1}, {2} };
        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, 0.5,
                "KMeans", 2, 5, 3.0
        );
        final List<String> ids = Arrays.asList("user1", "user2", "user3");

        // Act
        resultado.setIdsUsuarios(ids);

        // Assert
        assertNotNull("idsUsuarios no debería ser null", resultado.idsUsuarios);
        assertEquals("El tamaño de idsUsuarios no coincide", 3, resultado.idsUsuarios.size());
        assertEquals("El primer ID no coincide", "user1", resultado.idsUsuarios.get(0));
    }

    /**
     * Comprueba que {@link ResultadoClustering#getUsuariosPorGrupo()}
     * agrupa correctamente los usuarios según el vector {@code groups}.
     */
    @Test
    public void getUsuariosPorGrupo_deberiaAgruparUsuariosCorrectamente() {
        // Arrange
        final int[] groups = new int[] {0, 1, 0, 1, 2};
        final Object[][] centers = new Object[][] { {1}, {2}, {3} };

        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, 0.6,
                "KMeans", 3, 10, 5.0
        );

        resultado.setIdsUsuarios(Arrays.asList("u1", "u2", "u3", "u4", "u5"));

        // Act
        final List<List<String>> usuariosPorGrupo = resultado.getUsuariosPorGrupo();

        // Assert
        assertEquals("Debería haber 3 grupos", 3, usuariosPorGrupo.size());
        assertEquals(2, usuariosPorGrupo.get(0).size());
        assertEquals(2, usuariosPorGrupo.get(1).size());
        assertEquals(1, usuariosPorGrupo.get(2).size());
    }

    /**
     * Verifica que {@link ResultadoClustering#getUsuariosPorGrupo()}
     * lanza {@link NullPointerException} si {@code idsUsuarios} no ha sido inicializado.
     */
    @Test
    public void getUsuariosPorGrupo_sinIdsUsuarios_deberiaLanzarExcepcion() {
        // Arrange
        final int[] groups = new int[] {0, 1};
        final Object[][] centers = new Object[][] { {1}, {2} };

        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, 0.5,
                "KMeans", 2, 5, 3.0
        );

        // Act & Assert
        try {
            resultado.getUsuariosPorGrupo();
            fail("Debería lanzar NullPointerException cuando idsUsuarios es null");
        } catch (NullPointerException e) {
            // Esperado
        }
    }

    /**
     * Documenta el comportamiento de aliasing al no realizar copia defensiva
     * del array {@code groups} en el constructor.
     */
    @Test
    public void modificacionDeArrayExterno_deberiaAfectarAGroupsInternoPorqueNoHayCopia() {
        // Arrange
        final int[] groups = new int[] {0, 1};
        final Object[][] centers = new Object[][] { {1}, {2} };
        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, 0.3,
                "KMeans", 2, 5, 3.0
        );

        // Act
        groups[0] = 99;

        // Assert
        assertEquals(99, resultado.getGroups()[0]);
    }

    /**
     * Comprueba el funcionamiento de {@link ResultadoClustering#getUsuariosPorGrupo()}
     * cuando solo existe un único cluster (k = 1).
     */
    @Test
    public void getUsuariosPorGrupo_conUnSoloGrupo_deberiaFuncionar() {
        // Arrange
        final int[] groups = new int[] {0, 0, 0};
        final Object[][] centers = new Object[][] { {1.0} };

        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, 0.9,
                "KMeans", 1, 5, 2.0
        );

        resultado.setIdsUsuarios(Arrays.asList("u1", "u2", "u3"));

        // Act
        final List<List<String>> usuariosPorGrupo = resultado.getUsuariosPorGrupo();

        // Assert
        assertEquals(1, usuariosPorGrupo.size());
        assertEquals(3, usuariosPorGrupo.get(0).size());
    }

    /**
     * Verifica que {@link ResultadoClustering#getUsuariosPorGrupo()}
     * crea listas vacías para clusters sin usuarios asignados.
     */
    @Test
    public void getUsuariosPorGrupo_conGruposVacios_deberiaCrearListasVacias() {
        // Arrange
        final int[] groups = new int[] {0, 2};
        final Object[][] centers = new Object[][] { {1}, {2}, {3} };

        final ResultadoClustering resultado = new ResultadoClustering(
                groups, centers, 0.7,
                "KMeans", 3, 8, 4.0
        );

        resultado.setIdsUsuarios(Arrays.asList("u1", "u2"));

        // Act
        final List<List<String>> usuariosPorGrupo = resultado.getUsuariosPorGrupo();

        // Assert
        assertEquals(3, usuariosPorGrupo.size());
        assertEquals(1, usuariosPorGrupo.get(0).size());
        assertEquals(0, usuariosPorGrupo.get(1).size());
        assertEquals(1, usuariosPorGrupo.get(2).size());
    }
}
