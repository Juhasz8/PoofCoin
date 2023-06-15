package com.example.poof_ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class TraderGUI extends AnchorPane {

    public TraderGUI() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML/TraderGUI.fxml"));
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
    private ImageView traderPp;

    @FXML
    private Label CoinLabel;

    public void setProfilePicture(Image image) {
        traderPp.setImage(image);
    }

    public void SetCoinLabel(String coin)
    {
        CoinLabel.setText(coin);
    }
}
