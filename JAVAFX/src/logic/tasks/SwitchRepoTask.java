package logic.tasks;

import Engine.Engine;
import javafx.application.Platform;
import javafx.concurrent.Task;
import logic.EngineAdapter;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.function.BiConsumer;

public class SwitchRepoTask extends Task<Boolean> {
    private Engine engine;
    private BiConsumer<String,String> repDetailsDelegate;
    private String path;

    public SwitchRepoTask(Engine engine, String path, BiConsumer<String,String> repDetailsDelegate){
        this.engine = engine;
        this.path = path;
        this.repDetailsDelegate = repDetailsDelegate;
        setOnFailed(event -> {
            EngineAdapter.throwableConsumer.accept(getException());
        });
    }


    @Override
    protected Boolean call() throws Exception {
        engine.switchRepository(path);

        Platform.runLater(
                () -> repDetailsDelegate.accept(engine.GetCurrentRepository().GetName(),
                        engine.GetCurrentRepository().GetRepositoryPath().toString())
        );

        return Boolean.TRUE;
    }
}
