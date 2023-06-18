package com.example.poof_ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class MinerGUI extends AnchorPane {

    public MinerGUI() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML/MinerGUI.fxml"));
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            AnchorPane root = fxmlLoader.getRoot();
            this.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private ImageView minerPp;

    @FXML
    private Label hashLabel;

    @FXML
    private Label miningPowerLabel;

    @FXML
    private Label CoinLabel;
    @FXML
    private Rectangle outline;

    public void SetMiningPowerLabel(String powerString)
    {
        miningPowerLabel.setText(powerString);
    }

    public void SetHashLabel(String hash)
    {
        hashLabel.setText(hash);
    }

    public void SetCoinLabel(String coin)
    {
        CoinLabel.setText(coin);
    }

    public void SetWalletColor(String color){
        CoinLabel.setStyle(color);
    }

    public void SetWalletOutline(String outlineColor) {
        outline.setFill(Paint.valueOf(outlineColor));
    }
}
