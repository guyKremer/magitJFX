package components;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.net.URL;

import static javafx.application.Application.launch;


public class Main extends Application {
    public final static String APP_FXML_INCLUDE_RESOURCE = "/components/app/app.fxml";

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(APP_FXML_INCLUDE_RESOURCE);
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());
        Scene scene = new Scene(root, 500, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
