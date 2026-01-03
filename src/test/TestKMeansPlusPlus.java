package test;

import main.domain.classes.*;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Test para el algoritmo KMeansPlusPlus.
 *
 * Este archivo valida el comportamiento de la implementación de KMeans++,
 * cubriendo tanto casos básicos como extremos y escenarios realistas.
 *
 * KMeans++ introduce una selección inicial de centroides optimizada que:
 *   - Reduce la probabilidad de converger en mínimos locales pobres.
 *   - Acelera la convergencia.
 *   - Mejora la calidad estructural del clustering (e.g., silhouette mayor).
 *
 * Los tests se centran en:
 *   ✔ Corrección de asignaciones (groups).
 *   ✔ Validez dimensional de los centros.
 *   ✔ Mantenimiento de invariantes fundamentales del clustering.
 *   ✔ Manejo de errores ante configuraciones inválidas.
 *   ✔ Consistencia numérica (ausencia de NaN/∞).
 *   ✔ Comprobación empírica de que KMeans++ tiende a obtener silhouette ≥ KMeans.
 */
public class TestKMeansPlusPlus {

    // =========================================================================
    // TEST 1 — CASO BÁSICO NUMÉRICO (k = 2)
    // =========================================================================
    /**
     * Caso fundamental con datos numéricos 1D claramente separables.
     *
     * Este test asegura que:
     *   - La asignación de clusters tiene coherencia dimensional.
     *   - El número de centros es exactamente k.
     *   - La silhouette cae dentro del intervalo válido [-1, 1].
     *   - No aparecen errores debidos a la inicialización probabilística.
     *
     * Justificación:
     *   Es el equivalente al test mínimo operativo del algoritmo.
     *   Para KMeans++ también garantiza que la inicialización no rompe la ejecución.
     */
    @Test
    public void testKMeansPlusPlusSoloNumericaK2() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: KMEANS++ SOLO NUMÉRICA (k = 2)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Dataset: 4 usuarios con una sola dimensión numérica
        Object[][] data = {
                { 10.0 },
                { 11.0 },
                { 30.0 },
                { 31.0 }
        };

        int k = 2;
        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(k, 50);

        // Configuración necesaria
        kmeanspp.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeanspp.setNumericRange(0, 0.0, 40.0);

        ResultadoClustering res = kmeanspp.execute(data);

        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double silhouette = res.getSilhouette();

        // Comprobaciones mínimas de consistencia
        assertEquals("groups debe tener misma longitud que dataset", data.length, groups.length);
        assertEquals("Debe haber k centros", k, centers.length);
        assertEquals("Cada centro debe tener 1 dimensión", 1, centers[0].length);
        assertTrue("silhouette debe estar en [-1,1]", silhouette >= -1 && silhouette <= 1);

        System.out.println("\n✓ Test 1 completado correctamente\n");
    }

    // =========================================================================
    // TEST 2 — CASO EXTREMO: UN SOLO ELEMENTO
    // =========================================================================
    /**
     * Caso extremo con un único elemento.
     *
     * Verifica:
     *   - groups = {0}
     *   - Un único centro
     *   - silhouette = 0.0
     *
     * Justificación:
     *   Con n = 1 no existen distancias interpunto, por lo que silhouette → 0.
     *   Garantiza estabilidad del algoritmo en casos degenerados.
     */
    @Test
    public void testKMeansPlusPlusUnSoloElemento() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: KMEANS++ CON UN SOLO ELEMENTO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Object[][] data = { { "ROJO" } };

        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(1, 10);
        kmeanspp.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.CATEGORIA_SIMPLE });

        ResultadoClustering res = kmeanspp.execute(data);

        assertEquals("groups debe tener 1 elemento", 1, res.getGroups().length);
        assertEquals("Elemento único debe caer en cluster 0", 0, res.getGroups()[0]);
        assertEquals("Debe haber un solo centro", 1, res.getCenters().length);
        assertEquals("silhouette debe ser 0 con un solo dato", 0.0, res.getSilhouette(), 1e-9);

        System.out.println("\n✓ Test 2 completado correctamente\n");
    }

    // =========================================================================
    // TEST 3 — TIPOS MIXTOS
    // =========================================================================
    /**
     * Valida que el algoritmo maneja correctamente todos los tipos de pregunta
     * simultáneamente: NUMÉRICA, ORDINAL, CATEGORÍA SIMPLE, MÚLTIPLE y LIBRE.
     *
     * Se comprueba:
     *   - Dimensionalidad exacta de centros.
     *   - groups válido.
     *   - silhouette en rango legal.
     *
     * Justificación:
     *   Este test garantiza que el motor de distancia y la actualización de centros
     *   funciona correctamente en un dominio heterogéneo.
     */
    @Test
    public void testKMeansPlusPlusMixtoTipos() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: KMEANS++ CON TIPOS MIXTOS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Object[][] data = {
                { 10.0, "BAJO",  "ROJO", "A,B", "hola mundo" },
                { 20.0, "MEDIO", "ROJO", "A",   "hola mundo!" },
                { 30.0, "ALTO",  "AZUL", "B",   "adios" }
        };

        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(2, 50);
        TipoPregunta[] tipos = {
                TipoPregunta.NUMERICA,
                TipoPregunta.ORDINAL,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.CATEGORIA_MULTIPLE,
                TipoPregunta.LIBRE
        };
        kmeanspp.setTipoPreguntas(tipos);
        kmeanspp.setNumericRange(0, 0.0, 40.0);

        Set<String> orden = Set.of("BAJO", "MEDIO", "ALTO");
        kmeanspp.setOrdinalOptions(1, orden);

        ResultadoClustering res = kmeanspp.execute(data);

        assertEquals("groups debe tener longitud = filas", data.length, res.getGroups().length);
        assertEquals("Debe haber k centros", 2, res.getCenters().length);
        assertEquals("Cada centro debe tener 5 columnas", 5, res.getCenters()[0].length);
        assertTrue("Silhouette debe ser válida", res.getSilhouette() >= -1 && res.getSilhouette() <= 1);

        System.out.println("\n✓ Test 3 completado correctamente\n");
    }

    // =========================================================================
    // TEST 4 — DATASET VACÍO
    // =========================================================================
    /**
     * Verifica que ejecutar el algoritmo con un dataset vacío produce una excepción clara.
     */
    @Test
    public void testKMeansPlusPlusDatasetVacio() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 4: DATASET VACÍO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(2, 10);
        kmeanspp.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });

        boolean error = false;
        try {
            kmeanspp.execute(new Object[0][0]);
        } catch (IllegalArgumentException e) {
            error = true;
        }

        assertTrue("Debe lanzar IllegalArgumentException", error);

        System.out.println("\n✓ Test 4 completado correctamente\n");
    }

    // =========================================================================
    // TEST 5 — USO INCORRECTO SIN CONFIGURAR TIPOS
    // =========================================================================
    /**
     * Ejecutar sin definir setTipoPreguntas() debe provocar excepción.
     */
    @Test
    public void testKMeansPlusPlusSinTipoPreguntas() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 5: USO SIN setTipoPreguntas()");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(1, 10);

        boolean error = false;
        try {
            kmeanspp.execute(new Object[][] { {1.0}, {2.0} });
        } catch (IllegalStateException | NullPointerException e) {
            error = true;
        }

        assertTrue("Debe detectarse configuración incompleta", error);

        System.out.println("\n✓ Test 5 completado correctamente\n");
    }

    // =========================================================================
    // TEST 6 — k ≤ 0
    // =========================================================================
        /**
     * Test que valida que k &le; 0 provoca una excepción.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testKMeansPlusPlusKNoPositivoLanzaExcepcion() {
        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(0, 10);
        kmeanspp.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeanspp.execute(new Object[][] { {1.0}, {2.0} });
    }

    // =========================================================================
    // TEST 7 — k > n
    // =========================================================================
    /**
     * Test que valida que k mayor que el número de puntos no es válido.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testKMeansPlusPlusKMayorQueNumeroPuntosLanzaExcepcion() {
        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(5, 10);
        kmeanspp.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeanspp.execute(new Object[][] { {1.0}, {2.0}, {3.0} });
    }

    // =========================================================================
    // TEST 8 — TODOS LOS PUNTOS IDÉNTICOS
    // =========================================================================
        /**
     * Test con todos los puntos idénticos y k = 1.
     *
     * <p>
     * Verifica que:
     * <ul>
     *   <li>Todos los puntos pertenecen al mismo cluster.</li>
     *   <li>El centro coincide con el valor común.</li>
     *   <li>La silhouette es 0.</li>
     * </ul>
     * </p>
     */
    @Test
    public void testKMeansPlusPlusTodosPuntosIdenticos() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 8: TODOS LOS PUNTOS IDÉNTICOS (k = 1)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Object[][] data = { {5.0}, {5.0}, {5.0}, {5.0} };

        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(1, 20);
        kmeanspp.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeanspp.setNumericRange(0, 0.0, 10.0);

        ResultadoClustering res = kmeanspp.execute(data);

        assertEquals(4, res.getGroups().length);
        for (int g : res.getGroups()) assertEquals(0, g);

        assertEquals(5.0, (double) res.getCenters()[0][0], 1e-6);
        assertEquals(0.0, res.getSilhouette(), 1e-6);

        System.out.println("\n✓ Test 8 completado correctamente\n");
    }

    // =========================================================================
    // TEST 9 — DOS GRUPOS BIEN SEPARADOS = SILHOUETTE ALTA
    // =========================================================================
        /**
     * Test con dos grupos numéricos bien separados.
     *
     * <p>
     * Se espera una silhouette alta, indicando buena separación de clusters.
     * </p>
     */
    @Test
    public void testKMeansPlusPlusDosGruposBienSeparadosSilhouetteAlta() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 9: DOS GRUPOS BIEN SEPARADOS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Object[][] data = {
                {10.0}, {11.0}, {30.0}, {31.0}
        };

        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(2, 50);
        kmeanspp.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeanspp.setNumericRange(0, 0.0, 40.0);

        double silhouette = kmeanspp.execute(data).getSilhouette();

        assertTrue("Silhouette debería ser alta", silhouette > 0.5);
        assertFalse(Double.isNaN(silhouette));

        System.out.println("\n✓ Test 9 completado correctamente\n");
    }

    // =========================================================================
    // TEST 10 — ROBUSTEZ NUMÉRICA
    // =========================================================================
        /**
     * Test de robustez numérica con tipos mixtos.
     *
     * <p>
     * Verifica que el cálculo de la silhouette no produce NaN ni infinito.
     * </p>
     */
    @Test
    public void testKMeansPlusPlusRobustezNumericaMixto() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 10: ROBUSTEZ NUMÉRICA CON TIPOS MIXTOS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Object[][] data = {
                {10.0, "BAJO", "ROJO", "A,B", "hola"},
                {20.0, "MEDIO", "ROJO", "A",   "hola!!"},
                {30.0, "ALTO",  "AZUL", "B",   "adios"}
        };

        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(2, 50);
        TipoPregunta[] tipos = {
                TipoPregunta.NUMERICA,
                TipoPregunta.ORDINAL,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.CATEGORIA_MULTIPLE,
                TipoPregunta.LIBRE
        };
        kmeanspp.setTipoPreguntas(tipos);
        kmeanspp.setNumericRange(0, 0.0, 40.0);
        kmeanspp.setOrdinalOptions(1, Set.of("BAJO", "MEDIO", "ALTO"));

        double silhouette = kmeanspp.execute(data).getSilhouette();

        assertFalse(Double.isNaN(silhouette));
        assertFalse(Double.isInfinite(silhouette));

        System.out.println("\n✓ Test 10 completado correctamente\n");
    }

    // =========================================================================
    // TEST 11 — DATASET TIPO “LOAN” REALISTA
    // =========================================================================
        /**
     * Test de integración con un dataset realista tipo loan.
     *
     * <p>
     * Comprueba coherencia global del clustering en un escenario heterogéneo real.
     * </p>
     */
    @Test
    public void testKMeansPlusPlusLoanLikeDataset() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 11: DATASET TIPO LOAN-*.csv (HARDCODED)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Object[][] data = {
                { "Male", "Yes", 0, "Graduate", "No", 5720.0, 0.0, 110.0, 360.0, 1.0, "Urban" },
                { "Male", "Yes", 1, "Graduate", "No", 4583.0,1508.0,128.0,360.0, 1.0, "Rural" },
                { "Male", "Yes", 2, "Graduate", "No", 6000.0, 0.0,141.0,360.0, 1.0, "Urban" },
                { "Male", "No",  0, "Not Graduate", "Yes",2333.0,1516.0,95.0,360.0,1.0,"Semiurban" },
                { "Female","No", 0,"Graduate","No",3036.0, 0.0,80.0,360.0,1.0,"Urban"},
                { "Male",  "Yes",3,"Graduate","Yes",4000.0,2500.0,120.0,360.0,0.0,"Rural"},
                { "Female","Yes",1,"Graduate","No",6000.0,0.0,150.0,360.0,1.0,"Semiurban"},
                { "Male","No",0,"Graduate","No",2500.0,2000.0,110.0,180.0,0.0,"Urban"},
                { "Male","No",0,"Not Graduate","No",3500.0,1200.0,120.0,180.0,1.0,"Rural"}
        };

        int k = 3;

        KMeansPlusPlus kmeanspp = new KMeansPlusPlus(k, 100);

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
        kmeanspp.setTipoPreguntas(tipos);

        // Normalización numérica
        kmeanspp.setNumericRange(2, 0.0, 3.0);
        kmeanspp.setNumericRange(5, 0.0, 10000.0);
        kmeanspp.setNumericRange(6, 0.0, 5000.0);
        kmeanspp.setNumericRange(7, 0.0, 500.0);
        kmeanspp.setNumericRange(8, 0.0, 480.0);
        kmeanspp.setNumericRange(9, 0.0, 1.0);

        ResultadoClustering res = kmeanspp.execute(data);
        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double silhouette = res.getSilhouette();

        assertEquals(data.length, groups.length);
        assertEquals(k, centers.length);
        for (Object[] center : centers) assertEquals(11, center.length);

        assertTrue(silhouette >= -1 && silhouette <= 1);
        assertFalse(Double.isNaN(silhouette));

        // Al menos dos clusters distintos deben usarse
        assertTrue(new HashSet<>(Arrays.asList(Arrays.stream(groups).boxed().toArray(Integer[]::new))).size() >= 2);

        System.out.println("\n✓ Test 11 completado correctamente\n");
    }

    // =========================================================================
    // TEST 12 — KMEANS++ DEBERÍA SUPERAR A KMEANS EN SILHOUETTE
    // =========================================================================
    /**
     * Comparación directa entre KMeans y KMeans++.
     *
     * Propósito:
     *   - Mostrar que KMeans++ suele producir resultados superiores gracias
     *     a su inicialización ponderada por distancias.
     *
     * No es una garantía matemática estricta, pero ocurre en la práctica.
     */
    @Test
    public void testKMeansPlusPlusMejorQueKMeans() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 12: COMPROBACIÓ KMEANS++ > KMEANS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Object[][] data = {
                {10.0}, {11.0}, {12.0},
                {50.0}, {51.0}, {52.0},
                {200.0}, {201.0}, {202.0}
        };

        KMeans normal = new KMeans(3, 20);
        KMeansPlusPlus kpp = new KMeansPlusPlus(3, 20);

        TipoPregunta[] tipos = { TipoPregunta.NUMERICA };
        normal.setTipoPreguntas(tipos);
        kpp.setTipoPreguntas(tipos);

        normal.setNumericRange(0, 0.0, 300.0);
        kpp.setNumericRange(0, 0.0, 300.0);

        double silhouetteNormal = normal.execute(data).getSilhouette();
        double silhouettePlusPlus = kpp.execute(data).getSilhouette();

        assertTrue(
                "KMeans++ debería producir silhouette ≥ KMeans (diferencia tolerada 1e-6)",
                silhouettePlusPlus >= silhouetteNormal - 1e-6
        );

        System.out.println("\n✓ Test 12 completado correctamente\n");
    }
}
