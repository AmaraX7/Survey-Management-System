package test;

import main.domain.classes.*;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Tests para el algoritmo KMedoids.
 *
 * KMedoids es una variante de clustering basada en prototipos reales del dataset
 * (medoides en lugar de centroides). Estos tests verifican:
 *
 *  - Correcta asignación de grupos y estructura de los centros.
 *  - Manejo de distintos tipos de pregunta (numérica, ordinal, categórica, etc.).
 *  - Comportamiento en casos extremos (dataset vacío, k inválido, todos los puntos iguales…).
 *  - Robustez numérica (ausencia de NaN/∞ en silhouette).
 *  - Funcionamiento en un escenario “realista” estilo dataset loan-*.csv.
 *
 * La estructura de cada test sigue la filosofía:
 *   - Arrange: preparación del dataset y configuración del algoritmo.
 *   - Act: ejecución de KMedoids.
 *   - Assert: comprobación de invariantes y resultados esperados.
 */
public class TestKMedoids {

    // =========================================================================
    // TEST 1: Caso básico NUMÉRICO con k = 2
    // =========================================================================
    /**
     * Caso básico sobre un dataset 1D numérico con dos grupos claramente separados.
     *
     * Comprueba:
     *  - Que el tamaño de groups coincide con el número de filas.
     *  - Que se generan exactamente k centros.
     *  - Que la dimensión de cada centro es correcta.
     *  - Que la silhouette se mantiene en el rango [-1, 1].
     *
     * Justificación:
     *  Es el test mínimo para comprobar que KMedoids funciona correctamente
     *  en el caso más simple y típico de clustering numérico.
     */
    @Test
    public void testKMedoidsSoloNumericaK2() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: KMEDOIDS SOLO NUMÉRICA (k = 2)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange: dataset 1D con dos grupos numéricos claros
        Object[][] data = {
                { 10.0 },
                { 11.0 },
                { 30.0 },
                { 31.0 }
        };

        int k = 2;
        KMedoids kmedoids = new KMedoids(k, 50);
        TipoPregunta[] tipos = { TipoPregunta.NUMERICA };
        kmedoids.setTipoPreguntas(tipos);
        kmedoids.setNumericRange(0, 0.0, 40.0);

        // Act
        ResultadoClustering res = kmedoids.execute(data);
        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double silhouette = res.getSilhouette();

        // Assert
        assertEquals("El tamaño de groups no coincide con el número de filas",
                data.length, groups.length);
        assertEquals("Debe haber exactamente k centros", k, centers.length);
        assertEquals("Cada centro debe tener 1 dimensión (1 pregunta)",
                1, centers[0].length);
        assertTrue("La silhouette debe estar en [-1,1]",
                silhouette >= -1.0 && silhouette <= 1.0);

        System.out.println("\n✓ Test 1 completado correctamente\n");
    }

    // =========================================================================
    // TEST 2: Caso extremo - un solo elemento
    // =========================================================================
    /**
     * Caso extremo con un único elemento y k = 1.
     *
     * Comprueba:
     *  - Que el único punto se asigna al cluster 0.
     *  - Que hay un único centro y de dimensión correcta.
     *  - Que la silhouette es 0 (no tiene sentido cohesión/separación con un solo punto).
     *
     * Importancia:
     *  Asegura que KMedoids no falla en casos degenerados con n = 1.
     */
    @Test
    public void testKMedoidsUnSoloElemento() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: KMEDOIDS CON UN SOLO ELEMENTO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange
        Object[][] data = {
                { "ROJO" }
        };

        int k = 1;
        KMedoids kmedoids = new KMedoids(k, 10);
        TipoPregunta[] tipos = { TipoPregunta.CATEGORIA_SIMPLE };
        kmedoids.setTipoPreguntas(tipos);

        // Act
        ResultadoClustering res = kmedoids.execute(data);
        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double silhouette = res.getSilhouette();

        // Assert
        assertEquals("Debe haber exactamente 1 elemento", 1, groups.length);
        assertEquals("El único elemento debería pertenecer al cluster 0", 0, groups[0]);
        assertEquals("Debe haber un único centro", 1, centers.length);
        assertEquals("El centro debe tener una dimensión", 1, centers[0].length);
        assertEquals("Con un solo elemento la silhouette debe ser 0",
                0.0, silhouette, 1e-9);

        System.out.println("\n✓ Test 2 completado correctamente\n");
    }

    // =========================================================================
    // TEST 3: Dataset con todos los tipos de pregunta
    // =========================================================================
    /**
     * Dataset mixto con NUMÉRICA, ORDINAL, CATEGORÍA_SIMPLE, CATEGORÍA_MÚLTIPLE, LIBRE.
     *
     * Comprueba:
     *  - Que groups tiene una asignación por cada fila.
     *  - Que se generan exactamente k centros.
     *  - Que la dimensionalidad de cada centro coincide con el número de columnas (5).
     *  - Que la silhouette es válida.
     *
     * Importancia:
     *  Valida que KMedoids integra correctamente la distancia heterogénea con
     *  múltiples tipos de atributos.
     */
    @Test
    public void testKMedoidsMixtoTipos() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: KMEDOIDS CON TIPOS MIXTOS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange
        Object[][] data = {
                { 10.0, "BAJO",  "ROJO", "A,B", "hola mundo" },
                { 20.0, "MEDIO", "ROJO", "A",   "hola mundo!" },
                { 30.0, "ALTO",  "AZUL", "B",   "adios" }
        };

        int k = 2;
        KMedoids kmedoids = new KMedoids(k, 50);

        TipoPregunta[] tipos = {
                TipoPregunta.NUMERICA,
                TipoPregunta.ORDINAL,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.CATEGORIA_MULTIPLE,
                TipoPregunta.LIBRE
        };
        kmedoids.setTipoPreguntas(tipos);
        kmedoids.setNumericRange(0, 0.0, 40.0);

        Set<String> orden = new HashSet<>();
        orden.add("BAJO");
        orden.add("MEDIO");
        orden.add("ALTO");
        kmedoids.setOrdinalOptions(1, orden);

        // Act
        ResultadoClustering res = kmedoids.execute(data);
        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double silhouette = res.getSilhouette();

        // Assert
        assertEquals("groups debe tener un valor por cada persona",
                data.length, groups.length);
        assertEquals("Debe haber k centros", k, centers.length);
        assertEquals("Cada centro debe tener 5 componentes",
                5, centers[0].length);

        for (int g : groups) {
            assertTrue("Asignación de grupo inválida: " + g, g >= 0 && g < k);
        }

        assertTrue("La silhouette debe estar en [-1,1]",
                silhouette >= -1.0 && silhouette <= 1.0);

        System.out.println("\n✓ Test 3 completado correctamente\n");
    }

    // =========================================================================
    // TEST 4: Dataset vacío
    // =========================================================================
    /**
     * Comportamiento ante dataset vacío.
     *
     * Debe lanzar IllegalArgumentException al intentar ejecutar con n = 0.
     */
    @Test
    public void testKMedoidsDatasetVacio() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 4: DATASET VACÍO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange
        Object[][] dataVacio = new Object[0][0];

        KMedoids kmedoids = new KMedoids(2, 10);
        TipoPregunta[] tipos = { TipoPregunta.NUMERICA };
        kmedoids.setTipoPreguntas(tipos);

        // Act
        boolean error = false;
        try {
            kmedoids.execute(dataVacio);
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Se lanzó IllegalArgumentException como se esperaba: " + e.getMessage());
            error = true;
        }

        // Assert
        assertTrue("Se esperaba IllegalArgumentException al ejecutar con dataset vacío", error);

        System.out.println("\n✓ Test 4 completado correctamente\n");
    }

    // =========================================================================
    // TEST 5: Uso incorrecto - sin setTipoPreguntas()
    // =========================================================================
    /**
     * Ejecutar KMedoids sin setTipoPreguntas() debe provocar una excepción
     * (IllegalStateException o NullPointerException).
     *
     * Justificación:
     *  La implementación depende del conocimiento del tipo de cada columna
     *  para calcular distancias. Sin esa configuración, el uso es inválido.
     */
    @Test
    public void testKMedoidsSinTipoPreguntas() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 5: USO SIN setTipoPreguntas()");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange
        Object[][] data = {
                { 1.0 },
                { 2.0 }
        };

        KMedoids kmedoids = new KMedoids(1, 10);

        // Act
        boolean error = false;
        try {
            kmedoids.execute(data);
        } catch (IllegalStateException | NullPointerException e) {
            System.out.println("  ✓ Se detectó uso incorrecto sin tipos: " + e.getClass().getSimpleName());
            error = true;
        }

        // Assert
        assertTrue("Se esperaba excepción al ejecutar KMedoids sin setTipoPreguntas()", error);

        System.out.println("\n✓ Test 5 completado correctamente\n");
    }

    // =========================================================================
    // TEST 6: k <= 0
    // =========================================================================
    /**
     * k ≤ 0 debe lanzar IllegalArgumentException al ejecutar el algoritmo.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testKMedoidsKNoPositivoLanzaExcepcion() {
        // Arrange
        Object[][] data = {
                { 1.0 },
                { 2.0 }
        };

        KMedoids kmedoids = new KMedoids(0, 10);
        kmedoids.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });

        // Act (lanzará excepción)
        kmedoids.execute(data);
    }

    // =========================================================================
    // TEST 7: k > número de puntos
    // =========================================================================
    /**
     * Si k > n (más clusters que puntos), la implementación debe lanzar
     * IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testKMedoidsKMayorQueNumeroPuntosLanzaExcepcion() {
        // Arrange
        Object[][] data = {
                { 1.0 },
                { 2.0 },
                { 3.0 }
        };

        int k = 5;
        KMedoids kmedoids = new KMedoids(k, 10);
        kmedoids.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });

        // Act (lanzará excepción)
        kmedoids.execute(data);
    }

    // =========================================================================
    // TEST 8: Todos los puntos idénticos con k = 1
    // =========================================================================
    /**
     * Todos los puntos son idénticos y k = 1.
     *
     * Comprueba:
     *  - Todos los groups asignados al mismo cluster.
     *  - El medoide es un punto con valor 5.0.
     *  - La silhouette es 0 (no hay separación entre clusters).
     */
    @Test
    public void testKMedoidsTodosPuntosIdenticos() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 8: TODOS LOS PUNTOS IDÉNTICOS (k = 1)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange
        Object[][] data = {
                { 5.0 },
                { 5.0 },
                { 5.0 },
                { 5.0 }
        };

        int k = 1;
        KMedoids kmedoids = new KMedoids(k, 20);
        kmedoids.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmedoids.setNumericRange(0, 0.0, 10.0);

        // Act
        ResultadoClustering res = kmedoids.execute(data);
        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double silhouette = res.getSilhouette();

        // Assert
        assertEquals(4, groups.length);
        for (int g : groups) {
            assertEquals(0, g);
        }

        assertEquals(1, centers.length);
        assertEquals(1, centers[0].length);
        double centro = (double) centers[0][0];
        assertEquals(5.0, centro, 1e-6);

        assertEquals(0.0, silhouette, 1e-6);
        assertFalse(Double.isNaN(silhouette));

        System.out.println("\n✓ Test 8 completado correctamente\n");
    }

    // =========================================================================
    // TEST 9: Dos grupos bien separados → silhouette alta
    // =========================================================================
    /**
     * Dos grupos numéricos bien separados con k = 2.
     *
     * Aquí se espera una silhouette relativamente alta (> 0.5) y finita.
     * Sirve para validar que el algoritmo detecta correctamente clusters
     * bien definidos.
     */
    @Test
    public void testKMedoidsDosGruposBienSeparadosSilhouetteAlta() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 9: DOS GRUPOS BIEN SEPARADOS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange
        Object[][] data = {
                { 10.0 },
                { 11.0 },
                { 30.0 },
                { 31.0 }
        };

        int k = 2;
        KMedoids kmedoids = new KMedoids(k, 50);
        kmedoids.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmedoids.setNumericRange(0, 0.0, 40.0);

        // Act
        ResultadoClustering res = kmedoids.execute(data);
        double silhouette = res.getSilhouette();

        // Assert
        assertTrue("Silhouette debería ser > 0.5 en grupos bien separados", silhouette > 0.5);
        assertFalse("Silhouette no debe ser NaN", Double.isNaN(silhouette));

        System.out.println("\n✓ Test 9 completado correctamente\n");
    }

    // =========================================================================
    // TEST 10: Robustez numérica con tipos mixtos
    // =========================================================================
    /**
     * Test de robustez numérica con tipos mixtos (igual que en KMeans/KMeans++).
     *
     * Se comprueba únicamente que:
     *  - La silhouette no es NaN.
     *  - La silhouette no es infinita.
     *
     * Esto garantiza que no hay divisiones por cero no controladas ni
     * desbordamientos numéricos en el cálculo del índice de silhouette.
     */
    @Test
    public void testKMedoidsRobustezNumericaMixto() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 10: ROBUSTEZ NUMÉRICA CON TIPOS MIXTOS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange
        Object[][] data = {
                { 10.0, "BAJO",  "ROJO", "A,B", "hola" },
                { 20.0, "MEDIO", "ROJO", "A",   "hola!!" },
                { 30.0, "ALTO",  "AZUL", "B",   "adios" }
        };

        int k = 2;
        KMedoids kmedoids = new KMedoids(k, 50);

        TipoPregunta[] tipos = {
                TipoPregunta.NUMERICA,
                TipoPregunta.ORDINAL,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.CATEGORIA_MULTIPLE,
                TipoPregunta.LIBRE
        };
        kmedoids.setTipoPreguntas(tipos);
        kmedoids.setNumericRange(0, 0.0, 40.0);

        Set<String> orden = new HashSet<>();
        orden.add("BAJO");
        orden.add("MEDIO");
        orden.add("ALTO");
        kmedoids.setOrdinalOptions(1, orden);

        // Act
        ResultadoClustering res = kmedoids.execute(data);
        double silhouette = res.getSilhouette();

        // Assert
        assertFalse("La silhouette no debería ser NaN", Double.isNaN(silhouette));
        assertFalse("La silhouette no debería ser infinita", Double.isInfinite(silhouette));

        System.out.println("  ✓ Silhouette: " + silhouette);
        System.out.println("\n✓ Test 10 completado correctamente\n");
    }

    // =========================================================================
    // TEST 11: Dataset realista estilo loan-*.csv
    // =========================================================================
    /**
     * Dataset “realista” similar al clásico loan-train / loan-test.
     *
     * Valida que:
     *  - groups tiene tamaño n.
     *  - Se generan k centros, cada uno con 11 columnas.
     *  - Todas las asignaciones de grupos son válidas (0 ≤ g < k).
     *  - La silhouette es finita y está en el rango [-1, 1].
     *  - Se utilizan al menos 2 clusters distintos.
     *
     * Importancia:
     *  Es una prueba de integración de KMedoids en un contexto de datos
     *  heterogéneos más real.
     */
    @Test
    public void testKMedoidsLoanLikeDataset() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 11: DATASET TIPO LOAN-*.csv (HARDCODED)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Arrange
        Object[][] data = {
                { "Male",   "Yes", 0, "Graduate",     "No",  5720.0,  0.0, 110.0, 360.0, 1.0, "Urban"   },
                { "Male",   "Yes", 1, "Graduate",     "No",  4583.0, 1508.0,128.0,360.0, 1.0, "Rural"   },
                { "Male",   "Yes", 2, "Graduate",     "No",  6000.0,  0.0, 141.0,360.0, 1.0, "Urban"   },
                { "Male",   "No",  0, "Not Graduate", "Yes", 2333.0, 1516.0, 95.0,360.0, 1.0, "Semiurban"},
                { "Female", "No",  0, "Graduate",     "No",  3036.0,  0.0,  80.0,360.0, 1.0, "Urban"   },
                { "Male",   "Yes", 3, "Graduate",     "Yes", 4000.0, 2500.0,120.0,360.0, 0.0, "Rural"   },
                { "Female", "Yes", 1, "Graduate",     "No",  6000.0,  0.0, 150.0,360.0, 1.0, "Semiurban"},
                { "Male",   "No",  0, "Graduate",     "No",  2500.0, 2000.0,110.0,180.0, 0.0, "Urban"   },
                { "Male",   "No",  0, "Not Graduate", "No",  3500.0, 1200.0,120.0,180.0, 1.0, "Rural"   }
        };

        int n = data.length;
        int k = 3;

        KMedoids kmedoids = new KMedoids(k, 100);

        TipoPregunta[] tipos = {
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.NUMERICA,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.NUMERICA,
                TipoPregunta.NUMERICA,
                TipoPregunta.NUMERICA,
                TipoPregunta.NUMERICA,
                TipoPregunta.NUMERICA,
                TipoPregunta.CATEGORIA_SIMPLE
        };
        kmedoids.setTipoPreguntas(tipos);

        // Normalización aproximada de las columnas numéricas
        kmedoids.setNumericRange(2, 0.0, 3.0);
        kmedoids.setNumericRange(5, 0.0, 10000.0);
        kmedoids.setNumericRange(6, 0.0, 5000.0);
        kmedoids.setNumericRange(7, 0.0, 500.0);
        kmedoids.setNumericRange(8, 0.0, 480.0);
        kmedoids.setNumericRange(9, 0.0, 1.0);

        // Act
        ResultadoClustering res = kmedoids.execute(data);
        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double silhouette = res.getSilhouette();

        // Assert
        assertEquals(n, groups.length);
        assertEquals(k, centers.length);

        for (int i = 0; i < k; i++) {
            assertEquals("Cada centro debe tener 11 componentes", 11, centers[i].length);
        }

        for (int g : groups) {
            assertTrue("Grupo fuera de rango: " + g, g >= 0 && g < k);
        }

        assertFalse("La silhouette no debe ser NaN", Double.isNaN(silhouette));
        assertFalse("La silhouette no debe ser infinita", Double.isInfinite(silhouette));
        assertTrue("La silhouette debe estar en [-1,1]",
                silhouette >= -1.0 && silhouette <= 1.0);

        Set<Integer> usados = new HashSet<>();
        for (int g : groups) usados.add(g);
        assertTrue("Se deberían utilizar al menos 2 clusters distintos", usados.size() >= 2);

        System.out.println("  ✓ Clusters utilizados: " + usados);
        System.out.println("\n✓ Test 11 (loan) completado correctamente\n");
    }

}
