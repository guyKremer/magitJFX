package components.app;

import components.center.CenterController;
import components.header.HeaderController;
import components.left.leftController;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import logic.EngineAdapter;

import java.util.function.Consumer;

public class AppController {

    @FXML private VBox headerComponent;
    @FXML private HeaderController headerComponentController;
    @FXML private BorderPane centerComponent;
    @FXML private CenterController centerComponentController;
    @FXML private VBox leftComponent;
    @FXML private leftController leftComponentController;

    private Stage primaryStage;
    private EngineAdapter engineAdapter;


    @FXML
    public void initialize(){
        if(headerComponentController != null && centerComponentController!=null && leftComponentController !=null){
            headerComponentController.setMainController(this);
            centerComponentController.setMainController(this);
            leftComponentController.setMainController(this);
        }
        engineAdapter = new EngineAdapter();
    }

    public EngineAdapter getEngineAdapter() {
        return engineAdapter;
    }

    public void setPrimaryStage(Stage mainStage) {
        this.primaryStage = mainStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void changeUserName(String name){
        centerComponentController.changeUserName(name);
    }

}
