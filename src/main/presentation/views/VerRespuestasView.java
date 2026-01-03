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
 * Vista para ver todas las respuestas de una encuesta.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Listar todas las respuestas por usuario.</li>
 *   <li>Mostrar detalles de cada respuesta.</li>
 * </ul>
 */
public class VerRespuestasView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final String idEncuesta;

    public VerRespuestasView(CtrlPresentacion ctrl, String idEncuesta) {
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

        // ⭐ Obtener datos a través de CtrlPresentacion
        Map<String, Object> datosEncuesta = ctrl.obtenerDatosEncuesta(idEncuesta);

        if (datosEncuesta == null) {
            return crearEscenaError("Encuesta no encontrada");
        }

        // Header
        VBox header = crearHeader(datosEncuesta);
        root.setTop(header);

        // Centro: Respuestas
        ScrollPane scrollPane = crearContenidoRespuestas(datosEncuesta);
        root.setCenter(scrollPane);

        // Footer: Botones
        HBox footer = crearFooter();
        root.setBottom(footer);

        return new Scene(root, 1000, 700);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param datosEncuesta parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearHeader(Map<String, Object> datosEncuesta) {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 0, 20, 0));
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        header.setPadding(new Insets(20));

        String titulo = (String) datosEncuesta.get("titulo");
        int numUsuarios = (Integer) datosEncuesta.get("numUsuarios");

        Label lblTitulo = new Label("Respuestas: " + titulo);
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");
        lblTitulo.setWrapText(true);

        Label lblInfo = new Label(numUsuarios + " usuarios han respondido");
        lblInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblInfo.setStyle("-fx-text-fill: #3498db;");

        header.getChildren().addAll(lblTitulo, lblInfo);

        return header;
    }

    @SuppressWarnings("unchecked")
/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param datosEncuesta parámetro de entrada.
 * @return resultado de la operación.
 */
    private ScrollPane crearContenidoRespuestas(Map<String, Object> datosEncuesta) {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(20, 10, 20, 10));
        contenedor.setAlignment(Pos.TOP_CENTER);

        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) datosEncuesta.get("usuarios");

        if (usuarios.isEmpty()) {
            Label lblVacio = new Label("Nadie ha respondido esta encuesta todavía");
            lblVacio.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            lblVacio.setStyle("-fx-text-fill: #7f8c8d;");
            contenedor.getChildren().add(lblVacio);
        } else {
            for (Map<String, Object> usuario : usuarios) {
                VBox cardUsuario = crearCardUsuario(usuario);
                contenedor.getChildren().add(cardUsuario);
            }
        }

        ScrollPane scrollPane = new ScrollPane(contenedor);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        return scrollPane;
    }

    @SuppressWarnings("unchecked")
/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param usuario parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearCardUsuario(Map<String, Object> usuario) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );
        card.setMaxWidth(900);

        String nombre = (String) usuario.get("nombre");
        String id = (String) usuario.get("id");

        // Header del usuario
        HBox headerUsuario = new HBox(10);
        headerUsuario.setAlignment(Pos.CENTER_LEFT);
        headerUsuario.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10; -fx-background-radius: 5;");

        Label lblUsuario = new Label(nombre);
        lblUsuario.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblUsuario.setStyle("-fx-text-fill: #2c3e50;");

        Label lblId = new Label("(ID: " + id + ")");
        lblId.setFont(Font.font("Arial", 12));
        lblId.setStyle("-fx-text-fill: #7f8c8d;");

        headerUsuario.getChildren().addAll(lblUsuario, lblId);

        // Separador
        Separator separador = new Separator();

        // Respuestas
        VBox respuestas = new VBox(8);
        List<Map<String, String>> respuestasUsuario = (List<Map<String, String>>) usuario.get("respuestas");

        for (Map<String, String> respuesta : respuestasUsuario) {
            HBox itemRespuesta = crearItemRespuesta(respuesta);
            respuestas.getChildren().add(itemRespuesta);
        }

        card.getChildren().addAll(headerUsuario, separador, respuestas);

        return card;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param respuesta parámetro de entrada.
 * @return resultado de la operación.
 */
    private HBox crearItemRespuesta(Map<String, String> respuesta) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.TOP_LEFT);
        item.setPadding(new Insets(5));

        String pregunta = respuesta.get("pregunta");
        String valor = respuesta.get("valor");

        // Pregunta
        VBox preguntaBox = new VBox(3);
        HBox.setHgrow(preguntaBox, Priority.ALWAYS);

        Label lblPregunta = new Label(pregunta);
        lblPregunta.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        lblPregunta.setStyle("-fx-text-fill: #34495e;");
        lblPregunta.setWrapText(true);

        preguntaBox.getChildren().add(lblPregunta);

        // Respuesta
        VBox respuestaBox = new VBox(3);
        respuestaBox.setMinWidth(300);

        Label lblRespuesta = new Label(valor);
        lblRespuesta.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblRespuesta.setStyle("-fx-text-fill: #2c3e50;");
        lblRespuesta.setWrapText(true);

        respuestaBox.getChildren().add(lblRespuesta);

        item.getChildren().addAll(preguntaBox, respuestaBox);

        return item;
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

        Button btnVolver = new Button("← Volver a Estadísticas");
        btnVolver.setPrefWidth(200);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> ctrl.mostrarAnalisisResultados());

        footer.getChildren().add(btnVolver);

        return footer;
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
 * Devuelve la escena asociada a esta vista.
 *
 * @return resultado de la operación.
 */
    public Scene getScene() {
        return scene;
    }
}
