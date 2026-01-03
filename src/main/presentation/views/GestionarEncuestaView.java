package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.presentation.controllers.CtrlPresentacion;
import java.util.Map;

/**
 * Vista para gestionar una encuesta.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Editar el título y descripción de la encuesta.</li>
 *   <li>Eliminar la encuesta.</li>
 *   <li>Limpiar todas las preguntas de la encuesta.</li>
 * </ul>
 */
public class GestionarEncuestaView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final String idEncuesta;

    public GestionarEncuestaView(CtrlPresentacion ctrl, String idEncuesta) {
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

        Map<String, Object> datos = ctrl.obtenerDetalleEncuesta(idEncuesta);

        if (datos == null) {
            return crearEscenaError("Encuesta no encontrada");
        }

        VBox header = crearHeader();
        root.setTop(header);

        VBox centro = crearFormulario(datos);
        root.setCenter(centro);

        HBox footer = crearFooter(datos);
        root.setBottom(footer);

        return new Scene(root, 800, 600);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 0, 20, 0));

        Label lblTitulo = new Label("Gestionar Encuesta");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        header.getChildren().add(lblTitulo);
        return header;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param datos parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearFormulario(Map<String, Object> datos) {
        VBox formulario = new VBox(20);
        formulario.setAlignment(Pos.CENTER);
        formulario.setPadding(new Insets(30));
        formulario.setMaxWidth(600);
        formulario.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        String id = (String) datos.get("id");
        String titulo = (String) datos.get("titulo");
        String descripcion = (String) datos.get("descripcion");
        int numPreguntas = (Integer) datos.get("numPreguntas");

        // ID (solo lectura)
        VBox idBox = new VBox(5);
        Label lblIdLabel = new Label("ID de la Encuesta:");
        lblIdLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TextField txtId = new TextField(id);
        txtId.setEditable(false);
        txtId.setStyle("-fx-background-color: #ecf0f1;");

        idBox.getChildren().addAll(lblIdLabel, txtId);

        // Título
        VBox tituloBox = new VBox(5);
        Label lblTituloLabel = new Label("Título:");
        lblTituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TextField txtTitulo = new TextField(titulo);
        txtTitulo.setPromptText("Título de la encuesta");

        tituloBox.getChildren().addAll(lblTituloLabel, txtTitulo);

        // Descripción
        VBox descripcionBox = new VBox(5);
        Label lblDescLabel = new Label("Descripción:");
        lblDescLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TextArea txtDescripcion = new TextArea(descripcion);
        txtDescripcion.setPromptText("Descripción de la encuesta");
        txtDescripcion.setPrefRowCount(4);
        txtDescripcion.setWrapText(true);

        descripcionBox.getChildren().addAll(lblDescLabel, txtDescripcion);

        // Información adicional
        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15; -fx-background-radius: 5;");

        Label lblInfo = new Label("Información");
        lblInfo.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Label lblNumPreguntas = new Label("Número de preguntas: " + numPreguntas);
        lblNumPreguntas.setFont(Font.font("Arial", 12));

        int numRespuestas = ctrl.obtenerNumeroRespuestas(idEncuesta);
        Label lblNumRespuestas = new Label("Usuarios que respondieron: " + numRespuestas);
        lblNumRespuestas.setFont(Font.font("Arial", 12));

        infoBox.getChildren().addAll(lblInfo, lblNumPreguntas, lblNumRespuestas);

        // Botón Guardar Cambios
        Button btnGuardar = new Button("Guardar Cambios");
        btnGuardar.setPrefWidth(200);
        btnGuardar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> {
            String nuevoTitulo = txtTitulo.getText().trim();
            String nuevaDesc = txtDescripcion.getText().trim();

            if (nuevoTitulo.isEmpty()) {
                mostrarError("El título no puede estar vacío");
                txtTitulo.requestFocus();
                return;
            }

            if (nuevoTitulo.length() < 3) {
                mostrarError("El título debe tener al menos 3 caracteres");
                txtTitulo.requestFocus();
                return;
            }

            if (nuevoTitulo.length() > 200) {
                mostrarError("El título no puede exceder 200 caracteres\n" +
                        "Caracteres actuales: " + nuevoTitulo.length());
                txtTitulo.requestFocus();
                return;
            }

            if (nuevaDesc.length() > 1000) {
                mostrarError("La descripción no puede exceder 1000 caracteres\n" +
                        "Caracteres actuales: " + nuevaDesc.length());
                txtDescripcion.requestFocus();
                return;
            }

            try {
                boolean ok = ctrl.modificarEncuestaCompleta(idEncuesta, nuevoTitulo, nuevaDesc);
                if (ok) {
                    mostrarInfo("Encuesta modificada exitosamente");
                    ctrl.mostrarDetalleEncuesta(idEncuesta);
                } else {
                    mostrarError("Error al modificar la encuesta");
                }
            } catch (Exception ex) {
                mostrarError("Error: " + ex.getMessage());
            }
        });

        formulario.getChildren().addAll(idBox, tituloBox, descripcionBox, infoBox, btnGuardar);

        return formulario;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param datos parámetro de entrada.
 * @return resultado de la operación.
 */
    private HBox crearFooter(Map<String, Object> datos) {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));

        Button btnVolver = new Button("← Volver");
        btnVolver.setPrefWidth(150);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> ctrl.mostrarGestionEncuestas());

        Button btnEliminar = new Button("Eliminar Encuesta");
        btnEliminar.setPrefWidth(180);
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        btnEliminar.setOnAction(e -> confirmarEliminar());

        int numPreguntas = (Integer) datos.get("numPreguntas");

        Button btnLimpiarPreguntas = new Button("Limpiar Preguntas");
        btnLimpiarPreguntas.setPrefWidth(180);
        btnLimpiarPreguntas.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 14;");
        btnLimpiarPreguntas.setOnAction(e -> confirmarLimpiarPreguntas(numPreguntas));

        footer.getChildren().addAll(btnVolver, btnLimpiarPreguntas, btnEliminar);

        return footer;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void confirmarEliminar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar esta encuesta?");
        alert.setContentText("Esta acción no se puede deshacer. Se eliminarán todas las preguntas y respuestas asociadas.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean ok = ctrl.eliminarEncuesta(idEncuesta);
                    if (ok) {
                        mostrarInfo("Encuesta eliminada exitosamente");
                        ctrl.mostrarListarEncuestas();
                    } else {
                        mostrarError("Error al eliminar la encuesta");
                    }
                } catch (Exception e) {
                    mostrarError("Error: " + e.getMessage());
                }
            }
        });
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param numPreguntas parámetro de entrada.
 */
    private void confirmarLimpiarPreguntas(int numPreguntas) {
        if (numPreguntas == 0) {
            mostrarInfo("La encuesta no tiene preguntas para limpiar");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar limpieza");
        alert.setHeaderText("¿Eliminar todas las preguntas?");
        alert.setContentText("Vas a eliminar " + numPreguntas + " preguntas. Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Eliminar de atrás hacia adelante
                    for (int i = numPreguntas - 1; i >= 0; i--) {
                        ctrl.eliminarPregunta(idEncuesta, i);
                    }
                    mostrarInfo("Todas las preguntas han sido eliminadas");
                    ctrl.mostrarGestionarEncuesta(idEncuesta); // Recargar
                } catch (Exception e) {
                    mostrarError("Error: " + e.getMessage());
                }
            }
        });
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
        root.setPadding(new Insets(50));

        Label lblError = new Label(mensaje);
        lblError.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Button btnVolver = new Button("← Volver");
        btnVolver.setOnAction(e -> ctrl.mostrarListarEncuestas());

        root.getChildren().addAll(lblError, btnVolver);

        return new Scene(root, 800, 600);
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
}
