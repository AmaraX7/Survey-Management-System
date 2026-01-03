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
 * Vista del menú inicial.
 * <p>
 * Casos de uso del usuario general:
 * - Registrarse como Administrador
 * - Registrarse como Respondedor
 * - Iniciar sesión
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Permitir el inicio de sesión.</li>
 *   <li>Permitir el registro de nuevos usuarios.</li>
 * </ul>
 */
public class MenuInicialView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

    public MenuInicialView(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        this.scene = crearEscena();
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private Scene crearEscena() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Título
        Label titulo = new Label("SISTEMA DE GESTIÓN DE ENCUESTAS");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Bienvenido");
        subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        // Botones según casos de uso
        Button btnRegistrarAdmin = crearBoton("Registrarse como Administrador", "#3498db");
        btnRegistrarAdmin.setOnAction(e -> ctrl.mostrarRegistroAdmin()); 

        Button btnRegistrarRespondedor = crearBoton("Registrarse como Respondedor", "#2ecc71");
        btnRegistrarRespondedor.setOnAction(e -> ctrl.mostrarRegistroRespondedor()); 

        Button btnLogin = crearBoton("Iniciar Sesión", "#9b59b6");
        btnLogin.setOnAction(e -> ctrl.mostrarLogin());

        Button btnSalir = crearBoton("Salir", "#e74c3c");
        btnSalir.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(
                titulo,
                subtitulo,
                btnRegistrarAdmin,
                btnRegistrarRespondedor,
                btnLogin,
                btnSalir
        );

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
        btn.setPrefHeight(50);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + color + ", -10%);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;" +
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
