package main.domain.controllers;

import main.domain.classes.*;
import main.persistence.PersistenciaCSV;

import java.util.*;

/**
 * Controlador de lógica de negocio para la importación de datasets desde CSV.
 * <p>
 * Su responsabilidad es transformar datos crudos procedentes de CSV en objetos de dominio:
 * {@link Encuesta}, {@link Pregunta}, {@link UsuarioRespondedor} y {@link Respuesta}.
 * <p>
 * No guarda nada en persistencia del dominio; únicamente crea objetos en memoria.
 * Puede depender de utilidades de lectura CSV (por ejemplo {@link PersistenciaCSV}) para
 * acceder al contenido estructurado del CSV, pero no realiza operaciones de almacenamiento
 * permanente.
 */
public class ControladorImportacion {
    private final ControladorUsuarios controladorUsuarios;
    private final ControladorEncuestas controladorEncuestas;
    private final ControladorRespuestas controladorRespuestas;

    /**
     * Construye el controlador de importación con las dependencias necesarias
     * para crear usuarios, encuestas y respuestas en memoria.
     *
     * @param controladorUsuarios    controlador responsable de la creación/gestión de usuarios
     * @param controladorEncuestas   controlador responsable de la creación/gestión de encuestas y preguntas
     * @param controladorRespuestas  controlador responsable de la creación/validación de respuestas
     */
    public ControladorImportacion(
            ControladorUsuarios controladorUsuarios,
            ControladorEncuestas controladorEncuestas,
            ControladorRespuestas controladorRespuestas) {

        this.controladorUsuarios = controladorUsuarios;
        this.controladorEncuestas = controladorEncuestas;
        this.controladorRespuestas = controladorRespuestas;
    }

    /**
     * Procesa los datos de un CSV ya parseados y crea objetos de dominio en memoria.
     * <p>
     * Flujo general:
     * <ol>
     *   <li>Obtiene encabezados y filas del CSV.</li>
     *   <li>Crea una encuesta con título y descripción.</li>
     *   <li>Infiere tipos de preguntas a partir de los valores observados en cada columna.</li>
     *   <li>Crea las preguntas y las añade a la encuesta.</li>
     *   <li>Procesa cada fila, creando usuarios y sus respuestas asociadas a la encuesta.</li>
     * </ol>
     * <p>
     * Este método no persiste nada; retorna un {@link ResultadoImportacion} con lo creado.
     *
     * @param datosCSV     estructura con encabezados y filas ya leídas desde CSV
     * @param titulo       título para la encuesta creada
     * @param descripcion  descripción para la encuesta creada
     * @return resultado con la encuesta creada, usuarios procesados y respuestas por usuario
     * @throws IllegalArgumentException si el CSV no contiene filas,
     *                                  si la encuesta resultante no tiene preguntas,
     *                                  o si se detecta ausencia de respuestas/usuarios en la entrada
     */
    public ResultadoImportacion procesarDatosCSV(
            PersistenciaCSV.DatosCSV datosCSV,
            String titulo,
            String descripcion) {

        String[] nombresColumnas = datosCSV.getEncabezados();
        List<PersistenciaCSV.FilaRespuesta> filas = datosCSV.getFilas();

        if (filas.isEmpty()) {
            throw new IllegalArgumentException("El CSV no contiene datos");
        }

        // Ignorar primera columna (ID de usuario)
        String[] nombresPreguntas = new String[nombresColumnas.length - 1];
        System.arraycopy(nombresColumnas, 1, nombresPreguntas, 0, nombresColumnas.length - 1);

        // Crear encuesta
        Encuesta encuesta = controladorEncuestas.crearEncuesta(titulo, descripcion);
        System.out.println("✓ Encuesta creada: " + encuesta.getId());

        // Inferir tipos de las preguntas
        List<TipoInferido> tiposInferidos = inferirTiposDeColumnas(filas, nombresColumnas.length - 1);

        // Crear preguntas
        crearPreguntasDesdeInferencia(encuesta, nombresPreguntas, tiposInferidos);
        System.out.println("✓ " + nombresPreguntas.length + " preguntas creadas");

        // Procesar respuestas
        ResultadoImportacion resultado = procesarRespuestas(encuesta, filas);
        System.out.println("✓ " + resultado.usuariosCreados.size() + " usuarios procesados");

        return resultado;
    }

    // ========== INFERENCIA DE TIPOS ==========

    /**
     * Estructura auxiliar interna para representar el tipo inferido de una columna del CSV.
     * <p>
     * Contiene:
     * <ul>
     *   <li>Un identificador textual del tipo inferido (por ejemplo "NUMERICA", "CATEGORIA_SIMPLE", "LIBRE").</li>
     *   <li>Un conjunto de opciones (si aplica a tipos categóricos).</li>
     *   <li>Rango mínimo y máximo (si aplica a tipos numéricos).</li>
     * </ul>
     */
    private static class TipoInferido {
        String tipo;
        Set<String> opciones;
        double min;
        double max;

        /**
         * Crea una estructura de tipo inferido inicializando valores por defecto.
         * <p>
         * Inicializa:
         * <ul>
         *   <li>{@code opciones} como {@link LinkedHashSet} para preservar orden de inserción.</li>
         *   <li>{@code min} y {@code max} con valores extremos para permitir actualización incremental.</li>
         * </ul>
         *
         * @param tipo tipo inicial (por ejemplo "LIBRE")
         */
        TipoInferido(String tipo) {
            this.tipo = tipo;
            this.opciones = new LinkedHashSet<>();
            this.min = Double.MAX_VALUE;
            this.max = Double.MIN_VALUE;
        }
    }

    /**
     * Infiere el tipo de cada columna (pregunta) del CSV a partir de las filas observadas.
     * <p>
     * Heurística utilizada:
     * <ul>
     *   <li>Si no hay valores no vacíos en la columna: se infiere "LIBRE".</li>
     *   <li>Si todos los valores son numéricos:
     *       <ul>
     *         <li>Si hay &lt;= 2 valores únicos: se infiere "CATEGORIA_SIMPLE".</li>
     *         <li>Si hay más valores únicos: se infiere "NUMERICA" y se estima un rango con margen.</li>
     *       </ul>
     *   </li>
     *   <li>Si no todos son numéricos y hay &lt;= 10 valores únicos: se infiere "CATEGORIA_SIMPLE".</li>
     *   <li>En otro caso: se infiere "LIBRE".</li>
     * </ul>
     *
     * @param filas       filas del CSV
     * @param numColumnas número de columnas a inferir (sin contar la columna de ID de usuario)
     * @return lista de {@link TipoInferido} con el tipo estimado por columna
     */
    private List<TipoInferido> inferirTiposDeColumnas(
            List<PersistenciaCSV.FilaRespuesta> filas,
            int numColumnas) {

        List<TipoInferido> tipos = new ArrayList<>();

        for (int col = 0; col < numColumnas; col++) {
            TipoInferido tipo = new TipoInferido("LIBRE");
            boolean todoNumerico = true;
            Set<String> valoresUnicos = new LinkedHashSet<>();
            int totalValores = 0;

            for (PersistenciaCSV.FilaRespuesta fila : filas) {
                String[] respuestas = fila.getRespuestas();
                if (col >= respuestas.length) continue;

                String valor = respuestas[col].trim();
                if (valor.isEmpty()) continue;

                totalValores++;
                valoresUnicos.add(valor);

                if (todoNumerico) {
                    try {
                        double num = Double.parseDouble(valor);
                        tipo.min = Math.min(tipo.min, num);
                        tipo.max = Math.max(tipo.max, num);
                    } catch (NumberFormatException e) {
                        todoNumerico = false;
                    }
                }
            }

            if (totalValores == 0) {
                tipo.tipo = "LIBRE";
            } else if (todoNumerico) {
                if (valoresUnicos.size() <= 2) {
                    tipo.tipo = "CATEGORIA_SIMPLE";
                    tipo.opciones = valoresUnicos;
                } else {
                    tipo.tipo = "NUMERICA";
                    double margen = (tipo.max - tipo.min) * 0.05;
                    if (margen == 0) margen = 1.0;

                    if (tipo.min >= 0) {
                        tipo.min = Math.max(0, tipo.min - margen);
                    } else {
                        tipo.min -= margen;
                    }
                    tipo.max += margen;
                }
            } else if (valoresUnicos.size() <= 10) {
                tipo.tipo = "CATEGORIA_SIMPLE";
                tipo.opciones = valoresUnicos;
            } else {
                tipo.tipo = "LIBRE";
            }

            tipos.add(tipo);
        }

        return tipos;
    }

    /**
     * Crea las preguntas dentro de una encuesta a partir de los tipos inferidos.
     * <p>
     * Para cada columna:
     * <ul>
     *   <li>NUMERICA -&gt; {@link Numerica}</li>
     *   <li>CATEGORIA_SIMPLE -&gt; {@link CategoriaSimple}</li>
     *   <li>LIBRE (u otros) -&gt; {@link Libre}</li>
     * </ul>
     * Las preguntas creadas se añaden a la encuesta mediante {@link ControladorEncuestas#agregarPregunta(Encuesta, Pregunta)}.
     *
     * @param encuesta        encuesta donde se añadirán las preguntas
     * @param nombres         nombres/enunciados de las preguntas (derivados del encabezado)
     * @param tiposInferidos  lista de tipos inferidos por columna
     */
    private void crearPreguntasDesdeInferencia(
            Encuesta encuesta,
            String[] nombres,
            List<TipoInferido> tiposInferidos) {

        for (int i = 0; i < nombres.length; i++) {
            String textoPregunta = nombres[i];
            TipoInferido tipoInf = tiposInferidos.get(i);

            Pregunta pregunta;

            switch (tipoInf.tipo) {
                case "NUMERICA":
                    pregunta = new Numerica(textoPregunta, tipoInf.min, tipoInf.max);
                    break;

                case "CATEGORIA_SIMPLE":
                    pregunta = new CategoriaSimple(textoPregunta, tipoInf.opciones);
                    break;

                case "LIBRE":
                default:
                    pregunta = new Libre(textoPregunta, 1000);
                    break;
            }

            controladorEncuestas.agregarPregunta(encuesta, pregunta);
        }
    }

    // ========== PROCESAMIENTO DE RESPUESTAS ==========

    /**
     * Procesa las filas del CSV creando usuarios y registrando sus respuestas en memoria.
     * <p>
     * Por cada fila:
     * <ul>
     *   <li>Lee el ID de usuario.</li>
     *   <li>Crea un {@link UsuarioRespondedor}.</li>
     *   <li>Convierte y registra respuestas para cada pregunta.</li>
     * </ul>
     * <p>
     * Si ocurre un error al procesar un usuario, se informa por stderr y se continúa con el resto.
     *
     * @param encuesta        encuesta a la que pertenecen las preguntas/respuestas
     * @param filasRespuestas filas leídas desde el CSV
     * @return resultado con usuarios creados y respuestas por usuario
     */
    private ResultadoImportacion procesarRespuestas(
            Encuesta encuesta,
            List<PersistenciaCSV.FilaRespuesta> filasRespuestas) {

        List<UsuarioRespondedor> usuariosCreados = new ArrayList<>();
        Map<String, Map<String, Object>> respuestasPorUsuario = new HashMap<>();

        for (PersistenciaCSV.FilaRespuesta fila : filasRespuestas) {
            String idUsuario = fila.getIdUsuario();
            String[] respuestas = fila.getRespuestas();

            if (idUsuario.isEmpty()) {
                System.err.println("⚠ Fila sin ID de usuario, se omite");
                continue;
            }

            try {
                // Crear usuario
                UsuarioRespondedor usuario = controladorUsuarios.crearUsuarioRespondedor(
                        idUsuario, "Usuario " + idUsuario
                );
                usuariosCreados.add(usuario);

                // Asignar respuestas
                Map<String, Object> respuestasUsuario =
                        asignarRespuestasUsuario(usuario, encuesta, respuestas);
                respuestasPorUsuario.put(idUsuario, respuestasUsuario);

            } catch (Exception e) {
                System.err.println("⚠ Error procesando usuario " + idUsuario + ": " + e.getMessage());
            }
        }

        return new ResultadoImportacion(encuesta, usuariosCreados, respuestasPorUsuario);
    }

    /**
     * Asigna y registra las respuestas de un usuario para la encuesta.
     * <p>
     * Para cada posición i:
     * <ul>
     *   <li>Si el valor está vacío, se considera {@code null}.</li>
     *   <li>Si no está vacío, se convierte según el tipo de {@link Pregunta}.</li>
     *   <li>Se registra la respuesta mediante {@link ControladorRespuestas#responderPregunta(UsuarioRespondedor, Encuesta, String, Object)}.</li>
     * </ul>
     * <p>
     * Si ocurre error en una pregunta concreta, se informa por stderr y se continúa con el resto.
     *
     * @param usuario   usuario a quien se asignan las respuestas
     * @param encuesta  encuesta asociada
     * @param respuestas array de respuestas crudas (String) leídas del CSV
     * @return mapa (idPregunta -&gt; valorConvertido) con los valores asignados
     */
    private Map<String, Object> asignarRespuestasUsuario(
            UsuarioRespondedor usuario,
            Encuesta encuesta,
            String[] respuestas) {

        List<Pregunta> preguntas = encuesta.getPreguntas();
        Map<String, Object> respuestasMap = new HashMap<>();

        for (int i = 0; i < respuestas.length && i < preguntas.size(); i++) {
            String valorStr = respuestas[i].trim();
            Pregunta pregunta = preguntas.get(i);

            try {
                Object valorConvertido = valorStr.isEmpty() ?
                        null : convertirValor(valorStr, pregunta);

                controladorRespuestas.responderPregunta(
                        usuario, encuesta, pregunta.getId(), valorConvertido
                );

                respuestasMap.put(pregunta.getId(), valorConvertido);

            } catch (Exception e) {
                System.err.println("⚠ Error en pregunta " + pregunta.getId() + ": " + e.getMessage());
            }
        }

        return respuestasMap;
    }

    /**
     * Convierte un valor textual del CSV al tipo de dato esperado por una pregunta.
     *
     * @param valor     valor en formato texto
     * @param pregunta  pregunta que determina el tipo de conversión
     * @return objeto convertido según el tipo de pregunta
     * @throws IllegalArgumentException si el valor no puede convertirse en un número para preguntas numéricas
     */
    private Object convertirValor(String valor, Pregunta pregunta) {
        TipoPregunta tipo = pregunta.getTipoPregunta();

        switch (tipo) {
            case NUMERICA:
                try {
                    return Double.parseDouble(valor);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Valor numérico inválido: '" + valor + "'");
                }

            case ORDINAL:
            case CATEGORIA_SIMPLE:
                return valor;

            case CATEGORIA_MULTIPLE:
                Set<String> opciones = new HashSet<>();
                for (String opcion : valor.split(",")) {
                    opciones.add(opcion.trim());
                }
                return opciones;

            case LIBRE:
                return valor;

            default:
                return valor;
        }
    }

    // ========== RESULTADO ==========

    /**
     * Resultado de una importación de CSV a objetos de dominio.
     * <p>
     * Contiene:
     * <ul>
     *   <li>La {@link Encuesta} creada.</li>
     *   <li>La lista de {@link UsuarioRespondedor} creados o procesados.</li>
     *   <li>Las respuestas convertidas por usuario, indexadas por (idUsuario -&gt; (idPregunta -&gt; valor)).</li>
     * </ul>
     */
    public static class ResultadoImportacion {
        public final Encuesta encuesta;
        public final List<UsuarioRespondedor> usuariosCreados;
        public final Map<String, Map<String, Object>> respuestasPorUsuario;

        /**
         * Crea un resultado de importación.
         *
         * @param encuesta   encuesta creada durante la importación
         * @param usuarios   usuarios creados/procesados
         * @param respuestas respuestas convertidas por usuario
         */
        public ResultadoImportacion(
                Encuesta encuesta,
                List<UsuarioRespondedor> usuarios,
                Map<String, Map<String, Object>> respuestas) {

            this.encuesta = encuesta;
            this.usuariosCreados = usuarios;
            this.respuestasPorUsuario = respuestas;
        }
    }
}
