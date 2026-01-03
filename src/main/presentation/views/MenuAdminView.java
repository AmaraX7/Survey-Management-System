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
 * Menú principal para administradores.
 * <p>
 * Casos de uso del administrador según diagrama:
 * 1. Explorar encuestas
 * 2. Gestión de encuestas
 * 3. Gestionar preguntas
 * 4. Análisis y resultados
 * 5. Importar/Exportar
 * 6. Cerrar sesión
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Explorar encuestas.</li>
 *   <li>Gestionar encuestas.</li>
 *   <li>Gestionar preguntas.</li>
 *   <li>Analizar resultados.</li>
 *   <li>Importar/exportar datos.</li>
 *   <li>Cerrar sesión.</li>
 * </ul>
 */
public class MenuAdminView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

/**
 * Crea una instancia de la vista.
 *
 * @param ctrl parámetro de entrada.
 */
    public MenuAdminView(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        this.scene = crearEscena();
    }

    private Scene crearEscena() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #f5f5f5;");

        String nombreUsuario = ctrl.obtenerNombreUsuarioActual();
        Label lblUsuario = new Label("Administrador: " + nombreUsuario);
        lblUsuario.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblUsuario.setStyle("-fx-text-fill: #34495e;");

        // Título
        Label titulo = new Label("MENÚ ADMINISTRADOR");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        // ========== CU: EXPLORAR ENCUESTAS ==========
        Label lblSeccion1 = new Label("EXPLORAR");
        lblSeccion1.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblSeccion1.setStyle("-fx-text-fill: #7f8c8d;");

        Button btnExplorar = crearBoton("Explorar Encuestas", "#3498db");
        btnExplorar.setOnAction(e -> ctrl.mostrarExplorarEncuestas());

        // ========== CU: GESTIÓN DE ENCUESTAS ==========
        Label lblSeccion2 = new Label("GESTIÓN");
        lblSeccion2.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblSeccion2.setStyle("-fx-text-fill: #7f8c8d;");

        Button btnGestion = crearBoton("Gestión de Encuestas", "#2ecc71");
        btnGestion.setOnAction(e -> ctrl.mostrarGestionEncuestas());

        Button btnPreguntas = crearBoton("Gestionar Preguntas", "#16a085");
        btnPreguntas.setOnAction(e -> ctrl.mostrarGestionarPreguntasMenu());

        // ========== CU: ANÁLISIS Y RESULTADOS ==========
        Label lblSeccion3 = new Label("ANÁLISIS");
        lblSeccion3.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblSeccion3.setStyle("-fx-text-fill: #7f8c8d;");

        Button btnAnalisis = crearBoton("Análisis y Resultados", "#9b59b6");
        btnAnalisis.setOnAction(e -> ctrl.mostrarAnalisisResultados());

        // ========== CU: IMPORTAR/EXPORTAR ==========
        Label lblSeccion4 = new Label("DATOS");
        lblSeccion4.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblSeccion4.setStyle("-fx-text-fill: #7f8c8d;");

        Button btnImportarExportar = crearBoton("Importar / Exportar", "#e67e22");
        btnImportarExportar.setOnAction(e -> ctrl.mostrarImportarExportar());

        // ========== CU: CERRAR SESIÓN ==========
        Button btnCerrarSesion = crearBoton("Cerrar Sesión", "#e74c3c");
        btnCerrarSesion.setOnAction(e -> ctrl.cerrarSesion());

        root.getChildren().addAll(
                lblUsuario,
                titulo,
                lblSeccion1, btnExplorar,
                lblSeccion2, btnGestion, btnPreguntas,
                lblSeccion3, btnAnalisis,
                lblSeccion4, btnImportarExportar,
                btnCerrarSesion
        );

        return new Scene(root, 900, 750);
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
        btn.setPrefWidth(350);
        btn.setPrefHeight(60);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + color + ", -15%);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-scale-x: 1.03;" +
                        "-fx-scale-y: 1.03;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
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
