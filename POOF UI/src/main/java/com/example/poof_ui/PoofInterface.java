package com.example.poof_ui;
import com.example.poof_ui.Blockchain_Side.Miner;
import com.example.poof_ui.Blockchain_Side.SimulationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// PoofInterface class definition
public class PoofInterface extends Application{


    // Override start method
    @Override
    public void start(Stage stage) throws IOException
    {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/Poof-Interface.fxml"));

        // Load the root node from the FXML file
        Parent root = loader.load();

        // Create a new scene with the root node
        Scene scene = new Scene(root);

        // Add the styling CSS file to the scene
        scene.getStylesheets().add("styling.css");

        // Set the scene for the stage
        stage.setScene(scene);
        // Show the stage
        stage.show();
    }

    // Main method
    public static void main(String[] args)
    {
        // Launch the JavaFX application
        launch();
    }

}
