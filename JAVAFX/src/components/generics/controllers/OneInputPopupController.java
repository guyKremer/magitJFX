package components.generics.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

    @FXML private void okButtonActionListener(){
        consumer.accept(input.getText());
    }

    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
    }
}
