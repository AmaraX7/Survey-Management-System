package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import main.presentation.controllers.CtrlPresentacion;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Vista para exportar encuestas a CSV.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Seleccionar la encuesta a exportar.</li>
 *   <li>Configurar el destino del archivo CSV.</li>
 *   <li>Ejecutar la exportación de respuestas.</li>
 * </ul>
 */
public class ExportarCSVView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

    private ComboBox<EncuestaItem> cmbEncuestas;
    private TextField txtRuta;
    private Label lblInfo;
    private Button btnExportar;

    public ExportarCSVView(CtrlPresentacion ctrl) {
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

        VBox header = crearHeader();
        root.setTop(header);

        VBox centro = crearFormulario();
        root.setCenter(centro);

        HBox footer = crearFooter();
        root.setBottom(footer);

        return new Scene(root, 900, 650);
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

        Label titulo = new Label("Exportar Encuesta a CSV");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Exporte los datos de una encuesta a formato CSV");
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
    private VBox crearFormulario() {
        VBox contenedor = new VBox(20);
        contenedor.setAlignment(Pos.CENTER);

        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        form.setMaxWidth(650);

        VBox instrucciones = crearInstrucciones();
        form.getChildren().add(instrucciones);

        Separator sep = new Separator();
        form.getChildren().add(sep);

        Label lblEncuesta = new Label("Seleccione la encuesta a exportar:");
        lblEncuesta.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        cmbEncuestas = new ComboBox<>();
        cmbEncuestas.setPrefWidth(500);
        cmbEncuestas.setPromptText("Seleccione una encuesta...");
        cargarEncuestas();

        cmbEncuestas.setOnAction(e -> actualizarInformacion());

        lblInfo = new Label("");
        lblInfo.setFont(Font.font("Arial", 13));
        lblInfo.setStyle("-fx-text-fill: #34495e;");
        lblInfo.setWrapText(true);

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle(
                "-fx-background-color: #ecf0f1;" +
                        "-fx-background-radius: 5;"
        );
        infoBox.getChildren().add(lblInfo);

        Label lblDestino = new Label("Guardar como:");
        lblDestino.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox selectorDestino = new HBox(10);
        selectorDestino.setAlignment(Pos.CENTER_LEFT);

        txtRuta = new TextField();
        txtRuta.setPromptText("Seleccione ubicación para guardar...");
        txtRuta.setEditable(false);
        txtRuta.setPrefWidth(420);

        Button btnSeleccionar = new Button("Examinar");
        btnSeleccionar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnSeleccionar.setOnAction(e -> seleccionarDestino());

        selectorDestino.getChildren().addAll(txtRuta, btnSeleccionar);

        form.getChildren().addAll(
                lblEncuesta, cmbEncuestas,
                infoBox,
                lblDestino, selectorDestino
        );

        contenedor.getChildren().add(form);
        return contenedor;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearInstrucciones() {
        VBox instrucciones = new VBox(8);
        instrucciones.setPadding(new Insets(10));
        instrucciones.setStyle(
                "-fx-background-color: #d5f4e6;" +
                        "-fx-background-radius: 5;"
        );

        Label lblTitulo = new Label("Formato de exportación:");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblTitulo.setStyle("-fx-text-fill: #27ae60;");

        Label instr1 = new Label("• Primera fila: nombres de columnas (ID_Usuario + preguntas)");
        Label instr2 = new Label("• Una fila por usuario que respondió la encuesta");
        Label instr3 = new Label("• Compatible para re-importación");

        for (Label lbl : new Label[]{instr1, instr2, instr3}) {
            lbl.setFont(Font.font("Arial", 12));
            lbl.setStyle("-fx-text-fill: #27ae60;");
        }

        instrucciones.getChildren().addAll(lblTitulo, instr1, instr2, instr3);
        return instrucciones;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void cargarEncuestas() {
        cmbEncuestas.getItems().clear();

        List<Map<String, Object>> encuestas = ctrl.listarEncuestasParaExportar();

        for (Map<String, Object> datos : encuestas) {
            cmbEncuestas.getItems().add(new EncuestaItem(datos));
        }

        if (cmbEncuestas.getItems().isEmpty()) {
            lblInfo.setText("No hay encuestas con respuestas disponibles para exportar");
            lblInfo.setStyle("-fx-text-fill: #e67e22;");
        }
    }

/**
 * Actualiza el contenido mostrado en la vista.
 */
    private void actualizarInformacion() {
        EncuestaItem item = cmbEncuestas.getValue();

        if (item == null) {
            lblInfo.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Encuesta: ").append(item.titulo).append("\n");
        sb.append("Preguntas: ").append(item.numPreguntas).append("\n");
        sb.append("Usuarios que respondieron: ").append(item.numUsuarios).append("\n");
        sb.append("Archivo resultante: ").append(item.numUsuarios).append(" filas x ");
        sb.append(item.numPreguntas + 1).append(" columnas");

        lblInfo.setText(sb.toString());
        lblInfo.setStyle("-fx-text-fill: #2c3e50;");

        if (txtRuta.getText().isEmpty()) {
            String nombreSugerido = item.titulo
                    .replaceAll("[^a-zA-Z0-9]", "_")
                    .toLowerCase() + ".csv";
            txtRuta.setText(nombreSugerido);
        }
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void seleccionarDestino() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar archivo CSV");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        EncuestaItem item = cmbEncuestas.getValue();
        if (item != null) {
            String nombreSugerido = item.titulo
                    .replaceAll("[^a-zA-Z0-9]", "_")
                    .toLowerCase() + ".csv";
            fileChooser.setInitialFileName(nombreSugerido);
        }

        File archivo = fileChooser.showSaveDialog(ctrl.getPrimaryStage());

        if (archivo != null) {
            String ruta = archivo.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".csv")) {
                ruta += ".csv";
            }
            txtRuta.setText(ruta);
        }
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void exportarCSV() {
        EncuestaItem item = cmbEncuestas.getValue();
        String ruta = txtRuta.getText().trim();

        if (item == null) {
            mostrarError("Debe seleccionar una encuesta");
            return;
        }

        if (ruta.isEmpty()) {
            mostrarError("Debe especificar un archivo de destino");
            return;
        }

        btnExportar.setDisable(true);
        btnExportar.setText("Exportando...");

        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("Exportando CSV");
        progress.setHeaderText("Por favor espere...");
        progress.setContentText("Generando archivo CSV...");
        progress.show();

        new Thread(() -> {
            try {
                ctrl.exportarCSV(item.id, ruta);

                javafx.application.Platform.runLater(() -> {
                    progress.close();
                    btnExportar.setDisable(false);
                    btnExportar.setText("Exportar");

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Exportación Exitosa");
                    success.setHeaderText("✓ CSV exportado correctamente");
                    success.setContentText(
                            "Archivo: " + ruta + "\n\n" +
                                    "Preguntas: " + item.numPreguntas + "\n" +
                                    "Respuestas: " + item.numUsuarios
                    );

                    ButtonType btnAbrir = new ButtonType("Abrir Ubicación");
                    ButtonType btnOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

                    success.getButtonTypes().setAll(btnAbrir, btnOk);

                    success.showAndWait().ifPresent(response -> {
                        if (response == btnAbrir) {
                            try {
                                File archivo = new File(ruta);
                                java.awt.Desktop.getDesktop().open(archivo.getParentFile());
                            } catch (Exception e) {
                                mostrarError("No se pudo abrir la ubicación: " + e.getMessage());
                            }
                        }
                    });
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    progress.close();
                    btnExportar.setDisable(false);
                    btnExportar.setText("Exportar");

                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error de Exportación");
                    error.setHeaderText("No se pudo exportar el CSV");
                    error.setContentText(e.getMessage());

                    TextArea txtError = new TextArea(e.toString());
                    txtError.setEditable(false);
                    txtError.setWrapText(true);
                    txtError.setPrefRowCount(10);

                    error.getDialogPane().setExpandableContent(txtError);
                    error.showAndWait();

                    e.printStackTrace();
                });
            }
        }).start();
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

        btnExportar = new Button("Exportar a CSV");
        btnExportar.setPrefWidth(200);
        btnExportar.setPrefHeight(45);
        btnExportar.setStyle(
                "-fx-background-color: #2ecc71;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-font-weight: bold;"
        );
        btnExportar.setOnAction(e -> exportarCSV());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(150);
        btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnCancelar.setOnAction(e -> ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), true));

        footer.getChildren().addAll(btnExportar, btnCancelar);
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
        final int numUsuarios;

        EncuestaItem(Map<String, Object> datos) {
            this.id = (String) datos.get("id");
            this.titulo = (String) datos.get("titulo");
            this.numPreguntas = (Integer) datos.get("numPreguntas");
            this.numUsuarios = (Integer) datos.get("numUsuarios");
        }

        @Override
/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @return resultado de la operación.
 */
        public String toString() {
            return String.format("%s (%d preguntas, %d respuestas)",
                    titulo, numPreguntas, numUsuarios);
        }
    }
}
