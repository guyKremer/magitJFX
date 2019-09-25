package components.app;

import Engine.Engine;
import components.center.CenterController;
import components.commons.ConflictComponentController;
import components.header.HeaderController;
import components.left.leftController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextInputDialog;
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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
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

    public void Merge()throws FileNotFoundException,IOException ,Exception{
        Map<Path,Conflict> conflicts;
        String branchName =showTextInputDialog("Merge","Merge","choose branch to merge with: "+engineAdapter.getEngine().GetHeadBranch().getName());
        conflicts = engineAdapter.CheckConflicts(branchName);
        if(!conflicts.isEmpty()){
            VBox conflictFiles= new VBox();
            for (Map.Entry<Path,Conflict> entry : conflicts.entrySet()){
                conflictFiles.getChildren().add(createHyperLink(entry.getValue()));
            }

            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            Scene dialogScene = new Scene(conflictFiles, 300, 200);
            dialog.setScene(dialogScene);
            dialog.show();

        }
    }

    private Hyperlink createHyperLink(Conflict i_conflict) {
        Hyperlink link = new Hyperlink();
        link.setText(i_conflict.getFilePath().toString());
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e)  {
                try{
                    displayConflictsComponent(i_conflict);
                }
                catch (Exception ex){

                }
            }
        });
        return link;
    }

    private void displayConflictsComponent(Conflict i_conflict) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/components/commons/ConflictComponent.fxml");
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());
        ConflictComponentController controller = (ConflictComponentController) fxmlLoader.getController();
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        Scene dialogScene = new Scene(root, 300, 200);
        dialog.setScene(dialogScene);
        dialog.setOnShown(event ->{
          controller.setConflict(i_conflict);
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
