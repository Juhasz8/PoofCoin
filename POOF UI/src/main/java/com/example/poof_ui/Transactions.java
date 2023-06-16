package com.example.poof_ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class Transactions extends AnchorPane {
    private String transactionS;
    private float minPriceChange;
    private float maxPriceChange;

    // Constructor to create a CurrentEvent object
    public Transactions(String transactionString) {
        this.transactionS = transactionString; // The name of the Current Event

        CreateTransactionUI();
    }

    private void CreateTransactionUI() {
        // Create a new FXMLLoader for loading the FXML file and setting the controller
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML/Transaction.fxml"));
        fxmlLoader.setController(this);

        try {
            // Load the FXML file and retrieve the root AnchorPane
            fxmlLoader.load();
            AnchorPane root = fxmlLoader.getRoot();

            // Add the root pane to the children of the CurrentEvent object
            this.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Label transactionLabel;

    // Method called when the FXML is initialized
    public void initialize() {
        // Set the text of the currentEventName label to the eventName
        transactionLabel.setWrapText(true);
        transactionLabel.setPrefWidth(250);
        transactionLabel.setText(transactionS);
    }
}
