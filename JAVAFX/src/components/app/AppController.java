package components.app;

import components.center.CenterController;
import components.header.HeaderController;
import components.left.leftController;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class AppController {

    @FXML private VBox headerComponent;
    @FXML private HeaderController headerComponentController;
    @FXML private BorderPane centerComponent;
    @FXML private CenterController centerComponentController;
    @FXML private VBox leftComponent;
    @FXML private leftController leftComponentController;

    @FXML
    public void initialize(){
        if(headerComponentController != null && centerComponentController!=null && leftComponentController !=null){
            headerComponentController.setMainController(this);
            centerComponentController.setMainController(this);
            leftComponentController.setMainController(this);
        }

    }
}
