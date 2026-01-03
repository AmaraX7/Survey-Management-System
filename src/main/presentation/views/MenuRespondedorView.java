package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.presentation.controllers.CtrlPresentacion;

/**
 * Menú principal para respondedores.
 * <p>
 * Casos de uso del respondedor según diagrama:
 * 1. Explorar encuestas (→ Listar + Ver detalle)
 * 2. Responder encuesta (→ Responder preguntas)
 * 3. Cerrar sesión
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Explorar encuestas disponibles.</li>
 *   <li>Responder encuestas.</li>
 *   <li>Cerrar sesión.</li>
 * </ul>
 */
public class MenuRespondedorView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

    public MenuRespondedorView(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        this.scene = crearEscena();
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private Scene crearEscena() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Información del usuario - obtenida a través de CtrlPresentacion
        String nombreUsuario = ctrl.obtenerNombreUsuarioActual();
        Label lblUsuario = new Label("Usuario: " + nombreUsuario);
        lblUsuario.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblUsuario.setStyle("-fx-text-fill: #34495e;");

        // Título
        Label titulo = new Label("Menú Principal");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        // ========== CU: EXPLORAR ENCUESTAS ==========
        Button btnExplorar = crearBoton("Explorar Encuestas", "#3498db");
        btnExplorar.setOnAction(e -> ctrl.mostrarExplorarEncuestas());

        // ========== CU: CERRAR SESIÓN ==========
        Button btnCerrarSesion = crearBoton("Cerrar Sesión", "#e74c3c");
        btnCerrarSesion.setOnAction(e -> ctrl.cerrarSesion());

        root.getChildren().addAll(lblUsuario, titulo, btnExplorar, btnCerrarSesion);

        return new Scene(root, 800, 600);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param texto parámetro de entrada.
 * @param color parámetro de entrada.
 * @return resultado de la operación.
 */
    private Button crearBoton(String texto, String color) {
        Button btn = new Button(texto);
        btn.setPrefWidth(300);
        btn.setPrefHeight(80);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + color + ", -10%);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-scale-x: 1.05;" +
                        "-fx-scale-y: 1.05;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        ));

        return btn;
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
