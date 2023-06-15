package com.example.poof_ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

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

    public void setProfilePicture(Image image)
    {
        minerPp.setImage(image);
    }

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
}
