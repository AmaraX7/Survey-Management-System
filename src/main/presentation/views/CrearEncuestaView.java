package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.presentation.controllers.CtrlPresentacion;

/**
 * Vista para crear una nueva encuesta.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Ingresar el título y descripción de la encuesta.</li>
 *   <li>Guardar la nueva encuesta en el sistema.</li>
 * </ul>
 */
public class CrearEncuestaView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

    private TextField txtTitulo;
    private TextArea txtDescripcion;

    public CrearEncuestaView(CtrlPresentacion ctrl) {
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

        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 30, 0));

        Label titulo = new Label("Crear nueva encuesta");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        header.getChildren().add(titulo);
        root.setTop(header);

        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.TOP_LEFT);
        formContainer.setPadding(new Insets(20));
        formContainer.setMaxWidth(600);
        formContainer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label lblTitulo = new Label("Título de la encuesta:");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        txtTitulo = new TextField();
        txtTitulo.setPromptText("Ej: Encuesta de satisfacción 2024");
        txtTitulo.setFont(Font.font("Arial", 14));
        txtTitulo.setPrefHeight(40);

        Label lblDescripcion = new Label("Descripción:");
        lblDescripcion.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Describe el propósito de la encuesta...");
        txtDescripcion.setFont(Font.font("Arial", 14));
        txtDescripcion.setPrefRowCount(4);
        txtDescripcion.setWrapText(true);

        formContainer.getChildren().addAll(
                lblTitulo, txtTitulo,
                lblDescripcion, txtDescripcion
        );

        VBox centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.getChildren().add(formContainer);
        root.setCenter(centerContainer);

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(30, 0, 0, 0));

        Button btnCrear = new Button("Crear Encuesta");
        btnCrear.setPrefWidth(180);
        btnCrear.setPrefHeight(40);
        btnCrear.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        btnCrear.setOnAction(e -> crearEncuesta());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(180);
        btnCancelar.setPrefHeight(40);
        btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnCancelar.setOnAction(e -> ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), true));

        footer.getChildren().addAll(btnCrear, btnCancelar);
        root.setBottom(footer);

        return new Scene(root, 800, 600);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 */
    private void crearEncuesta() {
        String titulo = txtTitulo.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (titulo.isEmpty()) {
            mostrarError("El título no puede estar vacío");
            txtTitulo.requestFocus();
            return;
        }

        if (titulo.length() < 3) {
            mostrarError("El título debe tener al menos 3 caracteres");
            txtTitulo.requestFocus();
            return;
        }

        if (titulo.length() > 200) {
            mostrarError("El título no puede exceder 200 caracteres\n" +
                    "Caracteres actuales: " + titulo.length());
            txtTitulo.requestFocus();
            return;
        }

        if (descripcion.length() > 1000) {
            mostrarError("La descripción no puede exceder 1000 caracteres\n" +
                    "Caracteres actuales: " + descripcion.length());
            txtDescripcion.requestFocus();
            return;
        }

        try {
            String idEncuesta = ctrl.crearEncuesta(titulo, descripcion);

            if (idEncuesta != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Encuesta Creada");
                alert.setHeaderText("Encuesta creada exitosamente");
                alert.setContentText("ID: " + idEncuesta + "\n\n¿Deseas añadir preguntas ahora?");

                ButtonType btnSi = new ButtonType("Sí, añadir preguntas");
                ButtonType btnNo = new ButtonType("No, volver al menú");

                alert.getButtonTypes().setAll(btnSi, btnNo);

                alert.showAndWait().ifPresent(response -> {
                    if (response == btnSi) {
                        ctrl.mostrarGestionarPreguntas(idEncuesta);
                    } else {
                        ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), true);
                    }
                });
            } else {
                mostrarError("Error al crear la encuesta");
            }

        } catch (Exception e) {
            mostrarError("Error al crear encuesta: " + e.getMessage());
        }
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
