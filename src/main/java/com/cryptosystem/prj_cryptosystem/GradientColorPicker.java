package com.cryptosystem.prj_cryptosystem;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;

public class GradientColorPicker {
    private enum Positioning {
        CENTER_X(gradientCanvas.getWidth() / 2.0),
        CENTER_Y(gradientCanvas.getHeight() / 2.0),
        RADIUS(gradientCanvas.getWidth() / 2.0);
        private final double value;
        Positioning (double value) {
            this.value = value;
        }
    }
    private enum Shape { TRIANGULAR, ROUND }
    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;

    private final Circle colorPreview = new Circle(HEIGHT / 8.0);
    private static final Canvas gradientCanvas = new Canvas(HEIGHT / 1.33, HEIGHT / 1.33);

    private final Canvas gradientColorPicker = new Canvas(HEIGHT / 3.0, HEIGHT / 3.0);

    private final Stage stage = new Stage();
    public ArrayList<RadioButton> radioButtonList;
    private TitledPane buttonMenu;
    private final Button selectAllButton;
    public Canvas getGradientColorPicker() {
        return gradientColorPicker;
    }


    public void addRadioButtons(Node[] nodes) {

        VBox radioBox = new VBox(radioButtonList.stream().toArray(RadioButton[]::new));
        radioBox.setSpacing(5);
        radioBox.getChildren().add(selectAllButton);
        for (Node node : nodes) {
            RadioButton radioButton = new RadioButton(node.getClass().getSimpleName());
            radioButton.setSelected(true);

            radioBox.getChildren().add(radioButton);
            radioButtonList.add(radioButton);
        }

        AnchorPane.setTopAnchor(radioBox, 30.0);
        AnchorPane.setLeftAnchor(radioBox, 20.0);

        //AnchorPane.setLeftAnchor(selectAllButton, 150.0);

        AnchorPane root = (AnchorPane) stage.getScene().getRoot();
        buttonMenu = new TitledPane("Elements", radioBox);
        buttonMenu.setExpanded(false);
        root.getChildren().addAll(buttonMenu);

        root.setOnMouseClicked(event -> {
            // Check if the mouse click is outside of the TitledPane bounds
            if (!buttonMenu.contains(event.getX(), event.getY())) {
                buttonMenu.setExpanded(false);
            }
        });

    }

    public GradientColorPicker()  {
        radioButtonList = new ArrayList<>();
        selectAllButton = new Button("De/Select All");
        final int[] counter = {0};

        selectAllButton.setId("CloseButton");

        selectAllButton.setOnAction((ActionEvent e) -> {
            if (counter[0] %2 == 0) {
                for (RadioButton radioButton : radioButtonList) {
                    radioButton.setSelected(false);
                }
            }
            else {
                for (RadioButton radioButton : radioButtonList) {
                    radioButton.setSelected(true);
                }
            }
            counter[0]++;
        });

        GridPane gridPane = new GridPane();
        gridPane.setMinSize(WIDTH, HEIGHT);
        gridPane.setStyle("-fx-background-color: #5a5c5dff ");
        //#6c6e6fff

        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #31333b;");
        Menu menuFile = new Menu();
        menuBar.getMenus().add(menuFile);
        VBox menu = new VBox();
        menu.getChildren().add(menuBar);
        menu.setMinWidth(WIDTH);
        menu.setLayoutY(0);

        Button closeButton = new Button("X");
        closeButton.setLayoutX(WIDTH - 30);
        closeButton.setId("CloseButton");

        stage.initStyle(StageStyle.UNDECORATED);
        // Set up the color preview box
        colorPreview.setFill(Color.BLACK);

        // Set up the gradient square
        GraphicsContext gc = gradientCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillOval(0, 0, gradientCanvas.getWidth(), gradientCanvas.getHeight());

        Circle circle = new Circle(gradientCanvas.getHeight() / 2.5);
        circle.setFill(Paint.valueOf("#5a5c5dff"));

        // Set up the triangular gradient
        GraphicsContext tc = gradientColorPicker.getGraphicsContext2D();
        //tc.setFill(Color.WHITE);
        //tc.fillPolygon(new double[]{0, 200, 0}, new double[]{0, 200, 200}, 3);

        renderChooser(gc);
        updateZone(tc, Color.BLACK, Shape.ROUND);

        gradientCanvas.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();
            chooseColor(x, y);
            updateZone(tc, (Color) colorPreview.getFill(), Shape.ROUND);
        });
//        gradientColorPicker.setOnMouseDragged(event -> {
//            chooseColor(event);
//        });

        HBox buttonBox = new HBox(radioButtonList.toArray(RadioButton[]::new));

        VBox vBox = new VBox();
        vBox.getChildren().addAll(buttonBox, menu);
        AnchorPane.setTopAnchor(vBox, 30.0);
        AnchorPane.setLeftAnchor(vBox, 20.0);

        AnchorPane root = new AnchorPane();

        root.getChildren().addAll(gridPane, colorPreview, gradientCanvas, circle, gradientColorPicker, menu, closeButton, vBox );

        // Center the gradient canvas
        AnchorPane.setTopAnchor(gradientCanvas, (HEIGHT - gradientCanvas.getHeight()) / 2);
        AnchorPane.setLeftAnchor(gradientCanvas, (WIDTH - gradientCanvas.getWidth()) / 2);

        // Center the triangle canvas within the gradient canvas
        AnchorPane.setTopAnchor(gradientColorPicker, AnchorPane.getTopAnchor(gradientCanvas) + (gradientCanvas.getHeight() - gradientColorPicker.getHeight()) / 2);
        AnchorPane.setLeftAnchor(gradientColorPicker, AnchorPane.getLeftAnchor(gradientCanvas) + (gradientCanvas.getWidth() - gradientColorPicker.getWidth()) / 2);

        // Center the color preview circle to the right of the gradient canvas
        AnchorPane.setTopAnchor(colorPreview, AnchorPane.getTopAnchor(gradientCanvas) + gradientCanvas.getHeight() / 2 - colorPreview.getRadius());
        AnchorPane.setLeftAnchor(colorPreview, AnchorPane.getLeftAnchor(gradientCanvas) + gradientCanvas.getWidth() + 50);

        // Center the gradient canvas
        AnchorPane.setTopAnchor(circle, (HEIGHT - circle.getRadius() * 2) / 2);
        AnchorPane.setLeftAnchor(circle, (WIDTH - circle.getRadius() * 2) / 2);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource("__PRJ_CryptoSystem_Stylesheet.css").toExternalForm());
        stage.setScene(scene);

        Listener.AddCloseAction(closeButton);
        Listener.getOffsets(menuBar);
        Listener.setDraggable(menuBar, stage);

    }

    public void show() {
        stage.show();
    }

    public Color chooseColor (MouseEvent event ) {
        return pickColorFromZone(event, gradientColorPicker);
    }

    private Color pickColorFromZone(MouseEvent event, Canvas canvas) {
        double x = event.getX();
        double y = event.getY();
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return null;
        }

        // Create a snapshot of the GraphicsContext
        //WritableImage snapshot = gc.getCanvas().snapshot(null, null);
        // Get the pixel reader from the snapshot
        //PixelReader reader = snapshot.getPixelReader();

        Color color = canvas.snapshot(null, null).getPixelReader().getColor((int) x, (int) y);
        //Color color = reader.getColor((int) x, (int) y);
        //System.out.println("Picked color: " + color);
        colorPreview.setFill(color);

        return color;
    }
    private void updateZone(GraphicsContext gc, Color color, Shape shape) {
        // clear the canvas
        gc.clearRect(0, 0, gradientColorPicker.getWidth(), gradientColorPicker.getHeight());

        // calculate the side length of the equilateral triangle
        double sideLength = Math.min(gradientColorPicker.getWidth(), gradientColorPicker.getHeight()) /* * 0.8 */;  //* 0.8 is a padding
        // calculate the coordinates of the vertices of the triangle
        double x1 = gradientColorPicker.getWidth() / 2.0;
        double y1 = (gradientColorPicker.getHeight() - sideLength * Math.sqrt(3) / 2) / 2.0;
        double x2 = x1 - sideLength / 2.0;
        double y2 = y1 + sideLength * Math.sqrt(3) / 2.0;
        double x3 = x1 + sideLength / 2.0;
        double y3 = y2;

        // create an array of x coordinates for the vertices of the triangle
        double[] xPoints = {x1, x2, x3};

        // create an array of y coordinates for the vertices of the triangle
        double[] yPoints = {y1, y2, y3};

        // create a gradient fill for the triangle
        Stop[] stops = {new Stop(0.5, color), new Stop(1, Color.BLACK), new Stop(0.0, Color.DARKGRAY)};
        LinearGradient gradient = new LinearGradient(x1, y1, x3, y3, false, CycleMethod.NO_CYCLE, stops);

        // fill the triangle with the gradient
        gc.setFill(gradient);

        if (shape.equals(Shape.TRIANGULAR)) gc.fillPolygon(xPoints, yPoints, 3);
        if (shape.equals(Shape.ROUND)) gc.fillRect(0,0, gradientColorPicker.getWidth(), gradientColorPicker.getHeight());
    }


    private void chooseColor (double x, double y) {

        double distance = Math.sqrt(Math.pow(x - Positioning.CENTER_X.value, 2) + Math.pow(y - Positioning.CENTER_Y.value, 2));
        if (distance <= Positioning.RADIUS.value) {
            double angle = Math.atan2(y - Positioning.CENTER_Y.value, x - Positioning.CENTER_X.value);
            double hue = Math.toDegrees(angle) + 180;
            double saturation = distance / Positioning.RADIUS.value;
            Color color = Color.hsb(hue, saturation, 1.0);
            colorPreview.setFill(color);
        }
    }

    private void renderChooser (GraphicsContext gc) {
        for (int y = 0; y < gradientCanvas.getHeight(); y++) {
            for (int x = 0; x < gradientCanvas.getWidth(); x++) { //render zone
                double distance = Math.sqrt(Math.pow(x - Positioning.CENTER_X.value, 2) + Math.pow(y - Positioning.CENTER_Y.value, 2));
                if (distance <= Positioning.RADIUS.value) {
                    double angle = Math.atan2(y - Positioning.CENTER_Y.value, x - Positioning.CENTER_X.value);
                    double hue = Math.toDegrees(angle) + 180;
                    double saturation = distance / Positioning.RADIUS.value;
                    Color color = Color.hsb(hue, saturation, 1.0); //hue is the color value we get
                    gc.getPixelWriter().setColor(x, y, color);
                }
            }
        }
    }


}
