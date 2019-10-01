package components.left;

import Engine.Status;
import components.app.AppController;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.awt.*;
import java.util.List;

public class leftController {

    private AppController mainController;
    @FXML private TextFlow changedFiles = new TextFlow();
    @FXML private TextFlow addedFiles = new TextFlow();
    @FXML private TextFlow deletedFiles = new TextFlow();

    public void ShowStatus(Status i_status) {

        createStringFromList(i_status.getModifiedFiles(), changedFiles);
        createStringFromList(i_status.getAddedFiles(), addedFiles);
        createStringFromList(i_status.getDeletedFiles(), deletedFiles);
    }

    public void createStringFromList(List<String> lst, TextFlow tf){
        String res = "";

        for(String str : lst){
            tf.getChildren().add(new Text(str + System.lineSeparator()));
        }
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }
}
