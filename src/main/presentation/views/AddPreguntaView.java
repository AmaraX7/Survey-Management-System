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
 * Vista para añadir una nueva pregunta a una encuesta.
 * Caso de uso: Añadir pregunta
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Seleccionar el tipo de pregunta (numérica, ordinal, etc.).</li>
 *   <li>Configurar los parámetros específicos de cada tipo.</li>
 *   <li>Guardar la nueva pregunta en la encuesta.</li>
 * </ul>
 */
public class AddPreguntaView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final String idEncuesta;
    private String tituloEncuesta;

    public AddPreguntaView(CtrlPresentacion ctrl, String idEncuesta) {
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
        root.setPadding(new Insets(30));

        // Obtener datos de la encuesta
        Map<String, Object> datosEncuesta = ctrl.obtenerDetalleEncuesta(idEncuesta);

        if (datosEncuesta == null) {
            return crearEscenaError();
        }

        tituloEncuesta = (String) datosEncuesta.get("titulo");

        // Header
        VBox header = crearHeader();
        root.setTop(header);

        // Centro: Formulario de creación
        ScrollPane scrollPane = crearFormularioCreacion();
        root.setCenter(scrollPane);

        // Footer
        HBox footer = crearFooter();
        root.setBottom(footer);

        return new Scene(root, 900, 700);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 30, 0));

        Label titulo = new Label("Añadir Nueva Pregunta");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Encuesta: " + tituloEncuesta);
        subtitulo.setFont(Font.font("Arial", 14));
        subtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(titulo, subtitulo);
        return header;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private ScrollPane crearFormularioCreacion() {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(20));
        contenedor.setAlignment(Pos.TOP_CENTER);
        contenedor.setMaxWidth(700);

        // Panel de tipo de pregunta
        VBox panelTipo = crearPanelTipoPregunta();

        // Panel de configuración básica
        VBox panelBasico = crearPanelConfiguracionBasica();

        // Panel de configuración específica (se actualiza según el tipo)
        VBox panelEspecifico = new VBox(15);
        panelEspecifico.setPadding(new Insets(25));
        panelEspecifico.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label lblConfigEspecifica = new Label("Configuración Específica");
        lblConfigEspecifica.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblConfigEspecifica.setStyle("-fx-text-fill: #2c3e50;");

        VBox contenidoEspecifico = new VBox(10);
        panelEspecifico.getChildren().addAll(lblConfigEspecifica, contenidoEspecifico);

        // Botón para crear la pregunta
        Button btnCrear = new Button("✓ Crear Pregunta");
        btnCrear.setPrefWidth(200);
        btnCrear.setPrefHeight(45);
        btnCrear.setStyle(
                "-fx-background-color: #2ecc71;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;"
        );

        // Guardar referencias a los componentes para usarlos después
        ComboBox<String> cmbTipo = new ComboBox<>();
        TextField txtEnunciado = new TextField();
        CheckBox chkObligatoria = new CheckBox();

        // Buscar los componentes en los paneles
        for (var node : panelTipo.getChildren()) {
            if (node instanceof ComboBox) {
                cmbTipo = (ComboBox<String>) node;
            }
        }

        for (var node : panelBasico.getChildren()) {
            if (node instanceof TextField) {
                txtEnunciado = (TextField) node;
            } else if (node instanceof CheckBox) {
                chkObligatoria = (CheckBox) node;
            }
        }

        final ComboBox<String> cmbTipoFinal = cmbTipo;
        final TextField txtEnunciadoFinal = txtEnunciado;
        final CheckBox chkObligatoriaFinal = chkObligatoria;

        cmbTipoFinal.setOnAction(e -> {
            String tipoSeleccionado = cmbTipoFinal.getValue();
            contenidoEspecifico.getChildren().clear();
            actualizarConfiguracionEspecifica(contenidoEspecifico, tipoSeleccionado);
        });

        // Inicializar con tipo por defecto
        actualizarConfiguracionEspecifica(contenidoEspecifico, "Numérica");

        btnCrear.setOnAction(e -> {
            String enunciado = txtEnunciadoFinal.getText().trim();
            String tipo = cmbTipoFinal.getValue();
            boolean obligatoria = chkObligatoriaFinal.isSelected();

            // Validar enunciado
            if (!validarEnunciado(enunciado)) {
                return;
            }

            // Recoger datos específicos y crear pregunta
            procesarCreacionPregunta(tipo, enunciado, obligatoria, contenidoEspecifico);
        });

        contenedor.getChildren().addAll(panelTipo, panelBasico, panelEspecifico, btnCrear);

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f5f5f5;");
        return scroll;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelTipoPregunta() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label lblTitulo = new Label("1. Tipo de Pregunta");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll(
                "Numérica",
                "Texto libre",
                "Categoría simple",
                "Ordinal",
                "Categoría múltiple"
        );
        cmbTipo.setValue("Numérica");
        cmbTipo.setPrefWidth(400);
        cmbTipo.setStyle("-fx-font-size: 14;");

        Label lblInfo = new Label("Selecciona el tipo de respuesta que esperas recibir");
        lblInfo.setFont(Font.font("Arial", 12));
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        panel.getChildren().addAll(lblTitulo, cmbTipo, lblInfo);
        return panel;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelConfiguracionBasica() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label lblTitulo = new Label("2. Configuración Básica");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        TextField txtEnunciado = new TextField();
        txtEnunciado.setPromptText("Escribe el enunciado de la pregunta...");
        txtEnunciado.setPrefHeight(40);
        txtEnunciado.setStyle("-fx-font-size: 14;");

        CheckBox chkObligatoria = new CheckBox("Pregunta obligatoria");
        chkObligatoria.setStyle("-fx-font-size: 14;");

        Label lblContador = new Label("0 / 500 caracteres");
        lblContador.setFont(Font.font("Arial", 11));
        lblContador.setStyle("-fx-text-fill: #95a5a6;");

        txtEnunciado.textProperty().addListener((obs, old, nuevo) -> {
            int longitud = nuevo.length();
            lblContador.setText(longitud + " / 500 caracteres");

            if (longitud > 500) {
                lblContador.setStyle("-fx-text-fill: #e74c3c;");
            } else if (longitud > 400) {
                lblContador.setStyle("-fx-text-fill: #f39c12;");
            } else {
                lblContador.setStyle("-fx-text-fill: #95a5a6;");
            }
        });

        panel.getChildren().addAll(lblTitulo, txtEnunciado, chkObligatoria, lblContador);
        return panel;
    }

/**
 * Actualiza el contenido mostrado en la vista.
 *
 * @param contenedor parámetro de entrada.
 * @param tipo parámetro de entrada.
 */
    private void actualizarConfiguracionEspecifica(VBox contenedor, String tipo) {
        contenedor.getChildren().clear();

        switch (tipo) {
            case "Numérica":
                crearConfigNumerica(contenedor);
                break;
            case "Texto libre":
                crearConfigLibre(contenedor);
                break;
            case "Categoría simple":
            case "Ordinal":
                crearConfigConOpciones(contenedor, tipo);
                break;
            case "Categoría múltiple":
                crearConfigMultiple(contenedor);
                break;
        }
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param contenedor parámetro de entrada.
 */
    private void crearConfigNumerica(VBox contenedor) {
        Label lblInfo = new Label("Define el rango de valores numéricos permitidos:");
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        HBox rangoBox = new HBox(15);
        rangoBox.setAlignment(Pos.CENTER_LEFT);

        VBox minBox = new VBox(5);
        Label lblMin = new Label("Valor mínimo:");
        TextField txtMin = new TextField();
        txtMin.setPromptText("Ej: 0");
        txtMin.setPrefWidth(150);
        txtMin.setUserData("minimo");
        minBox.getChildren().addAll(lblMin, txtMin);

        VBox maxBox = new VBox(5);
        Label lblMax = new Label("Valor máximo:");
        TextField txtMax = new TextField();
        txtMax.setPromptText("Ej: 100");
        txtMax.setPrefWidth(150);
        txtMax.setUserData("maximo");
        maxBox.getChildren().addAll(lblMax, txtMax);

        rangoBox.getChildren().addAll(minBox, maxBox);

        contenedor.getChildren().addAll(lblInfo, rangoBox);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param contenedor parámetro de entrada.
 */
    private void crearConfigLibre(VBox contenedor) {
        Label lblInfo = new Label("Define la longitud máxima del texto:");
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        HBox longitudBox = new HBox(15);
        longitudBox.setAlignment(Pos.CENTER_LEFT);

        Label lblLongitud = new Label("Longitud máxima:");
        Spinner<Integer> spinner = new Spinner<>(50, 5000, 1000, 50);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);
        spinner.setUserData("longitudMaxima");

        longitudBox.getChildren().addAll(lblLongitud, spinner);

        contenedor.getChildren().addAll(lblInfo, longitudBox);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param contenedor parámetro de entrada.
 * @param tipo parámetro de entrada.
 */
    private void crearConfigConOpciones(VBox contenedor, String tipo) {
        Label lblInfo = new Label("Escribe las opciones disponibles (una por línea):");
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        TextArea txtOpciones = new TextArea();
        txtOpciones.setPromptText("Opción 1\nOpción 2\nOpción 3\n...");
        txtOpciones.setPrefRowCount(6);
        txtOpciones.setPrefWidth(600);
        txtOpciones.setUserData("opciones");

        Label lblContador = new Label("Opciones: 0 (mínimo 2, máximo 50)");
        lblContador.setFont(Font.font("Arial", 11));
        lblContador.setStyle("-fx-text-fill: #95a5a6;");

        txtOpciones.textProperty().addListener((obs, old, nuevo) -> {
            int count = (int) nuevo.lines().filter(s -> !s.trim().isEmpty()).count();
            lblContador.setText("Opciones: " + count + " (mínimo 2, máximo 50)");

            if (count < 2) {
                lblContador.setStyle("-fx-text-fill: #e74c3c;");
            } else if (count > 50) {
                lblContador.setStyle("-fx-text-fill: #e74c3c;");
            } else {
                lblContador.setStyle("-fx-text-fill: #27ae60;");
            }
        });

        contenedor.getChildren().addAll(lblInfo, txtOpciones, lblContador);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param contenedor parámetro de entrada.
 */
    private void crearConfigMultiple(VBox contenedor) {
        Label lblInfo = new Label("Configura las opciones y el máximo de selecciones:");
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        TextArea txtOpciones = new TextArea();
        txtOpciones.setPromptText("Opción 1\nOpción 2\nOpción 3\n...");
        txtOpciones.setPrefRowCount(6);
        txtOpciones.setPrefWidth(600);
        txtOpciones.setUserData("opciones");

        HBox maxSelBox = new HBox(15);
        maxSelBox.setAlignment(Pos.CENTER_LEFT);
        maxSelBox.setPadding(new Insets(10, 0, 0, 0));

        Label lblMaxSel = new Label("Máximo de selecciones:");
        Spinner<Integer> spinnerMax = new Spinner<>(1, 10, 2);
        spinnerMax.setEditable(true);
        spinnerMax.setPrefWidth(100);
        spinnerMax.setUserData("maxSelecciones");

        maxSelBox.getChildren().addAll(lblMaxSel, spinnerMax);

        contenedor.getChildren().addAll(lblInfo, txtOpciones, maxSelBox);
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param enunciado parámetro de entrada.
 * @return resultado de la operación.
 */
    private boolean validarEnunciado(String enunciado) {
        if (enunciado.isEmpty()) {
            mostrarError("El enunciado no puede estar vacío");
            return false;
        }

        if (enunciado.length() < 5) {
            mostrarError("El enunciado debe tener al menos 5 caracteres");
            return false;
        }

        if (enunciado.length() > 500) {
            mostrarError("El enunciado no puede exceder 500 caracteres\nCaracteres actuales: " + enunciado.length());
            return false;
        }

        return true;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param tipo parámetro de entrada.
 * @param enunciado parámetro de entrada.
 * @param obligatoria parámetro de entrada.
 * @param contenidoEspecifico parámetro de entrada.
 */
    private void procesarCreacionPregunta(String tipo, String enunciado, boolean obligatoria, VBox contenidoEspecifico) {
        Map<String, Object> datosPregunta = new HashMap<>();
        datosPregunta.put("enunciado", enunciado);
        datosPregunta.put("obligatoria", obligatoria);

        try {
            switch (tipo) {
                case "Numérica":
                    if (!procesarNumerica(datosPregunta, contenidoEspecifico)) return;
                    break;
                case "Texto libre":
                    if (!procesarLibre(datosPregunta, contenidoEspecifico)) return;
                    break;
                case "Categoría simple":
                    if (!procesarConOpciones(datosPregunta, contenidoEspecifico, "CATEGORIA_SIMPLE")) return;
                    break;
                case "Ordinal":
                    if (!procesarConOpciones(datosPregunta, contenidoEspecifico, "ORDINAL")) return;
                    break;
                case "Categoría múltiple":
                    if (!procesarMultiple(datosPregunta, contenidoEspecifico)) return;
                    break;
            }

            // Llamar al controlador para añadir la pregunta
            String resultado = ctrl.addPregunta(idEncuesta, datosPregunta);

            if ("OK".equals(resultado)) {
                mostrarExito("Pregunta añadida exitosamente");
                // Volver a la vista de gestión de preguntas
                ctrl.mostrarGestionarPreguntas(idEncuesta);
            } else {
                mostrarError(resultado);
            }

        } catch (Exception e) {
            mostrarError("Error al crear la pregunta: " + e.getMessage());
        }
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param datos parámetro de entrada.
 * @param contenedor parámetro de entrada.
 * @return resultado de la operación.
 */
    private boolean procesarNumerica(Map<String, Object> datos, VBox contenedor) {
        TextField txtMin = buscarCampoPorUserData(contenedor, "minimo");
        TextField txtMax = buscarCampoPorUserData(contenedor, "maximo");

        String minStr = txtMin.getText().trim();
        String maxStr = txtMax.getText().trim();

        if (minStr.isEmpty() || maxStr.isEmpty()) {
            mostrarError("Debe introducir el mínimo y máximo");
            return false;
        }

        try {
            double min = Double.parseDouble(minStr);
            double max = Double.parseDouble(maxStr);

            if (min >= max) {
                mostrarError("El mínimo debe ser menor que el máximo\nMínimo: " + min + "\nMáximo: " + max);
                return false;
            }

            datos.put("tipo", "NUMERICA");
            datos.put("min", min);
            datos.put("max", max);
            return true;

        } catch (NumberFormatException e) {
            mostrarError("Los valores deben ser números válidos");
            return false;
        }
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param datos parámetro de entrada.
 * @param contenedor parámetro de entrada.
 * @return resultado de la operación.
 */
    private boolean procesarLibre(Map<String, Object> datos, VBox contenedor) {
        Spinner<Integer> spinner = buscarSpinnerPorUserData(contenedor, "longitudMaxima");
        int longitud = spinner.getValue();

        if (longitud < 10) {
            mostrarError("La longitud máxima debe ser al menos 10 caracteres");
            return false;
        }

        if (longitud > 10000) {
            mostrarError("La longitud máxima no puede exceder 10000 caracteres");
            return false;
        }

        datos.put("tipo", "LIBRE");
        datos.put("longitudMaxima", longitud);
        return true;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param datos parámetro de entrada.
 * @param contenedor parámetro de entrada.
 * @param tipoPregunta parámetro de entrada.
 * @return resultado de la operación.
 */
    private boolean procesarConOpciones(Map<String, Object> datos, VBox contenedor, String tipoPregunta) {
        TextArea txtOpciones = buscarTextAreaPorUserData(contenedor, "opciones");
        String texto = txtOpciones.getText();

        List<String> opciones = new ArrayList<>();
        for (String linea : texto.split("\n")) {
            String opcion = linea.trim();
            if (!opcion.isEmpty()) {
                opciones.add(opcion);
            }
        }

        if (opciones.isEmpty()) {
            mostrarError("Debe proporcionar al menos una opción");
            return false;
        }

        if (opciones.size() < 2) {
            mostrarError("Debe proporcionar al menos 2 opciones");
            return false;
        }

        if (opciones.size() > 50) {
            mostrarError("No puede exceder 50 opciones\nOpciones actuales: " + opciones.size());
            return false;
        }

        for (String opcion : opciones) {
            if (opcion.length() > 200) {
                mostrarError("Una opción excede 200 caracteres: \"" + opcion.substring(0, 50) + "...\"");
                return false;
            }
        }

        datos.put("tipo", tipoPregunta);
        datos.put("opciones", opciones);
        return true;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param datos parámetro de entrada.
 * @param contenedor parámetro de entrada.
 * @return resultado de la operación.
 */
    private boolean procesarMultiple(Map<String, Object> datos, VBox contenedor) {
        TextArea txtOpciones = buscarTextAreaPorUserData(contenedor, "opciones");
        Spinner<Integer> spinnerMax = buscarSpinnerPorUserData(contenedor, "maxSelecciones");

        String texto = txtOpciones.getText();
        List<String> opciones = new ArrayList<>();
        for (String linea : texto.split("\n")) {
            String opcion = linea.trim();
            if (!opcion.isEmpty()) {
                opciones.add(opcion);
            }
        }

        int maxSelecciones = spinnerMax.getValue();

        if (opciones.isEmpty()) {
            mostrarError("Debe proporcionar al menos una opción");
            return false;
        }

        if (opciones.size() < 2) {
            mostrarError("Debe proporcionar al menos 2 opciones");
            return false;
        }

        if (maxSelecciones < 1) {
            mostrarError("El máximo de selecciones debe ser al menos 1");
            return false;
        }

        if (maxSelecciones > opciones.size()) {
            mostrarError("El máximo de selecciones (" + maxSelecciones +
                    ") no puede ser mayor que el número de opciones (" + opciones.size() + ")");
            return false;
        }

        datos.put("tipo", "CATEGORIA_MULTIPLE");
        datos.put("opciones", opciones);
        datos.put("maxSelecciones", maxSelecciones);
        return true;
    }

    // Métodos auxiliares para buscar componentes
/**
 * Ejecuta la funcionalidad de la vista.
 *
 * @param contenedor parámetro de entrada.
 * @param userData parámetro de entrada.
 * @return resultado de la operación.
 */
    private TextField buscarCampoPorUserData(VBox contenedor, String userData) {
        return buscarComponente(contenedor, TextField.class, userData);
    }

    private Spinner<Integer> buscarSpinnerPorUserData(VBox contenedor, String userData) {
        return buscarComponente(contenedor, Spinner.class, userData);
    }

    private TextArea buscarTextAreaPorUserData(VBox contenedor, String userData) {
        return buscarComponente(contenedor, TextArea.class, userData);
    }

    @SuppressWarnings("unchecked")
    private <T> T buscarComponente(VBox contenedor, Class<T> tipo, String userData) {
        for (var nodo : contenedor.getChildren()) {
            if (tipo.isInstance(nodo) && userData.equals(nodo.getUserData())) {
                return (T) nodo;
            }
            if (nodo instanceof HBox) {
                for (var hijo : ((HBox) nodo).getChildren()) {
                    if (tipo.isInstance(hijo) && userData.equals(hijo.getUserData())) {
                        return (T) hijo;
                    }
                    if (hijo instanceof VBox) {
                        for (var nieto : ((VBox) hijo).getChildren()) {
                            if (tipo.isInstance(nieto) && userData.equals(nieto.getUserData())) {
                                return (T) nieto;
                            }
                        }
                    }
                }
            }
        }
        return null;
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

        Button btnCancelar = new Button("✕ Cancelar");
        btnCancelar.setPrefWidth(150);
        btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnCancelar.setOnAction(e -> ctrl.mostrarGestionarPreguntasMenu());

        footer.getChildren().add(btnCancelar);
        return footer;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
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

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
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
