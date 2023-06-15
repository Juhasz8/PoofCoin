package com.example.poof_ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class TrustedBlocksGUI extends AnchorPane {

    public TrustedBlocksGUI() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML/TrustedBlocks.fxml"));
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
    private Label blockNumber;
    @FXML
    private Label hashNumber;
    @FXML
    private Label minersPublicKey;
    @FXML
    private Label merkleRoot;
    @FXML
    private Label previousHash;

    public void setBlockNumber(String blockNumberString)
    {
        blockNumber.setText(blockNumberString);
    }
    public void setHashNumber(String hashNumberString)
    {
        hashNumber.setText(hashNumberString);
    }
    public void setMinersPublicKey(String minersPublicKeyString)
    {
        minersPublicKey.setText(minersPublicKeyString);
    }
    public void setMerkleRoot(String merkleRootString)
    {
        merkleRoot.setText(merkleRootString);
    }
    public void setPreviousHash(String previousHashString) { previousHash.setText(previousHashString);}
}
