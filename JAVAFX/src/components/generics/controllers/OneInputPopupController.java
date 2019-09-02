package components.generics.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;
import java.util.function.Function;

public class OneInputPopupController {

    @FXML
    private Button okButton;
    @FXML
    private Label title;
    @FXML
    private TextField input;


    private Consumer<String> consumer;
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML private void okButtonActionListener(){
        consumer.accept(input.getText());
        primaryStage.close();

    }

    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    public void setTitle(String title){
        this.title.setText(title);
    }
}
