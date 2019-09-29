package logic.tasks;

import Engine.Engine;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CreateNewRepoTask extends Task<Boolean> {

    private Engine engine;
    private BiConsumer<String,String> repDetailsDelegate;
    private String path;
    private String repoName;
    Consumer<Throwable> throwableConsumer;
    public CreateNewRepoTask(Consumer<Throwable> throwableConsumer,Engine engine, String path,String repoName, BiConsumer<String, String> repDetailsDelegate) {
        this.engine = engine;
        this.path = path;
        this.repoName = repoName;
        this.repDetailsDelegate = repDetailsDelegate;
    }

    @Override
    protected Boolean call() throws Exception {
        engine.initializeRepository(throwableConsumer,path,repoName);

        Platform.runLater(
                () -> repDetailsDelegate.accept(engine.GetCurrentRepository().GetName(),
                        engine.GetCurrentRepository().GetRepositoryPath().toString())
        );

        return Boolean.TRUE;
    }
}
