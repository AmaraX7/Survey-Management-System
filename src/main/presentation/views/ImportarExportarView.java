package main.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Node;

import main.presentation.controllers.CtrlPresentacion;

/**
 * Vista para importar/exportar.
 * <p>
 * Casos de uso incluidos:
 * - Importar encuesta desde CSV
 * - Exportar encuesta a CSV
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Redirigir a la vista de importar desde CSV.</li>
 *   <li>Redirigir a la vista de exportar a CSV.</li>
 * </ul>
 */
public class ImportarExportarView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;

    public ImportarExportarView(CtrlPresentacion ctrl) {
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
        root.setPadding(new Insets(30));

        // Header
        VBox header = crearHeader();
        root.setTop(header);

        // Centro
        VBox centro = crearCentro();
        root.setCenter(centro);

        // Footer
        HBox footer = crearFooter();
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
        header.setPadding(new Insets(0, 0, 40, 0));

        Label titulo = new Label("Importar / Exportar");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Gestiona tus datos en formato CSV");
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
    private VBox crearCentro() {
        VBox contenedor = new VBox(30);
        contenedor.setAlignment(Pos.CENTER);

        // CU: Importar encuesta desde CSV
        VBox panelImportar = crearPanelImportar();

        // CU: Exportar encuesta a CSV
        VBox panelExportar = crearPanelExportar();

        contenedor.getChildren().addAll(panelImportar, panelExportar);

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setPadding(Insets.EMPTY);

        VBox wrapper = new VBox(scroll);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setFillWidth(true);

        return wrapper;
    }


/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelImportar() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        panel.setMaxWidth(650);

        // Header del panel
        HBox headerPanel = new HBox(15);
        headerPanel.setAlignment(Pos.CENTER_LEFT);

        Label icono = new Label("★");

        VBox textos = new VBox(5);
        Label lblTitulo = new Label("Importar desde CSV");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        Label lblDesc = new Label("Carga una encuesta completa desde un archivo CSV");
        lblDesc.setFont(Font.font("Arial", 13));
        lblDesc.setStyle("-fx-text-fill: #7f8c8d;");

        textos.getChildren().addAll(lblTitulo, lblDesc);
        headerPanel.getChildren().addAll(icono, textos);

        // Información del formato
        VBox infoFormato = new VBox(8);
        infoFormato.setPadding(new Insets(15));
        infoFormato.setStyle(
                "-fx-background-color: #e8f5e9;" +
                        "-fx-background-radius: 5;"
        );

        Label lblInfoTitulo = new Label("Formato esperado:");
        lblInfoTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblInfoTitulo.setStyle("-fx-text-fill: #27ae60;");

        Label info1 = new Label("• Primera fila: nombres de columnas");
        Label info2 = new Label("• Primera columna: ID de usuario");
        Label info3 = new Label("• Resto: respuestas (tipos inferidos automáticamente)");

        for (Label lbl : new Label[]{info1, info2, info3}) {
            lbl.setFont(Font.font("Arial", 11));
            lbl.setStyle("-fx-text-fill: #27ae60;");
        }

        infoFormato.getChildren().addAll(lblInfoTitulo, info1, info2, info3);

        // Botón
        Button btnImportar = new Button("Seleccionar archivo CSV");
        btnImportar.setPrefWidth(300);
        btnImportar.setPrefHeight(50);
        btnImportar.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;"
        );
        btnImportar.setOnAction(e -> ctrl.mostrarImportarCSV());

        panel.getChildren().addAll(headerPanel, infoFormato, btnImportar);
        return panel;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelExportar() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        panel.setMaxWidth(650);

        // Header del panel
        HBox headerPanel = new HBox(15);
        headerPanel.setAlignment(Pos.CENTER_LEFT);

        Label icono = new Label("★");

        VBox textos = new VBox(5);
        Label lblTitulo = new Label("Exportar a CSV");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        Label lblDesc = new Label("Descarga los datos de una encuesta en formato CSV");
        lblDesc.setFont(Font.font("Arial", 13));
        lblDesc.setStyle("-fx-text-fill: #7f8c8d;");

        textos.getChildren().addAll(lblTitulo, lblDesc);
        headerPanel.getChildren().addAll(icono, textos);

        // Información del formato
        VBox infoFormato = new VBox(8);
        infoFormato.setPadding(new Insets(15));
        infoFormato.setStyle(
                "-fx-background-color: #e3f2fd;" +
                        "-fx-background-radius: 5;"
        );

        Label lblInfoTitulo = new Label("Qué incluye:");
        lblInfoTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblInfoTitulo.setStyle("-fx-text-fill: #2196f3;");

        Label info1 = new Label("• Todas las preguntas de la encuesta");
        Label info2 = new Label("• Respuestas de todos los usuarios");
        Label info3 = new Label("• Compatible para re-importación");

        for (Label lbl : new Label[]{info1, info2, info3}) {
            lbl.setFont(Font.font("Arial", 11));
            lbl.setStyle("-fx-text-fill: #2196f3;");
        }

        infoFormato.getChildren().addAll(lblInfoTitulo, info1, info2, info3);

        // Botón
        Button btnExportar = new Button("Seleccionar encuesta");
        btnExportar.setPrefWidth(300);
        btnExportar.setPrefHeight(50);
        btnExportar.setStyle(
                "-fx-background-color: #2196f3;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;"
        );
        btnExportar.setOnAction(e -> ctrl.mostrarExportarCSV());

        panel.getChildren().addAll(headerPanel, infoFormato, btnExportar);
        return panel;
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

        Button btnVolver = new Button("← Volver al Menú");
        btnVolver.setPrefWidth(180);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), true));

        footer.getChildren().add(btnVolver);
        return footer;
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
