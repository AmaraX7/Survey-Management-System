package main.presentation;

import javafx.application.Application;
import javafx.stage.Stage;
import main.presentation.controllers.CtrlPresentacion;

/**
 * Punto de entrada de la aplicación JavaFX
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        CtrlPresentacion ctrlPresentacion = new CtrlPresentacion(primaryStage);
        ctrlPresentacion.iniciar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}