package test;

import main.domain.classes.*;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Batería de tests unitarios para validar el comportamiento de la clase {@link Encuesta}.
 *
 * <p>Los tests comprueban la correcta inicialización de la encuesta, la gestión de preguntas,
 * la generación de identificadores únicos y el comportamiento ante índices válidos y fuera
 * de rango, así como la búsqueda de preguntas por identificador lógico.</p>
 */
public class EncuestaTest {

    private Encuesta encuesta;

    /**
     * Implementación auxiliar de {@link Pregunta} que permite controlar explícitamente
     * el identificador devuelto por {@link #getId()}.
     *
     * <p>Se utiliza para comprobar que los métodos de {@link Encuesta} trabajan con el
     * identificador lógico de la pregunta y no con la referencia de la instancia.</p>
     */
    private static class PreguntaWithCustomId extends Pregunta {

        private final String customId;

        /**
         * Crea una pregunta con un identificador personalizado.
         *
         * @param id identificador lógico de la pregunta
         * @param enunciado texto del enunciado
         */
        public PreguntaWithCustomId(String id, String enunciado) {
            super(enunciado);
            this.customId = id;
        }

        /**
         * Devuelve el identificador lógico de la pregunta.
         *
         * @return identificador de la pregunta
         */
        @Override
        public String getId() {
            return customId;
        }

        /**
         * Devuelve el tipo de la pregunta.
         *
         * @return tipo de pregunta {@link TipoPregunta#LIBRE}
         */
        @Override
        public TipoPregunta getTipoPregunta() {
            return TipoPregunta.LIBRE;
        }

        /**
         * Valida una respuesta asociada a la pregunta.
         *
         * @param valor valor de la respuesta
         * @return {@code true} siempre, ya que no se aplica validación
         */
        @Override
        public boolean validarRespuesta(Object valor) {
            return true;
        }
    }

    /**
     * Inicializa una instancia base de {@link Encuesta} que se reutiliza
     * en la mayoría de los tests.
     */
    @Before
    public void setUp() {
        encuesta = new Encuesta("Titulo inicial", "Descripcion inicial");
    }

    /**
     * Comprueba que el constructor de {@link Encuesta} inicializa correctamente
     * todos sus campos básicos.
     */
    @Test
    public void constructor_deberiaInicializarCamposCorrectamente() {
        final String id = encuesta.getId();
        final String titulo = encuesta.getTitulo();
        final String descripcion = encuesta.getDescripcion();
        final List<Pregunta> preguntas = encuesta.getPreguntas();
        final Integer numPreguntas = encuesta.getNumPreguntas();

        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertEquals("Titulo inicial", titulo);
        assertEquals("Descripcion inicial", descripcion);
        assertNotNull(preguntas);
        assertTrue(preguntas.isEmpty());
        assertEquals(Integer.valueOf(0), numPreguntas);
    }

    /**
     * Verifica que dos instancias distintas de {@link Encuesta}
     * generan identificadores diferentes.
     */
    @Test
    public void constructor_deberiaGenerarIdsDistintosParaEncuestasDistintas() {
        final Encuesta encuesta2 = new Encuesta("Otra", "Otra desc");

        final String id1 = encuesta.getId();
        final String id2 = encuesta2.getId();

        assertNotEquals(id1, id2);
    }

    /**
     * Comprueba que {@link Encuesta#setTitulo(String)} actualiza correctamente
     * el título de la encuesta.
     */
    @Test
    public void setTitulo_deberiaActualizarTitulo() {
        final String nuevoTitulo = "Nuevo titulo";

        encuesta.setTitulo(nuevoTitulo);

        assertEquals(nuevoTitulo, encuesta.getTitulo());
    }

    /**
     * Comprueba que {@link Encuesta#setDescripcion(String)} actualiza correctamente
     * la descripción de la encuesta.
     */
    @Test
    public void setDescripcion_deberiaActualizarDescripcion() {
        final String nuevaDescripcion = "Nueva descripcion";

        encuesta.setDescripcion(nuevaDescripcion);

        assertEquals(nuevaDescripcion, encuesta.getDescripcion());
    }

    /**
     * Verifica que {@link Encuesta#agregarPregunta(Pregunta)} añade una pregunta
     * a la encuesta y actualiza el número total de preguntas.
     */
    @Test
    public void agregarPregunta_deberiaAnadirPreguntaALaLista() {
        final Pregunta p1 = new Libre("Pregunta 1");

        encuesta.agregarPregunta(p1);

        assertEquals(Integer.valueOf(1), encuesta.getNumPreguntas());
        assertSame(p1, encuesta.getPregunta(0));
    }

    /**
     * Comprueba que {@link Encuesta#getPregunta(int)} devuelve la pregunta
     * correspondiente al índice indicado.
     */
    @Test
    public void getPregunta_deberiaDevolverPreguntaCorrectaPorIndice() {
        final Pregunta p1 = new Libre("Pregunta 1");
        final Pregunta p2 = new Libre("Pregunta 2");

        encuesta.agregarPregunta(p1);
        encuesta.agregarPregunta(p2);

        final Pregunta resultado1 = encuesta.getPregunta(0);
        final Pregunta resultado2 = encuesta.getPregunta(1);

        assertSame(p1, resultado1);
        assertSame(p2, resultado2);
    }

    /**
     * Verifica que {@link Encuesta#getPregunta(int)} lanza una excepción
     * cuando el índice está fuera de rango.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void getPregunta_deberiaLanzarExcepcionSiIndiceFueraDeRango() {
        final Pregunta p1 = new Libre("Pregunta 1");
        encuesta.agregarPregunta(p1);

        encuesta.getPregunta(5);
    }

    /**
     * Comprueba que {@link Encuesta#modificarPregunta(Pregunta, int)} reemplaza
     * correctamente la pregunta en un índice válido.
     */
    @Test
    public void modificarPregunta_deberiaReemplazarPreguntaEnIndiceValido() {
        final Pregunta p1 = new Libre("Pregunta 1");
        final Pregunta p2 = new Libre("Pregunta 2");
        encuesta.agregarPregunta(p1);

        encuesta.modificarPregunta(p2, 0);

        assertEquals(Integer.valueOf(1), encuesta.getNumPreguntas());
        assertSame(p2, encuesta.getPregunta(0));
    }

    /**
     * Verifica que {@link Encuesta#modificarPregunta(Pregunta, int)} no modifica
     * la encuesta cuando el índice es negativo.
     */
    @Test
    public void modificarPregunta_noDeberiaHacerNadaSiIndiceEsNegativo() {
        final Pregunta p1 = new Libre("Pregunta 1");
        final Pregunta p2 = new Libre("Pregunta 2");
        encuesta.agregarPregunta(p1);
        final int numAntes = encuesta.getNumPreguntas();

        encuesta.modificarPregunta(p2, -1);

        assertEquals(Integer.valueOf(numAntes), encuesta.getNumPreguntas());
        assertSame(p1, encuesta.getPregunta(0));
    }

    /**
     * Comprueba que {@link Encuesta#modificarPregunta(Pregunta, int)} no modifica
     * la encuesta cuando el índice es igual al tamaño de la lista.
     */
    @Test
    public void modificarPregunta_noDeberiaHacerNadaSiIndiceEsIgualAlTamanyo() {
        final Pregunta p1 = new Libre("Pregunta 1");
        final Pregunta p2 = new Libre("Pregunta 2");
        encuesta.agregarPregunta(p1);
        final int numAntes = encuesta.getNumPreguntas();

        encuesta.modificarPregunta(p2, numAntes);

        assertEquals(Integer.valueOf(numAntes), encuesta.getNumPreguntas());
        assertSame(p1, encuesta.getPregunta(0));
    }

    /**
     * Verifica que {@link Encuesta#eliminarPregunta(int)} elimina correctamente
     * una pregunta en un índice válido.
     */
    @Test
    public void eliminarPregunta_deberiaEliminarPreguntaEnIndiceValido() {
        final Pregunta p1 = new Libre("Pregunta 1");
        final Pregunta p2 = new Libre("Pregunta 2");
        encuesta.agregarPregunta(p1);
        encuesta.agregarPregunta(p2);

        encuesta.eliminarPregunta(0);

        assertEquals(Integer.valueOf(1), encuesta.getNumPreguntas());
        assertSame(p2, encuesta.getPregunta(0));
    }

    /**
     * Comprueba que {@link Encuesta#eliminarPregunta(int)} no realiza
     * ninguna acción si el índice es negativo.
     */
    @Test
    public void eliminarPregunta_noDeberiaHacerNadaSiIndiceEsNegativo() {
        final Pregunta p1 = new Libre("Pregunta 1");
        encuesta.agregarPregunta(p1);
        final int numAntes = encuesta.getNumPreguntas();

        encuesta.eliminarPregunta(-1);

        assertEquals(Integer.valueOf(numAntes), encuesta.getNumPreguntas());
    }

    /**
     * Verifica que {@link Encuesta#eliminarPregunta(int)} no modifica
     * la encuesta cuando el índice está fuera de rango.
     */
    @Test
    public void eliminarPregunta_noDeberiaHacerNadaSiIndiceEsMayorOTamanyo() {
        final Pregunta p1 = new Libre("Pregunta 1");
        encuesta.agregarPregunta(p1);
        final int numAntes = encuesta.getNumPreguntas();

        encuesta.eliminarPregunta(numAntes);

        assertEquals(Integer.valueOf(numAntes), encuesta.getNumPreguntas());
    }

    /**
     * Comprueba que {@link Encuesta#eliminarTodasPreguntas()} vacía completamente
     * la encuesta.
     */
    @Test
    public void eliminarTodasPreguntas_deberiaVaciarLista() {
        final Pregunta p1 = new Libre("Pregunta 1");
        final Pregunta p2 = new Libre("Pregunta 2");
        encuesta.agregarPregunta(p1);
        encuesta.agregarPregunta(p2);

        encuesta.eliminarTodasPreguntas();

        assertEquals(Integer.valueOf(0), encuesta.getNumPreguntas());
        assertTrue(encuesta.getPreguntas().isEmpty());
    }

    /**
     * Verifica que {@link Encuesta#getIndicePregunta(Pregunta)} devuelve
     * el índice correcto cuando la pregunta está en la encuesta.
     */
    @Test
    public void getIndicePregunta_deberiaDevolverIndiceSiPreguntaEstaEnLista() {
        final Pregunta p1 = new Libre("Pregunta 1");
        final Pregunta p2 = new Libre("Pregunta 2");

        encuesta.agregarPregunta(p1);
        encuesta.agregarPregunta(p2);

        assertEquals(0, encuesta.getIndicePregunta(p1));
        assertEquals(1, encuesta.getIndicePregunta(p2));
    }

    /**
     * Comprueba que {@link Encuesta#getIndicePregunta(Pregunta)} utiliza
     * el identificador lógico de la pregunta y no la referencia del objeto.
     */
    @Test
    public void getIndicePregunta_deberiaUsarIdDePreguntaNoReferencia() {
        final String sharedId = "id-compartido";
        final Pregunta almacenada = new PreguntaWithCustomId(sharedId, "Pregunta almacenada");
        final Pregunta buscada = new PreguntaWithCustomId(sharedId, "Pregunta buscada");

        encuesta.agregarPregunta(almacenada);

        final int indice = encuesta.getIndicePregunta(buscada);

        assertEquals(0, indice);
    }

    /**
     * Verifica que {@link Encuesta#getIndicePregunta(Pregunta)} devuelve {@code -1}
     * cuando la pregunta no está en la encuesta.
     */
    @Test
    public void getIndicePregunta_deberiaDevolverMenosUnoSiPreguntaNoEstaEnLista() {
        final Pregunta p1 = new Libre("Pregunta 1");
        final Pregunta p2 = new Libre("Pregunta 2");

        encuesta.agregarPregunta(p1);

        assertEquals(-1, encuesta.getIndicePregunta(p2));
    }

    /**
     * Comprueba que {@link Encuesta#getIndicePregunta(Pregunta)} devuelve {@code -1}
     * cuando la pregunta pasada es {@code null}.
     */
    @Test
    public void getIndicePregunta_deberiaDevolverMenosUnoSiPreguntaEsNull() {
        assertEquals(-1, encuesta.getIndicePregunta(null));
    }

    /**
     * Verifica que {@link Encuesta#getIndicePregunta(Pregunta)} devuelve {@code -1}
     * cuando la encuesta no contiene ninguna pregunta.
     */
    @Test
    public void getIndicePregunta_deberiaDevolverMenosUnoSiListaEstaVacia() {
        final Pregunta p = new Libre("Pregunta cualquiera");

        assertEquals(-1, encuesta.getIndicePregunta(p));
    }

    /**
     * Comprueba que {@link Encuesta#agregarPregunta(Pregunta)} permite
     * añadir distintos tipos de {@link Pregunta} en la misma encuesta.
     */
    @Test
    public void agregarPregunta_deberiaPermitirVariosTiposDePregunta() {
        final Pregunta p1 = new Libre("Pregunta libre");
        final Pregunta p2 = new Numerica("Edad", 0.0, 120.0);

        encuesta.agregarPregunta(p1);
        encuesta.agregarPregunta(p2);

        assertEquals(Integer.valueOf(2), encuesta.getNumPreguntas());
        assertSame(p1, encuesta.getPregunta(0));
        assertSame(p2, encuesta.getPregunta(1));
    }
}
