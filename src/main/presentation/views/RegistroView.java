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
 * Vista para registro de usuarios.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Ingresar datos de nuevo usuario.</li>
 *   <li>Seleccionar el tipo de usuario (admin o respondedor).</li>
 *   <li>Registrar al usuario en el sistema.</li>
 * </ul>
 */
public class RegistroView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final boolean esAdmin;

    public RegistroView(CtrlPresentacion ctrl, boolean esAdmin) {
        this.ctrl = ctrl;
        this.esAdmin = esAdmin;
        this.scene = crearEscena();
    }

    private Scene crearEscena() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #f5f5f5;");

        String tipo = esAdmin ? "Administrador" : "Respondedor";
        Label titulo = new Label("Registro - " + tipo);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField txtId = new TextField();
        txtId.setPromptText("ID de usuario");
        txtId.setMaxWidth(300);

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");
        txtNombre.setMaxWidth(300);

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setMaxWidth(300);

        PasswordField txtConfirmar = new PasswordField();
        txtConfirmar.setPromptText("Confirmar contraseña");
        txtConfirmar.setMaxWidth(300);

        Button btnRegistrar = new Button("Registrarse");
        btnRegistrar.setPrefWidth(150);
        btnRegistrar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnRegistrar.setOnAction(e -> {
            String id = txtId.getText().trim();
            String nombre = txtNombre.getText().trim();
            String password = txtPassword.getText();
            String confirmar = txtConfirmar.getText();

            // ⭐ VALIDACIONES MEJORADAS - Campos vacíos
            if (id.isEmpty()) {
                mostrarError("El ID de usuario no puede estar vacío");
                txtId.requestFocus();
                return;
            }

            if (nombre.isEmpty()) {
                mostrarError("El nombre no puede estar vacío");
                txtNombre.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                mostrarError("La contraseña no puede estar vacía");
                txtPassword.requestFocus();
                return;
            }

            if (confirmar.isEmpty()) {
                mostrarError("Debe confirmar la contraseña");
                txtConfirmar.requestFocus();
                return;
            }

            // ⭐ VALIDACIÓN - Longitudes mínimas
            if (id.length() < 3) {
                mostrarError("El ID debe tener al menos 3 caracteres");
                txtId.requestFocus();
                return;
            }

            if (id.length() > 50) {
                mostrarError("El ID no puede exceder 50 caracteres\n" +
                        "Caracteres actuales: " + id.length());
                txtId.requestFocus();
                return;
            }

            if (nombre.length() < 2) {
                mostrarError("El nombre debe tener al menos 2 caracteres");
                txtNombre.requestFocus();
                return;
            }

            if (nombre.length() > 100) {
                mostrarError("El nombre no puede exceder 100 caracteres\n" +
                        "Caracteres actuales: " + nombre.length());
                txtNombre.requestFocus();
                return;
            }

            if (password.length() < 4) {
                mostrarError("La contraseña debe tener al menos 4 caracteres");
                txtPassword.requestFocus();
                return;
            }

            if (password.length() > 100) {
                mostrarError("La contraseña no puede exceder 100 caracteres");
                txtPassword.requestFocus();
                return;
            }

            // ⭐ VALIDACIÓN - Caracteres válidos en ID
            if (!id.matches("^[a-zA-Z0-9_-]+$")) {
                mostrarError("El ID solo puede contener letras, números, guiones y guiones bajos");
                txtId.requestFocus();
                return;
            }

            // ⭐ VALIDACIÓN - Contraseñas coinciden
            if (!password.equals(confirmar)) {
                mostrarError("Las contraseñas no coinciden.\n\n" +
                        "Por favor, verifica que ambas contraseñas sean idénticas.");
                txtConfirmar.clear();
                txtPassword.clear();
                txtPassword.requestFocus();
                return;
            }

            // ⭐ VALIDACIÓN - Fortaleza de contraseña (opcional pero recomendado)
            if (password.length() < 6) {
                Alert advertencia = new Alert(Alert.AlertType.WARNING);
                advertencia.setTitle("Contraseña débil");
                advertencia.setHeaderText("La contraseña es muy corta");
                advertencia.setContentText("Se recomienda usar al menos 6 caracteres.\n" +
                        "¿Desea continuar de todos modos?");

                ButtonType btnSi = new ButtonType("Sí, continuar");
                ButtonType btnNo = new ButtonType("No, cambiar contraseña", ButtonBar.ButtonData.CANCEL_CLOSE);
                advertencia.getButtonTypes().setAll(btnSi, btnNo);

                var resultado = advertencia.showAndWait();
                if (resultado.isEmpty() || resultado.get() == btnNo) {
                    txtPassword.requestFocus();
                    return;
                }
            }

            try {
                // ⭐ Registrar a través de CtrlPresentacion
                String resultado = ctrl.registrarUsuario(id, nombre, password, esAdmin);

                if (resultado.equals("OK")) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registro Exitoso");
                    alert.setHeaderText("✓ Usuario registrado correctamente");
                    alert.setContentText(
                            "ID: " + id + "\n" +
                                    "Nombre: " + nombre + "\n" +
                                    "Tipo: " + (esAdmin ? "Administrador" : "Respondedor") + "\n\n" +
                                    "Ya puedes iniciar sesión con tus credenciales."
                    );
                    alert.showAndWait();

                    ctrl.mostrarLogin();
                } else {
                    // El resultado contiene el mensaje de error
                    mostrarError(resultado);
                    if (resultado.contains("ID de usuario")) {
                        txtId.requestFocus();
                        txtId.selectAll();
                    }
                }

            } catch (Exception ex) {
                mostrarError("Error al registrar usuario: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        txtId.setOnAction(e -> txtNombre.requestFocus());
        txtNombre.setOnAction(e -> txtPassword.requestFocus());
        txtPassword.setOnAction(e -> txtConfirmar.requestFocus());
        txtConfirmar.setOnAction(e -> btnRegistrar.fire());

        Button btnVolver = new Button("Volver");
        btnVolver.setOnAction(e -> ctrl.mostrarMenuInicial());

        root.getChildren().addAll(titulo, txtId, txtNombre, txtPassword,
                txtConfirmar, btnRegistrar, btnVolver);

        return new Scene(root, 800, 600);
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
