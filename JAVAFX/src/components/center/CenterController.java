package components.center;

import Engine.MagitObjects.Commit;
import components.app.AppController;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CenterController {

    @FXML private Text userName;
    @FXML private Text repoName;
    @FXML private Text repoPath;
    @FXML private Text authorText;
    @FXML private Text dateText;
    @FXML private Text commitSha1Text;
    @FXML private Text parent1Sha1Text;
    @FXML private Text parent2Sha1Text;

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

    public void switchRepo(String path) {
        BiConsumer<String,String> biConsumer = (a,b)->{
            repoName.textProperty().set(a);
            repoPath.textProperty().set(b);
        };
        mainController.getEngineAdapter().SwitchRepo(path,biConsumer);
    }

    public void createNewBranch(String branchName, boolean checkout) {
        mainController.getEngineAdapter().createNewBranch(branchName, checkout);
    }

    public void checkout(String branchName) {
        mainController.getEngineAdapter().checkout(branchName);
    }

    public void Commit(String message) {
        Consumer<Commit> commitConsumer = commit -> {
            authorText.textProperty().set(commit.getCreator());
            dateText.textProperty().set(commit.getDateOfCreation());
            commitSha1Text.textProperty().set(commit.getSha1());
            parent1Sha1Text.textProperty().set(commit.getFirstPrecedingSha1());
            parent2Sha1Text.textProperty().set(commit.getSecondPrecedingSha1());
        };
        mainController.getEngineAdapter().Commit(message,commitConsumer);
    }
}
