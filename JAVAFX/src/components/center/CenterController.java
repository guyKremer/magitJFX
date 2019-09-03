package components.center;

import components.app.AppController;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.io.File;
import java.util.function.BiConsumer;

public class CenterController {

    @FXML private Text userName;
    @FXML private Text repoName;
    @FXML private Text repoPath;

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
}
