package yuri.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import yuri.Yuri;

/**
 * Controller for MainWindow. Wires the UI to the Yuri backend.
 */
public class MainWindow {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox dialogContainer;
    @FXML private TextField userInput;
    @FXML private Button sendButton;

    private Yuri yuri;

    /** Initializes auto-scroll. */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /** Injects the backend instance. Call this from Main after FXML load. */
    public void setYuri(Yuri yuri) {
        this.yuri = yuri;
        // greeting bubble
        dialogContainer.getChildren().add(
                DialogBox.getYuriDialog(yuri.getGreeting())
        );
    }

    /** Handles one round of user input. */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input == null || input.isBlank()) {
            return;
        }

        String response = yuri.getResponse(input);  // <- step 2 adds this

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input),
                DialogBox.getYuriDialog(response)
        );

        if (input.trim().equalsIgnoreCase("bye")) {
            userInput.setDisable(true);
            sendButton.setDisable(true);
        }
        userInput.clear();
    }
}
