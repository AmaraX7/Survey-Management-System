package test;

import main.domain.classes.*;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase KMeans.
 *
 * Se prueban:
 *  - Casos básicos numéricos y mixtos.
 *  - Casos extremos (un solo elemento, dataset vacío, todos los puntos idénticos).
 *  - Robustez de la silhouette y coherencia de grupos/centros.
 *  - Validación de precondiciones (k > 0, k ≤ n, tipos de pregunta configurados).
 */
public class TestKMeans {

    @Test
    public void testKMeansSoloNumericaK2() {
        Object[][] data = { {10.0}, {11.0}, {30.0}, {31.0} };
        int k = 2;
        KMeans kmeans = new KMeans(k, 50);
        kmeans.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeans.setNumericRange(0, 0.0, 40.0);

        ResultadoClustering res = kmeans.execute(data);

        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double silhouette = res.getSilhouette();

        assertEquals(data.length, groups.length);
        assertEquals(k, centers.length);
        assertEquals(1, centers[0].length);
        assertTrue(silhouette >= -1.0 && silhouette <= 1.0);
    }

    @Test
    public void testKMeansUnSoloElemento() {
        Object[][] data = { { "ROJO" } };
        KMeans kmeans = new KMeans(1, 10);
        kmeans.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.CATEGORIA_SIMPLE });

        ResultadoClustering res = kmeans.execute(data);

        assertEquals(1, res.getGroups().length);
        assertEquals(0, res.getGroups()[0]);
        assertEquals(1, res.getCenters().length);
        assertEquals(1, res.getCenters()[0].length);
        assertEquals(0.0, res.getSilhouette(), 1e-9);
    }

    @Test
    public void testKMeansMixtoTipos() {
        Object[][] data = {
                { 10.0, "BAJO",  "ROJO", "A,B", "hola mundo" },
                { 20.0, "MEDIO", "ROJO", "A",   "hola mundo!" },
                { 30.0, "ALTO",  "AZUL", "B",   "adios" }
        };
        int k = 2;
        KMeans kmeans = new KMeans(k, 50);
        TipoPregunta[] tipos = {
                TipoPregunta.NUMERICA,
                TipoPregunta.ORDINAL,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.CATEGORIA_MULTIPLE,
                TipoPregunta.LIBRE
        };
        kmeans.setTipoPreguntas(tipos);
        kmeans.setNumericRange(0, 0.0, 40.0);

        Set<String> orden = new HashSet<>(Arrays.asList("BAJO","MEDIO","ALTO"));
        kmeans.setOrdinalOptions(1, orden);

        ResultadoClustering res = kmeans.execute(data);

        assertEquals(data.length, res.getGroups().length);
        assertEquals(k, res.getCenters().length);
        assertEquals(5, res.getCenters()[0].length);
        for (int g : res.getGroups()) {
            assertTrue(g >= 0 && g < k);
        }
        double s = res.getSilhouette();
        assertTrue(s >= -1.0 && s <= 1.0);
    }

    @Test
    public void testKMeansDatasetVacio() {
        Object[][] dataVacio = new Object[0][0];
        KMeans kmeans = new KMeans(2, 10);
        kmeans.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });

        boolean exceptionThrown = false;
        try {
            kmeans.execute(dataVacio);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testKMeansSinTipoPreguntas() {
        Object[][] data = { {1.0}, {2.0} };
        KMeans kmeans = new KMeans(1, 10);

        boolean exceptionThrown = false;
        try {
            kmeans.execute(data);
        } catch (IllegalStateException | NullPointerException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKMeansKNoPositivoLanzaExcepcion() {
        Object[][] data = { {1.0}, {2.0} };
        KMeans kmeans = new KMeans(0, 10);
        kmeans.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeans.execute(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKMeansKMayorQueNumeroPuntosLanzaExcepcion() {
        Object[][] data = { {1.0}, {2.0}, {3.0} };
        KMeans kmeans = new KMeans(5, 10);
        kmeans.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeans.execute(data);
    }

    @Test
    public void testKMeansTodosPuntosIdenticos() {
        Object[][] data = { {5.0}, {5.0}, {5.0}, {5.0} };
        KMeans kmeans = new KMeans(1, 20);
        kmeans.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeans.setNumericRange(0, 0.0, 10.0);

        ResultadoClustering res = kmeans.execute(data);
        for (int g : res.getGroups()) assertEquals(0, g);
        assertEquals(1, res.getCenters().length);
        assertEquals(1, res.getCenters()[0].length);
        assertEquals(5.0, (double)res.getCenters()[0][0], 1e-6);
        assertEquals(0.0, res.getSilhouette(), 1e-6);
    }

    @Test
    public void testKMeansDosGruposBienSeparadosSilhouetteAlta() {
        Object[][] data = { {10.0}, {11.0}, {30.0}, {31.0} };
        KMeans kmeans = new KMeans(2, 50);
        kmeans.setTipoPreguntas(new TipoPregunta[]{ TipoPregunta.NUMERICA });
        kmeans.setNumericRange(0, 0.0, 40.0);

        ResultadoClustering res = kmeans.execute(data);
        double s = res.getSilhouette();
        assertTrue(s > 0.5);
        assertFalse(Double.isNaN(s));
    }

    @Test
    public void testKMeansRobustezNumericaMixto() {
        Object[][] data = {
                { 10.0, "BAJO",  "ROJO", "A,B", "hola" },
                { 20.0, "MEDIO", "ROJO", "A",   "hola!!" },
                { 30.0, "ALTO",  "AZUL", "B",   "adios" }
        };
        KMeans kmeans = new KMeans(2, 50);
        TipoPregunta[] tipos = {
                TipoPregunta.NUMERICA,
                TipoPregunta.ORDINAL,
                TipoPregunta.CATEGORIA_SIMPLE,
                TipoPregunta.CATEGORIA_MULTIPLE,
                TipoPregunta.LIBRE
        };
        kmeans.setTipoPreguntas(tipos);
        kmeans.setNumericRange(0, 0.0, 40.0);
        Set<String> orden = new HashSet<>(Arrays.asList("BAJO","MEDIO","ALTO"));
        kmeans.setOrdinalOptions(1, orden);

        ResultadoClustering res = kmeans.execute(data);
        double s = res.getSilhouette();
        assertFalse(Double.isNaN(s));
        assertFalse(Double.isInfinite(s));
    }

    @Test
    public void testKMeansLoanLikeDataset() {
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
        int k = 3;
        KMeans km = new KMeans(k, 100);
        TipoPregunta[] tipos = {
                TipoPregunta.CATEGORIA_SIMPLE, TipoPregunta.CATEGORIA_SIMPLE, TipoPregunta.NUMERICA,
                TipoPregunta.CATEGORIA_SIMPLE, TipoPregunta.CATEGORIA_SIMPLE, TipoPregunta.NUMERICA,
                TipoPregunta.NUMERICA, TipoPregunta.NUMERICA, TipoPregunta.NUMERICA, TipoPregunta.NUMERICA,
                TipoPregunta.CATEGORIA_SIMPLE
        };
        km.setTipoPreguntas(tipos);

        km.setNumericRange(2, 0.0, 3.0);
        km.setNumericRange(5, 0.0, 10000.0);
        km.setNumericRange(6, 0.0, 5000.0);
        km.setNumericRange(7, 0.0, 500.0);
        km.setNumericRange(8, 0.0, 480.0);
        km.setNumericRange(9, 0.0, 1.0);

        ResultadoClustering res = km.execute(data);

        int[] groups = res.getGroups();
        Object[][] centers = res.getCenters();
        double s = res.getSilhouette();

        assertEquals(data.length, groups.length);
        assertEquals(k, centers.length);
        for (Object[] c : centers) assertEquals(11, c.length);
        for (int g : groups) assertTrue(g >= 0 && g < k);

        assertFalse(Double.isNaN(s));
        assertFalse(Double.isInfinite(s));

        Set<Integer> usados = new HashSet<>();
        for (int g : groups) usados.add(g);
        assertTrue(usados.size() >= 2);
    }
}
