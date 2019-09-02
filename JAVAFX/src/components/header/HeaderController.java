package components.header;

import components.CommonResourcesPaths;
import components.app.AppController;
import components.generics.controllers.OneInputPopupController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;

public class HeaderController {

    private AppController mainController;

     @FXML
     private MenuItem changeUserName;

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void changeNameActionListener(ActionEvent actionEvent) throws Exception{
        final Stage popupWindow = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.initOwner(mainController.getPrimaryStage());
        URL url = getClass().getResource(CommonResourcesPaths.ONE_INPUT_POPUP);
        fxmlLoader.setLocation(url);
        OneInputPopupController oneInputPopupController = (OneInputPopupController) fxmlLoader.getController();
        oneInputPopupController.setConsumer(input -> {
            mainController.changeUserName(input);
        });
        Parent root = fxmlLoader.load(url.openStream());
        Scene scene = new Scene(root, 300, 200);
        popupWindow.setScene(scene);
        popupWindow.show();
    }
}
