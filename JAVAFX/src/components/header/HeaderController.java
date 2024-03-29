package components.header;

import Engine.Status;
import components.CommonResourcesPaths;
import components.app.AppController;
import components.center.CenterController;
import components.generics.controllers.OneInputPopupController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

public class HeaderController {

    private AppController mainController;

    @FXML private MenuItem loadFromXml;
    @FXML private MenuItem changeUserName;
    @FXML private MenuItem createNewRepo;
    @FXML private MenuItem merge;
    @FXML private MenuItem switchRepo;
    @FXML private MenuItem showAllBranches;
    @FXML private MenuItem createNewBranch;
    @FXML private MenuItem checkout;
    @FXML private Button commit;
    @FXML private Button clone;
    @FXML private Button fetch;
    @FXML private Button pull;

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }
    private Consumer<Throwable> throwableConsumer;

    public void setThrowableConsumer(Consumer<Throwable> throwableConsumer) {
        this.throwableConsumer = throwableConsumer;
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
    public void loadRepositoryFromXmlActionListener(ActionEvent actionEvent) throws InterruptedException, IOException {
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

        file = showDirChooserDialog("Select Repository directory");

        repoName = showTextInputDialog("Repository Name","Repository Name", "Please Enter Repository Name");

        if(file != null){
            mainController.createNewRepo(file.getPath(), repoName);
        }
    }

    @FXML
    public void switchRepoActionListener(ActionEvent actionEvent) throws IOException, InterruptedException {
        File file;

        file = showDirChooserDialog("Select Repository directory");

        if(file != null){
            mainController.switchRepo(file.getPath());
        }
    }

    @FXML
    public void showAllBranchesActiveListener(ActionEvent actionEvent) throws IOException {

        mainController.getEngineAdapter().showAllBranches();

    }

    @FXML
    private void createNewBranchActionListener(ActionEvent actionEvent) throws IOException {
        String branchName;
        boolean checkout, discardChanges = true;

        branchName = showTextInputDialog("New Branch","New Branch", "Enter new Branch Name");
        checkout = showConfirmationDialog("Checkout", "Checkout Branch","Checkout to new branch");

        mainController.createNewBranch(branchName, checkout);
    }

    @FXML
    public void checkoutActionListener(ActionEvent actionEvent) throws IOException, InterruptedException {
        String branchName;
        boolean checkout = false;

        branchName = showTextInputDialog("Checkout Branch","Checkout Branch", "Enter Branch Name");

        mainController.checkout(branchName);
    }

    @FXML
    public void commitActionListener(ActionEvent actionEvent) throws IOException, InterruptedException {
        String message =  showTextInputDialog("Commit","Commit", "Enter Commit Message");
        mainController.Commit(message);
    }

    @FXML
    public void cloneActionListener(ActionEvent actionEvent) throws IOException {
        File RR = showDirChooserDialog("Select Remote Repository");
        File LR = showDirChooserDialog("Select destination folder");
        String repoName = showTextInputDialog("Repository name","Enter Repository Name","Name");

        if(LR != null && RR != null){
            //mainController.createNewRepo(LR.getPath(), repoName);
            mainController.getEngineAdapter().getEngine().Clone(RR,LR,repoName);
            mainController.ResetCommitTree();
        }
    }

    @FXML
    public void fetchActionListener(ActionEvent actionEvent) throws IOException {
        mainController.getEngineAdapter().getEngine().Fetch();
        mainController.ResetCommitTree();
    }

    @FXML
    public void pullActionListener(ActionEvent actionEvent) throws IOException {
        mainController.getEngineAdapter().getEngine().Pull();
        mainController.ResetCommitTree();
    }

    @FXML
    public void pushActionListener(ActionEvent actionEvent) throws IOException {
        mainController.getEngineAdapter().getEngine().Push();
    }

    @FXML
    public void showStatusActionListener(ActionEvent actionEvent) throws IOException {
       mainController.ShowStatus();
    }


    @FXML
    public void mergeActionListener(ActionEvent actionEvent)throws FileNotFoundException,IOException,Exception{
        mainController.Merge();
        mainController.ResetCommitTree();
    }
    @FXML
    public void resetBranchActionListener(ActionEvent actionEvent){
        mainController.resetBranch();
    }

    /*
    @FXML
    public void showStatusActionListener(ActionEvent actionEvent){
        mainController.ShowStatus();
    }
     */


    public boolean showConfirmationDialog(String Title, String Header, String content){
        boolean res = false;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Checkout");
        alert.setHeaderText("Checkout Branch");
        alert.setContentText("Checkout to the new branch");

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

    public File showDirChooserDialog(String i_title){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(i_title);
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
