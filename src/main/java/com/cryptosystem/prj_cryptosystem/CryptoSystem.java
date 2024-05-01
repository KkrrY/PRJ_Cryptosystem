package com.cryptosystem.prj_cryptosystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class CryptoSystem extends Application {
    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(CryptoSystem.class.getResource("crypto-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),
                Screen.getPrimary().getBounds().getWidth(),
                Screen.getPrimary().getBounds().getHeight() - 50);
        stage.setTitle("Cryptos");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}