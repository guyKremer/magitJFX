package logic.tasks;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import javafx.application.Platform;
import javafx.concurrent.Task;
import logic.EngineAdapter;

import java.util.function.Consumer;

public class CheckoutTask extends Task<Boolean> {
    private Engine engine;
    private String branchName;
    private Consumer<Commit> commitConsumer;


    public CheckoutTask(Engine engine, String branchName, Consumer<Commit> commitConsumer) {
        this.engine = engine;
        this.branchName = branchName;
        this.commitConsumer = commitConsumer;
        setOnFailed(event -> {
            EngineAdapter.throwableConsumer.accept(getException());
        });
    }

    @Override
    protected Boolean call() throws Exception {
        engine.checkOut(branchName);
        Platform.runLater(
                () -> commitConsumer.accept(engine.GetCurrentRepository().GeCurrentCommit())
        );
        return Boolean.TRUE;
    }
}
