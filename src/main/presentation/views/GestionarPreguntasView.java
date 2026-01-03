package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.presentation.controllers.CtrlPresentacion;

import java.util.*;

/**
 * Vista para ver y eliminar preguntas de una encuesta.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Listar todas las preguntas de la encuesta.</li>
 *   <li>Eliminar preguntas específicas.</li>
 *   <li>Redirigir a añadir nuevas preguntas.</li>
 * </ul>
 *
 * Casos de uso:
 * - Ver preguntas
 * - Eliminar pregunta (extend de Ver preguntas)
 */
public class GestionarPreguntasView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final String idEncuesta;

/**
 * Crea una instancia de la vista.
 *
 * @param ctrl parámetro de entrada.
 * @param idEncuesta parámetro de entrada.
 */
    public GestionarPreguntasView(CtrlPresentacion ctrl, String idEncuesta) {
        this.ctrl = ctrl;
        this.idEncuesta = idEncuesta;
        this.scene = crearEscena();
    }

    private Scene crearEscena() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setPadding(new Insets(20));

        Map<String, Object> datosEncuesta = ctrl.obtenerDetalleEncuesta(idEncuesta);

        if (datosEncuesta == null) {
/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
            return crearEscenaError();
        }

        String titulo = (String) datosEncuesta.get("titulo");

        // Header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label("Ver preguntas");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitulo = new Label("Encuesta: " + titulo);
        subtitulo.setFont(Font.font("Arial", 14));
        subtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(lblTitulo, subtitulo);
        root.setTop(header);

        // Centro: Lista de preguntas
        ScrollPane scrollPane = crearListaPreguntas(datosEncuesta);
        root.setCenter(scrollPane);

        // Footer: Botones
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));

        // Botón para ir a añadir pregunta
        Button btnAñadir = new Button("+ Añadir Pregunta");
        btnAñadir.setPrefWidth(170);
        btnAñadir.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14;");
        btnAñadir.setOnAction(e -> ctrl.mostrarAddPregunta(idEncuesta));

        Button btnVolver = new Button("← Volver");
        btnVolver.setPrefWidth(150);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> ctrl.mostrarGestionarPreguntasMenu());

        footer.getChildren().addAll(btnAñadir, btnVolver);
        root.setBottom(footer);

        return new Scene(root, 900, 700);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param datosEncuesta parámetro de entrada.
 * @return resultado de la operación.
 */
    private ScrollPane crearListaPreguntas(Map<String, Object> datosEncuesta) {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));
        contenedor.setAlignment(Pos.TOP_CENTER);

        @SuppressWarnings("unchecked")
        List<Map<String, String>> preguntas =
                (List<Map<String, String>>) datosEncuesta.get("preguntas");

        if (preguntas == null || preguntas.isEmpty()) {
            VBox vacio = new VBox(15);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(50));

            Label lblVacio = new Label("No hay preguntas en esta encuesta");
            lblVacio.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            lblVacio.setStyle("-fx-text-fill: #7f8c8d;");

            Label lblInfo = new Label("Añade la primera pregunta para comenzar");
            lblInfo.setFont(Font.font("Arial", 14));
            lblInfo.setStyle("-fx-text-fill: #95a5a6;");

            Button btnAñadirPrimera = new Button("+ Añadir Primera Pregunta");
            btnAñadirPrimera.setPrefWidth(200);
            btnAñadirPrimera.setStyle(
                    "-fx-background-color: #2ecc71;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 10 20;"
            );
            btnAñadirPrimera.setOnAction(e -> ctrl.mostrarAddPregunta(idEncuesta));

            vacio.getChildren().addAll(lblVacio, lblInfo, btnAñadirPrimera);
            contenedor.getChildren().add(vacio);
        } else {
            // Contador de preguntas
            HBox headerInfo = new HBox(10);
            headerInfo.setAlignment(Pos.CENTER_LEFT);
            headerInfo.setPadding(new Insets(0, 0, 15, 20));

            Label lblTotal = new Label("Total: " + preguntas.size() + " pregunta" +
                    (preguntas.size() != 1 ? "s" : ""));
            lblTotal.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            lblTotal.setStyle("-fx-text-fill: #2c3e50;");

            headerInfo.getChildren().add(lblTotal);
            contenedor.getChildren().add(headerInfo);

            for (int i = 0; i < preguntas.size(); i++) {
                HBox card = crearCardPregunta(preguntas.get(i), i);
                contenedor.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f5f5f5;");
        return scroll;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param preguntaData parámetro de entrada.
 * @param index parámetro de entrada.
 * @return resultado de la operación.
 */
    private HBox crearCardPregunta(Map<String, String> preguntaData, int index) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        card.setMaxWidth(800);

        String enunciado = preguntaData.get("enunciado");
        String tipo = preguntaData.get("tipo");

        // Número de pregunta
        Label lblNumero = new Label(String.valueOf(index + 1));
        lblNumero.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblNumero.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-background-color: #3498db;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 40;" +
                        "-fx-min-height: 40;" +
                        "-fx-alignment: center;"
        );
        lblNumero.setAlignment(Pos.CENTER);

        // Contenido
        VBox contenido = new VBox(5);
        HBox.setHgrow(contenido, Priority.ALWAYS);

        Label lblEnunciado = new Label(enunciado);
        lblEnunciado.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblEnunciado.setWrapText(true);

        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label lblTipo = new Label(obtenerNombreTipo(tipo));
        lblTipo.setFont(Font.font("Arial", 12));
        lblTipo.setStyle("-fx-text-fill: #7f8c8d;");

        infoBox.getChildren().add(lblTipo);

        contenido.getChildren().addAll(lblEnunciado, infoBox);

        // Botones de acción
        VBox botones = new VBox(8);
        botones.setAlignment(Pos.CENTER);

        Button btnEliminar = new Button("X");
        btnEliminar.setTooltip(new Tooltip("Eliminar pregunta"));
        btnEliminar.setPrefWidth(40);
        btnEliminar.setPrefHeight(40);
        btnEliminar.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-background-radius: 5;"
        );
        btnEliminar.setOnAction(e -> eliminarPregunta(index, enunciado));

        btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle(
                "-fx-background-color: #c0392b;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        ));

        btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-background-radius: 5;"
        ));

        botones.getChildren().add(btnEliminar);

        card.getChildren().addAll(lblNumero, contenido, botones);
        HBox.setHgrow(contenido, Priority.ALWAYS);

        return card;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param index parámetro de entrada.
 * @param enunciado parámetro de entrada.
 */
    private void eliminarPregunta(int index, String enunciado) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar esta pregunta?");
        alert.setContentText("Pregunta: \"" +
                (enunciado.length() > 60 ? enunciado.substring(0, 60) + "..." : enunciado) + "\"");

        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnEliminar, btnCancelar);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnEliminar) {
                try {
                    boolean exito = ctrl.eliminarPregunta(idEncuesta, index);

                    if (exito) {
                        mostrarInfo("Pregunta eliminada exitosamente");
                        ctrl.mostrarGestionarPreguntas(idEncuesta);
                    } else {
                        mostrarError("Error al eliminar la pregunta");
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

    private Scene crearEscenaError() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");

        Label lbl = new Label("Encuesta no encontrada");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Button btnVolver = new Button("← Volver");
        btnVolver.setOnAction(e -> ctrl.mostrarGestionarPreguntasMenu());

        root.getChildren().addAll(lbl, btnVolver);
        return new Scene(root, 900, 700);
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
