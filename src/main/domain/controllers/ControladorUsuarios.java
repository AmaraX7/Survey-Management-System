package main.domain.controllers;

import main.domain.classes.*;
import java.util.*;

/**
 * Controlador de lógica de negocio para la gestión de usuarios.
 * <p>
 * Este controlador se encarga de la creación y validación de usuarios del dominio,
 * así como de operaciones relacionadas con usuarios respondedores y sus respuestas.
 * <p>
 * No interactúa con persistencia; todas las operaciones se realizan únicamente
 * sobre objetos en memoria.
 */
public class ControladorUsuarios {

    /**
     * Crea una instancia del controlador de usuarios.
     * <p>
     * No requiere dependencias externas ni acceso a persistencia.
     */
    public ControladorUsuarios() {
        // Sin dependencias de persistencia
    }

    // ========== CREACIÓN DE USUARIOS ==========

    /**
     * Crea un usuario respondedor con contraseña.
     *
     * @param id        identificador único del usuario
     * @param nombre    nombre del usuario
     * @param password  contraseña del usuario
     * @return nuevo {@link UsuarioRespondedor} creado
     * @throws IllegalArgumentException si el id o el nombre son inválidos
     */
    public UsuarioRespondedor crearUsuarioRespondedor(String id, String nombre, String password) {
        validarDatosUsuario(id, nombre);
        return new UsuarioRespondedor(id, nombre, password);
    }

    /**
     * Crea un usuario respondedor sin contraseña.
     *
     * @param id      identificador único del usuario
     * @param nombre  nombre del usuario
     * @return nuevo {@link UsuarioRespondedor} creado
     * @throws IllegalArgumentException si el id o el nombre son inválidos
     */
    public UsuarioRespondedor crearUsuarioRespondedor(String id, String nombre) {
        validarDatosUsuario(id, nombre);
        return new UsuarioRespondedor(id, nombre);
    }

    /**
     * Crea un usuario administrador.
     *
     * @param id        identificador único del administrador
     * @param nombre    nombre del administrador
     * @param password  contraseña del administrador
     * @return nuevo {@link UsuarioAdmin} creado
     * @throws IllegalArgumentException si el id o el nombre son inválidos
     */
    public UsuarioAdmin crearUsuarioAdmin(String id, String nombre, String password) {
        validarDatosUsuario(id, nombre);
        return new UsuarioAdmin(id, nombre, password);
    }

    // ========== OPERACIONES CON RESPONDEDORES ==========

    /**
     * Filtra los usuarios respondedores que han contestado una encuesta concreta.
     *
     * @param usuarios    lista de usuarios respondedores
     * @param idEncuesta  identificador de la encuesta
     * @return lista de usuarios que han respondido la encuesta
     */
    public List<UsuarioRespondedor> filtrarUsuariosQueRespondieron(
            List<UsuarioRespondedor> usuarios,
            String idEncuesta) {

        List<UsuarioRespondedor> resultado = new ArrayList<>();
        for (UsuarioRespondedor usuario : usuarios) {
            if (usuario.haRespondidoEncuesta(idEncuesta)) {
                resultado.add(usuario);
            }
        }
        return resultado;
    }

    /**
     * Obtiene las respuestas de un usuario para una encuesta concreta.
     *
     * @param usuario     usuario respondedor
     * @param idEncuesta  identificador de la encuesta
     * @return lista de {@link Respuesta} asociadas al usuario y encuesta;
     *         lista vacía si el usuario es {@code null} o no tiene respuestas
     */
    public List<Respuesta> obtenerRespuestasUsuario(
            UsuarioRespondedor usuario,
            String idEncuesta) {

        if (usuario == null) return new ArrayList<>();
        return usuario.getRespuestasEncuesta(idEncuesta);
    }

    // ========== VALIDACIONES ==========

    /**
     * Valida los datos básicos de un usuario.
     *
     * @param id      identificador del usuario
     * @param nombre  nombre del usuario
     * @throws IllegalArgumentException si el id o el nombre son nulos o vacíos
     */
    private void validarDatosUsuario(String id, String nombre) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede estar vacío");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
    }

    /**
     * Comprueba si un usuario es válido según las reglas básicas del dominio.
     *
     * @param usuario usuario a validar
     * @return {@code true} si el usuario es válido; {@code false} en caso contrario
     */
    public boolean esUsuarioValido(Usuario usuario) {
        if (usuario == null) return false;
        if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
            return false;
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
