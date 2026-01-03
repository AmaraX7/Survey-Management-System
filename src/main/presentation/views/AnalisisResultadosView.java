package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.presentation.controllers.CtrlPresentacion;

import java.util.List;
import java.util.Map;

/**
 * Vista para analizar los resultados de una encuesta.
 * <p>
 * Casos de uso incluidos:
 * - Ver respuestas de encuesta
 * - Ver estadísticas
 * - Ejecutar clustering
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Visualizar estadísticas generales de la encuesta.</li>
 *   <li>Explorar respuestas individuales por pregunta.</li>
 *   <li>Generar gráficos y resúmenes de datos.</li>
 * </ul>
 */
public class AnalisisResultadosView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private ComboBox<EncuestaItem> cmbEncuestas;

    public AnalisisResultadosView(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
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
        root.setPadding(new Insets(30));

        // Header
        VBox header = crearHeader();
        root.setTop(header);

        // Centro
        VBox centro = crearCentro();
        root.setCenter(centro);

        // Footer
        HBox footer = crearFooter();
        root.setBottom(footer);

        return new Scene(root, 900, 700);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 30, 0));

        Label titulo = new Label("Análisis y Resultados");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Visualiza y analiza los resultados de tus encuestas que hayan sido respondidas por mínimo 1 persona");
        subtitulo.setFont(Font.font("Arial", 14));
        subtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(titulo, subtitulo);
        return header;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearCentro() {
        VBox contenedor = new VBox(20);
        contenedor.setAlignment(Pos.CENTER);

        // Panel de selección
        VBox panelSeleccion = crearPanelSeleccion();

        // Panel de opciones
        VBox panelOpciones = crearPanelOpciones();

        contenedor.getChildren().addAll(panelSeleccion, panelOpciones);

        return contenedor;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelSeleccion() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        panel.setMaxWidth(600);

        Label lblTitulo = new Label("Seleccionar Encuesta");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        cmbEncuestas = new ComboBox<>();
        cmbEncuestas.setPromptText("Seleccione una encuesta para analizar...");
        cmbEncuestas.setPrefWidth(500);
        cmbEncuestas.setMaxWidth(500);
        cargarEncuestas();

        // Info de la encuesta seleccionada
        Label lblInfo = new Label("");
        lblInfo.setFont(Font.font("Arial", 12));
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");
        lblInfo.setWrapText(true);

        cmbEncuestas.setOnAction(e -> {
            EncuestaItem item = cmbEncuestas.getValue();
            if (item != null) {
                int numRespuestas = ctrl.obtenerNumeroRespuestas(item.id);
                lblInfo.setText(item.numPreguntas + " preguntas | " + numRespuestas + " respuestas");
            }
        });

        panel.getChildren().addAll(lblTitulo, cmbEncuestas, lblInfo);
        return panel;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelOpciones() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        panel.setMaxWidth(600);

        Label lblTitulo = new Label("¿Qué deseas hacer?");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        // Opciones
        VBox opciones = new VBox(12);

        // CU: Ver respuestas de encuesta
        Button btnRespuestas = crearBotonOpcion(
                "Ver Respuestas Detalladas",
                "Visualiza todas las respuestas de cada usuario",
                "#27ae60"
        );
        btnRespuestas.setOnAction(e -> {
            EncuestaItem item = cmbEncuestas.getValue();
            if (item != null) {
                ctrl.mostrarVerRespuestas(item.id);
            } else {
                mostrarError("Seleccione una encuesta");
            }
        });

        // CU: Ver estadísticas
        Button btnEstadisticas = crearBotonOpcion(
                "Ver Estadísticas",
                "Gráficos y resúmenes estadísticos",
                "#3498db"
        );
        btnEstadisticas.setOnAction(e -> {
            EncuestaItem item = cmbEncuestas.getValue();
            if (item != null) {
                ctrl.mostrarEstadisticas(item.id);
            } else {
                mostrarError("Seleccione una encuesta");
            }
        });

        // CU: Ejecutar clustering
        Button btnClustering = crearBotonOpcion(
                "Análisis de Clustering",
                "Agrupa respuestas similares con K-Means, K-Means++ o K-Medoids",
                "#9b59b6"
        );
        btnClustering.setOnAction(e -> {
            EncuestaItem item = cmbEncuestas.getValue();
            if (item != null) {
                ctrl.mostrarClustering(item.id);
            } else {
                mostrarError("Seleccione una encuesta");
            }
        });

        opciones.getChildren().addAll(btnRespuestas, btnEstadisticas, btnClustering);

        panel.getChildren().addAll(lblTitulo, opciones);
        return panel;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param titulo parámetro de entrada.
 * @param descripcion parámetro de entrada.
 * @param color parámetro de entrada.
 * @return resultado de la operación.
 */
    private Button crearBotonOpcion(String titulo, String descripcion, String color) {
        VBox contenido = new VBox(5);
        contenido.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblTitulo.setStyle("-fx-text-fill: white;");

        Label lblDesc = new Label(descripcion);
        lblDesc.setFont(Font.font("Arial", 11));
        lblDesc.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
        lblDesc.setWrapText(true);

        contenido.getChildren().addAll(lblTitulo, lblDesc);

        Button btn = new Button();
        btn.setGraphic(contenido);
        btn.setPrefWidth(550);
        btn.setPrefHeight(70);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + color + ", -10%);" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-scale-x: 1.02;" +
                        "-fx-scale-y: 1.02;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void cargarEncuestas() {
        cmbEncuestas.getItems().clear();

        List<Map<String, Object>> encuestas = ctrl.listarEncuestas();

        for (Map<String, Object> datosEncuesta : encuestas) {
            String id = (String) datosEncuesta.get("id");
            String titulo = (String) datosEncuesta.get("titulo");
            Integer numPreguntas = (Integer) datosEncuesta.get("numPreguntas");

            int numRespuestas = ctrl.obtenerNumeroRespuestas(id);
            if (numRespuestas > 0) {
                cmbEncuestas.getItems().add(new EncuestaItem(id, titulo, numPreguntas));
            }
        }

        if (cmbEncuestas.getItems().isEmpty()) {
            Label lblVacio = new Label("No hay encuestas con respuestas para analizar");
            lblVacio.setFont(Font.font("Arial", 14));
            lblVacio.setStyle("-fx-text-fill: #e67e22;");
        }
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private HBox crearFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));

        Button btnVolver = new Button("← Volver al Menú");
        btnVolver.setPrefWidth(180);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), true));

        footer.getChildren().add(btnVolver);
        return footer;
    }

/**
 * Muestra la información indicada en la vista.
 *
 * @param mensaje parámetro de entrada.
 */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }

    private static class EncuestaItem {
        final String id;
        final String titulo;
        final int numPreguntas;

        EncuestaItem(String id, String titulo, int numPreguntas) {
            this.id = id;
            this.titulo = titulo;
            this.numPreguntas = numPreguntas;
        }

        @Override
/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @return resultado de la operación.
 */
        public String toString() {
            return titulo;
        }
    }
}
