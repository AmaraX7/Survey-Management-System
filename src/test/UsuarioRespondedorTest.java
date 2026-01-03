package test;

import main.domain.classes.*;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase {@link main.domain.classes.UsuarioRespondedor}.
 * <p>
 * {@code UsuarioRespondedor} representa a un usuario que:
 * <ul>
 *     <li>Puede responder a múltiples encuestas.</li>
 *     <li>Almacena las respuestas agrupadas por idEncuesta.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Se valida principalmente:
 * <ul>
 *     <li>Inicialización correcta de los campos básicos (id, nombre, password) con y sin password.</li>
 *     <li>Estado inicial de las estructuras internas de respuestas (listas vacías, contador a 0).</li>
 *     <li>Correcto funcionamiento de los setters de id, nombre y password.</li>
 *     <li>Lógica de almacenamiento de respuestas:
 *         <ul>
 *             <li>Creación automática de la entrada para una encuesta al añadir la primera respuesta.</li>
 *             <li>Soporte para múltiples respuestas dentro de la misma encuesta.</li>
 *             <li>Separación correcta de respuestas por encuesta (mapa clave = idEncuesta).</li>
 *             <li>Comportamiento de {@link UsuarioRespondedor#haRespondidoEncuesta(String)} y del contador de encuestas respondidas.</li>
 *             <li>Encapsulamiento: {@link UsuarioRespondedor#getRespuestasEncuesta(String)} devuelve copia independiente.</li>
 *         </ul>
 *     </li>
 *     <li>Eliminación de respuestas por encuesta y su impacto en {@link UsuarioRespondedor#haRespondidoEncuesta(String)}
 *         y {@link UsuarioRespondedor#getNumeroEncuestasRespondidas()}.</li>
 * </ul>
 * </p>
 */
public class UsuarioRespondedorTest {

    /** Testea que el constructor sin password inicializa correctamente id y nombre y deja password en null. */
    @Test
    public void constructorSinPassword_deberiaInicializarCamposCorrectamente() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("id1", "usuario");

        assertEquals("El id no es correcto", "id1", usuario.getId());
        assertEquals("El nombre no es correcto", "usuario", usuario.getNombre());
        assertNull("El password deberia ser null al usar el constructor sin password", usuario.getPassword());

        final List<Respuesta> respuestas = usuario.getRespuestasEncuesta("encuesta1");
        assertNotNull("La lista de respuestas deberia existir aunque no haya respuestas", respuestas);
        assertTrue("La lista de respuestas deberia estar vacia al inicio", respuestas.isEmpty());
        assertFalse("No deberia marcar que ha respondido una encuesta inexistente", usuario.haRespondidoEncuesta("encuesta1"));
        assertEquals("El numero de encuestas respondidas al inicio deberia ser 0", 0, usuario.getNumeroEncuestasRespondidas());
    }

    /** Testea que el constructor con password inicializa correctamente id, nombre y password. */
    @Test
    public void constructorConPassword_deberiaInicializarCamposCorrectamente() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("id2", "usuario2", "secret");

        assertEquals("El id no es correcto", "id2", usuario.getId());
        assertEquals("El nombre no es correcto", "usuario2", usuario.getNombre());
        assertEquals("El password no es correcto", "secret", usuario.getPassword());

        final List<Respuesta> respuestas = usuario.getRespuestasEncuesta("encuestaX");
        assertNotNull("La lista de respuestas no deberia ser null", respuestas);
        assertTrue("La lista de respuestas deberia estar vacia", respuestas.isEmpty());
    }

    /** Testea que los setters actualizan correctamente los campos id, nombre y password. */
    @Test
    public void setters_deberianActualizarLosCampos() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("id", "nombre", "pass");

        usuario.setId("nuevoId");
        usuario.setNombre("nuevoNombre");
        usuario.setPassword("nuevaPass");

        assertEquals("El id no se ha actualizado correctamente", "nuevoId", usuario.getId());
        assertEquals("El nombre no se ha actualizado correctamente", "nuevoNombre", usuario.getNombre());
        assertEquals("El password no se ha actualizado correctamente", "nuevaPass", usuario.getPassword());
    }

    /** Testea que addRespuesta crea la entrada para la encuesta si no existía y añade la respuesta correctamente. */
    @Test
    public void addRespuesta_deberiaCrearEntradaParaEncuestaYAnadirRespuesta() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");
        final String idEncuesta = "encuesta1";
        final Respuesta respuesta = new Respuesta("u1", "p1", idEncuesta, "valor1");

        usuario.addRespuesta(idEncuesta, respuesta);

        final List<Respuesta> respuestas = usuario.getRespuestasEncuesta(idEncuesta);
        assertEquals("Deberia haber exactamente una respuesta almacenada", 1, respuestas.size());

        final Respuesta almacenada = respuestas.get(0);
        assertSame("La instancia de respuesta almacenada deberia ser la misma que se paso", respuesta, almacenada);
        assertEquals("El idUsuario no coincide", "u1", almacenada.getIdUsuario());
        assertEquals("El idPregunta no coincide", "p1", almacenada.getIdPregunta());
        assertEquals("El idEncuesta no coincide", idEncuesta, almacenada.getIdEncuesta());
        assertEquals("El valor no coincide", "valor1", almacenada.getValor());
        assertTrue("La respuesta deberia estar marcada como contestada", almacenada.estaContestada());

        assertTrue("haRespondidoEncuesta deberia ser true tras anadir una respuesta", usuario.haRespondidoEncuesta(idEncuesta));
        assertEquals("El numero de encuestas respondidas deberia ser 1", 1, usuario.getNumeroEncuestasRespondidas());
    }

    /** Testea que addRespuesta permite añadir múltiples respuestas para la misma encuesta. */
    @Test
    public void addRespuesta_deberiaPermitirMultiplesRespuestasParaMismaEncuesta() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");
        final String idEncuesta = "encuesta1";

        final Respuesta r1 = new Respuesta("u1", "p1", idEncuesta, "v1");
        final Respuesta r2 = new Respuesta("u1", "p2", idEncuesta, "v2");
        final Respuesta r3 = new Respuesta("u1", "p3", idEncuesta, "v3");

        usuario.addRespuesta(idEncuesta, r1);
        usuario.addRespuesta(idEncuesta, r2);
        usuario.addRespuesta(idEncuesta, r3);

        final List<Respuesta> respuestas = usuario.getRespuestasEncuesta(idEncuesta);
        assertEquals("Deberia haber tres respuestas almacenadas", 3, respuestas.size());
        assertSame(r1, respuestas.get(0));
        assertSame(r2, respuestas.get(1));
        assertSame(r3, respuestas.get(2));
        assertTrue("haRespondidoEncuesta deberia ser true", usuario.haRespondidoEncuesta(idEncuesta));
        assertEquals("El numero de encuestas respondidas deberia ser 1", 1, usuario.getNumeroEncuestasRespondidas());
    }

    /** Testea que las respuestas se mantienen separadas por encuesta usando un mapa interno. */
    @Test
    public void addRespuesta_deberiaMantenerRespuestasSeparadasPorEncuesta() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");

        final Respuesta r1 = new Respuesta("u1", "p1", "encuesta1", "v1");
        final Respuesta r2 = new Respuesta("u1", "p2", "encuesta1", "v2");
        final Respuesta r3 = new Respuesta("u1", "p3", "encuesta2", "v3");

        usuario.addRespuesta("encuesta1", r1);
        usuario.addRespuesta("encuesta1", r2);
        usuario.addRespuesta("encuesta2", r3);

        final List<Respuesta> respuestas1 = usuario.getRespuestasEncuesta("encuesta1");
        final List<Respuesta> respuestas2 = usuario.getRespuestasEncuesta("encuesta2");

        assertEquals("encuesta1 deberia tener 2 respuestas", 2, respuestas1.size());
        assertEquals("encuesta2 deberia tener 1 respuesta", 1, respuestas2.size());

        assertSame("La primera respuesta de encuesta1 no coincide", r1, respuestas1.get(0));
        assertSame("La segunda respuesta de encuesta1 no coincide", r2, respuestas1.get(1));
        assertSame("La respuesta de encuesta2 no coincide", r3, respuestas2.get(0));

        assertTrue("haRespondidoEncuesta deberia ser true para encuesta1", usuario.haRespondidoEncuesta("encuesta1"));
        assertTrue("haRespondidoEncuesta deberia ser true para encuesta2", usuario.haRespondidoEncuesta("encuesta2"));
        assertFalse("haRespondidoEncuesta deberia ser false para una encuesta sin respuestas", usuario.haRespondidoEncuesta("encuestaInexistente"));

        assertEquals("El numero de encuestas respondidas deberia ser 2", 2, usuario.getNumeroEncuestasRespondidas());
    }

    /** Testea que getRespuestasEncuesta devuelve una lista vacía si no hay respuestas. */
    @Test
    public void getRespuestasEncuesta_deberiaDevolverListaVaciaSiNoHayRespuestas() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");

        final List<Respuesta> respuestas = usuario.getRespuestasEncuesta("encuestaInexistente");

        assertNotNull("La lista devuelta no deberia ser null", respuestas);
        assertTrue("La lista devuelta deberia estar vacia", respuestas.isEmpty());
    }

    /** Testea que getRespuestasEncuesta devuelve una copia independiente para evitar romper el encapsulamiento. */
    @Test
    public void getRespuestasEncuesta_deberiaDevolverCopiaIndependiente() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");
        final String idEncuesta = "encuesta1";
        final Respuesta r1 = new Respuesta("u1", "p1", idEncuesta, "v1");
        usuario.addRespuesta(idEncuesta, r1);

        final List<Respuesta> lista1 = usuario.getRespuestasEncuesta(idEncuesta);
        lista1.clear();
        final List<Respuesta> lista2 = usuario.getRespuestasEncuesta(idEncuesta);

        assertEquals("La modificacion de la lista devuelta no deberia afectar al almacenamiento interno", 1, lista2.size());
        assertSame("La respuesta almacenada deberia seguir siendo r1", r1, lista2.get(0));
    }

    /** Testea que haRespondidoEncuesta devuelve false si no existe ninguna entrada para esa encuesta. */
    @Test
    public void haRespondidoEncuesta_deberiaSerFalseSiNoExisteEntradaParaEsaEncuesta() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");

        final boolean haRespondido = usuario.haRespondidoEncuesta("encuestaInexistente");

        assertFalse("No deberia indicar que ha respondido una encuesta que no existe en el mapa", haRespondido);
    }

    /** Testea que haRespondidoEncuesta devuelve true aunque la respuesta no esté contestada (valor null). */
    @Test
    public void haRespondidoEncuesta_deberiaSerTrueAunqueRespuestaNoEsteContestada() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");
        final String idEncuesta = "encuesta1";
        final Respuesta respuesta = new Respuesta("u1", "p1", idEncuesta);

        usuario.addRespuesta(idEncuesta, respuesta);

        assertFalse("La respuesta no deberia estar contestada", respuesta.estaContestada());
        assertTrue("haRespondidoEncuesta solo comprueba si hay respuestas, no si estan contestadas", usuario.haRespondidoEncuesta(idEncuesta));
        assertEquals("El numero de encuestas respondidas deberia ser 1", 1, usuario.getNumeroEncuestasRespondidas());
    }

    /** Testea que getNumeroEncuestasRespondidas cuenta solo encuestas distintas, no número de respuestas. */
    @Test
    public void getNumeroEncuestasRespondidas_deberiaContarClavesDistintasNoNumeroDeRespuestas() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");

        final Respuesta r1 = new Respuesta("u1", "p1", "encuesta1", "v1");
        final Respuesta r2 = new Respuesta("u1", "p2", "encuesta1", "v2");
        final Respuesta r3 = new Respuesta("u1", "p3", "encuesta2", "v3");

        usuario.addRespuesta("encuesta1", r1);
        usuario.addRespuesta("encuesta1", r2);
        usuario.addRespuesta("encuesta2", r3);

        assertEquals("Deberia contar solo las encuestas distintas", 2, usuario.getNumeroEncuestasRespondidas());
    }

    /** Testea que eliminarRespuestasEncuesta borra todas las respuestas de la encuesta indicada. */
    @Test
    public void eliminarRespuestasEncuesta_deberiaEliminarTodasLasRespuestasDeEsaEncuesta() {
        final UsuarioRespondedor usuario = new UsuarioRespondedor("u1", "resp");

        final Respuesta r1 = new Respuesta("u1", "p1", "encuesta1", "v1");
        final Respuesta r2 = new Respuesta("u1", "p2", "encuesta2", "v2");

        usuario.addRespuesta("encuesta1", r1);
        usuario.addRespuesta("encuesta2", r2);

        usuario.eliminarRespuestasEncuesta("encuesta1");

        final List<Respuesta> respuestas1 = usuario.getRespuestasEncuesta("encuesta1");
        final List<Respuesta> respuestas2 = usuario.getRespuestasEncuesta("encuesta2");

        assertTrue("encuesta1 deberia quedar sin respuestas", respuestas1.isEmpty());
        assertEquals("encuesta2 deberia seguir teniendo 1 respuesta", 1, respuestas2.size());
        assertFalse("haRespondidoEncuesta deberia ser false para encuesta1", usuario.haRespondidoEncuesta("encuesta1"));
        assertTrue("haRespondidoEncuesta deberia seguir siendo true para encuesta2", usuario.haRespondidoEncuesta("encuesta2"));
        assertEquals("El numero de encuestas respondidas deberia ser 1 despues de eliminar encuesta1", 1, usuario.getNumeroEncuestasRespondidas());
    }
}
