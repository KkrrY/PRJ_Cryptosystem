package com.cryptosystem.prj_cryptosystem;

import com.cryptosystem.prj_cryptosystem.service.Cypher;
import javafx.collections.FXCollections;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutionException;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.SEVERE;

public class Controller {

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem saveAsMenuItem;
    private File loadedFileReference;
    private FileTime lastModifiedTime;

    @FXML
    private TextArea textArea;

    @FXML
    private Button encryptButton;

    @FXML
    private Button decryptButton;

    @FXML
    private Button bruteForceButton;

    @FXML
    private Label statusMessage;

    @FXML
    Button loadChangesButton;

    @FXML
    private TextArea cypherKeyArea;
    @FXML
    private TextArea privateKeyArea;

    @FXML
    private ProgressBar progressBar;
    @FXML
    private MenuBar menuBar;
    @FXML
    TitledPane menuCypher;

    @FXML
    private HBox regimes; // Added from FXML

    @FXML
    private Button generateKeyButton; // Added from FXML

    @FXML
    private TextArea keyLengthArea; // Added from FXML

    @FXML
    private ChoiceBox<Integer> fontSizeChoiceBox; // Assuming this is the font size choice box

    @FXML
    private Button pickerButton; // Added from FXML
    @FXML
    private TextArea entryPoint;

    @FXML
    private StackPane infoButton;

    @FXML
    private Button closeButton;

    @FXML
    private Button minimizeButton;

    @FXML
    Label aboutLabel;
    GradientColorPicker colorPicker = new GradientColorPicker();


    private Cypher cypher;

    public void setCypher(Cypher cypher) {
        this.cypher = cypher;
    }

    public double getScreenWidth() {
        return Screen.getPrimary().getVisualBounds().getWidth();
    }

    public double getScreenHeight() {
        return Screen.getPrimary().getVisualBounds().getHeight();
    }

    @FXML
    void initialize() {
        CryptoSystem.getPrimaryStage().initStyle(StageStyle.UNDECORATED);

        loadChangesButton.setVisible(false);
        menuCypher.setContent(CypherButtonBindings.cypherButtons);
        entryPoint.textProperty().bindBidirectional(CypherButtonBindings.entryPoint.textProperty());
//        regimes.getChildren().add(CypherButtonBindings.regimes.stream().toArray(RadioButton[]::new));
        regimes.getChildren().addAll(CypherButtonBindings.regimes);
        fontSizeChoiceBox.getItems().addAll(FXCollections.observableArrayList(Listener.getFontSizes()));

        Circle circle = new Circle(12, Color.DARKGRAY);
        Label label = new Label("i");
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(14));
        infoButton.getChildren().addAll(circle, label);

        Tooltip tooltip = new Tooltip(" Developer: Name Surname \n GitHub: https://github.com/ ");
        aboutLabel.setTooltip(tooltip); //or Tooltip.install();
        tooltip.setShowDelay(Duration.ZERO);

        CypherButtonBindings.setItemVisibility(bruteForceButton, 0);
        CypherButtonBindings.setItemVisibility(regimes, 5, 6, 7);
        CypherButtonBindings.setItemVisibility(entryPoint,8);
        CypherButtonBindings.setItemVisibility(privateKeyArea, 8,9);
        CypherButtonBindings.setItemVisibility(keyLengthArea, 8,9);
        CypherButtonBindings.setItemVisibility(generateKeyButton, 8,9);

        Listener.AddKeyGeneration(generateKeyButton, keyLengthArea, cypherKeyArea, privateKeyArea);

        Listener.AddEncryption(encryptButton, textArea, cypherKeyArea, privateKeyArea);
        Listener.AddDecryption(decryptButton, textArea, cypherKeyArea, privateKeyArea);
        Listener.AddBruteForce(bruteForceButton, textArea);

//        Listener.AddOpenFileAction(menuItem, this);
//        Listener.AddSaveFileAction(menuItem2, this);
//        Listener.AddSaveToOtherFileAction(menuItem3, this);
//        Listener.AddLoadChangesAction(controller.loadChangesButton, this);

        Listener.AddMinimizeAction(minimizeButton);
        Listener.AddTerminateAction(closeButton);

        UnaryOperator<TextFormatter.Change> checker = CypherButtonBindings.setChanger();
        Listener.AddDigitFormatter(cypherKeyArea, checker);


        Listener.setChangeFontSizeAction(fontSizeChoiceBox, new Node[]{textArea, cypherKeyArea});

        Listener.setColorChangeAction(colorPicker,
                Stream.of(textArea, cypherKeyArea, progressBar, encryptButton, decryptButton, bruteForceButton, pickerButton, privateKeyArea, keyLengthArea, generateKeyButton, entryPoint)
                        .toArray(Node[]::new));
        pickerButton.setOnAction(event -> {
            colorPicker.show();
        });

        Listener.getOffsets(menuBar);
        Listener.setDraggable(menuBar, CryptoSystem.getPrimaryStage());


        Listener.setToolTip(infoButton, new Tooltip(("""
                    In Caesar cypher you can enter only digits to encrypt your text\040
                    and define the value you move symbols""")));

        CypherButtonBindings.getTrithemiusRadioButton().setOnAction(actionEvent -> { //set on action of selecting
            Listener.setToolTip(infoButton, new Tooltip(""" 
                You can encrypt or decrypt your texts depending on key.\s
                If you enter only digits separated with spaces, you will use\s
                your key as coefficients for equation (2 coefficients represent equation
                `k = Ap^2 + Bp + C`, 3 equals to `k = Ap^3 + Bp^2 + Cp + D` , etc...
                If your input contains letters, you will cypher message like Vigen-ere cypher
                
                WARNING!!! Attack method is temporary unavailable"""));

        });
        CypherButtonBindings.getXORRadioButton().setOnAction(actionEvent -> { // \040 - escape trailing whitespace
            Listener.setToolTip(infoButton, new Tooltip("""
                    You can encrypt or decrypt your texts using XOR encryption\040
                    Now the key user inputs is a seed which helps to generate real key is\040
                    impossible to crack because of its length is equal or greater than
                    whole input text you want to cypher
                    """));
        });
        CypherButtonBindings.getDisposableNotebooksButton().setOnAction(actionEvent -> {
            Listener.setToolTip(infoButton, new Tooltip("""
                    This encryption works similar to XOR, but it cyphers 
                    text depending on key, not seed (in case of XOR, key was
                    randomly generated using seed)
                    """));
        });
        CypherButtonBindings.getBookRadioButton().setOnAction(actionEvent -> {
            Listener.setToolTip(infoButton, new Tooltip("""
                    You can insert a poem and use it as a key.
                    Be careful using this encryption.
                    You wouldn't be capable to decrypt
                    your message correctly if there are symbols
                    which didn't exist in a key.
                    """));
        });
    }

    @FXML
    void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        //only allow text files to be selected using chooser
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
        );
        //set initial directory somewhere user will recognise
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        //let user select file
        File fileToLoad = fileChooser.showOpenDialog(null);
        //if file has been chosen, load it using asynchronous method (define later)
        if (fileToLoad != null) {
            loadFileToTextArea(fileToLoad);
        }
    }
    private void loadFileToTextArea(File fileToLoad) {
        Task<String> loadTask = fileLoaderTask(fileToLoad);
        progressBar.progressProperty().bind(loadTask.progressProperty());
        loadTask.run();
    }
    private Task<String> fileLoaderTask(File fileToLoad) {
        //Create a task to load the file asynchronously
        Task<String> loadFileTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                BufferedReader reader = new BufferedReader(new FileReader(fileToLoad));
                //Use Files.lines() to calculate total lines - used for progress
                long lineCount;
                try (Stream<String> stream = Files.lines(fileToLoad.toPath())) {
                    lineCount = stream.count();
                }
                //Load in all lines one by one into a StringBuilder separated by "\n" - compatible with TextArea
                String line;
                StringBuilder totalFile = new StringBuilder();
                long linesLoaded = 0;
                while ((line = reader.readLine()) != null) {
                    totalFile.append(line);
                    totalFile.append("\n");
                    updateProgress(++linesLoaded, lineCount);
                }
                return totalFile.toString();
            }
        };
        //If successful, update the text area, display a success message and store the loaded file reference
        loadFileTask.setOnSucceeded(workerStateEvent -> {
            try {
                textArea.setText(loadFileTask.get());
                statusMessage.setText("File loaded: " + fileToLoad.getName());
                loadedFileReference = fileToLoad;
                lastModifiedTime = Files.readAttributes(fileToLoad.toPath(), BasicFileAttributes.class).lastModifiedTime();
            } catch (InterruptedException | ExecutionException | IOException e) {
                Logger.getLogger(getClass().getName()).log(SEVERE, null, e);
                textArea.setText("Could not load file from:\n " + fileToLoad.getAbsolutePath());
            }
            scheduleFileChecking(loadedFileReference);
        });
        //If unsuccessful, set text area with error message and status message to failed
        loadFileTask.setOnFailed(workerStateEvent -> {
            textArea.setText("Could not load file from:\n " + fileToLoad.getAbsolutePath());
            statusMessage.setText("Failed to load file");
        });
        return loadFileTask;
    }
    private void scheduleFileChecking(File file) {
        ScheduledService<Boolean> fileChangeCheckingService = createFileChangesCheckingService(file);
        fileChangeCheckingService.setOnSucceeded(workerStateEvent -> {
            if (fileChangeCheckingService.getLastValue() == null) return;
            if (fileChangeCheckingService.getLastValue()) {
                //no need to keep checking
                fileChangeCheckingService.cancel();
                notifyUserOfChanges();
            }
        });
        System.out.println("Starting Checking Service...");
        fileChangeCheckingService.start();
    }
    private ScheduledService<Boolean> createFileChangesCheckingService(File file) {
        ScheduledService<Boolean> scheduledService = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        FileTime lastModifiedAsOfNow = Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime();
                        return lastModifiedAsOfNow.compareTo(lastModifiedTime) > 0;
                    }
                };
            }
        };
        scheduledService.setPeriod(Duration.seconds(1));
        return scheduledService;
    }
    private void notifyUserOfChanges() {
        loadChangesButton.setVisible(true);
    }
    @FXML
    public void loadChanges(ActionEvent event) {
        loadFileToTextArea(loadedFileReference);
        loadChangesButton.setVisible(false);
    }

    @FXML
    public void saveFile(ActionEvent event) {
        try {
            FileWriter myWriter = new FileWriter(loadedFileReference);
            myWriter.write(textArea.getText());
            myWriter.close();
            lastModifiedTime = FileTime.fromMillis(System.currentTimeMillis() + 3000);
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(SEVERE, null, e);
        }
    }

    @FXML
    void saveToOtherFile (ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save to other file");
        fileChooser.setInitialFileName("New File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("text file", "*.txt" /*, "*.docx"*/)
//                , new FileChooser.ExtensionFilter("pdf", "*.pdf"),
//                new FileChooser.ExtensionFilter("images", "*.jpg")
        );

        loadedFileReference = fileChooser.showSaveDialog(null);
        fileChooser.setInitialDirectory(loadedFileReference.getParentFile());
        try {
            PrintStream printStream = new PrintStream(loadedFileReference);
            printStream.println(textArea.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}