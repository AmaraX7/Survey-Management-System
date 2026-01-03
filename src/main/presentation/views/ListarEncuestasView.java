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
 * Vista para listar todas las encuestas.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Mostrar una lista de todas las encuestas disponibles.</li>
 *   <li>Permitir explorar detalles de cada encuesta.</li>
 * </ul>
 */
public class ListarEncuestasView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

    public ListarEncuestasView(CtrlPresentacion ctrl) {
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
        root.setPadding(new Insets(20));

        VBox header = crearHeader();
        root.setTop(header);

        ScrollPane scrollPane = crearListaEncuestas();
        root.setCenter(scrollPane);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20));

        Button btnVolver = new Button("← Volver");
        btnVolver.setPrefWidth(150);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> {
            ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), ctrl.esAdmin());
        });

        footer.getChildren().add(btnVolver);
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
        header.setPadding(new Insets(0, 0, 20, 0));

        Label titulo = new Label("Explorar Encuestas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        header.getChildren().add(titulo);
        return header;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private ScrollPane crearListaEncuestas() {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(10));
        contenedor.setAlignment(Pos.TOP_CENTER);

        List<Map<String, Object>> encuestas = ctrl.listarEncuestas();

        if (encuestas.isEmpty()) {
            Label lblVacio = new Label("No hay encuestas disponibles");
            lblVacio.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            lblVacio.setStyle("-fx-text-fill: #7f8c8d;");
            contenedor.getChildren().add(lblVacio);
        } else {
            for (Map<String, Object> encuesta : encuestas) {
                VBox card = crearCardEncuesta(encuesta);
                contenedor.getChildren().add(card);
            }
        }

        ScrollPane scrollPane = new ScrollPane(contenedor);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        return scrollPane;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param encuesta parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearCardEncuesta(Map<String, Object> encuesta) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        card.setMaxWidth(700);

        String id = (String) encuesta.get("id");
        String titulo = (String) encuesta.get("titulo");
        String descripcion = (String) encuesta.get("descripcion");
        int numPreguntas = (Integer) encuesta.get("numPreguntas");

        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        // ID
        Label lblId = new Label("ID: " + id);
        lblId.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        lblId.setStyle("-fx-text-fill: #7f8c8d;");

        // Descripción
        Label lblDesc = new Label(descripcion);
        lblDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        lblDesc.setStyle("-fx-text-fill: #34495e;");
        lblDesc.setWrapText(true);

        // Info
        Label lblInfo = new Label(numPreguntas + " preguntas");
        lblInfo.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        // Botones
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER_RIGHT);

        Button btnVer = new Button("Ver Detalle");
        btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnVer.setOnAction(e -> ctrl.mostrarDetalleEncuesta(id));

        if (!ctrl.esAdmin()) {
            if (numPreguntas > 0) {
                Button btnResponder = new Button("Responder");
                btnResponder.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                btnResponder.setOnAction(e -> ctrl.mostrarResponderEncuesta(id));
                botones.getChildren().add(btnResponder);
            }
        }

        botones.getChildren().add(btnVer);

        card.getChildren().addAll(lblTitulo, lblId, lblDesc, lblInfo, botones);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #ecf0f1;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        ));

        return card;
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
