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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.net.URL;

public class HeaderController {

    private AppController mainController;
    private MenuItem loadFromXml;

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
        Parent root = fxmlLoader.load(url.openStream());
        OneInputPopupController oneInputPopupController = (OneInputPopupController) fxmlLoader.getController();
        oneInputPopupController.setPrimaryStage(popupWindow);
        oneInputPopupController.setTitle("Insert User Name:");
        oneInputPopupController.setConsumer(input -> {
            mainController.changeUserName(input);
        });
        Scene scene = new Scene(root, 300, 200);
        popupWindow.setScene(scene);
        popupWindow.show();
    }

    @FXML
    public void loadRepositoryFromXmlActionListener(ActionEvent actionEvent){
        File file;
        file = showFileChooserDialog();
        if(file != null){
            mainController.loadFromXml(file);
        }
    }

    public File showFileChooserDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xml Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(mainController.getPrimaryStage());

        return selectedFile;
    }
}
