package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.Node;
import javafx.scene.text.FontWeight;
import main.presentation.controllers.CtrlPresentacion;

import java.util.*;

/**
 * Vista para responder encuestas.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Mostrar las preguntas de la encuesta.</li>
 *   <li>Permitir ingresar respuestas.</li>
 *   <li>Enviar las respuestas completadas.</li>
 * </ul>
 */
public class ResponderEncuestaView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final String idEncuesta;
    private final Map<String, Node> controlesRespuesta;

    public ResponderEncuestaView(CtrlPresentacion ctrl, String idEncuesta) {
        this.ctrl = ctrl;
        this.idEncuesta = idEncuesta;
        this.controlesRespuesta = new HashMap<>();
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

        Map<String, Object> datosEncuesta = ctrl.obtenerDatosEncuestaParaResponder(idEncuesta);

        if (datosEncuesta == null) {
            return crearEscenaError("Encuesta no válida");
        }

        String titulo = (String) datosEncuesta.get("titulo");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> preguntas = (List<Map<String, Object>>) datosEncuesta.get("preguntas");

        if (preguntas == null || preguntas.isEmpty()) {
/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param preguntas" parámetro de entrada.
 * @return resultado de la operación.
 */
            return crearEscenaError("Encuesta sin preguntas");
        }

        // Header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 0, 20, 0));

        Label lblTitulo = new Label("Responder Encuesta");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitulo = new Label(titulo);
        subtitulo.setFont(Font.font("Arial", 14));
        subtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(lblTitulo, subtitulo);
        root.setTop(header);

        // Centro: Formulario de preguntas
        ScrollPane scrollPane = crearFormulario(preguntas);
        root.setCenter(scrollPane);

        // Footer: Botones
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));

        Button btnEnviar = new Button("Enviar Respuestas");
        btnEnviar.setPrefWidth(180);
        btnEnviar.setPrefHeight(45);
        btnEnviar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        btnEnviar.setOnAction(e -> enviarRespuestas(preguntas));

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(150);
        btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnCancelar.setOnAction(e -> ctrl.mostrarListarEncuestas());

        footer.getChildren().addAll(btnEnviar, btnCancelar);
        root.setBottom(footer);

        return new Scene(root, 900, 700);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param preguntas parámetro de entrada.
 * @return resultado de la operación.
 */
    private ScrollPane crearFormulario(List<Map<String, Object>> preguntas) {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(20));
        contenedor.setAlignment(Pos.TOP_CENTER);

        for (int i = 0; i < preguntas.size(); i++) {
            VBox cardPregunta = crearCardPregunta(preguntas.get(i), i + 1);
            contenedor.getChildren().add(cardPregunta);
        }

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        return scroll;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param preguntaData parámetro de entrada.
 * @param numero parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearCardPregunta(Map<String, Object> preguntaData, int numero) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        card.setMaxWidth(700);

        String id = (String) preguntaData.get("id");
        String enunciado = (String) preguntaData.get("enunciado");
        Boolean obligatoria = (Boolean) preguntaData.get("obligatoria");
        String tipo = (String) preguntaData.get("tipo");

        // Enunciado
        Label lblNumero = new Label("Pregunta " + numero);
        lblNumero.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblNumero.setStyle("-fx-text-fill: #3498db;");

        Label lblEnunciado = new Label(enunciado + (obligatoria ? " *" : ""));
        lblEnunciado.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lblEnunciado.setWrapText(true);

        if (obligatoria) {
            lblEnunciado.setStyle("-fx-text-fill: #2c3e50;");
        }

        // Control según tipo
        Node control = crearControlPregunta(preguntaData);
        controlesRespuesta.put(id, control);

        card.getChildren().addAll(lblNumero, lblEnunciado, control);

        return card;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param preguntaData parámetro de entrada.
 * @return resultado de la operación.
 */
    private Node crearControlPregunta(Map<String, Object> preguntaData) {
        String tipo = (String) preguntaData.get("tipo");

        switch (tipo) {
            case "NUMERICA": {
                Double min = (Double) preguntaData.get("min");
                Double max = (Double) preguntaData.get("max");

                TextField txt = new TextField();
                String prompt = "Ingrese un número";
                if (min != null && max != null) {
                    prompt += " entre " + formatearNumero(min) + " y " + formatearNumero(max);
                }
                txt.setPromptText(prompt);
                return txt;
            }

            case "LIBRE": {
                TextArea txt = new TextArea();
                txt.setPromptText("Escriba su respuesta aquí...");
                txt.setPrefRowCount(3);
                txt.setWrapText(true);
                return txt;
            }

            case "CATEGORIA_SIMPLE": {
                @SuppressWarnings("unchecked")
                List<String> opciones = (List<String>) preguntaData.get("opciones");

                ComboBox<String> cmb = new ComboBox<>();
                if (opciones != null) {
                    cmb.getItems().addAll(opciones);
                }
                cmb.setPromptText("Seleccione una opción");
                cmb.setPrefWidth(400);
                return cmb;
            }

            case "ORDINAL": {
                @SuppressWarnings("unchecked")
                List<String> opciones = (List<String>) preguntaData.get("opciones");

                ComboBox<String> cmb = new ComboBox<>();
                if (opciones != null) {
                    cmb.getItems().addAll(opciones);
                }
                cmb.setPromptText("Seleccione un nivel");
                cmb.setPrefWidth(400);
                return cmb;
            }

            case "CATEGORIA_MULTIPLE": {
                @SuppressWarnings("unchecked")
                List<String> opciones = (List<String>) preguntaData.get("opciones");

                VBox vbox = new VBox(8);
                if (opciones != null) {
                    for (String opcion : opciones) {
                        CheckBox chk = new CheckBox(opcion);
                        vbox.getChildren().add(chk);
                    }
                }
                vbox.setUserData("CATEGORIA_MULTIPLE");
                return vbox;
            }

            default:
                return new TextField();
        }
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param preguntas parámetro de entrada.
 */
    private void enviarRespuestas(List<Map<String, Object>> preguntas) {
        Map<String, Object> respuestas = new HashMap<>();
        List<String> erroresValidacion = new ArrayList<>();

        try {
            for (Map<String, Object> preguntaData : preguntas) {
                String id = (String) preguntaData.get("id");
                String enunciado = (String) preguntaData.get("enunciado");
                Boolean obligatoria = (Boolean) preguntaData.get("obligatoria");
                String tipo = (String) preguntaData.get("tipo");

                Node control = controlesRespuesta.get(id);
                Object valor = null;

                try {
                    valor = extraerValor(control, tipo);
                } catch (IllegalArgumentException e) {
                    erroresValidacion.add("• " + enunciado + ": " + e.getMessage());
                    continue;
                }

                // Validación - Preguntas obligatorias
                if (obligatoria) {
                    if (valor == null) {
                        erroresValidacion.add("• " + enunciado + ": Este campo es obligatorio");
                        continue;
                    }

                    if (valor instanceof String && ((String) valor).trim().isEmpty()) {
                        erroresValidacion.add("• " + enunciado + ": Este campo es obligatorio");
                        continue;
                    }

                    if (valor instanceof Set && ((Set<?>) valor).isEmpty()) {
                        erroresValidacion.add("• " + enunciado + ": Debe seleccionar al menos una opción");
                        continue;
                    }
                }

                // Validaciones específicas por tipo
                if (valor != null) {
                    if (tipo.equals("NUMERICA")) {
                        Double min = (Double) preguntaData.get("min");
                        Double max = (Double) preguntaData.get("max");
                        double valorNum = ((Number) valor).doubleValue();

                        if (min != null && valorNum < min) {
                            erroresValidacion.add("• " + enunciado +
                                    ": El valor debe ser al menos " + formatearNumero(min));
                            continue;
                        }

                        if (max != null && valorNum > max) {
                            erroresValidacion.add("• " + enunciado +
                                    ": El valor no puede exceder " + formatearNumero(max));
                            continue;
                        }
                    }

                    if (tipo.equals("LIBRE")) {
                        Integer longMax = (Integer) preguntaData.get("longitudMaxima");
                        String texto = valor.toString();

                        if (longMax != null && texto.length() > longMax) {
                            erroresValidacion.add("• " + enunciado +
                                    ": El texto excede " + longMax + " caracteres\n" +
                                    "  Caracteres actuales: " + texto.length());
                            continue;
                        }
                    }

                    if (tipo.equals("CATEGORIA_MULTIPLE")) {
                        Integer maxSel = (Integer) preguntaData.get("maxSelecciones");
                        Set<?> seleccionadas = (Set<?>) valor;

                        if (maxSel != null && seleccionadas.size() > maxSel) {
                            erroresValidacion.add("• " + enunciado +
                                    ": Puede seleccionar máximo " + maxSel + " opciones\n" +
                                    "  Opciones seleccionadas: " + seleccionadas.size());
                            continue;
                        }
                    }

                    respuestas.put(id, valor);
                }
            }

            // Mostrar errores si existen
            if (!erroresValidacion.isEmpty()) {
                mostrarErroresValidacion(erroresValidacion);
                return;
            }

            // Confirmación si está vacío
            if (respuestas.isEmpty()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Encuesta vacía");
                confirm.setHeaderText("No has respondido ninguna pregunta");
                confirm.setContentText("¿Deseas enviar la encuesta sin respuestas?");

                var resultado = confirm.showAndWait();
                if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                    return;
                }
            }

            boolean exito = ctrl.enviarRespuestasEncuesta(idEncuesta, respuestas);

            if (exito) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Éxito");
                success.setHeaderText("✓ Encuesta respondida exitosamente");
                success.setContentText("Gracias por completar la encuesta.\n" +
                        "Respuestas guardadas: " + respuestas.size());
                success.showAndWait();

                ctrl.mostrarListarEncuestas();
            } else {
                mostrarError("Error al guardar las respuestas");
            }

        } catch (Exception e) {
            mostrarError("Error al enviar respuestas: " + e.getMessage());
            e.printStackTrace();
        }
    }

/**
 * Muestra la información indicada en la vista.
 *
 * @param errores parámetro de entrada.
 */
    private void mostrarErroresValidacion(List<String> errores) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Por favor, corrige los siguientes errores:\n\n");
        for (String error : errores) {
            mensaje.append(error).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Errores de Validación");
        alert.setHeaderText("Hay " + errores.size() + " error(es) en el formulario");
        alert.setContentText(mensaje.toString());

        TextArea textArea = new TextArea(mensaje.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param control parámetro de entrada.
 * @param tipo parámetro de entrada.
 * @return resultado de la operación.
 */
    private Object extraerValor(Node control, String tipo) {
        if (control instanceof TextField) {
            String texto = ((TextField) control).getText().trim();
            if (texto.isEmpty()) return null;

            if (tipo.equals("NUMERICA")) {
                try {
                    return Double.parseDouble(texto);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("debe ser un número válido");
                }
            }
            return texto;
        }

        if (control instanceof TextArea) {
            String texto = ((TextArea) control).getText().trim();
            return texto.isEmpty() ? null : texto;
        }

        if (control instanceof ComboBox) {
            Object valor = ((ComboBox<?>) control).getValue();
            return valor;
        }

        if (control instanceof VBox && "CATEGORIA_MULTIPLE".equals(control.getUserData())) {
            Set<String> seleccionados = new HashSet<>();
            for (var node : ((VBox) control).getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox chk = (CheckBox) node;
                    if (chk.isSelected()) {
                        seleccionados.add(chk.getText());
                    }
                }
            }
            return seleccionados.isEmpty() ? null : seleccionados;
        }

        return null;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param num parámetro de entrada.
 * @return resultado de la operación.
 */
    private String formatearNumero(double num) {
        if (num == Math.floor(num)) {
            return String.valueOf((int) num);
        }
        return String.format("%.2f", num);
    }

    private Scene crearEscenaError(String mensaje) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        Label lbl = new Label(mensaje);
        Button btnVolver = new Button("← Volver");
        btnVolver.setOnAction(e -> ctrl.mostrarListarEncuestas());
        root.getChildren().addAll(lbl, btnVolver);
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
