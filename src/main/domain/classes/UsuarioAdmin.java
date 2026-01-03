package main.domain.classes;

import java.util.*;

/**
 * Representa un usuario administrador del sistema.
 * <p>
 * Un {@code UsuarioAdmin} es un tipo concreto de {@link Usuario} con permisos
 * de administración sobre el sistema. Esta clase actúa como una especialización
 * semántica del usuario genérico.
 */
public class UsuarioAdmin extends Usuario {

    /**
     * Crea un usuario administrador con identificador y nombre.
     *
     * @param id     identificador único del administrador
     * @param nombre nombre del administrador
     */
    public UsuarioAdmin(String id, String nombre) {
        super(id, nombre);
    }

    /**
     * Crea un usuario administrador con identificador, nombre y contraseña.
     *
     * @param id       identificador único del administrador
     * @param nombre   nombre del administrador
     * @param password contraseña del administrador
     */
    public UsuarioAdmin(String id, String nombre, String password) {
        super(id, nombre, password);
    }
}
