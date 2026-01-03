package test;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import main.domain.classes.*;
import java.util.*;

/**
 * Batería de tests unitarios para validar el comportamiento de la clase {@link Clustering}.
 *
 * <p>Estos tests comprueban la correcta integración de distintos algoritmos de clustering,
 * el tratamiento de diferentes tipos de preguntas y respuestas, la reproducibilidad mediante
 * semillas y la robustez ante datos incompletos.</p>
 */
public class ClusteringTest {

    private Clustering clustering;
    private Encuesta encuesta;
    private List<UsuarioRespondedor> usuarios;
    private IndicePregunta indicePregunta;

    /**
     * Inicializa el entorno común necesario para la ejecución de los tests.
     *
     * <p>Se crea una instancia de {@link Clustering} y un {@link IndicePregunta} de prueba
     * que traduce identificadores de preguntas con formato {@code "pX"} a su índice numérico.</p>
     */
    @Before
    public void setUp() {
        clustering = new Clustering();

        indicePregunta = new IndicePregunta() {
            @Override
            public int obtenerIndice(String idEncuesta, String idPregunta) {
                if (idPregunta.startsWith("p")) {
                    try {
                        return Integer.parseInt(idPregunta.substring(1));
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                }
                return -1;
            }
        };
    }

    /**
     * Verifica que el algoritmo {@link KMeans} genera un {@link ResultadoClustering} válido
     * al ejecutarse sobre una encuesta con una única pregunta numérica.
     */
    @Test
    public void ejecutarClusteringConKMeans_deberiaRetornarResultadoValido() {
        encuesta = crearEncuestaConUnaPreguntaNumerica();
        usuarios = crearUsuariosConRespuestasNumericas();

        KMeans kmeans = new KMeans(2, 20);

        ResultadoClustering resultado = clustering.ejecutarClustering(
                kmeans, usuarios, encuesta, indicePregunta
        );

        assertNotNull(resultado);
        assertNotNull(resultado.getIdsUsuarios());
        assertEquals(4, resultado.getIdsUsuarios().size());
        assertEquals(encuesta.getId(), resultado.getIdEncuesta());
        assertTrue(resultado.getSilhouette() >= -1.0 && resultado.getSilhouette() <= 1.0);
        assertEquals(2, resultado.getK());
    }

    /**
     * Comprueba que {@link KMeansPlusPlus} se integra correctamente en el flujo de clustering
     * y produce un resultado válido para un conjunto de datos numéricos.
     */
    @Test
    public void ejecutarClusteringConKMeansPlusPlus_deberiaRetornarResultadoValido() {
        encuesta = crearEncuestaConUnaPreguntaNumerica();
        usuarios = crearUsuariosConRespuestasNumericas();

        KMeansPlusPlus kpp = new KMeansPlusPlus(2, 20);

        ResultadoClustering resultado = clustering.ejecutarClustering(
                kpp, usuarios, encuesta, indicePregunta
        );

        assertNotNull(resultado);
        assertEquals(usuarios.size(), resultado.getIdsUsuarios().size());
        assertTrue(resultado.getSilhouette() >= -1.0 && resultado.getSilhouette() <= 1.0);
    }

    /**
     * Valida el funcionamiento del algoritmo {@link KMedoids} sobre datos numéricos
     * y la correcta propagación del valor de {@code k} al resultado.
     */
    @Test
    public void ejecutarClusteringConKMedoids_deberiaRetornarResultadoValido() {
        encuesta = crearEncuestaConUnaPreguntaNumerica();
        usuarios = crearUsuariosConRespuestasNumericas();

        KMedoids kmedoids = new KMedoids(2, 20);

        ResultadoClustering resultado = clustering.ejecutarClustering(
                kmedoids, usuarios, encuesta, indicePregunta
        );

        assertNotNull(resultado);
        assertEquals(2, resultado.getK());
        assertNotNull(resultado.getIdsUsuarios());
    }

    /**
     * Comprueba que el sistema de clustering soporta encuestas con preguntas ordinales
     * y respuestas simbólicas ordenadas.
     */
    @Test
    public void ejecutarClusteringConPreguntasOrdinales_deberiaFuncionar() {
        encuesta = crearEncuestaConPreguntaOrdinal();
        usuarios = crearUsuariosConRespuestasOrdinales();

        KMeans kmeans = new KMeans(2, 10);

        ResultadoClustering resultado = clustering.ejecutarClustering(
                kmeans, usuarios, encuesta, indicePregunta
        );

        assertNotNull(resultado);
        assertEquals(4, resultado.getIdsUsuarios().size());
        assertTrue(resultado.getSilhouette() >= -1.0);
    }

    /**
     * Verifica el funcionamiento del clustering con preguntas categóricas simples
     * y respuestas no ordenadas.
     */
    @Test
    public void ejecutarClusteringConPreguntasCategoricas_deberiaFuncionar() {
        encuesta = crearEncuestaConPreguntaCategorica();
        usuarios = crearUsuariosConRespuestasCategoricas();

        KMedoids kmedoids = new KMedoids(2, 10);

        ResultadoClustering resultado = clustering.ejecutarClustering(
                kmedoids, usuarios, encuesta, indicePregunta
        );

        assertNotNull(resultado);
        assertEquals(usuarios.size(), resultado.getIdsUsuarios().size());
    }

    /**
     * Comprueba que la configuración de una semilla fija produce resultados reproducibles
     * al ejecutar el clustering con los mismos datos y parámetros.
     */
    @Test
    public void configurarSemilla_conKMeans_deberiaProducirResultadosConsistentes() {
        encuesta = crearEncuestaConUnaPreguntaNumerica();
        usuarios = crearUsuariosConRespuestasNumericas();

        KMeans kmeans1 = new KMeans(2, 20);
        KMeans kmeans2 = new KMeans(2, 20);

        clustering.configurarSemilla(kmeans1, 12345L);
        clustering.configurarSemilla(kmeans2, 12345L);

        ResultadoClustering r1 = clustering.ejecutarClustering(
                kmeans1, usuarios, encuesta, indicePregunta
        );

        ResultadoClustering r2 = clustering.ejecutarClustering(
                kmeans2, usuarios, encuesta, indicePregunta
        );

        assertEquals(r1.getSilhouette(), r2.getSilhouette(), 0.001);
    }

    /**
     * Verifica que el clustering puede ejecutarse correctamente cuando existen
     * respuestas nulas o ausentes en algunos usuarios.
     */
    @Test
    public void ejecutarClusteringConRespuestasNulas_deberiaImputarValores() {
        encuesta = crearEncuestaConUnaPreguntaNumerica();
        usuarios = crearUsuariosConAlgunasRespuestasNulas();

        KMeans kmeans = new KMeans(2, 20);

        ResultadoClustering resultado = clustering.ejecutarClustering(
                kmeans, usuarios, encuesta, indicePregunta
        );

        assertNotNull(resultado);
        assertEquals(usuarios.size(), resultado.getIdsUsuarios().size());
        assertTrue(resultado.getSilhouette() >= -1.0 && resultado.getSilhouette() <= 1.0);
    }

    /**
     * Comprueba que el clustering soporta encuestas con múltiples preguntas
     * de distintos tipos de forma simultánea.
     */
    @Test
    public void ejecutarClusteringConMultiplesPreguntas_deberiaFuncionar() {
        encuesta = crearEncuestaConMultiplesPreguntas();
        usuarios = crearUsuariosConMultiplesRespuestas();

        KMeansPlusPlus kpp = new KMeansPlusPlus(2, 20);

        ResultadoClustering resultado = clustering.ejecutarClustering(
                kpp, usuarios, encuesta, indicePregunta
        );

        assertNotNull(resultado);
        assertEquals(usuarios.size(), resultado.getIdsUsuarios().size());
        assertEquals(encuesta.getId(), resultado.getIdEncuesta());
    }

    /**
     * Crea una encuesta con una única pregunta numérica.
     *
     * @return encuesta de prueba con una pregunta numérica
     */
    private Encuesta crearEncuestaConUnaPreguntaNumerica() {
        Encuesta enc = new Encuesta("Encuesta Test", "Test");
        Pregunta p = new Numerica("Edad", 0.0, 100.0);
        p.setId("p0");
        enc.agregarPregunta(p);
        return enc;
    }

    /**
     * Crea una encuesta con una pregunta ordinal.
     *
     * @return encuesta con una pregunta ordinal
     */
    private Encuesta crearEncuestaConPreguntaOrdinal() {
        Encuesta enc = new Encuesta("Encuesta Ordinal", "Test");
        Set<String> opciones = new LinkedHashSet<>(Arrays.asList("BAJO", "MEDIO", "ALTO"));
        Pregunta p = new Ordinal("Nivel", opciones);
        p.setId("p0");
        enc.agregarPregunta(p);
        return enc;
    }

    /**
     * Crea una encuesta con una pregunta categórica simple.
     *
     * @return encuesta con una pregunta categórica
     */
    private Encuesta crearEncuestaConPreguntaCategorica() {
        Encuesta enc = new Encuesta("Encuesta Categórica", "Test");
        Set<String> opciones = new LinkedHashSet<>(Arrays.asList("A", "B", "C"));
        Pregunta p = new CategoriaSimple("Opción", opciones);
        p.setId("p0");
        enc.agregarPregunta(p);
        return enc;
    }

    /**
     * Crea una encuesta con múltiples preguntas de distinto tipo.
     *
     * @return encuesta con varias preguntas
     */
    private Encuesta crearEncuestaConMultiplesPreguntas() {
        Encuesta enc = new Encuesta("Encuesta Múltiple", "Test");

        Pregunta p1 = new Numerica("Edad", 0.0, 100.0);
        p1.setId("p0");
        enc.agregarPregunta(p1);

        Set<String> opciones = new LinkedHashSet<>(Arrays.asList("BAJO", "MEDIO", "ALTO"));
        Pregunta p2 = new Ordinal("Nivel", opciones);
        p2.setId("p1");
        enc.agregarPregunta(p2);

        return enc;
    }

    /**
     * Genera una lista de usuarios con respuestas numéricas.
     *
     * @return lista de usuarios con respuestas numéricas
     */
    private List<UsuarioRespondedor> crearUsuariosConRespuestasNumericas() {
        List<UsuarioRespondedor> lista = new ArrayList<>();

        UsuarioRespondedor u1 = new UsuarioRespondedor("u1", "Usuario1");
        u1.addRespuesta(encuesta.getId(), new Respuesta("u1", "p0", encuesta.getId(), 10.0));
        lista.add(u1);

        UsuarioRespondedor u2 = new UsuarioRespondedor("u2", "Usuario2");
        u2.addRespuesta(encuesta.getId(), new Respuesta("u2", "p0", encuesta.getId(), 15.0));
        lista.add(u2);

        UsuarioRespondedor u3 = new UsuarioRespondedor("u3", "Usuario3");
        u3.addRespuesta(encuesta.getId(), new Respuesta("u3", "p0", encuesta.getId(), 85.0));
        lista.add(u3);

        UsuarioRespondedor u4 = new UsuarioRespondedor("u4", "Usuario4");
        u4.addRespuesta(encuesta.getId(), new Respuesta("u4", "p0", encuesta.getId(), 90.0));
        lista.add(u4);

        return lista;
    }

    /**
     * Genera usuarios con respuestas ordinales.
     *
     * @return lista de usuarios con respuestas ordinales
     */
    private List<UsuarioRespondedor> crearUsuariosConRespuestasOrdinales() {
        List<UsuarioRespondedor> lista = new ArrayList<>();

        UsuarioRespondedor u1 = new UsuarioRespondedor("u1", "Usuario1");
        u1.addRespuesta(encuesta.getId(), new Respuesta("u1", "p0", encuesta.getId(), "BAJO"));
        lista.add(u1);

        UsuarioRespondedor u2 = new UsuarioRespondedor("u2", "Usuario2");
        u2.addRespuesta(encuesta.getId(), new Respuesta("u2", "p0", encuesta.getId(), "BAJO"));
        lista.add(u2);

        UsuarioRespondedor u3 = new UsuarioRespondedor("u3", "Usuario3");
        u3.addRespuesta(encuesta.getId(), new Respuesta("u3", "p0", encuesta.getId(), "ALTO"));
        lista.add(u3);

        UsuarioRespondedor u4 = new UsuarioRespondedor("u4", "Usuario4");
        u4.addRespuesta(encuesta.getId(), new Respuesta("u4", "p0", encuesta.getId(), "ALTO"));
        lista.add(u4);

        return lista;
    }

    /**
     * Genera usuarios con respuestas categóricas simples.
     *
     * @return lista de usuarios con respuestas categóricas
     */
    private List<UsuarioRespondedor> crearUsuariosConRespuestasCategoricas() {
        List<UsuarioRespondedor> lista = new ArrayList<>();

        UsuarioRespondedor u1 = new UsuarioRespondedor("u1", "Usuario1");
        u1.addRespuesta(encuesta.getId(), new Respuesta("u1", "p0", encuesta.getId(), "A"));
        lista.add(u1);

        UsuarioRespondedor u2 = new UsuarioRespondedor("u2", "Usuario2");
        u2.addRespuesta(encuesta.getId(), new Respuesta("u2", "p0", encuesta.getId(), "A"));
        lista.add(u2);

        UsuarioRespondedor u3 = new UsuarioRespondedor("u3", "Usuario3");
        u3.addRespuesta(encuesta.getId(), new Respuesta("u3", "p0", encuesta.getId(), "B"));
        lista.add(u3);

        UsuarioRespondedor u4 = new UsuarioRespondedor("u4", "Usuario4");
        u4.addRespuesta(encuesta.getId(), new Respuesta("u4", "p0", encuesta.getId(), "B"));
        lista.add(u4);

        return lista;
    }

    /**
     * Genera usuarios con algunas respuestas ausentes.
     *
     * @return lista de usuarios con respuestas nulas
     */
    private List<UsuarioRespondedor> crearUsuariosConAlgunasRespuestasNulas() {
        List<UsuarioRespondedor> lista = new ArrayList<>();

        UsuarioRespondedor u1 = new UsuarioRespondedor("u1", "Usuario1");
        u1.addRespuesta(encuesta.getId(), new Respuesta("u1", "p0", encuesta.getId(), 20.0));
        lista.add(u1);

        UsuarioRespondedor u2 = new UsuarioRespondedor("u2", "Usuario2");
        u2.addRespuesta(encuesta.getId(), new Respuesta("u2", "p0", encuesta.getId(), 25.0));
        lista.add(u2);

        UsuarioRespondedor u3 = new UsuarioRespondedor("u3", "Usuario3");
        lista.add(u3);

        UsuarioRespondedor u4 = new UsuarioRespondedor("u4", "Usuario4");
        u4.addRespuesta(encuesta.getId(), new Respuesta("u4", "p0", encuesta.getId(), 80.0));
        lista.add(u4);

        return lista;
    }

    /**
     * Genera usuarios con respuestas para múltiples preguntas.
     *
     * @return lista de usuarios con múltiples respuestas
     */
    private List<UsuarioRespondedor> crearUsuariosConMultiplesRespuestas() {
        List<UsuarioRespondedor> lista = new ArrayList<>();

        UsuarioRespondedor u1 = new UsuarioRespondedor("u1", "Usuario1");
        u1.addRespuesta(encuesta.getId(), new Respuesta("u1", "p0", encuesta.getId(), 25.0));
        u1.addRespuesta(encuesta.getId(), new Respuesta("u1", "p1", encuesta.getId(), "BAJO"));
        lista.add(u1);

        UsuarioRespondedor u2 = new UsuarioRespondedor("u2", "Usuario2");
        u2.addRespuesta(encuesta.getId(), new Respuesta("u2", "p0", encuesta.getId(), 30.0));
        u2.addRespuesta(encuesta.getId(), new Respuesta("u2", "p1", encuesta.getId(), "MEDIO"));
        lista.add(u2);

        UsuarioRespondedor u3 = new UsuarioRespondedor("u3", "Usuario3");
        u3.addRespuesta(encuesta.getId(), new Respuesta("u3", "p0", encuesta.getId(), 75.0));
        u3.addRespuesta(encuesta.getId(), new Respuesta("u3", "p1", encuesta.getId(), "ALTO"));
        lista.add(u3);

        UsuarioRespondedor u4 = new UsuarioRespondedor("u4", "Usuario4");
        u4.addRespuesta(encuesta.getId(), new Respuesta("u4", "p0", encuesta.getId(), 80.0));
        u4.addRespuesta(encuesta.getId(), new Respuesta("u4", "p1", encuesta.getId(), "ALTO"));
        lista.add(u4);

        return lista;
    }
}
