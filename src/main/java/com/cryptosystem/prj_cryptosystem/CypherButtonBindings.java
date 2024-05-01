package com.cryptosystem.prj_cryptosystem;

import com.cryptosystem.prj_cryptosystem.service.Cypher;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.UnaryOperator;
import com.cryptosystem.prj_cryptosystem.service.impl.Cyphers.*;

class CypherButtonBindings {
    public static RadioButton getCaesarRadioButton() {
        return caesarRadioButton;
    }

    public static RadioButton getTrithemiusRadioButton() {
        return trithemiusRadioButton;
    }

    public static RadioButton getDisposableNotebooksButton() {
        return DisposableNotebooksButton;
    }

    public static RadioButton getXORRadioButton() {
        return XORRadioButton;
    }

    public static RadioButton getBookRadioButton() {
        return bookRadioButton;
    }

    public static RadioButton getDesRadioButton() {
        return desRadioButton;
    }

    public static RadioButton getDesEDERadioButton() {
        return desEDERadioButton;
    }

    public static RadioButton getAesRadioButton() {
        return aesRadioButton;
    }

    public static RadioButton getKnapsackRadioButton() {
        return knapsackRadioButton;
    }

    public static RadioButton getRSARadioButton() {
        return RSARadiobutton;
    }
    private static final ToggleGroup toggleGroup = new ToggleGroup();
    private static final ToggleGroup regimeGroup = new ToggleGroup();
    static final VBox cypherButtons;
    private static final RadioButton caesarRadioButton = new RadioButton("Caesar");
    private static final RadioButton trithemiusRadioButton = new RadioButton("Trithemius");
    private static final RadioButton DisposableNotebooksButton = new RadioButton("Disposable notebooks");
    private static final RadioButton XORRadioButton = new RadioButton("XOR");
    private static final RadioButton bookRadioButton = new RadioButton("Book");
    private static final RadioButton desRadioButton = new RadioButton("DES");
    private static final RadioButton desEDERadioButton = new RadioButton("3DES");
    private static final RadioButton aesRadioButton = new RadioButton("AES");
    private static final RadioButton knapsackRadioButton = new RadioButton("Knapsack");
    private static final RadioButton RSARadiobutton = new RadioButton("RSA");
    private static final Alert alert = new Alert(Alert.AlertType.ERROR);
    static final TextArea entryPoint = new TextArea();
    static final List<RadioButton> regimes = new ArrayList<>(Arrays.asList(new RadioButton("ECB"), new RadioButton("CBC"), new RadioButton("CFB"), new RadioButton("OFB"), new RadioButton("CTR") ));
    private static List<Cypher> cyphers = Collections.unmodifiableList(new ArrayList<>());
    private static List<RadioButton> buttonsList = Collections.unmodifiableList(new ArrayList<>());
    static {
        caesarRadioButton.setSelected(true);


        setButtonDependency(caesarRadioButton, new Caesar(EncryptionAccessor.Symmetric));
        setButtonDependency(trithemiusRadioButton, new Trithemius(EncryptionAccessor.Symmetric));
        setButtonDependency(XORRadioButton, new XOR(EncryptionAccessor.Symmetric));
        setButtonDependency(DisposableNotebooksButton, new DisposableNotebooks(EncryptionAccessor.Symmetric));
        setButtonDependency(bookRadioButton, new Book(EncryptionAccessor.Symmetric));
        setButtonDependency(desRadioButton, new DES(EncryptionAccessor.Symmetric, regimes.get(0), regimes.get(1), regimes.get(2), regimes.get(3), regimes.get(4), alert));
        setButtonDependency(desEDERadioButton, new DES_EDE(EncryptionAccessor.Symmetric, regimes.get(0), regimes.get(1), regimes.get(2), regimes.get(3), regimes.get(4), alert ));
        setButtonDependency(aesRadioButton, new AES(EncryptionAccessor.Symmetric, regimes.get(0), regimes.get(1), regimes.get(2), regimes.get(3), regimes.get(4), alert ));
        setButtonDependency(knapsackRadioButton, new Knapsack(EncryptionAccessor.Asymmetric, alert, entryPoint));
        setButtonDependency(RSARadiobutton, new RSA(EncryptionAccessor.Asymmetric, alert));

        buttonsList.forEach(x -> setRadioToggleSwitcher(x, toggleGroup));
        regimes.forEach(x -> setRadioToggleSwitcher(x, regimeGroup));

    }
    static {
        cypherButtons = new VBox(CypherButtonBindings.buttonsList.stream().toArray(RadioButton[]::new)); //The `::new` syntax is used to reference a constructor, in this case the constructor of the `Button` class, and the `Button[]` specifies the type of the array to be created.
        cypherButtons.setSpacing(10);
    }

    public static void setItemVisibility(Node node, int... indexes) {
        Map<RadioButton, Boolean> radioToVisibility = new HashMap<>();
        RadioButton[] radioButtons = buttonsList.toArray(new RadioButton[0]);
        for (int i = 0; i < radioButtons.length; i++) {
            boolean visible = false;
            for (int index : indexes) {
                if (i == index) {
                    visible = true;
                    break;
                }
            }
            radioToVisibility.put(radioButtons[i], visible);
            int finalI = i;
            radioButtons[i].selectedProperty().addListener((observableValue, oldSelected, newSelected) -> {
                boolean attackVisible = radioToVisibility.get(radioButtons[finalI]);
                node.setVisible(newSelected && attackVisible);
            });
        }
    }


    private static void setButtonDependency (RadioButton button, Cypher cypher) {
        buttonsList = new ArrayList<>(buttonsList);
        buttonsList.add(button);
        cyphers = new ArrayList<>(cyphers);
        cyphers.add(cypher);
        buttonsList = Collections.unmodifiableList(buttonsList);
        cyphers = Collections.unmodifiableList(cyphers);
    }
    //cyphers.addAll(Arrays.asList(new Caesar(), new Trithemius(), new XOR(), new DisposableNotebooks()));

    public static Cypher getCypher () throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        for (int i = 0; i < CypherButtonBindings.buttonsList.size(); i++) {
            if (CypherButtonBindings.buttonsList.get(i).isSelected()) {
                return CypherButtonBindings.cyphers.get(i);
            }
        }

        return null;
    }
    private static void setRadioToggleSwitcher (RadioButton buttonToSetGroup, ToggleGroup toggleGroup) {

        buttonToSetGroup.setToggleGroup(toggleGroup); //setting a toggleGroup ( a tracker that synchronizes added buttons )

        //When one clicked, the other becomes non-selected
        toggleGroup.selectedToggleProperty().addListener((observableValue, oldToggle, newToggle) -> {
            if (toggleGroup.getSelectedToggle() != null) {
                for (Toggle toggle : toggleGroup.getToggles()) {
                    RadioButton radioButton = (RadioButton) toggle;
                    if (!radioButton.equals(toggleGroup.getSelectedToggle())) {
                        radioButton.setSelected(false);
                    } else {
                        radioButton.setSelected(true);
                    }
                }
            }
        });
    }

    public static UnaryOperator<TextFormatter.Change> setChanger () {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String input = change.getText();
            for (int i = 0; i < CypherButtonBindings.buttonsList.size(); i++) {

            }
            if (CypherButtonBindings.caesarRadioButton.isSelected() && input.matches("[0-9]?")) { //avoid wrapped if statement
                //"-?([0-9]*)?" //accept negative numbers
                return change;
            }
            if (!CypherButtonBindings.caesarRadioButton.isSelected() ) {
                return change;
            }
            return null;
        };
        return integerFilter;
    }

}
