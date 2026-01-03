package test;

import main.domain.classes.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase {@link UsuarioAdmin}.
 *
 * <p>UsuarioAdmin representa un usuario administrador del sistema, encargado
 * de gestionar encuestas (crear, modificar, eliminar) y que no responde
 * encuestas como lo haría un {@link UsuarioRespondedor}.</p>
 *
 * <p>Estos tests verifican principalmente:</p>
 * <ul>
 *     <li>Inicialización correcta de los campos {@code id}, {@code nombre} y {@code password} mediante ambos constructores.</li>
 *     <li>Comportamiento esperado cuando no se proporciona password (debería quedar {@code null}).</li>
 *     <li>Correcto funcionamiento de los setters para {@code id}, {@code nombre} y {@code password}.</li>
 * </ul>
 *
 * <p>Cada test sigue la estructura estándar de Arrange-Act-Assert:</p>
 * <ol>
 *     <li><b>Arrange:</b> se preparan los datos de prueba y se instancia el objeto UsuarioAdmin.</li>
 *     <li><b>Act:</b> se ejecuta la acción a probar (constructor o setter).</li>
 *     <li><b>Assert:</b> se comprueba que los resultados cumplen con lo esperado.</li>
 * </ol>
 */
public class UsuarioAdminTest {

    /**
     * Verifica que el constructor sin password inicializa correctamente los campos {@code id} y {@code nombre},
     * y que el {@code password} queda en {@code null}.
     */
    @Test
    public void constructorSinPassword_deberiaInicializarCamposCorrectamente() {
        final UsuarioAdmin usuario = new UsuarioAdmin("id1", "admin");

        assertEquals("El id no es correcto", "id1", usuario.getId());
        assertEquals("El nombre no es correcto", "admin", usuario.getNombre());
        assertNull("El password deberia ser null al usar el constructor sin password",
                usuario.getPassword());
    }

    /**
     * Verifica que el constructor con password inicializa correctamente {@code id}, {@code nombre} y {@code password}.
     */
    @Test
    public void constructorConPassword_deberiaInicializarCamposCorrectamente() {
        final UsuarioAdmin usuario = new UsuarioAdmin("id2", "admin2", "secret");

        assertEquals("El id no es correcto", "id2", usuario.getId());
        assertEquals("El nombre no es correcto", "admin2", usuario.getNombre());
        assertEquals("El password no es correcto", "secret", usuario.getPassword());
    }

    /**
     * Comprueba que los setters {@link UsuarioAdmin#setId}, {@link UsuarioAdmin#setNombre} y {@link UsuarioAdmin#setPassword}
     * actualizan correctamente los campos internos del objeto.
     */
    @Test
    public void setters_deberianActualizarLosCampos() {
        final UsuarioAdmin usuario = new UsuarioAdmin("id", "nombre", "pass");

        usuario.setId("nuevoId");
        usuario.setNombre("nuevoNombre");
        usuario.setPassword("nuevaPass");

        assertEquals("El id no se ha actualizado correctamente",
                "nuevoId", usuario.getId());
        assertEquals("El nombre no se ha actualizado correctamente",
                "nuevoNombre", usuario.getNombre());
        assertEquals("El password no se ha actualizado correctamente",
                "nuevaPass", usuario.getPassword());
    }
}
