package logic.tasks;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import javafx.application.Platform;
import javafx.concurrent.Task;
import logic.EngineAdapter;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SwitchRepoTask extends Task<Boolean> {
    private Engine engine;
    private BiConsumer<String,String> repDetailsDelegate;
    private String path;
    private Consumer<Commit> commitConsumer;

    public SwitchRepoTask(Engine engine, String path, BiConsumer<String,String> repDetailsDelegate, Consumer<Commit>commitConsumer){
        this.engine = engine;
        this.path = path;
        this.repDetailsDelegate = repDetailsDelegate;
        this.commitConsumer=commitConsumer;
        setOnFailed(event -> {
            EngineAdapter.throwableConsumer.accept(getException());
        });
    }


    @Override
    protected Boolean call() throws Exception {
        engine.switchRepository(path);

        Platform.runLater(
                () -> {repDetailsDelegate.accept(engine.GetCurrentRepository().GetName(),
                        engine.GetCurrentRepository().GetRepositoryPath().toString());
                        this.commitConsumer.accept(engine.GetCurrentRepository().GeCurrentCommit());}
        );

        return Boolean.TRUE;
    }
}
