package com.cryptosystem.prj_cryptosystem;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

class Listener {

    static final double[] xOffset = {0};
    static final double[] yOffset = {0};

    private static final List<Integer> FONT_SIZES = Arrays.asList(10, 12, 14, 16, 18, 20, 22, 24, 26);
    private static final int DEFAULT_FONT_SIZE_INDEX = 1;

    public static List<Integer> getFontSizes () {
        return FONT_SIZES;
    }

    public static void setChangeFontSizeAction(ChoiceBox<Integer> choiceBox, Node[] nodeToInteractWith) {

        choiceBox.getSelectionModel().select(DEFAULT_FONT_SIZE_INDEX);
        // Set the font size based on the selected item in the ChoiceBox
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                setFontSize(nodeToInteractWith, newValue);
            }
        });
    }

    public static void setColorChangeAction(GradientColorPicker colorPicker, Node[] node) {
        // Set default gradient to the area
//            LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
//                    new Stop(0, Color.valueOf("#666666")), new Stop(1, Color.BLACK));
//            for (int i = 0; i < node.length ; i++) {
//                node[i].setStyle("-fx-background-color: " + toCssColor(gradient.getStops()));
//            }

        colorPicker.addRadioButtons(node);

        // Connect the color picker to the text area
        colorPicker.getGradientColorPicker().setOnMouseDragged(event -> {
            Color color = colorPicker.chooseColor(event);
            String textColor = toCssColor(color);

            for (int i = 0; i < node.length ; i++) {
                if (colorPicker.radioButtonList.get(i).isSelected()) {
                    Node originalNode = node[i];
                    String newStyle;
                    // Get the current style String of the node
                    String currentStyle = node[i].getStyle();
                    LinearGradient newGradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.valueOf("#666666")), new Stop(1, color));
                    // Create a new style String that includes the new style properties while preserving the previous ones
                    newStyle = currentStyle + ";-fx-background-color: " + toCssColor(newGradient.getStops())
                            + ";-fx-text-fill: " + textColor + ";";

                    if (node[i] instanceof Button) {
                        newStyle = currentStyle + ";-fx-border-color: " + toCssColor(color) + ";";
                    }

                    if (node[i] instanceof ProgressBar) {
                        node[i] = node[i].lookup(".progress-bar:indeterminate > .bar");
                        newStyle = ";-fx-background-color: linear-gradient(to left, transparent," + toCssColor(color) + ");";
                    }

                    // Update the node's style
                    node[i].setStyle(newStyle);
                    node[i] = originalNode; //preventing progress bar taking property `lookup(".progress-bar:indeterminate > .bar");`

                }
            }

        });
    }
    // Utility method to convert a color to CSS-compatible String
    //Stop represents a color stop in a gradient. In other words, it defines the location and color of a specific point in a gradient to create gradient fills for shapes and backgrounds in JavaFX applications.
    //The Stop class has two properties: offset, which represents the location of the stop along the gradient, and color, which represents the color of the stop. The offset property is a value between 0.0 and 1.0, and the color property is a javafx.scene.paint.Color object.
    private static String toCssColor(List<Stop> stops) { //to parse Stop type
        StringBuilder css = new StringBuilder();
        for (Stop stop : stops) {
            Color color = (Color) stop.getColor();
            css.append(String.format("rgba(%d,%d,%d,%.2f),",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255),
                    color.getOpacity()));
        }
        css.deleteCharAt(css.length() - 1); // remove trailing comma
        return "linear-gradient(to right, " + css.toString() + ")";
    }
    private static String toCssColor(Color color) { //to parse Color type
        try {
            return String.format("#%02x%02x%02x",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));
        } catch (NullPointerException e) {
            return null;
        }
    }


    private static void setFontSize(Node[] node, int size) {
        for (int i = 0; i < node.length; i++) {
            // Get the current style String of the node
            String currentStyle = node[i].getStyle();

            // Create a new style String that includes the new font size while preserving the previous styles
            String newStyle = currentStyle + ";-fx-font-size: " + size + "px;";

            // Update the node's style
            node[i].setStyle(newStyle);
        }
    }

    public static void setToolTip (Node nodeToOperateWith, Tooltip tooltip) {
        Tooltip.install(nodeToOperateWith, tooltip);

        //make tooltip showAble
        tooltip.setHideOnEscape(false);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setShowDuration(Duration.INDEFINITE);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);

        // Show the tooltip programmatically
        nodeToOperateWith.setOnMouseEntered(event -> {
            double x = event.getScreenX();
            double y = event.getScreenY();
            tooltip.show(nodeToOperateWith, x, y);
        });
        nodeToOperateWith.setOnMouseExited(event -> tooltip.hide());
    }

    public static void getOffsets (MenuBar menuBar) { //one of the methods to make application movable
        menuBar.setOnMousePressed(new EventHandler<MouseEvent>() { //make it draggable
            @Override
            public void handle(MouseEvent mouseEvent) {
                xOffset[0] = mouseEvent.getSceneX();
                yOffset[0] = mouseEvent.getSceneY();
            }
        });
    }
    public static void setDraggable (MenuBar menuBar, Stage stage) {  //one of the methods to make application movable
        menuBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                stage.setX(mouseEvent.getScreenX() - xOffset[0]);
                stage.setY(mouseEvent.getScreenY() - yOffset[0]);
            }
        });
    }

    public static void AddKeyGeneration (Button generateKeysButton, TextArea keyLengthArea, TextArea publicKeyArea, TextArea privateKeyArea) {
        generateKeysButton.setOnAction( (ActionEvent event) -> {
            try {
                CypherButtonBindings.getCypher().generateKeyPair(keyLengthArea, publicKeyArea, privateKeyArea);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void AddEncryption (Button encryptButton, TextArea inputTextArea, TextArea publicKeyArea, TextArea privateKeyArea) {
        encryptButton.setOnAction( (ActionEvent event) -> {
            String String = null;
            try {
                String = CypherButtonBindings.getCypher().encrypt(inputTextArea, publicKeyArea);

            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            inputTextArea.setText(String);
        });
    }

    public static void AddDecryption (Button decryptButton, TextArea inputTextArea, TextArea publicKeyArea, TextArea privateKeyArea) {
        decryptButton.setOnAction( (ActionEvent event) -> {
            String String = null;
            try {
                if (CypherButtonBindings.getCypher().getEncryptionAccessor().equals(EncryptionAccessor.Symmetric) )
                    String = CypherButtonBindings.getCypher().decrypt(inputTextArea, publicKeyArea);
                else String = CypherButtonBindings.getCypher().decrypt(inputTextArea, privateKeyArea); //because of asymmetric cipher

            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            inputTextArea.setText(String);
        });
    }

    public static void AddBruteForce (Button attackButton, TextArea inputTextArea) {
        attackButton.setOnAction((ActionEvent event) -> {
            try {
                CypherButtonBindings.getCypher().attack(inputTextArea);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void AddOpenFileAction (MenuItem menuItem, Controller controller) {
        menuItem.setOnAction((ActionEvent event) -> {
            controller.openFile(event);
        });
    }
    public static void AddSaveFileAction (MenuItem menuItem, Controller controller) {
        menuItem.setOnAction((ActionEvent event) -> {
            controller.saveFile(event);
        });
    }
    public static void AddSaveToOtherFileAction (MenuItem menuItem, Controller controller) {
        menuItem.setOnAction( (ActionEvent event ) -> {
            controller.saveToOtherFile(event);
        });
    }
    public static void AddLoadChangesAction (Button menuItem, Controller controller){
        controller.loadChangesButton.setOnAction((ActionEvent event) -> {
            controller.loadChanges(event);
        });
    }

    public static void AddDigitFormatter (TextArea textArea, UnaryOperator<TextFormatter.Change> checker) {
        textArea.setTextFormatter(new TextFormatter<String>(checker));
    }

    public static void AddMinimizeAction (Button minimizeButton) {
        minimizeButton.setOnAction((ActionEvent event) -> {
            ((Stage) ((Button) event.getSource()).getScene().getWindow()).setIconified(true);
        });
    }

    public static void AddCloseAction (Button closeButton) {
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

    }

    public static void AddTerminateAction (Button exitButton) {
        exitButton.setOnAction((ActionEvent event) -> {
            Platform.exit();
        });
    }

}
