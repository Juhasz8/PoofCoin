package com.example.poof_ui;

// Imports

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

import com.example.poof_ui.Blockchain_Side.FullNode;
import com.example.poof_ui.Blockchain_Side.FullNodeBlock;
import com.example.poof_ui.Blockchain_Side.SimulationManager;
import com.example.poof_ui.TrustedBlocksGUI;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PoofController implements Initializable {

    // UI elements
    @FXML
    private Label label_Years;
    @FXML
    private Label label_Weeks;
    @FXML
    private Label label_Months;
    @FXML
    private VBox currentEvents;
    @FXML
    private ScrollPane currentEventsScroll;
    @FXML
    private Button restart_button;
    @FXML
    private ImageView play_image;
    @FXML
    private Button play_button;
    @FXML
    private LineChart<?, ?> lineChart;
    @FXML
    private ScrollPane chartScroll;
    @FXML
    private TilePane tradersTile;
    @FXML
    private TilePane minersTile;
    @FXML
    private Label marketPrice;

    @FXML
    private Label marketPercentage;

    @FXML
    private TilePane BlockchainViewTile;

    @FXML
    private Label br1;
    @FXML
    private Label br2;
    @FXML
    private Label br3;
    @FXML
    private Label br4;
    @FXML
    private Label br5;
    @FXML
    private Label sr1;
    @FXML
    private Label sr2;
    @FXML
    private Label sr3;
    @FXML
    private Label sr4;
    @FXML
    private Label sr5;

    @FXML
    private Label hashDifficultyLabel;

    // Chart data
    private XYChart.Series series1;
    private List<Double> lastTwoValues = new ArrayList<>();
    private boolean isPlaying = false;

    // Chart animation
    private Timeline timeline;

    // Timeline variables
    int yearsPassed = 0;
    int monthsPassed = 0;
    int weeksPassed = 0;

    // create an object of Random class
    Random random = new Random();

    public static PoofController instance;

    public int eventIndex = 0;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        instance = this;

        // Initialize chart data
        series1 = new XYChart.Series();
        Platform.runLater(() -> lineChart.getData().addAll(series1));
        series1.getData().add(new XYChart.Data<>(String.valueOf(0), 0));

        // Initialize chart animation
        timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {

            // Timeline Counter
            // Increment the years, months, and weeks passed
            weeksPassed ++;
            if (weeksPassed == 4){
                monthsPassed ++;
                weeksPassed = 0;
            }
            if (monthsPassed == 12){
                yearsPassed ++;
                monthsPassed = 0;
            }
            label_Years.setText(String.valueOf(yearsPassed));
            label_Months.setText(String.valueOf(monthsPassed));
            label_Weeks.setText(String.valueOf(weeksPassed));

            /*
            if(eventIndex == 1)
            {
                PotentiallyMakeEvent();
                eventIndex = 0;
            }
            else
                eventIndex++;
             */

        }));

        // Set the timeline to repeat 500 times (10 minutes)
        timeline.setCycleCount(500);

    }

    public void SetEventUI(CurrentEvent currentEvent, String eventText)
    {
        Platform.runLater(() -> currentEvents.getChildren().add(0, new CurrentEvent(eventText)));
    }

    private Thread simulationThread;

    // Change image on the play button
    @FXML
    void startTimeline(ActionEvent event) {
        if (isPlaying) {
            // Change the image back to the play button
            Image playButton = new Image(getClass().getResourceAsStream("Icons/play_button.png"));
            play_image.setImage(playButton);
            // Stop the timeline
            timeline.stop();

            SimulationManager.getInstance().SuspendSimulation();

        } else {
            // Change the image to the pause button
            Image pauseButton = new Image(getClass().getResourceAsStream("Icons/pause_button.png"));
            play_image.setImage(pauseButton);
            // Start the timeline
            timeline.play();

            if(simulationThread == null)
                StartSimulation();
            else
                SimulationManager.getInstance().ResumeSimulation();
        }
        // Toggle the state of the button
        isPlaying = !isPlaying;
    }
    public void AddTrustedBlockGUI(TrustedBlocksGUI trustedBlocksGUI)
    {
        // Add a trusted block
        Platform.runLater(() -> BlockchainViewTile.getChildren().add(trustedBlocksGUI));
    }

    public void AddTrustedBlockTransactionGUI(TrustedBlocksGUI trustedBlocksGUI, String transaction)
    {
        // Add a transaction label
        Platform.runLater(()-> trustedBlocksGUI.setTransactionLabel(transaction));
    }


    // MINER GUI
    public void AddMinerGUI(MinerGUI minerGUI, String powerString, String icon)
    {
        // Add miners
        Platform.runLater(() -> minersTile.getChildren().add(minerGUI));

        // Add miners
        Platform.runLater(() -> minerGUI.SetMiningPowerLabel(powerString));

        //setting the icon
        Platform.runLater(() -> minerGUI.SetIcon("ProfileImages/"+icon+".png"));
    }

    public void SetMinerGUIHash(MinerGUI minerGUI, String hash)
    {
        Platform.runLater(() -> minerGUI.SetHashLabel(hash));
    }

    public void SetMinerGUICoin(MinerGUI minerGUI, String coin, String color)
    {
        Platform.runLater(() -> minerGUI.SetCoinLabel(coin));
        Platform.runLater(() -> minerGUI.SetWalletColor(color));
    }

    public void SetMinerGUIColor(MinerGUI minerGUI, String color)
    {
        Platform.runLater(() -> minerGUI.SetWalletColor(color));
    }

    // TRADER GUI
    public void AddTraderGUI(TraderGUI traderGUI, String icon)
    {
        Platform.runLater(() -> tradersTile.getChildren().add(traderGUI));
        //System.out.println("THIS IS THE ICON: " + icon);
        Platform.runLater(() -> traderGUI.SetIcon("ProfileImages/"+icon+".png"));
    }

    public void SetTraderGUICoin(TraderGUI traderGUI, String coin, String color)
    {
        Platform.runLater(() -> traderGUI.SetCoinLabel(coin));
        Platform.runLater(() -> traderGUI.SetWalletColor(color));
    }

    public void SetTraderGUIColor(TraderGUI traderGUI, String color)
    {
        Platform.runLater(() -> traderGUI.SetWalletColor(color));
    }

    public void SetHashDifficulty(String hashDifficulty){
        Platform.runLater(() -> hashDifficultyLabel.setText(hashDifficulty));
    }



    public void SetBuyingRequestLabelGUI(int index, int value)
    {
        if(index == 0)
            Platform.runLater(() -> br1.setText(Integer.toString(value)));
        else if(index == 1)
            Platform.runLater(() -> br2.setText(Integer.toString(value)));
        else if(index == 2)
            Platform.runLater(() -> br3.setText(Integer.toString(value)));
        else if(index == 3)
            Platform.runLater(() -> br4.setText(Integer.toString(value)));
        else if(index == 4)
            Platform.runLater(() -> br5.setText(Integer.toString(value)));
    }
    public void SetSellingRequestLabelGUI(int index, int value)
    {
        if(index == 0)
            Platform.runLater(() -> sr1.setText(Integer.toString(value)));
        else if(index == 1)
            Platform.runLater(() -> sr2.setText(Integer.toString(value)));
        else if(index == 2)
            Platform.runLater(() -> sr3.setText(Integer.toString(value)));
        else if(index == 3)
            Platform.runLater(() -> sr4.setText(Integer.toString(value)));
        else if(index == 4)
            Platform.runLater(() -> sr5.setText(Integer.toString(value)));
    }


    public void updateMarketPriceLabel(String Price)
    {
        Double currentPrice = Double.valueOf(Price);
        DecimalFormat decimalFormat = new DecimalFormat("€#.##");
        String formattedPrice = decimalFormat.format(currentPrice);
        Platform.runLater(() -> marketPrice.setText(String.valueOf(formattedPrice)));
    }

    // Declare the previousPrice as a class member variable
    private Double previousPrice = 0.0;

    // Declare colors
    private String redColor = "-fx-stroke: #ff5e57;";
    private String greenColor = "-fx-stroke: #4FCB59;";
    private String textGreenColor = "-fx-text-fill: #4FCB59;";
    private String textRedColor = "-fx-text-fill: #ff5e57;";

    public void updateMarketPercentageLabel(String price) {
        Double currentPrice = Double.valueOf(price);
        Double percentage = ((currentPrice - previousPrice) / previousPrice);

        if(previousPrice == 0)
            percentage = 1.0;

        previousPrice = currentPrice; // Update the previousPrice with the currentPrice


        DecimalFormat decimalFormat = new DecimalFormat("+#.##%;-#.##%");
        String formattedPercentage = decimalFormat.format(percentage); // Create the formatting

        if (currentPrice != 0) {
            Platform.runLater(() -> marketPercentage.setText(formattedPercentage)); // Change the text label
        } else {
            Platform.runLater(() -> marketPercentage.setText("-100%")); // If the price is 0
        }

        if (percentage > 0) {
            Platform.runLater(() -> marketPercentage.setStyle(textGreenColor)); // Change text to green
        } else {
            Platform.runLater(() -> marketPercentage.setStyle(textRedColor)); // Change text to red
        }
    }
    public void updatePriceGraph(String Price) {

        // Parse the Price string to a numeric type (e.g., Double)
        double priceValue = Double.parseDouble(Price);

        // Generate a new data point
        Platform.runLater(() -> series1.getData().add(new XYChart.Data<>(String.valueOf(series1.getData().size() + 1), priceValue)));

        // Keep track of the last two values in the chart
        lastTwoValues.add(priceValue);
        if (lastTwoValues.size() > 2) {
            lastTwoValues.remove(0);
        }

        // Change the color of the line according to if it's more or less

        if (lastTwoValues.size() == 2) {
            if (lastTwoValues.get(1) > lastTwoValues.get(0)) {
                series1.getNode().setStyle(greenColor);
            } else {
                series1.getNode().setStyle(redColor);
            }
        }

        // Increase the preferred width of the chart by 30 pixels
        double currentPrefWidth = lineChart.getPrefWidth();
        Platform.runLater(() -> lineChart.setPrefWidth(currentPrefWidth + 30));

        // Scroll the chart to the right to show the latest data point
        Platform.runLater(() -> chartScroll.setHvalue(1));
    }


    public static PoofController getInstance()
    {
        return instance;
    }

    public void StartSimulation()
    {
        System.out.println("---------------------------------------------");
        simulationThread = new Thread(SimulationManager.getInstance());
        simulationThread.start();
        System.out.println("Simulation started!");
        System.out.println("---------------------------------------------");
    }

    @FXML
    void restartApplication(ActionEvent event) throws IOException {
        // Get the current stage
        Stage stage = (Stage) restart_button.getScene().getWindow();

        // Create a new instance of the application
        PoofInterface poofApp = new PoofInterface();

        //Set height and with of the stage
        stage.setHeight(1080);
        stage.setWidth(1920);

        // Call the start method of the new instance with the current stage
        poofApp.start(stage);
        //SimulationManager.getInstance().RestartSimulation();
    }
}
