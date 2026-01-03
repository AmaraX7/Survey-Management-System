package main.persistence;

import java.io.*;
import java.util.*;

/**
 * Escritor de archivos CSV (Comma-Separated Values).
 *
 * <p>Responsabilidad: recibir datos crudos ({@code List<String[]>}) y escribirlos a disco en formato CSV,
 * sin conocer clases de dominio.</p>
 *
 * <h2>Características</h2>
 * <ul>
 *   <li>Separador configurable (por defecto coma {@code ,}).</li>
 *   <li>Escapado CSV básico: si un campo contiene separador, comillas o saltos de línea,
 *       se envuelve en comillas dobles y se duplican comillas internas (RFC-style).</li>
 *   <li>Opción de incluir BOM (Byte Order Mark) UTF-8 al inicio del archivo.</li>
 *   <li>Valida/crea el directorio padre del archivo de salida.</li>
 * </ul>
 *
 * <p><b>Nota:</b> Aunque el escapado es el típico de CSV, la lectura actual en {@code LectorCSV}
 * usa {@code String.split(",")} y por tanto <b>no reconstruye correctamente</b> campos con comas entre comillas.
 * Es decir: este escritor sí puede producir CSV con comillas, pero el lector simple no lo parsea “bien”.</p>
 */
public class EscritorCSV {

    /**
     * Separador utilizado entre columnas.
     * Por defecto: {@code ","}.
     */
    private String separador;

    /**
     * Si es {@code true}, escribe el BOM UTF-8 (U+FEFF) al inicio del archivo.
     * Útil para algunos Excel/Windows que detectan mejor UTF-8.
     */
    private boolean incluirBOM;

    /**
     * Crea un escritor CSV con configuración por defecto:
     * <ul>
     *   <li>Separador: coma ({@code ","})</li>
     *   <li>Incluir BOM: {@code false}</li>
     * </ul>
     */
    public EscritorCSV() {
        this.separador = ",";
        this.incluirBOM = false;
    }

    /**
     * Escribe un archivo CSV a partir de una lista de filas.
     *
     * <p>Cada fila es un {@code String[]} que representa las columnas. Los valores {@code null}
     * se convierten en string vacío. Cada fila se escribe en una línea distinta.</p>
     *
     * @param rutaArchivo ruta destino donde se escribirá el CSV.
     * @param filas lista de filas (cada una un array de columnas).
     * @throws IOException si no se puede crear el directorio o escribir el archivo.
     * @throws IllegalArgumentException si {@code rutaArchivo} es null/vacía o {@code filas} es null/vacía.
     */
    public void escribirArchivo(String rutaArchivo, List<String[]> filas) throws IOException {
        validarRuta(rutaArchivo);

        if (filas == null || filas.isEmpty()) {
            throw new IllegalArgumentException("No hay datos para escribir");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            // Escribir BOM si está configurado
            if (incluirBOM) {
                bw.write('\ufeff');
            }

            // Escribir todas las filas
            for (int i = 0; i < filas.size(); i++) {
                String lineaFormateada = formatearLinea(filas.get(i));
                bw.write(lineaFormateada);

                // Agregar salto de línea excepto en la última fila
                if (i < filas.size() - 1) {
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Escribe un archivo CSV recibiendo encabezados y filas.
     *
     * <p>Este método está marcado como {@link Deprecated} porque es redundante:
     * puedes construir una lista con encabezados como primera fila y llamar a
     * {@link #escribirArchivo(String, List)}.</p>
     *
     * @param rutaArchivo ruta destino.
     * @param encabezados primera fila del CSV.
     * @param filas resto de filas.
     * @throws IOException si hay errores de escritura.
     */
    @Deprecated
    public void escribirArchivo(String rutaArchivo, String[] encabezados,
                               List<String[]> filas) throws IOException {
        List<String[]> todasFilas = new ArrayList<>();
        todasFilas.add(encabezados);
        todasFilas.addAll(filas);

        escribirArchivo(rutaArchivo, todasFilas);
    }

    /**
     * Valida la ruta y asegura que el directorio padre exista o se pueda crear.
     *
     * @param rutaArchivo ruta de salida.
     * @throws IOException si no se puede crear el directorio padre.
     * @throws IllegalArgumentException si la ruta es null o vacía.
     */
    private void validarRuta(String rutaArchivo) throws IOException {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía");
        }

        File archivo = new File(rutaArchivo);
        File directorio = archivo.getParentFile();

        // Si hay un directorio padre, verificar que existe o se puede crear
        if (directorio != null && !directorio.exists()) {
            if (!directorio.mkdirs()) {
                throw new IOException("No se puede crear el directorio: " + directorio);
            }
        }
    }

    /**
     * Formatea una fila como una línea CSV.
     *
     * <p>Aplica escapado por campo usando {@link #escaparValor(String)} y une los campos
     * con {@link #separador}.</p>
     *
     * @param valores columnas de la fila.
     * @return línea CSV lista para escribirse.
     */
    private String formatearLinea(String[] valores) {
        if (valores == null || valores.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < valores.length; i++) {
            String valor = valores[i];

            // Manejar valores nulos
            if (valor == null) {
                valor = "";
            }

            // Escapar el valor si es necesario
            String valorEscapado = escaparValor(valor);
            sb.append(valorEscapado);

            // Agregar separador excepto en el último valor
            if (i < valores.length - 1) {
                sb.append(separador);
            }
        }

        return sb.toString();
    }

    /**
     * Escapa un valor para CSV si es necesario:
     * <ul>
     *   <li>Si contiene separador, comillas dobles o saltos de línea, se envuelve en comillas dobles.</li>
     *   <li>Las comillas dobles internas se duplican: {@code "} -> {@code ""}.</li>
     * </ul>
     *
     * @param valor campo original.
     * @return campo escapado en formato CSV.
     */
    private String escaparValor(String valor) {
        // Si está vacío, devolver tal cual
        if (valor.isEmpty()) {
            return valor;
        }

        // Verificar si necesita escapado
        boolean necesitaComillas = valor.contains(separador) ||
                                   valor.contains("\"") ||
                                   valor.contains("\n") ||
                                   valor.contains("\r");

        if (!necesitaComillas) {
            return valor;
        }

        // Duplicar comillas internas
        String valorEscapado = valor.replace("\"", "\"\"");

        // Envolver en comillas
        return "\"" + valorEscapado + "\"";
    }

}
