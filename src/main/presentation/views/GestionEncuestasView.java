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
 * Vista para gestión de encuestas.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Crear nuevas encuestas.</li>
 *   <li>Gestionar encuestas existentes.</li>
 *   <li>Eliminar o limpiar preguntas de encuestas.</li>
 * </ul>
 */
public class GestionEncuestasView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private ComboBox<EncuestaItem> cmbEncuestas;

    public GestionEncuestasView(CtrlPresentacion ctrl) {
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

        // Centro: Opciones
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

        Label titulo = new Label("Gestión de Encuestas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Administra tus encuestas");
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
        VBox contenedor = new VBox(25);
        contenedor.setAlignment(Pos.CENTER);

        VBox panelCrear = crearPanelCrear();
        VBox panelGestionar = crearPanelGestionar();

        contenedor.getChildren().addAll(panelCrear, panelGestionar);

        return contenedor;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelCrear() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        panel.setMaxWidth(600);

        Label lblTitulo = new Label("Crear Nueva Encuesta");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        Label lblDesc = new Label("Crea una nueva encuesta desde cero");
        lblDesc.setFont(Font.font("Arial", 13));
        lblDesc.setStyle("-fx-text-fill: #7f8c8d;");

        Button btnCrear = new Button("Crear Encuesta");
        btnCrear.setPrefWidth(250);
        btnCrear.setPrefHeight(50);
        btnCrear.setStyle(
                "-fx-background-color: #2ecc71;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;"
        );
        btnCrear.setOnAction(e -> ctrl.mostrarCrearEncuesta());

        panel.getChildren().addAll(lblTitulo, lblDesc, btnCrear);
        return panel;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelGestionar() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        panel.setMaxWidth(600);

        Label lblTitulo = new Label("Gestionar Encuesta Existente");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        Label lblDesc = new Label("Selecciona una encuesta para modificar, eliminar o limpiar");
        lblDesc.setFont(Font.font("Arial", 13));
        lblDesc.setStyle("-fx-text-fill: #7f8c8d;");

        // Selector de encuesta
        Label lblSeleccionar = new Label("Seleccionar encuesta:");
        lblSeleccionar.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        cmbEncuestas = new ComboBox<>();
        cmbEncuestas.setPromptText("Seleccione una encuesta...");
        cmbEncuestas.setPrefWidth(500);
        cmbEncuestas.setMaxWidth(500);

        // Configurar cómo se muestran los elementos en el dropdown
        cmbEncuestas.setCellFactory(param -> new ListCell<EncuestaItem>() {
            @Override
/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param item parámetro de entrada.
 * @param empty parámetro de entrada.
 */
            protected void updateItem(EncuestaItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Configurar cómo se muestra el elemento seleccionado
        cmbEncuestas.setButtonCell(new ListCell<EncuestaItem>() {
            @Override
/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param item parámetro de entrada.
 * @param empty parámetro de entrada.
 */
            protected void updateItem(EncuestaItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        cargarEncuestas();

        // Botones de acción
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);

        Button btnModificar = new Button("Modificar");
        btnModificar.setPrefWidth(150);
        btnModificar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnModificar.setOnAction(e -> {
            EncuestaItem item = cmbEncuestas.getValue();
            if (item != null) {
                ctrl.mostrarGestionarEncuesta(item.id);
            } else {
                mostrarError("Seleccione una encuesta");
            }
        });

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setPrefWidth(150);
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEliminar.setOnAction(e -> eliminarEncuesta());

        Button btnLimpiar = new Button("Limpiar Preguntas");
        btnLimpiar.setPrefWidth(180);
        btnLimpiar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLimpiar.setOnAction(e -> limpiarPreguntas());

        botones.getChildren().addAll(btnModificar, btnEliminar, btnLimpiar);

        panel.getChildren().addAll(lblTitulo, lblDesc, lblSeleccionar, cmbEncuestas, botones);
        return panel;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void cargarEncuestas() {
        cmbEncuestas.getItems().clear();

        List<Map<String, Object>> encuestas = ctrl.listarEncuestas();

        for (Map<String, Object> datos : encuestas) {
            String id = (String) datos.get("id");
            String titulo = (String) datos.get("titulo");
            Integer numPreguntas = (Integer) datos.get("numPreguntas");

            cmbEncuestas.getItems().add(new EncuestaItem(id, titulo, numPreguntas));
        }
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void eliminarEncuesta() {
        EncuestaItem item = cmbEncuestas.getValue();
        if (item == null) {
            mostrarError("Seleccione una encuesta");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar encuesta?");
        confirm.setContentText("Se eliminará: " + item.titulo + "\n\nEsta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = ctrl.eliminarEncuesta(item.id);
                if (ok) {
                    mostrarInfo("Encuesta eliminada exitosamente");
                    cargarEncuestas();
                } else {
                    mostrarError("Error al eliminar la encuesta");
                }
            }
        });
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void limpiarPreguntas() {
        EncuestaItem item = cmbEncuestas.getValue();
        if (item == null) {
            mostrarError("Seleccione una encuesta");
            return;
        }

        if (item.numPreguntas == 0) {
            mostrarInfo("La encuesta no tiene preguntas para limpiar");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar limpieza");
        confirm.setHeaderText("¿Limpiar todas las preguntas?");
        confirm.setContentText("Se eliminarán " + item.numPreguntas + " preguntas de:\n" +
                item.titulo + "\n\nEsta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    for (int i = item.numPreguntas - 1; i >= 0; i--) {
                        ctrl.eliminarPregunta(item.id, i);
                    }
                    mostrarInfo("Todas las preguntas han sido eliminadas");
                    cargarEncuestas();
                } catch (Exception e) {
                    mostrarError("Error: " + e.getMessage());
                }
            }
        });
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

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

/**
 * Devuelve la escena asociada a esta vista.
 *
 * @return resultado de la operación.
 */
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
            return titulo + " (" + numPreguntas + " preguntas)";
        }
    }
}
