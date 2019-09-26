package components.app;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import components.center.CenterController;
import components.commons.ConflictComponentController;
import components.header.HeaderController;
import components.left.leftController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import logic.EngineAdapter;
import Engine.Conflict;
import javafx.fxml.FXMLLoader;
import Engine.Status;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class AppController {
    public final static String ConflictComponent_FXML_INCLUDE_RESOURCE = "../components/commons/ConflictComponent.fxml";

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

    public void loadFromXml(File file) throws InterruptedException, IOException {
        centerComponentController.loadFromXml(file);
        centerComponentController.ResetCommitsTree();
    }

    public void createNewRepo(String path, String repoName){
        centerComponentController.createNewRepo(path, repoName);
    }

    public void Merge()throws FileNotFoundException,IOException,Exception{
        if (engineAdapter.isOpenChanges()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Open Changes");
            alert.setHeaderText("Cant merge");
            alert.setContentText("You have uncommitted changes,please change them first");

            alert.showAndWait();
            return;
        }
        Map<Path,Conflict> conflicts;
        String branchName =showTextInputDialog("Merge","Merge","choose branch to merge with: "+engineAdapter.getEngine().GetHeadBranch().getName());
        Button mergeButton = new Button("Merge");
        final Stage dialog = new Stage();
        Map<Path,Hyperlink> linksOnStage = new HashMap<>();
        VBox conflictFiles= new VBox();
        Consumer<Commit> commitConsumer = commit -> {
            centerComponentController.getAuthorText().textProperty().set(commit.getCreator());
            centerComponentController.getDateText().textProperty().set(commit.getDateOfCreation());
            centerComponentController.getCommitSha1Text().textProperty().set(commit.getSha1());
            centerComponentController.getParent1Sha1Text().textProperty().set(commit.getFirstPrecedingSha1());
            centerComponentController.getParent2Sha1Text().textProperty().set(commit.getSecondPrecedingSha1());
            centerComponentController.getCommitMsg().textProperty().set(commit.getMessage());
        };
        mergeButton.setId("mergeButton");
        mergeButton.setDisable(true);
        mergeButton.setOnAction((event) -> {
            try{
                engineAdapter.Merge(branchName,commitConsumer,false);
                dialog.close();
            }
            //if there is nothing to commit(very rare)
            catch (IOException e){
                dialog.close();
                System.out.println(e.getMessage());
            }
        });
        conflicts = engineAdapter.Merge(branchName,commitConsumer,true);
        Consumer<Conflict> conflictConsumer = conflict -> {
            Hyperlink linkToRemove;
            conflicts.remove(conflict.getFilePath());
            linkToRemove = linksOnStage.get(conflict.getFilePath());
            conflictFiles.getChildren().remove(linkToRemove);
            if(conflicts.isEmpty()){
                mergeButton.setDisable(false);
            }
        };

        if(!conflicts.isEmpty()){
            for (Map.Entry<Path,Conflict> entry : conflicts.entrySet()){
                Hyperlink currentLink = createHyperLink(entry.getValue(),conflictConsumer);
                conflictFiles.getChildren().add(currentLink);
                linksOnStage.put(entry.getKey(),currentLink);
            }

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            conflictFiles.getChildren().add(mergeButton);
            Scene dialogScene = new Scene(conflictFiles, 300, 200);
            dialog.setScene(dialogScene);
            dialog.show();
        }
    }

    private Hyperlink createHyperLink(Conflict i_conflict,Consumer<Conflict> conflictConsumer) {
        Hyperlink link = new Hyperlink();
        link.setText(i_conflict.getFilePath().toString());
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e)  {
                try{
                    displayConflictsComponent(i_conflict,conflictConsumer);
                }
                catch (Exception ex){

                }
            }
        });
        return link;
    }

    private void displayConflictsComponent(Conflict i_conflict,Consumer<Conflict> conflictConsumer) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/components/commons/ConflictComponent.fxml");
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());
        ConflictComponentController controller = (ConflictComponentController) fxmlLoader.getController();
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        Scene dialogScene = new Scene(root, 700, 500);
        dialog.setScene(dialogScene);
        dialog.setOnShown(event ->{
          controller.setConflict(i_conflict);
          controller.setStage(dialog);
          controller.setConflictConsumer(conflictConsumer);
        });
        dialog.showAndWait();
    }

    public String showTextInputDialog(String title,String headerText,String contentText ) {
        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        } else {
            return null;
        }
    }
    public void switchRepo(String path) throws IOException, InterruptedException {
        centerComponentController.switchRepo(path);
        centerComponentController.ResetCommitsTree();
    }

    public void createNewBranch(String branchName, boolean checkout) {
        centerComponentController.createNewBranch(branchName, checkout);
    }

    public void checkout(String branchName) {
        centerComponentController.checkout(branchName);
    }

    public void Commit(String message){
        centerComponentController.Commit(message);
    }
}
