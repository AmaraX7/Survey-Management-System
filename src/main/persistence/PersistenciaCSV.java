package main.persistence;

import java.io.*;
import java.util.*;

/**
 * Capa de persistencia para archivos CSV.
 * <p>
 * Responsable exclusivamente de leer y escribir archivos CSV mediante los componentes
 * {@link LectorCSV} y {@link EscritorCSV}. Esta clase <b>no</b> conoce objetos del dominio
 * (por ejemplo, {@code Encuesta}, {@code Pregunta}, {@code Respuesta}) y trabaja con datos crudos
 * representados como {@code String[]} y estructuras auxiliares simples.
 * </p>
 *
 * <p>
 * Proporciona dos estilos de operación:
 * </p>
 * <ul>
 *   <li><b>Lectura/escritura simple</b>: lista de filas {@code List<String[]>}.</li>
 *   <li><b>Lectura/escritura con encabezados</b>: primera fila como encabezados y el resto como datos,
 *   encapsulado en {@link PersistenciaCSV.DatosCSV}.</li>
 * </ul>
 *
 * <p>
 * Objetivo: desacoplar el sistema del formato CSV y centralizar la I/O CSV en una única clase.
 * </p>
 */
public class PersistenciaCSV {
    private final LectorCSV lector;
    private final EscritorCSV escritor;

    /**
     * Crea una instancia de persistencia CSV inicializando internamente un {@link LectorCSV}
     * y un {@link EscritorCSV}.
     */
    public PersistenciaCSV() {
        this.lector = new LectorCSV();
        this.escritor = new EscritorCSV();
    }

    // ========== LECTURA ==========

    /**
     * Lee un archivo CSV completo.
     * <p>
     * Retorna una lista de filas, donde cada fila es un array de {@link String} con las columnas
     * ya separadas (datos crudos). No interpreta tipos ni transforma valores.
     * </p>
     *
     * @param rutaArchivo ruta al archivo CSV a leer.
     * @return lista de filas como {@code List<String[]>}.
     * @throws IOException si ocurre un error de lectura del archivo.
     */
    public List<String[]> leerArchivo(String rutaArchivo) throws IOException {
        return lector.leerArchivo(rutaArchivo);
    }

    /**
     * Lee un archivo CSV que contiene encabezados.
     * <p>
     * La primera fila se interpreta como los encabezados (nombres de columnas) y el resto
     * como filas de datos. El formato retornado se encapsula en {@link PersistenciaCSV.DatosCSV}.
     * </p>
     *
     * <p>
     * Internamente delega la lectura a {@link LectorCSV#leerConEncabezados(String)} y convierte
     * sus objetos {@link LectorCSV.FilaRespuesta} en {@link PersistenciaCSV.FilaRespuesta},
     * para exponer una API consistente desde esta clase.
     * </p>
     *
     * @param rutaArchivo ruta al archivo CSV a leer.
     * @return objeto {@link PersistenciaCSV.DatosCSV} con encabezados y filas estructuradas.
     * @throws IOException si ocurre un error de lectura del archivo.
     */
    public DatosCSV leerConEncabezados(String rutaArchivo) throws IOException {
        // Leer con LectorCSV
        LectorCSV.DatosCSV datosLector = lector.leerConEncabezados(rutaArchivo);

        // Convertir FilaRespuesta de LectorCSV a FilaRespuesta de PersistenciaCSV
        List<FilaRespuesta> filasConvertidas = new ArrayList<>();
        for (LectorCSV.FilaRespuesta filaLector : datosLector.getFilas()) {
            FilaRespuesta filaConvertida = new FilaRespuesta(
                    filaLector.getIdUsuario(),
                    filaLector.getRespuestas()
            );
            filasConvertidas.add(filaConvertida);
        }

        // Retornar DatosCSV de PersistenciaCSV
        return new DatosCSV(datosLector.getEncabezados(), filasConvertidas);
    }

    // ========== ESCRITURA ==========

    /**
     * Escribe un archivo CSV a partir de filas crudas.
     * <p>
     * Cada elemento de {@code filas} representa una fila del CSV y cada {@code String[]}
     * representa las columnas de esa fila. Esta operación no añade encabezados por sí sola.
     * </p>
     *
     * @param rutaArchivo ruta destino del archivo CSV a escribir.
     * @param filas lista de filas crudas (arrays de strings).
     * @throws IOException si ocurre un error de escritura del archivo.
     */
    public void escribirArchivo(String rutaArchivo, List<String[]> filas) throws IOException {
        escritor.escribirArchivo(rutaArchivo, filas);
    }

    /**
     * Escribe un archivo CSV incluyendo una fila de encabezados.
     * <p>
     * Construye un nuevo listado donde la primera fila son los encabezados y luego se añaden
     * todas las filas de datos proporcionadas.
     * </p>
     *
     * @param rutaArchivo ruta destino del archivo CSV a escribir.
     * @param encabezados array con los nombres de las columnas (primera fila del archivo).
     * @param filas lista de filas de datos (sin incluir encabezados).
     * @throws IOException si ocurre un error de escritura del archivo.
     */
    public void escribirConEncabezados(String rutaArchivo, String[] encabezados,
                                       List<String[]> filas) throws IOException {
        List<String[]> todasFilas = new ArrayList<>();
        todasFilas.add(encabezados);
        todasFilas.addAll(filas);
        escritor.escribirArchivo(rutaArchivo, todasFilas);
    }

    // ========== CLASES AUXILIARES (expuestas) ==========

    /**
     * Wrapper para datos CSV estructurados.
     * <p>
     * Contiene los encabezados (nombres de columnas) y las filas de respuestas asociadas.
     * Está pensado para operaciones donde se requiere preservar el significado de cada columna.
     * </p>
     */
    public static class DatosCSV {
        private final String[] encabezados;
        private final List<FilaRespuesta> filas;

        /**
         * Construye una estructura de datos CSV con encabezados y filas.
         *
         * @param encabezados array de encabezados (nombres de columnas).
         * @param filas lista de filas estructuradas.
         */
        public DatosCSV(String[] encabezados, List<FilaRespuesta> filas) {
            this.encabezados = encabezados;
            this.filas = filas;
        }

        /**
         * Devuelve los encabezados del CSV.
         *
         * @return array de encabezados.
         */
        public String[] getEncabezados() {
            return encabezados;
        }

        /**
         * Devuelve las filas del CSV.
         *
         * @return lista de filas estructuradas.
         */
        public List<FilaRespuesta> getFilas() {
            return filas;
        }

        /**
         * Devuelve el número de columnas del CSV, equivalente a la longitud del array de encabezados.
         *
         * @return número de columnas.
         */
        public int getNumeroColumnas() {
            return encabezados.length;
        }

        /**
         * Devuelve el número de filas de datos (sin contar la fila de encabezados).
         *
         * @return número de filas.
         */
        public int getNumeroFilas() {
            return filas.size();
        }
    }

    /**
     * Wrapper para una fila con ID de usuario.
     * <p>
     * Representa una fila de datos donde la primera “columna lógica” es el identificador
     * del usuario y el resto de columnas se almacenan en {@code respuestas}.
     * </p>
     */
    public static class FilaRespuesta {
        private final String idUsuario;
        private final String[] respuestas;

        /**
         * Construye una fila de respuestas asociada a un usuario.
         *
         * @param idUsuario identificador del usuario.
         * @param respuestas columnas de respuesta (una por pregunta/columna del CSV).
         */
        public FilaRespuesta(String idUsuario, String[] respuestas) {
            this.idUsuario = idUsuario;
            this.respuestas = respuestas;
        }

        /**
         * Devuelve el ID del usuario asociado a la fila.
         *
         * @return id del usuario.
         */
        public String getIdUsuario() {
            return idUsuario;
        }

        /**
         * Devuelve el array de respuestas crudas de la fila.
         *
         * @return respuestas como array de strings.
         */
        public String[] getRespuestas() {
            return respuestas;
        }
    }

    // ========== ACCESO A COMPONENTES ==========

    /**
     * Devuelve el componente lector utilizado internamente.
     *
     * @return instancia de {@link LectorCSV}.
     */
    public LectorCSV getLector() {
        return lector;
    }

    /**
     * Devuelve el componente escritor utilizado internamente.
     *
     * @return instancia de {@link EscritorCSV}.
     */
    public EscritorCSV getEscritor() {
        return escritor;
    }
}
