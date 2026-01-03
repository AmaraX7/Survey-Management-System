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
 * Vista del detalle de una encuesta.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Mostrar la información general de la encuesta.</li>
 *   <li>Listar todas las preguntas con sus detalles.</li>
 *   <li>Navegar a opciones de gestión de la encuesta.</li>
 * </ul>
 */
public class DetalleEncuestaView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final String idEncuesta;

    public DetalleEncuestaView(CtrlPresentacion ctrl, String idEncuesta) {
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
        Map<String, Object> datosEncuesta = ctrl.obtenerDetalleEncuesta(idEncuesta);

        if (datosEncuesta == null) {
            return crearEscenaError("Encuesta no encontrada");
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setPadding(new Insets(20));

        VBox header = crearHeader(datosEncuesta);
        root.setTop(header);

        ScrollPane scrollPane = crearContenido(datosEncuesta);
        root.setCenter(scrollPane);

        HBox footer = crearFooter(datosEncuesta);
        root.setBottom(footer);

        return new Scene(root, 900, 700);
    }

    @SuppressWarnings("unchecked")
/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param datos parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearHeader(Map<String, Object> datos) {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 0, 20, 0));
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        header.setPadding(new Insets(20));

        String titulo = (String) datos.get("titulo");
        String id = (String) datos.get("id");
        String descripcion = (String) datos.get("descripcion");
        int numPreguntas = (Integer) datos.get("numPreguntas");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");
        lblTitulo.setWrapText(true);

        Label lblId = new Label("ID: " + id);
        lblId.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        lblId.setStyle("-fx-text-fill: #7f8c8d;");

        Label lblDesc = new Label(descripcion);
        lblDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        lblDesc.setStyle("-fx-text-fill: #34495e;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(700);

        Label lblInfo = new Label(numPreguntas + " preguntas");
        lblInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblInfo.setStyle("-fx-text-fill: #3498db;");

        header.getChildren().addAll(lblTitulo, lblId, lblDesc, lblInfo);

        return header;
    }

    @SuppressWarnings("unchecked")
/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param datos parámetro de entrada.
 * @return resultado de la operación.
 */
    private ScrollPane crearContenido(Map<String, Object> datos) {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20, 10, 20, 10));
        contenedor.setAlignment(Pos.TOP_LEFT);

        List<Map<String, String>> preguntas =
                (List<Map<String, String>>) datos.get("preguntas");

        if (preguntas.isEmpty()) {
            Label lblVacio = new Label("Esta encuesta no tiene preguntas aún");
            lblVacio.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            lblVacio.setStyle("-fx-text-fill: #7f8c8d;");
            contenedor.getChildren().add(lblVacio);
        } else {
            Label lblTituloPreguntas = new Label("Preguntas:");
            lblTituloPreguntas.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            lblTituloPreguntas.setStyle("-fx-text-fill: #2c3e50;");
            contenedor.getChildren().add(lblTituloPreguntas);

            for (int i = 0; i < preguntas.size(); i++) {
                VBox cardPregunta = crearCardPregunta(preguntas.get(i), i + 1);
                contenedor.getChildren().add(cardPregunta);
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
 * @param pregunta parámetro de entrada.
 * @param numero parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearCardPregunta(Map<String, String> pregunta, int numero) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );
        card.setMaxWidth(700);

        String enunciado = pregunta.get("enunciado");
        String tipo = pregunta.get("tipo");
        boolean obligatoria = Boolean.parseBoolean(pregunta.get("obligatoria"));

        Label lblNumero = new Label(numero + ".");
        lblNumero.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblNumero.setStyle("-fx-text-fill: #3498db;");

        Label lblEnunciado = new Label(enunciado);
        lblEnunciado.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        lblEnunciado.setStyle("-fx-text-fill: #2c3e50;");
        lblEnunciado.setWrapText(true);

        HBox headerPregunta = new HBox(10, lblNumero, lblEnunciado);
        headerPregunta.setAlignment(Pos.CENTER_LEFT);

        String nombreTipo = obtenerNombreTipo(tipo);

        Label lblTipo = new Label("Tipo: " + nombreTipo);
        lblTipo.setFont(Font.font("Arial", 12));
        lblTipo.setStyle("-fx-text-fill: #7f8c8d;");

        Label lblObligatoria = new Label("Obligatoria: " + (obligatoria ? "Sí" : "No"));
        lblObligatoria.setFont(Font.font("Arial", 12));
        lblObligatoria.setStyle("-fx-text-fill: " + (obligatoria ? "#e74c3c" : "#7f8c8d") + ";");

        HBox infoPregunta = new HBox(20, lblTipo, lblObligatoria);

        card.getChildren().addAll(headerPregunta, infoPregunta);

        return card;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param tipo parámetro de entrada.
 * @return resultado de la operación.
 */
    private String obtenerNombreTipo(String tipo) {
        switch (tipo) {
            case "NUMERICA": return "Numérica";
            case "LIBRE": return "Texto libre";
            case "CATEGORIA_SIMPLE": return "Categoría simple";
            case "ORDINAL": return "Ordinal";
            case "CATEGORIA_MULTIPLE": return "Categoría múltiple";
            default: return "Desconocido";
        }
    }

    private HBox crearFooter(Map<String, Object> datos) {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));

        Button btnVolver = new Button("← Volver");
        btnVolver.setPrefWidth(150);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> ctrl.mostrarListarEncuestas());

        footer.getChildren().add(btnVolver);

        int numPreguntas = (Integer) datos.get("numPreguntas");

        if (!ctrl.esAdmin() && numPreguntas > 0) {
            Button btnResponder = new Button("Responder Encuesta");
            btnResponder.setPrefWidth(170);
            btnResponder.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14;");
            btnResponder.setOnAction(e -> ctrl.mostrarResponderEncuesta(idEncuesta));
            footer.getChildren().add(btnResponder);
        }

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
