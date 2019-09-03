package components.center;

import Engine.MagitObjects.Commit;
import components.app.AppController;
import components.commitTree.CommitTreeController;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import logic.EngineAdapter;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

public class CenterController {

    @FXML private Text userName;
    @FXML private Text repoName;
    @FXML private Text repoPath;
    @FXML private ScrollPane commitTreeComponent;
    @FXML private CommitTreeController commitTreeComponentController;

    private EngineAdapter engineAdapter;


    @FXML
    public void initialize(){
        if(commitTreeComponentController != null){
            commitTreeComponentController.setMainController(this);
        }
        engineAdapter = new EngineAdapter();
    }
    private AppController mainController;
    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public void changeUserName(String name){
        mainController.getEngineAdapter().ChangeUserName(name, userName.textProperty()::set);
    }

    public void loadFromXml(File file) {
        BiConsumer<String,String> biConsumer = (a,b)->{
            repoName.textProperty().set(a);
            repoPath.textProperty().set(b);
        };
        mainController.getEngineAdapter().LoadFromXml(file, biConsumer);
    }

    public void createNewRepo(String path, String repName) {
        BiConsumer<String,String> biConsumer = (a,b)->{
            repoName.textProperty().set(a);
            repoPath.textProperty().set(b);
        };
        mainController.getEngineAdapter().CreateNewRepo(path,repName,biConsumer);
    }

    public void createCommitTree(){
        List<Commit> sortedCommits = mainController.getEngineAdapter().getAllCommits();
    }

}
