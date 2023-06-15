package com.example.poof_ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class CurrentEvent extends AnchorPane {
    private String eventName;
    private int probability;
    private int coolDown;

    private float minPriceChange;
    private float maxPriceChange;

    // Constructor to create a CurrentEvent object
    public CurrentEvent(String eventName, int probability, int coolDown, float minPriceChange, float maxPriceChange)
    {
        this.eventName = eventName; // The name of the Current Event
        this.probability = probability; // The probability of this event happening
        this.coolDown = coolDown; // The cool down before this event can happen again
        this.minPriceChange = minPriceChange;
        this.maxPriceChange = maxPriceChange;

        CreateEventUI();
    }

    //used just for the UI
    public CurrentEvent(String eventName)
    {
        this.eventName = eventName;
        CreateEventUI();
    }

    private void CreateEventUI()
    {
        // Create a new FXMLLoader for loading the FXML file and setting the controller
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML/CurrentEvent.fxml"));
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
    private Label currentEventName;

    // Method called when the FXML is initialized
    public void initialize() {
        // Set the text of the currentEventName label to the eventName
        currentEventName.setWrapText(true);
        currentEventName.setPrefWidth(350);
        currentEventName.setText(eventName);
    }

    // Getter method for probability
    public double getProbability() {
        return probability;
    }

    public String GetName() { return eventName; }
}
