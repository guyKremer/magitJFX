package components.header;

import components.CommonResourcesPaths;
import components.app.AppController;
import components.generics.controllers.OneInputPopupController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class HeaderController {

    private AppController mainController;

    @FXML private MenuItem loadFromXml;
    @FXML private MenuItem changeUserName;
    @FXML private MenuItem createNewRepo;
    @FXML private MenuItem switchRepo;
    @FXML private MenuItem showAllBranches;
    @FXML private MenuItem createNewBranch;
    @FXML private MenuItem checkout;
    @FXML private MenuItem resetBranch;
    @FXML private MenuItem deleteBranch;

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void deleteBranchActiveListener(ActionEvent actionEvent){
        String branchName;

        branchName = showTextInputDialog(
                "Delete Branch",
                "Delete Branch",
                "Enter branch to delete");

        mainController.deleteBranch(branchName);
    }

    @FXML
    public void resetBranchActiveListener(ActionEvent actionEvent){
        String sha1;

        sha1 = showTextInputDialog(
                "Reset Branch",
                "Reset Branch",
                "Enter commit sha1 to reset branch");

        mainController.resetBranch(sha1);

        //showErrorDialog(e.getMessage());
    }

    private void showErrorDialog(String content){

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error");
        alert.setContentText(content);

        alert.showAndWait();
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

    @FXML
    public void createNewRepoActionListener(ActionEvent actionEvent){
        File file;
        String repoName;

        file = showDirChooserDialog();

        repoName = showTextInputDialog("Repository Name","Repository Name", "Please Enter Repository Name");

        if(file != null){
            mainController.createNewRepo(file.getPath(), repoName);
        }
    }

    @FXML
    public void switchRepoActionListener(ActionEvent actionEvent){
        File file;

        file = showDirChooserDialog();

        if(file != null){
            mainController.switchRepo(file.getPath());
        }
    }

    @FXML
    public void showAllBranchesActiveListener(ActionEvent actionEvent) throws IOException {
        String allBranches;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Repository Branches");
        alert.setHeaderText("Repository Branches");

        allBranches = mainController.getEngineAdapter().showAllBranches();
        alert.setContentText(allBranches);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    @FXML
    private void createNewBranchActionListener(ActionEvent actionEvent) throws IOException {
        String branchName;
        boolean checkout, discardChanges;

        branchName = showTextInputDialog("New Branch","New Branch", "Enter new Branch Name");
        checkout = showConfirmationDialog("Checkout", "Checkout Branch","Checkout to new branch");

        discardChanges = checkChanges();
        if(discardChanges) {
            mainController.createNewBranch(branchName, checkout);
        }
    }

    @FXML
    public void checkoutActionListener(ActionEvent actionEvent) throws IOException {
        String branchName;
        boolean checkout = false, discardChanges;

        branchName = showTextInputDialog("Checkout Branch","Checkout Branch", "Enter Branch Name");

        if(mainController.getEngineAdapter().checkChangesBeforeOperation()){
            showWarningDialog("Can't Checkout",
                    "You Have Open Changes",
                    "Please save your changes before checkout");
        }
        else{
            mainController.checkout(branchName);
        }

    }

    private void showWarningDialog(String title, String header, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }

    private boolean checkChanges() throws IOException {
        boolean discardChanges = true;

        if(mainController.getEngineAdapter().checkChangesBeforeOperation()) {
            discardChanges = showConfirmationDialog("Confirm discard changes", "You have changes" ,
                    "You have open changes, if you continue this operation all uncomitted changes will be lost");
        }

        return discardChanges;
    }

    public boolean showConfirmationDialog(String title, String header, String content){
        boolean res = false;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            res = true;
        } else {
            res = false;
        }
        return res;
    }

    public String showTextInputDialog(String title,String header, String content){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();

        return result.get();
    }

    public File showDirChooserDialog(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Repository directory");
        File selectedDirectory = directoryChooser.showDialog(mainController.getPrimaryStage());

        return selectedDirectory;
    }

    public File showFileChooserDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xml Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(mainController.getPrimaryStage());

        return selectedFile;
    }
}
