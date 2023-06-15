module com.example.poof_ui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.poof_ui to javafx.fxml;
    exports com.example.poof_ui;
    exports com.example.poof_ui.Blockchain_Side;
    opens com.example.poof_ui.Blockchain_Side to javafx.fxml;
}