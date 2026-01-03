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

/**
 * Vista para importar encuestas desde CSV.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Seleccionar el archivo CSV a importar.</li>
 *   <li>Configurar el título y descripción de la nueva encuesta.</li>
 *   <li>Ejecutar la importación y crear la encuesta.</li>
 * </ul>
 */
public class ImportarCSVView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

    private TextField txtRuta;
    private TextField txtTitulo;
    private TextArea txtDescripcion;
    private TextArea txtVistaPrevia;
    private Button btnImportar;

/**
 * Crea una instancia de la vista.
 *
 * @param ctrl parámetro de entrada.
 */
    public ImportarCSVView(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        this.scene = crearEscena();
    }

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
        header.setPadding(new Insets(0, 0, 20, 0));

        Label titulo = new Label("Importar Encuesta desde CSV");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Seleccione un archivo CSV con el formato correcto");
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
        contenedor.setAlignment(Pos.TOP_CENTER);

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        form.setMaxWidth(700);

        VBox instrucciones = crearInstrucciones();
        form.getChildren().add(instrucciones);

        Separator sep1 = new Separator();
        form.getChildren().add(sep1);

        // Selección de archivo
        Label lblArchivo = new Label("Archivo CSV:");
        lblArchivo.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox selectorArchivo = new HBox(10);
        selectorArchivo.setAlignment(Pos.CENTER_LEFT);

        txtRuta = new TextField();
        txtRuta.setPromptText("Seleccione un archivo...");
        txtRuta.setEditable(false);
        txtRuta.setPrefWidth(450);

        Button btnSeleccionar = new Button("Examinar");
        btnSeleccionar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnSeleccionar.setOnAction(e -> seleccionarArchivo());

        Button btnVistaPrevia = new Button("Vista Previa");
        btnVistaPrevia.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        btnVistaPrevia.setOnAction(e -> mostrarVistaPrevia());

        selectorArchivo.getChildren().addAll(txtRuta, btnSeleccionar, btnVistaPrevia);

        // Título de la encuesta
        Label lblTitulo = new Label("Título de la encuesta:");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        txtTitulo = new TextField();
        txtTitulo.setPromptText("Ej: Dataset Importado 2024");
        txtTitulo.setText("Dataset Importado");

        // Descripción
        Label lblDescripcion = new Label("Descripción:");
        lblDescripcion.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción de la encuesta...");
        txtDescripcion.setText("Importado desde archivo CSV");
        txtDescripcion.setPrefRowCount(3);
        txtDescripcion.setWrapText(true);

        form.getChildren().addAll(
                lblArchivo, selectorArchivo,
                lblTitulo, txtTitulo,
                lblDescripcion, txtDescripcion
        );

        VBox panelVistaPrevia = crearPanelVistaPrevia();

        contenedor.getChildren().addAll(form, panelVistaPrevia);

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setPadding(Insets.EMPTY);

        VBox wrapper = new VBox(scroll);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setFillWidth(true);

        return wrapper;
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
                "-fx-background-color: #ecf0f1;" +
                        "-fx-background-radius: 5;"
        );

        Label lblTitulo = new Label("Formato del archivo CSV:");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        Label instr1 = new Label("• Primera fila: nombres de las columnas");
        Label instr2 = new Label("• Primera columna: ID de usuario");
        Label instr3 = new Label("• Resto de columnas: respuestas (tipos inferidos automáticamente)");
        Label instr4 = new Label("• Separador: coma (,)");

        for (Label lbl : new Label[]{instr1, instr2, instr3, instr4}) {
            lbl.setFont(Font.font("Arial", 12));
            lbl.setStyle("-fx-text-fill: #34495e;");
        }

        instrucciones.getChildren().addAll(lblTitulo, instr1, instr2, instr3, instr4);
        return instrucciones;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelVistaPrevia() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );
        panel.setMaxWidth(700);

        Label lblTitulo = new Label("Vista Previa del Archivo:");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        txtVistaPrevia = new TextArea();
        txtVistaPrevia.setEditable(false);
        txtVistaPrevia.setPrefRowCount(8);
        txtVistaPrevia.setWrapText(false);
        txtVistaPrevia.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11;");
        txtVistaPrevia.setText("Seleccione un archivo para ver su contenido...");

        panel.getChildren().addAll(lblTitulo, txtVistaPrevia);
        return panel;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void seleccionarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        File archivo = fileChooser.showOpenDialog(ctrl.getPrimaryStage());

        if (archivo != null) {
            txtRuta.setText(archivo.getAbsolutePath());

            if (txtTitulo.getText().equals("Dataset Importado")) {
                String nombreArchivo = archivo.getName().replace(".csv", "");
                txtTitulo.setText(nombreArchivo);
            }
        }
    }

/**
 * Muestra la información indicada en la vista.
 */
    private void mostrarVistaPrevia() {
        String ruta = txtRuta.getText().trim();

        if (ruta.isEmpty()) {
            mostrarError("Primero seleccione un archivo");
            return;
        }

        String vistaPrevia = ctrl.obtenerVistaPreviaCSV(ruta);
        txtVistaPrevia.setText(vistaPrevia);
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void importarCSV() {
        String ruta = txtRuta.getText().trim();
        String titulo = txtTitulo.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (ruta.isEmpty()) {
            mostrarError("Debe seleccionar un archivo CSV");
            return;
        }

        String validacion = ctrl.validarArchivoCSV(ruta);
        if (!validacion.equals("OK")) {
            mostrarError(validacion);
            return;
        }

        if (titulo.isEmpty()) {
            mostrarError("Debe ingresar un título para la encuesta");
            txtTitulo.requestFocus();
            return;
        }

        if (titulo.length() < 3) {
            mostrarError("El título debe tener al menos 3 caracteres");
            txtTitulo.requestFocus();
            return;
        }

        if (titulo.length() > 200) {
            mostrarError("El título no puede exceder 200 caracteres");
            txtTitulo.requestFocus();
            return;
        }

        btnImportar.setDisable(true);
        btnImportar.setText("Importando...");

        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("Importando CSV");
        progress.setHeaderText("Por favor espere...");
        progress.setContentText("Leyendo y procesando archivo CSV");
        progress.show();

        // Ejecutar importación en hilo separado
        new Thread(() -> {
            try {
                String idEncuesta = ctrl.importarCSV(ruta, titulo, descripcion);

                javafx.application.Platform.runLater(() -> {
                    progress.close();
                    btnImportar.setDisable(false);
                    btnImportar.setText("Importar");

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Importación Exitosa");
                    success.setHeaderText("✓ CSV importado correctamente");
                    success.setContentText(
                            "Encuesta creada: " + titulo + "\n" +
                                    "ID: " + idEncuesta
                    );

                    ButtonType btnVer = new ButtonType("Ver Encuesta");
                    ButtonType btnVolver = new ButtonType("Volver al Menú");

                    success.getButtonTypes().setAll(btnVer, btnVolver);

                    success.showAndWait().ifPresent(response -> {
                        if (response == btnVer) {
                            ctrl.mostrarDetalleEncuesta(idEncuesta);
                        } else {
                            ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), true);
                        }
                    });
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    progress.close();
                    btnImportar.setDisable(false);
                    btnImportar.setText("Importar");

                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error de Importación");
                    error.setHeaderText("No se pudo importar el CSV");
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

        btnImportar = new Button("Importar CSV");
        btnImportar.setPrefWidth(200);
        btnImportar.setPrefHeight(45);
        btnImportar.setStyle(
                "-fx-background-color: #2ecc71;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-font-weight: bold;"
        );
        btnImportar.setOnAction(e -> importarCSV());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(150);
        btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnCancelar.setOnAction(e -> ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), true));

        footer.getChildren().addAll(btnImportar, btnCancelar);
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
}
