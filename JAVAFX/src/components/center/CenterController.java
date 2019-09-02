package components.center;

import components.app.AppController;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class CenterController {

    @FXML private Text userName;

    private AppController mainController;
    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public void changeUserName(String name){
        mainController.getEngineAdapter().ChangeUserName(name, userName.textProperty()::set);
    }
}
