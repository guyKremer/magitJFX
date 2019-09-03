package logic.tasks;

import Engine.Engine;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.function.BiConsumer;

public class CreateNewRepoTask extends Task<Boolean> {

    private Engine engine;
    private BiConsumer<String,String> repDetailsDelegate;
    private String path;
    private String repoName;

    public CreateNewRepoTask(Engine engine, String path,String repoName, BiConsumer<String, String> repDetailsDelegate) {
        this.engine = engine;
        this.path = path;
        this.repoName = repoName;
        this.repDetailsDelegate = repDetailsDelegate;
    }

    @Override
    protected Boolean call() throws Exception {
        engine.initializeRepository(path,repoName);

        Platform.runLater(
                () -> repDetailsDelegate.accept(engine.GetCurrentRepository().GetName(),
                        engine.GetCurrentRepository().GetRepositoryPath().toString())
        );

        return Boolean.TRUE;
    }
}
