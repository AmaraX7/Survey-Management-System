package main.domain.classes;

import java.util.*;

/**
 * Representa un usuario genérico del sistema.
 * <p>
 * Esta clase abstracta define la información y el comportamiento común a todos
 * los tipos de usuarios del dominio, como su identificación, nombre y credenciales.
 * <p>
 * Las subclases concretas deben especializar el comportamiento del usuario según
 * su rol dentro del sistema.
 */
public abstract class Usuario {

    protected String id;
    protected String nombre;
    protected String password;

    /**
     * Crea un usuario con identificador y nombre.
     *
     * @param id     identificador único del usuario
     * @param nombre nombre del usuario
     */
    public Usuario(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /**
     * Crea un usuario con identificador, nombre y contraseña.
     *
     * @param id       identificador único del usuario
     * @param nombre   nombre del usuario
     * @param password contraseña del usuario
     */
    public Usuario(String id, String nombre, String password) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
    }

    /**
     * Devuelve el identificador del usuario.
     *
     * @return identificador del usuario
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el nombre del usuario.
     *
     * @return nombre del usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve la contraseña del usuario.
     *
     * @return contraseña del usuario
     */
    public String getPassword() {
        return password;
    }

    /**
     * Modifica el nombre del usuario.
     *
     * @param nombre nuevo nombre del usuario
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Modifica el identificador del usuario.
     *
     * @param id nuevo identificador del usuario
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Modifica la contraseña del usuario.
     *
     * @param password nueva contraseña del usuario
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Verifica si la contraseña proporcionada coincide con la del usuario.
     *
     * @param password contraseña a verificar
     * @return {@code true} si la contraseña coincide; {@code false} en caso contrario
     */
    public boolean verificarPassword(String password) {
        return this.password.equals(password);
    }
}

