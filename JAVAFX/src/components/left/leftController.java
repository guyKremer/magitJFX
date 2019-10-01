package components.left;

import Engine.Status;
import components.app.AppController;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.awt.*;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.function.Consumer;

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

    public void ShowStatus() {
        Consumer<Status> statusConsumer = (status)-> {
            changedFiles.getChildren().clear();
            addedFiles.getChildren().clear();
            deletedFiles.getChildren().clear();
            for (String str : status.getModifiedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                changedFiles.getChildren().add(new Text("- " +str));
                changedFiles.getChildren().add(new Text(System.lineSeparator()));
            }
            for (String str : status.getAddedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                addedFiles.getChildren().add(new Text("- " +str));
                addedFiles.getChildren().add(new Text(System.lineSeparator()));

            }
            for (String str : status.getDeletedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                deletedFiles.getChildren().add(new Text("- " +str));
                deletedFiles.getChildren().add(new Text(System.lineSeparator()));
            }
        };
        mainController.getEngineAdapter().ShowStatus(null,statusConsumer,null );
    }
}
