package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.presentation.controllers.CtrlPresentacion;

import java.util.*;

/**
 * Vista para mostrar estadísticas de una encuesta.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Presentar métricas generales como número de respuestas.</li>
 *   <li>Mostrar estadísticas por pregunta.</li>
 *   <li>Visualizar distribuciones de respuestas.</li>
 * </ul>
 */
public class EstadisticasView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final String idEncuesta;

    public EstadisticasView(CtrlPresentacion ctrl, String idEncuesta) {
        this.ctrl = ctrl;
        this.idEncuesta = idEncuesta;
        this.scene = crearEscena();
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private Scene crearEscena() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setPadding(new Insets(20));

        Map<String, Object> stats = ctrl.obtenerEstadisticasEncuesta(idEncuesta);

        if (stats == null) {
            return crearEscenaError("Encuesta no encontrada");
        }

        // Header
        VBox header = crearHeader(stats);
        root.setTop(header);

        // Centro: Estadísticas
        ScrollPane scrollPane = crearEstadisticas(stats);
        root.setCenter(scrollPane);

        // Footer: Botones
        HBox footer = crearFooter();
        root.setBottom(footer);

        return new Scene(root, 1000, 700);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param stats parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearHeader(Map<String, Object> stats) {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 0, 20, 0));
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        header.setPadding(new Insets(20));

        Label titulo = new Label("Estadísticas de Encuesta");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label((String) stats.get("titulo"));
        subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(titulo, subtitulo);
        return header;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param stats parámetro de entrada.
 * @return resultado de la operación.
 */
    private ScrollPane crearEstadisticas(Map<String, Object> stats) {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(20));
        contenedor.setAlignment(Pos.TOP_CENTER);

        // Resumen general
        VBox resumen = crearResumenGeneral(stats);
        contenedor.getChildren().add(resumen);

        Map<String, Object> datosEncuesta = ctrl.obtenerDetalleEncuesta(idEncuesta);
        Map<String, Object> datosRespuestas = ctrl.obtenerDatosEncuesta(idEncuesta);

        if (datosEncuesta != null && datosRespuestas != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> preguntas =
                    (List<Map<String, String>>) datosEncuesta.get("preguntas");

            if (preguntas != null && !preguntas.isEmpty()) {
                Label lblTitulo = new Label("Estadísticas por Pregunta:");
                lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                lblTitulo.setStyle("-fx-text-fill: #2c3e50;");
                contenedor.getChildren().add(lblTitulo);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> usuarios =
                        (List<Map<String, Object>>) datosRespuestas.get("usuarios");

                for (int i = 0; i < preguntas.size(); i++) {
                    Map<String, String> pregunta = preguntas.get(i);
                    VBox cardEstadistica = crearEstadisticaPregunta(pregunta, i + 1, usuarios);
                    contenedor.getChildren().add(cardEstadistica);
                }
            }
        }

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        return scroll;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param stats parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearResumenGeneral(Map<String, Object> stats) {
        VBox resumen = new VBox(15);
        resumen.setPadding(new Insets(20));
        resumen.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        resumen.setMaxWidth(800);

        Label lblTitulo = new Label("Resumen General");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        HBox infoBox = new HBox(40);
        infoBox.setAlignment(Pos.CENTER);

        // Tarjeta preguntas
        VBox cardPreguntas = crearTarjetaInfo(
                "Preguntas",
                stats.get("numPreguntas").toString(),
                "#3498db"
        );

        // Tarjeta respuestas
        VBox cardRespuestas = crearTarjetaInfo(
                "Respuestas",
                stats.get("numRespuestas").toString(),
                "#2ecc71"
        );

        infoBox.getChildren().addAll(cardPreguntas, cardRespuestas);

        resumen.getChildren().addAll(lblTitulo, infoBox);
        return resumen;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param titulo parámetro de entrada.
 * @param valor parámetro de entrada.
 * @param color parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearTarjetaInfo(String titulo, String valor, String color) {
        VBox tarjeta = new VBox(5);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 8;"
        );
        tarjeta.setPrefWidth(150);

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        lblValor.setStyle("-fx-text-fill: white;");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        lblTitulo.setStyle("-fx-text-fill: white;");

        tarjeta.getChildren().addAll(lblValor, lblTitulo);
        return tarjeta;
    }

    private VBox crearEstadisticaPregunta(Map<String, String> pregunta, int numero,
                                          List<Map<String, Object>> usuarios) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );
        card.setMaxWidth(800);

        String enunciado = pregunta.get("enunciado");
        String tipo = pregunta.get("tipo");

        // Encabezado
        Label lblNumero = new Label(numero + ". " + enunciado);
        lblNumero.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lblNumero.setStyle("-fx-text-fill: #2c3e50;");
        lblNumero.setWrapText(true);

        // Calcular estadísticas - CORRECCIÓN: pasar el ENUNCIADO para filtrar correctamente
        Map<String, Object> stats = calcularEstadisticasPregunta(enunciado, tipo, usuarios);

        VBox contenidoStats = new VBox(8);

        switch (tipo) {
            case "NUMERICA":
                Label lblMedia = new Label("Media: " + formatearNumero((Double) stats.get("media")));
                Label lblMin = new Label("Mínimo: " + formatearNumero((Double) stats.get("min")));
                Label lblMax = new Label("Máximo: " + formatearNumero((Double) stats.get("max")));
                Label lblRespuestas = new Label("Respuestas: " + stats.get("total"));
                contenidoStats.getChildren().addAll(lblMedia, lblMin, lblMax, lblRespuestas);
                break;

            case "CATEGORIA_SIMPLE":
            case "ORDINAL":
            case "CATEGORIA_MULTIPLE": {
                @SuppressWarnings("unchecked")
                Map<String, Integer> frecuencias = (Map<String, Integer>) stats.get("frecuencias");
                Integer total = (Integer) stats.get("total");

                Label lblTotal = new Label("Total de respuestas: " + total);
                lblTotal.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                contenidoStats.getChildren().add(lblTotal);

                VBox barras = new VBox(5);
                for (Map.Entry<String, Integer> entry : frecuencias.entrySet()) {
                    HBox barra = crearBarraFrecuencia(entry.getKey(), entry.getValue(), total);
                    barras.getChildren().add(barra);
                }
                contenidoStats.getChildren().add(barras);
                break;
            }

            case "LIBRE":
                Label lblRespuestasLibre = new Label("Total de respuestas: " + stats.get("total"));
                Label lblPromedio = new Label("Longitud promedio: " +
                        formatearNumero((Double) stats.get("longitudPromedio")) + " caracteres");
                contenidoStats.getChildren().addAll(lblRespuestasLibre, lblPromedio);
                break;
        }

        card.getChildren().addAll(lblNumero, contenidoStats);
        return card;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param opcion parámetro de entrada.
 * @param frecuencia parámetro de entrada.
 * @param total parámetro de entrada.
 * @return resultado de la operación.
 */
    private HBox crearBarraFrecuencia(String opcion, int frecuencia, int total) {
        HBox barra = new HBox(10);
        barra.setAlignment(Pos.CENTER_LEFT);

        Label lblOpcion = new Label(opcion);
        lblOpcion.setFont(Font.font("Arial", 12));
        lblOpcion.setPrefWidth(150);

        double porcentaje = total > 0 ? (frecuencia * 100.0 / total) : 0;

        ProgressBar pb = new ProgressBar(porcentaje / 100.0);
        pb.setPrefWidth(300);
        pb.setPrefHeight(20);
        pb.setStyle("-fx-accent: #3498db;");

        Label lblFrecuencia = new Label(frecuencia + " (" +
                String.format("%.1f", porcentaje) + "%)");
        lblFrecuencia.setFont(Font.font("Arial", 12));
        lblFrecuencia.setStyle("-fx-text-fill: #7f8c8d;");

        barra.getChildren().addAll(lblOpcion, pb, lblFrecuencia);
        return barra;
    }

    private Map<String, Object> calcularEstadisticasPregunta(String enunciadoPregunta, String tipo,
                                                             List<Map<String, Object>> usuarios) {
        Map<String, Object> stats = new HashMap<>();
        List<String> respuestas = new ArrayList<>();

        // CORRECCIÓN: Filtrar respuestas por el enunciado de la pregunta
        for (Map<String, Object> usuario : usuarios) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> respuestasUsuario =
                    (List<Map<String, String>>) usuario.get("respuestas");

            for (Map<String, String> respuesta : respuestasUsuario) {
                String preguntaResp = respuesta.get("pregunta");
                String valorStr = respuesta.get("valor");

                // Solo procesar si coincide el enunciado de la pregunta
                if (enunciadoPregunta.equals(preguntaResp) &&
                        valorStr != null && !valorStr.equals("(Sin respuesta)")) {
                    respuestas.add(valorStr);
                }
            }
        }

        stats.put("total", respuestas.size());

        switch (tipo) {
            case "NUMERICA": {
                double suma = 0;
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;

                for (String r : respuestas) {
                    try {
                        double val = Double.parseDouble(r);
                        suma += val;
                        min = Math.min(min, val);
                        max = Math.max(max, val);
                    } catch (NumberFormatException ignored) {}
                }

                stats.put("media", respuestas.isEmpty() ? 0.0 : suma / respuestas.size());
                stats.put("min", respuestas.isEmpty() ? 0.0 : min);
                stats.put("max", respuestas.isEmpty() ? 0.0 : max);
                break;
            }

            case "CATEGORIA_SIMPLE":
            case "ORDINAL": {
                Map<String, Integer> frecuencias = new LinkedHashMap<>();
                for (String r : respuestas) {
                    frecuencias.put(r, frecuencias.getOrDefault(r, 0) + 1);
                }
                stats.put("frecuencias", frecuencias);
                break;
            }

            case "CATEGORIA_MULTIPLE": {
                Map<String, Integer> frecuencias = new LinkedHashMap<>();
                for (String r : respuestas) {
                    // Las opciones múltiples vienen como "opcion1, opcion2, ..."
                    String[] opciones = r.split(", ");
                    for (String opcion : opciones) {
                        frecuencias.put(opcion.trim(), frecuencias.getOrDefault(opcion.trim(), 0) + 1);
                    }
                }
                stats.put("frecuencias", frecuencias);
                break;
            }

            case "LIBRE": {
                double sumaLongitud = 0;
                for (String r : respuestas) {
                    sumaLongitud += r.length();
                }
                stats.put("longitudPromedio", respuestas.isEmpty() ? 0.0 : sumaLongitud / respuestas.size());
                break;
            }
        }

        return stats;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param num parámetro de entrada.
 * @return resultado de la operación.
 */
    private String formatearNumero(double num) {
        if (num == Math.floor(num)) {
            return String.valueOf((int) num);
        }
        return String.format("%.2f", num);
    }

    private HBox crearFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));

        Button btnVolver = new Button("← Volver");
        btnVolver.setPrefWidth(150);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> ctrl.mostrarAnalisisResultados());

        Button btnVerRespuestas = new Button("Ver Respuestas Detalladas");
        btnVerRespuestas.setPrefWidth(220);
        btnVerRespuestas.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14;");
        btnVerRespuestas.setOnAction(e -> ctrl.mostrarVerRespuestas(idEncuesta));

        Button btnClustering = new Button("Análisis de Clustering");
        btnClustering.setPrefWidth(200);
        btnClustering.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 14;");
        btnClustering.setOnAction(e -> ctrl.mostrarClustering(idEncuesta));

        footer.getChildren().addAll(btnVolver, btnVerRespuestas, btnClustering);
        return footer;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param mensaje parámetro de entrada.
 * @return resultado de la operación.
 */
    private Scene crearEscenaError(String mensaje) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        Label lbl = new Label(mensaje);
        Button btnVolver = new Button("← Volver");
        btnVolver.setOnAction(e -> ctrl.mostrarListarEncuestas());
        root.getChildren().addAll(lbl, btnVolver);
        return new Scene(root, 800, 600);
    }

    public Scene getScene() {
        return scene;
    }
}
