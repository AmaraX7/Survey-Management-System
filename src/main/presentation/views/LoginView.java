package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.presentation.controllers.CtrlPresentacion;

/**
 * Vista de inicio de sesión.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Ingresar credenciales de usuario.</li>
 *   <li>Autenticar al usuario en el sistema.</li>
 * </ul>
 */
public class LoginView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

    public LoginView(CtrlPresentacion ctrl) {
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

        Label titulo = new Label("Iniciar Sesión");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField txtId = new TextField();
        txtId.setPromptText("ID de usuario");
        txtId.setMaxWidth(300);

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setMaxWidth(300);

        Button btnLogin = new Button("Ingresar");
        btnLogin.setPrefWidth(150);
        btnLogin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnLogin.setOnAction(e -> intentarLogin(txtId.getText(), txtPassword.getText()));

        txtId.setOnAction(e -> txtPassword.requestFocus());
        txtPassword.setOnAction(e -> btnLogin.fire());

        Button btnVolver = new Button("Volver");
        btnVolver.setOnAction(e -> ctrl.mostrarMenuInicial());

        root.getChildren().addAll(titulo, txtId, txtPassword, btnLogin, btnVolver);

        return new Scene(root, 800, 600);
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param id parámetro de entrada.
 * @param password parámetro de entrada.
 */
    private void intentarLogin(String id, String password) {
        if (id.trim().isEmpty() || password.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }

        String resultado = ctrl.verificarLogin(id.trim(), password);

        if (resultado.equals("OK")) {
            boolean esAdmin = ctrl.esUsuarioAdmin(id.trim());
            ctrl.mostrarMenuPrincipal(id.trim(), esAdmin);
        } else {
            mostrarError(resultado);
        }
    }

/**
 * Muestra la información indicada en la vista.
 *
 * @param mensaje parámetro de entrada.
 */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
