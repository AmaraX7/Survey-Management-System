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
 * Vista para ejecutar y visualizar clustering.
 * <p>
 * Esta clase proporciona una interfaz gráfica para:
 * <ul>
 *   <li>Seleccionar el algoritmo de clustering y parámetros.</li>
 *   <li>Ejecutar el clustering sobre una encuesta.</li>
 *   <li>Visualizar los resultados en forma de clusters.</li>
 *   <li>Gestionar el historial de resultados.</li>
 * </ul>
 */
public class ClusteringView {
    private final Scene scene;
    private final CtrlPresentacion ctrl;
    private final String idEncuesta;

    private ComboBox<String> cmbAlgoritmo;
    private Spinner<Integer> spinnerK;
    private Spinner<Integer> spinnerIteraciones;
    private VBox contenedorResultados;

/**
 * Crea una instancia de la vista.
 *
 * @param ctrl parámetro de entrada.
 * @param idEncuesta parámetro de entrada.
 */
    public ClusteringView(CtrlPresentacion ctrl, String idEncuesta) {
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
 * @param encontrada" parámetro de entrada.
 * @return resultado de la operación.
 */
            return crearEscenaError("Encuesta no encontrada");
        }

        String titulo = (String) datosEncuesta.get("titulo");

        // Header
        VBox header = crearHeader(titulo);
        root.setTop(header);

        // Centro: Configuración y resultados
        ScrollPane scrollPane = crearContenido();
        root.setCenter(scrollPane);

        // Footer: Botones
        HBox footer = crearFooter();
        root.setBottom(footer);

        return new Scene(root, 1000, 700);
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param titulo parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearHeader(String titulo) {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 0, 20, 0));
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        header.setPadding(new Insets(20));

        Label lblTitulo = new Label("Análisis de Clustering");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitulo = new Label(titulo);
        subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(lblTitulo, subtitulo);
        return header;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private ScrollPane crearContenido() {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(20));
        contenedor.setAlignment(Pos.TOP_CENTER);

        // Panel de configuración
        VBox panelConfig = crearPanelConfiguracion();
        contenedor.getChildren().add(panelConfig);

        // Contenedor para resultados
        contenedorResultados = new VBox(15);
        contenedorResultados.setAlignment(Pos.TOP_CENTER);

        // Mostrar resultados guardados si existen
        cargarResultadosGuardados();

        contenedor.getChildren().add(contenedorResultados);

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        return scroll;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @return resultado de la operación.
 */
    private VBox crearPanelConfiguracion() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        panel.setMaxWidth(700);

        Label lblTitulo = new Label("Configuración de Clustering");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        // Algoritmo
        Label lblAlgoritmo = new Label("Algoritmo:");
        lblAlgoritmo.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        cmbAlgoritmo = new ComboBox<>();
        cmbAlgoritmo.getItems().addAll("KMeans", "KMeans++", "KMedoids");
        cmbAlgoritmo.setValue("KMeans++");
        cmbAlgoritmo.setPrefWidth(200);

        // K máximo
        Label lblK = new Label("K máximo (Se recomienda que sea <15):");
        lblK.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        spinnerK = new Spinner<>(2, 50, 5);
        spinnerK.setPrefWidth(100);
        spinnerK.setEditable(true);

        // Iteraciones máximas
        Label lblIter = new Label("Iteraciones máximas:");
        lblIter.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        spinnerIteraciones = new Spinner<>(10, 1000, 300);
        spinnerIteraciones.setPrefWidth(100);
        spinnerIteraciones.setEditable(true);

        grid.add(lblAlgoritmo, 0, 0);
        grid.add(cmbAlgoritmo, 1, 0);
        grid.add(lblK, 0, 1);
        grid.add(spinnerK, 1, 1);
        grid.add(lblIter, 0, 2);
        grid.add(spinnerIteraciones, 1, 2);

        // Botón ejecutar
        Button btnEjecutar = new Button("Ejecutar Clustering");
        btnEjecutar.setPrefWidth(250);
        btnEjecutar.setPrefHeight(40);
        btnEjecutar.setStyle(
                "-fx-background-color: #2ecc71;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;"
        );
        btnEjecutar.setOnAction(e -> ejecutarClustering());

        // Botón limpiar
        Button btnLimpiar = new Button("Limpiar Historial");
        btnLimpiar.setPrefWidth(200);
        btnLimpiar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnLimpiar.setOnAction(e -> limpiarHistorial());

        HBox botones = new HBox(10, btnEjecutar, btnLimpiar);
        botones.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(lblTitulo, grid, botones);
        return panel;
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void ejecutarClustering() {
        try {
            String algoritmo = cmbAlgoritmo.getValue();
            int kMax = spinnerK.getValue();
            int maxIter = spinnerIteraciones.getValue();

            // Validaciones
            if (algoritmo == null || algoritmo.isEmpty()) {
                mostrarError("Debe seleccionar un algoritmo");
                return;
            }

            if (kMax < 2 || kMax > 50) {
                mostrarError("K máximo debe estar entre 2 y 50");
                return;
            }

            if (maxIter < 10 || maxIter > 1000) {
                mostrarError("Las iteraciones máximas deben estar entre 10 y 1000");
                return;
            }

            int numUsuarios = ctrl.obtenerNumeroRespuestas(idEncuesta);

            if (numUsuarios == 0) {
                mostrarError("No hay respuestas para analizar");
                return;
            }

            if (numUsuarios < kMax) {
                mostrarError("K máximo (" + kMax + ") debe ser menor o igual al número de usuarios (" +
                        numUsuarios + ")");
                return;
            }

            // Mostrar progreso
            Alert progress = new Alert(Alert.AlertType.INFORMATION);
            progress.setTitle("Ejecutando Clustering");
            progress.setHeaderText("Por favor espere...");
            progress.setContentText("Analizando datos con " + algoritmo);
            progress.show();

            // Ejecutar en hilo separado
            new Thread(() -> {
                try {
                    List<Map<String, Object>> resultados =
                            ctrl.ejecutarClusteringYObtenerResultados(
                                    idEncuesta, algoritmo, kMax, maxIter
                            );

                    javafx.application.Platform.runLater(() -> {
                        progress.close();
                        mostrarResultados(resultados);
                        mostrarInfo("Clustering ejecutado exitosamente");
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        progress.close();
                        mostrarError("Error al ejecutar clustering: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            }).start();

        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void cargarResultadosGuardados() {
        List<Map<String, Object>> resultados = ctrl.obtenerHistorialClustering(idEncuesta);

        if (!resultados.isEmpty()) {
            mostrarResultados(resultados);
        } else {
            Label lblVacio = new Label("No hay resultados guardados. Configure y ejecute el clustering.");
            lblVacio.setFont(Font.font("Arial", 14));
            lblVacio.setStyle("-fx-text-fill: #7f8c8d;");
            contenedorResultados.getChildren().add(lblVacio);
        }
    }

/**
 * Muestra la información indicada en la vista.
 *
 * @param resultados parámetro de entrada.
 */
    private void mostrarResultados(List<Map<String, Object>> resultados) {
        contenedorResultados.getChildren().clear();

        if (resultados.isEmpty()) {
            Label lblVacio = new Label("No se generaron resultados");
            contenedorResultados.getChildren().add(lblVacio);
            return;
        }

        Label lblTitulo = new Label("Resultados del Análisis:");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");
        contenedorResultados.getChildren().add(lblTitulo);

        // Encontrar mejor resultado
        Map<String, Object> mejor = resultados.get(0);
        for (Map<String, Object> r : resultados) {
            Double silhouette = (Double) r.get("silhouette");
            Double mejorSilhouette = (Double) mejor.get("silhouette");
            if (silhouette > mejorSilhouette) {
                mejor = r;
            }
        }

        // Mostrar mejor resultado destacado
        VBox cardMejor = crearCardResultado(mejor, true);
        contenedorResultados.getChildren().add(cardMejor);

        // Mostrar otros resultados
        if (resultados.size() > 1) {
            Label lblOtros = new Label("Otros resultados:");
            lblOtros.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            lblOtros.setStyle("-fx-text-fill: #34495e;");
            contenedorResultados.getChildren().add(lblOtros);

            for (Map<String, Object> r : resultados) {
                if (r != mejor) {
                    VBox card = crearCardResultado(r, false);
                    contenedorResultados.getChildren().add(card);
                }
            }
        }
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param resultado parámetro de entrada.
 * @param esMejor parámetro de entrada.
 * @return resultado de la operación.
 */
    private VBox crearCardResultado(Map<String, Object> resultado, boolean esMejor) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setMaxWidth(800);

        if (esMejor) {
            card.setStyle(
                    "-fx-background-color: #d5f4e6;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #27ae60;" +
                            "-fx-border-width: 3;" +
                            "-fx-border-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);"
            );

            Label lblMejor = new Label("MEJOR RESULTADO");
            lblMejor.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            lblMejor.setStyle("-fx-text-fill: #27ae60;");
            card.getChildren().add(lblMejor);
        } else {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-radius: 8;" +
                            "-fx-border-width: 1;"
            );
        }

        Integer k = (Integer) resultado.get("k");
        String algoritmo = (String) resultado.get("algoritmo");
        Double silhouette = (Double) resultado.get("silhouette");

        // Información principal
        HBox info = new HBox(30);
        info.setAlignment(Pos.CENTER_LEFT);

        Label lblK = new Label("K = " + k);
        lblK.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblK.setStyle("-fx-text-fill: #2c3e50;");

        Label lblAlg = new Label("Algoritmo: " + algoritmo);
        lblAlg.setFont(Font.font("Arial", 14));

        Label lblSilhouette = new Label("Silhouette: " + String.format("%.4f", silhouette));
        lblSilhouette.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblSilhouette.setStyle("-fx-text-fill: #2c3e50;");

        info.getChildren().addAll(lblK, lblAlg, lblSilhouette);
        card.getChildren().add(info);

        // Distribución de clusters
        Label lblDistribucion = new Label("Distribución de usuarios por cluster:");
        lblDistribucion.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        card.getChildren().add(lblDistribucion);

        @SuppressWarnings("unchecked")
        List<List<String>> usuariosPorGrupo = (List<List<String>>) resultado.get("usuariosPorGrupo");

        // Calcular total de usuarios
        int totalUsuarios = usuariosPorGrupo.stream()
                .mapToInt(List::size)
                .sum();

        VBox contenedorClusters = new VBox(8);
        for (int i = 0; i < k; i++) {
            List<String> idsUsuarios = usuariosPorGrupo.get(i);
            HBox clusterInfo = crearInfoClusterVisual(i, idsUsuarios.size(), totalUsuarios);
            contenedorClusters.getChildren().add(clusterInfo);
        }

        card.getChildren().add(contenedorClusters);

        // Botón ver detalles
        Button btnDetalles = new Button("Ver Usuarios en Clusters");
        btnDetalles.setPrefWidth(250);
        btnDetalles.setPrefHeight(35);
        btnDetalles.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5;");

        btnDetalles.setOnMouseEntered(e ->
                btnDetalles.setStyle(
                        "-fx-background-color: #2980b9; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 13; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 5;"));

        btnDetalles.setOnMouseExited(e ->
                btnDetalles.setStyle(
                        "-fx-background-color: #3498db; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 13; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 5;"));

        btnDetalles.setOnAction(e -> mostrarDetallesResultado(resultado));

        card.getChildren().add(btnDetalles);

        return card;
    }

/**
 * Crea y configura el componente de interfaz correspondiente.
 *
 * @param numCluster parámetro de entrada.
 * @param cantidad parámetro de entrada.
 * @param totalUsuarios parámetro de entrada.
 * @return resultado de la operación.
 */
    private HBox crearInfoClusterVisual(int numCluster, int cantidad, int totalUsuarios) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(8));

        // Label del cluster
        VBox labelBox = new VBox(2);
        labelBox.setPrefWidth(100);

        Label lblCluster = new Label("Cluster " + numCluster);
        lblCluster.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblCluster.setStyle("-fx-text-fill: #2c3e50;");

        Label lblCantidad = new Label(cantidad + " usuarios");
        lblCantidad.setFont(Font.font("Arial", 11));
        lblCantidad.setStyle("-fx-text-fill: #7f8c8d;");

        labelBox.getChildren().addAll(lblCluster, lblCantidad);

        // Calcular porcentaje
        double porcentaje = totalUsuarios > 0 ? (cantidad * 100.0 / totalUsuarios) : 0;

        // Barra de progreso
        HBox barraContenedor = new HBox();
        barraContenedor.setAlignment(Pos.CENTER_LEFT);
        barraContenedor.setPrefWidth(350);
        barraContenedor.setPrefHeight(28);
        barraContenedor.setStyle(
                "-fx-background-color: #ecf0f1; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-color: #bdc3c7; " +
                        "-fx-border-radius: 5; " +
                        "-fx-border-width: 1;");

        Region barra = new Region();
        barra.setPrefHeight(28);
        barra.setMinWidth(0);
        barra.setPrefWidth(350 * (porcentaje / 100.0));
        barra.setMaxWidth(350);
        barra.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-background-radius: 5 0 0 5;");

        barraContenedor.getChildren().add(barra);

        // Label del porcentaje
        Label lblPorcentaje = new Label(String.format("%.1f%%", porcentaje));
        lblPorcentaje.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblPorcentaje.setStyle("-fx-text-fill: #34495e;");
        lblPorcentaje.setPrefWidth(60);

        container.getChildren().addAll(labelBox, barraContenedor, lblPorcentaje);

        // Efecto hover
        container.setOnMouseEntered(e -> {
            container.setStyle(
                    "-fx-background-color: #f8f9fa; " +
                            "-fx-background-radius: 5; " +
                            "-fx-padding: 8;");
        });

        container.setOnMouseExited(e -> {
            container.setStyle("-fx-padding: 8;");
        });

        return container;
    }

/**
 * Muestra la información indicada en la vista.
 *
 * @param resultado parámetro de entrada.
 */
    private void mostrarDetallesResultado(Map<String, Object> resultado) {
        Dialog<Void> dialog = new Dialog<>();

        Integer k = (Integer) resultado.get("k");
        dialog.setTitle("Detalles del Clustering - K=" + k);
        dialog.setHeaderText("Distribución de usuarios en clusters");

        VBox contenido = new VBox(10);
        contenido.setPadding(new Insets(15));

        @SuppressWarnings("unchecked")
        List<List<String>> usuariosPorGrupo = (List<List<String>>) resultado.get("usuariosPorGrupo");

        for (int i = 0; i < k; i++) {
            List<String> idsUsuarios = usuariosPorGrupo.get(i);

            Label lblCluster = new Label("CLUSTER " + i + " (" + idsUsuarios.size() + " usuarios):");
            lblCluster.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            lblCluster.setStyle("-fx-text-fill: #2c3e50;");

            TextArea txtUsuarios = new TextArea();
            txtUsuarios.setEditable(false);
            txtUsuarios.setPrefRowCount(3);
            txtUsuarios.setWrapText(true);

            StringBuilder sb = new StringBuilder();
            for (String idUsuario : idsUsuarios) {
                String nombreUsuario = ctrl.obtenerNombreUsuarioPorId(idUsuario);
                sb.append("• ").append(nombreUsuario).append(" (ID: ").append(idUsuario).append(")\n");
            }
            txtUsuarios.setText(sb.toString());

            contenido.getChildren().addAll(lblCluster, txtUsuarios);
        }

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

/**
 * Ejecuta la funcionalidad de la vista.
 */
    private void limpiarHistorial() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText("¿Limpiar historial de clustering?");
        confirm.setContentText("Se eliminarán todos los resultados guardados.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean exito = ctrl.limpiarHistorialClustering(idEncuesta);
                if (exito) {
                    contenedorResultados.getChildren().clear();
                    Label lblVacio = new Label("⚠️ No hay resultados guardados.");
                    lblVacio.setFont(Font.font("Arial", 14));
                    contenedorResultados.getChildren().add(lblVacio);
                    mostrarInfo("Historial limpiado");
                } else {
                    mostrarError("No había resultados para limpiar");
                }
            }
        });
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
        btnVolver.setPrefWidth(180);
        btnVolver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14;");
        btnVolver.setOnAction(e -> ctrl.mostrarAnalisisResultados());

        Button btnMenu = new Button("Menú Principal");
        btnMenu.setPrefWidth(150);
        btnMenu.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14;");
        btnMenu.setOnAction(e -> ctrl.mostrarMenuPrincipal(ctrl.getUsuarioActualId(), true));

        footer.getChildren().addAll(btnVolver, btnMenu);
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
        Label lbl = new Label(mensaje);
        Button btnVolver = new Button("← Volver");
        btnVolver.setOnAction(e -> ctrl.mostrarListarEncuestas());
        root.getChildren().addAll(lbl, btnVolver);
        return new Scene(root, 800, 600);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

/**
 * Muestra la información indicada en la vista.
 *
 * @param mensaje parámetro de entrada.
 */
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
