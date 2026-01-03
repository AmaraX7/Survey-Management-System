package main.domain.controllers;

import main.domain.classes.*;
import java.util.*;

/**
 * Controlador de lógica de negocio encargado de la exportación de datos.
 * <p>
 * Esta clase transforma objetos del dominio (encuestas, usuarios y respuestas)
 * en estructuras de datos crudas listas para ser exportadas, típicamente en formato CSV.
 * <p>
 * No interactúa con el sistema de archivos ni con capas de persistencia; su única
 * responsabilidad es preparar los datos en memoria.
 */
public class ControladorExportacion {

    /**
     * Construye un controlador de exportación sin dependencias externas.
     */
    public ControladorExportacion() {
        // Sin dependencias
    }

    /**
     * Prepara los datos de una encuesta para su exportación.
     * <p>
     * Genera los encabezados y las filas correspondientes a cada usuario,
     * alineando las respuestas con el orden de las preguntas de la encuesta.
     *
     * @param encuesta             encuesta a exportar
     * @param usuarios             lista de usuarios respondedores
     * @param respuestasPorUsuario mapa de respuestas agrupadas por identificador de usuario
     * @return estructura {@link DatosExportacion} lista para ser exportada
     * @throws IllegalArgumentException si la encuesta es {@code null}, no tiene preguntas
     *                                  o no hay usuarios
     */
    public DatosExportacion prepararExportacion(
            Encuesta encuesta,
            List<UsuarioRespondedor> usuarios,
            Map<String, List<Respuesta>> respuestasPorUsuario) {

        if (encuesta == null) {
            throw new IllegalArgumentException("Encuesta no puede ser null");
        }

        List<Pregunta> preguntas = encuesta.getPreguntas();
        if (preguntas.isEmpty()) {
            throw new IllegalArgumentException("La encuesta no tiene preguntas");
        }

        if (usuarios.isEmpty()) {
            throw new IllegalArgumentException("La encuesta no tiene respuestas");
        }

        // Crear encabezados
        String[] encabezados = crearEncabezados(preguntas);

        // Crear filas de datos
        List<String[]> filas = crearFilas(preguntas, usuarios, respuestasPorUsuario);

        return new DatosExportacion(encabezados, filas);
    }

    /**
     * Obtiene información descriptiva sobre una exportación sin ejecutarla.
     *
     * @param encuesta encuesta a analizar
     * @param usuarios lista de usuarios respondedores
     * @return información de exportación o {@code null} si la encuesta es {@code null}
     */
    public InfoExportacion obtenerInfoExportacion(
            Encuesta encuesta,
            List<UsuarioRespondedor> usuarios) {

        if (encuesta == null) {
            return null;
        }

        int numPreguntas = encuesta.getPreguntas().size();
        int numUsuarios = usuarios != null ? usuarios.size() : 0;

        return new InfoExportacion(numPreguntas, numUsuarios, encuesta.getTitulo());
    }

    // ========== MÉTODOS PRIVADOS ==========

    /**
     * Crea el encabezado del CSV a partir de las preguntas de la encuesta.
     *
     * @param preguntas lista de preguntas
     * @return array de encabezados (ID_Usuario + enunciados)
     */
    private String[] crearEncabezados(List<Pregunta> preguntas) {
        String[] encabezados = new String[preguntas.size() + 1];
        encabezados[0] = "ID_Usuario";

        for (int i = 0; i < preguntas.size(); i++) {
            encabezados[i + 1] = preguntas.get(i).getEnunciado();
        }

        return encabezados;
    }

    /**
     * Crea las filas de datos correspondientes a cada usuario.
     *
     * @param preguntas             preguntas de la encuesta
     * @param usuarios              usuarios respondedores
     * @param respuestasPorUsuario  respuestas agrupadas por usuario
     * @return lista de filas listas para exportación
     */
    private List<String[]> crearFilas(
            List<Pregunta> preguntas,
            List<UsuarioRespondedor> usuarios,
            Map<String, List<Respuesta>> respuestasPorUsuario) {

        List<String[]> filas = new ArrayList<>();

        for (UsuarioRespondedor usuario : usuarios) {
            String[] fila = new String[preguntas.size() + 1];
            fila[0] = usuario.getId();

            List<Respuesta> respuestas = respuestasPorUsuario.getOrDefault(
                    usuario.getId(), new ArrayList<>()
            );

            // Mapear respuestas por ID de pregunta
            Map<String, Respuesta> mapaRespuestas = new HashMap<>();
            for (Respuesta r : respuestas) {
                mapaRespuestas.put(r.getIdPregunta(), r);
            }

            // Llenar fila con respuestas
            for (int i = 0; i < preguntas.size(); i++) {
                Pregunta pregunta = preguntas.get(i);
                Respuesta respuesta = mapaRespuestas.get(pregunta.getId());

                if (respuesta != null) {
                    fila[i + 1] = formatearRespuesta(respuesta.getValor(), pregunta);
                } else {
                    fila[i + 1] = "";
                }
            }

            filas.add(fila);
        }

        return filas;
    }

    /**
     * Formatea el valor de una respuesta según el tipo de pregunta,
     * adaptándolo a una representación textual apta para CSV.
     *
     * @param valor     valor de la respuesta
     * @param pregunta  pregunta asociada
     * @return representación en texto de la respuesta
     */
    private String formatearRespuesta(Object valor, Pregunta pregunta) {
        if (valor == null) return "";

        TipoPregunta tipo = pregunta.getTipoPregunta();

        switch (tipo) {
            case NUMERICA:
                if (valor instanceof Double) {
                    double d = (Double) valor;
                    if (d == Math.floor(d)) {
                        return String.valueOf((int) d);
                    }
                    return String.valueOf(d);
                }
                return valor.toString();

            case CATEGORIA_MULTIPLE:
                if (valor instanceof Set) {
                    Set<?> set = (Set<?>) valor;
                    return String.join(",", set.stream()
                            .map(Object::toString)
                            .toArray(String[]::new));
                }
                return valor.toString();

            default:
                return valor.toString();
        }
    }

    // ========== CLASES AUXILIARES ==========

    /**
     * Estructura que encapsula los datos preparados para exportación.
     */
    public static class DatosExportacion {
        public final String[] encabezados;
        public final List<String[]> filas;

        /**
         * Crea una estructura de datos de exportación.
         *
         * @param encabezados encabezados del CSV
         * @param filas       filas de datos
         */
        public DatosExportacion(String[] encabezados, List<String[]> filas) {
            this.encabezados = encabezados;
            this.filas = filas;
        }

        /**
         * Convierte los datos a una lista única (encabezados + filas).
         *
         * @return lista completa de filas
         */
        public List<String[]> toList() {
            List<String[]> todas = new ArrayList<>();
            todas.add(encabezados);
            todas.addAll(filas);
            return todas;
        }
    }

    /**
     * Información descriptiva sobre una exportación de encuesta.
     */
    public static class InfoExportacion {
        public final int numeroPreguntas;
        public final int numeroUsuarios;
        public final String tituloEncuesta;

        /**
         * Crea un objeto de información de exportación.
         *
         * @param numeroPreguntas número de preguntas de la encuesta
         * @param numeroUsuarios  número de usuarios respondedores
         * @param tituloEncuesta  título de la encuesta
         */
        public InfoExportacion(int numeroPreguntas, int numeroUsuarios, String tituloEncuesta) {
            this.numeroPreguntas = numeroPreguntas;
            this.numeroUsuarios = numeroUsuarios;
            this.tituloEncuesta = tituloEncuesta;
        }

        @Override
        public String toString() {
            return String.format(
                    "Encuesta: %s\nPreguntas: %d\nUsuarios: %d",
                    tituloEncuesta, numeroPreguntas, numeroUsuarios
            );
        }
    }
}
