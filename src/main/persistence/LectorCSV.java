package main.persistence;

import java.io.*;
import java.util.*;

/**
 * Lector de archivos CSV (Comma-Separated Values) con soporte de:
 * <ul>
 *   <li>Lectura completa del archivo como {@code List<String[]>}.</li>
 *   <li>Lectura estructurada con encabezados ({@link DatosCSV}).</li>
 *   <li>Separador configurable (por defecto coma) y opción de saltar líneas vacías.</li>
 * </ul>
 *
 * <p><b>Importante:</b> Esta implementación usa {@link String#split(String)} con el separador,
 * por lo que <b>NO</b> soporta campos con comillas que contengan comas internas (CSV “real”
 * estilo Excel) como: {@code "hola, mundo"}.</p>
 *
 * <p>Diseño: esta clase pertenece a la capa de persistencia y trabaja solo con Strings.
 * No conoce clases de dominio.</p>
 */
public class LectorCSV {

    /**
     * Separador usado para dividir columnas en cada línea.
     * Por defecto: {@code ","}.
     */
    private final String separador;

    /**
     * Si es {@code true}, se ignoran las líneas que estén vacías o solo contengan espacios.
     */
    private final boolean saltarLineasVacias;

    /**
     * Crea un lector CSV con configuración por defecto:
     * <ul>
     *   <li>Separador: coma ({@code ","})</li>
     *   <li>Saltar líneas vacías: {@code true}</li>
     * </ul>
     */
    public LectorCSV() {
        this.separador = ",";
        this.saltarLineasVacias = true;
    }

    /**
     * Lee un archivo CSV completo y devuelve todas sus líneas parseadas.
     *
     * <p>Cada línea se transforma en un {@code String[]} con las columnas separadas por {@link #separador},
     * aplicando {@link String#trim()} a cada columna.</p>
     *
     * @param rutaArchivo ruta del archivo CSV a leer.
     * @return lista de filas, donde cada fila es un array de Strings.
     * @throws IOException si la ruta es inválida, no existe, no se puede leer, o hay errores de I/O.
     * @throws FileNotFoundException si el archivo no existe.
     * @throws IllegalArgumentException si {@code rutaArchivo} es null o vacía.
     */
    public List<String[]> leerArchivo(String rutaArchivo) throws IOException {
        validarRuta(rutaArchivo);

        List<String[]> lineas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                if (saltarLineasVacias && linea.trim().isEmpty()) {
                    continue;
                }

                String[] valores = parsearLinea(linea);
                lineas.add(valores);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Archivo no encontrado: " + rutaArchivo);
        }

        return lineas;
    }

    /**
     * Lee un CSV asumiendo formato con encabezados:
     * <ul>
     *   <li>Primera fila: nombres de columnas (encabezados)</li>
     *   <li>Resto de filas: datos donde la columna 0 es {@code idUsuario}</li>
     * </ul>
     *
     * <p>Devuelve un objeto {@link DatosCSV} que contiene:
     * <ul>
     *   <li>{@code encabezados}: la primera fila del archivo</li>
     *   <li>{@code filas}: lista de {@link FilaRespuesta}, con idUsuario y respuestas desde columna 1</li>
     * </ul>
     *
     * @param rutaArchivo ruta del archivo CSV.
     * @return estructura {@link DatosCSV} con encabezados y filas procesadas.
     * @throws IOException si el archivo está vacío o hay errores de lectura.
     */
    public DatosCSV leerConEncabezados(String rutaArchivo) throws IOException {
        List<String[]> todasLineas = leerArchivo(rutaArchivo);

        if (todasLineas.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        String[] encabezados = todasLineas.get(0);

        // Convertir filas a FilaRespuesta
        List<FilaRespuesta> filas = new ArrayList<>();
        for (int i = 1; i < todasLineas.size(); i++) {
            String[] fila = todasLineas.get(i);
            if (fila.length > 0) {
                String idUsuario = fila[0].trim();
                String[] respuestas = extraerDesdeColumna1(fila);
                filas.add(new FilaRespuesta(idUsuario, respuestas));
            }
        }

        return new DatosCSV(encabezados, filas);
    }

    /**
     * Valida que la ruta del archivo sea correcta y que el archivo exista y sea legible.
     *
     * @param rutaArchivo ruta a validar.
     * @throws IOException si la ruta no corresponde a un archivo legible o hay problemas de acceso.
     * @throws FileNotFoundException si el archivo no existe.
     * @throws IllegalArgumentException si la ruta es null o vacía.
     */
    private void validarRuta(String rutaArchivo) throws IOException {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía");
        }

        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            throw new FileNotFoundException("El archivo no existe: " + rutaArchivo);
        }

        if (!archivo.isFile()) {
            throw new IOException("La ruta no corresponde a un archivo: " + rutaArchivo);
        }

        if (!archivo.canRead()) {
            throw new IOException("No se tienen permisos de lectura: " + rutaArchivo);
        }
    }

    /**
     * Parsea una línea del CSV en un array de valores.
     *
     * <p>Implementación: usa {@link String#split(String)} con {@link #separador} y aplica {@code trim()} a cada campo.</p>
     *
     * @param linea una línea de texto del CSV.
     * @return array con los campos parseados.
     */
    private String[] parsearLinea(String linea) {
        String[] valores = linea.split(separador);

        // Limpiar espacios en blanco
        for (int i = 0; i < valores.length; i++) {
            valores[i] = valores[i].trim();
        }

        return valores;
    }

    /**
     * Extrae elementos desde columna 1 (ignora columna 0).
     *
     * <p>Ejemplo: si {@code fila = [idUsuario, r1, r2, r3]}, entonces retorna {@code [r1, r2, r3]}.</p>
     *
     * @param fila fila completa del CSV.
     * @return array con las columnas desde la posición 1.
     */
    private String[] extraerDesdeColumna1(String[] fila) {
        if (fila.length <= 1) {
            return new String[0];
        }

        String[] resultado = new String[fila.length - 1];
        System.arraycopy(fila, 1, resultado, 0, fila.length - 1);
        return resultado;
    }

    // ========== CLASES INTERNAS ==========

    /**
     * Representa una fila de respuestas asociadas a un usuario.
     *
     * <p>Modelo de datos:</p>
     * <ul>
     *   <li>{@code idUsuario}: identificador del usuario (columna 0 del CSV original)</li>
     *   <li>{@code respuestas}: columnas desde la 1 en adelante</li>
     * </ul>
     */
    public static class FilaRespuesta {
        private final String idUsuario;
        private final String[] respuestas;

        /**
         * Construye una fila de respuesta.
         *
         * @param idUsuario id del usuario.
         * @param respuestas array de respuestas (columnas 1..n del CSV).
         */
        public FilaRespuesta(String idUsuario, String[] respuestas) {
            this.idUsuario = idUsuario;
            this.respuestas = respuestas;
        }

        /**
         * @return el ID del usuario asociado a esta fila.
         */
        public String getIdUsuario() {
            return idUsuario;
        }

        /**
         * @return array de respuestas asociadas al usuario.
         */
        public String[] getRespuestas() {
            return respuestas;
        }

        /**
         * Devuelve la respuesta en una posición concreta.
         *
         * @param indice índice dentro del array {@link #respuestas}.
         * @return respuesta en esa posición.
         * @throws IndexOutOfBoundsException si el índice está fuera de rango.
         */
        public String getRespuesta(int indice) {
            if (indice < 0 || indice >= respuestas.length) {
                throw new IndexOutOfBoundsException("Índice inválido: " + indice);
            }
            return respuestas[indice];
        }

        /**
         * @return número de respuestas disponibles en esta fila.
         */
        public int getNumeroRespuestas() {
            return respuestas.length;
        }
    }

    /**
     * Estructura de datos que representa el contenido de un CSV con encabezados.
     *
     * <p>Contiene:</p>
     * <ul>
     *   <li>{@code encabezados}: primera fila del CSV</li>
     *   <li>{@code filas}: filas con ID de usuario y respuestas</li>
     * </ul>
     */
    public static class DatosCSV {
        private final String[] encabezados;
        private final List<FilaRespuesta> filas;

        /**
         * Construye la estructura completa de datos CSV.
         *
         * @param encabezados primera fila del CSV (nombres de columnas).
         * @param filas lista de filas parseadas.
         */
        public DatosCSV(String[] encabezados, List<FilaRespuesta> filas) {
            this.encabezados = encabezados;
            this.filas = filas;
        }

        /**
         * @return encabezados (primera fila del CSV).
         */
        public String[] getEncabezados() {
            return encabezados;
        }

        /**
         * @return lista de filas con idUsuario + respuestas.
         */
        public List<FilaRespuesta> getFilas() {
            return filas;
        }

        /**
         * @return número de columnas del CSV.
         */
        public int getNumeroColumnas() {
            return encabezados.length;
        }

        /**
         * @return número de filas (sin contar encabezados).
         */
        public int getNumeroFilas() {
            return filas.size();
        }

        /**
         * Obtiene una fila por índice.
         *
         * @param indice índice de la fila en la lista.
         * @return la fila correspondiente.
         * @throws IndexOutOfBoundsException si el índice está fuera de rango.
         */
        public FilaRespuesta getFila(int indice) {
            if (indice < 0 || indice >= filas.size()) {
                throw new IndexOutOfBoundsException("Índice inválido: " + indice);
            }
            return filas.get(indice);
        }

        /**
         * Representación textual útil para depuración.
         *
         * @return string con número de columnas, filas y encabezados.
         */
        @Override
        public String toString() {
            return String.format("CSV: %d columnas, %d filas\nEncabezados: %s",
                    getNumeroColumnas(), getNumeroFilas(),
                    Arrays.toString(encabezados));
        }
    }
}
